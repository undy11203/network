package org.project.model.communication;

import com.google.common.eventbus.EventBus;
import org.project.SnakesProto;
import org.project.events.message.StartMasterRoutineEvent;
import org.project.model.Coordinate;
import org.project.model.GameAnnouncement;
import org.project.model.GameState;
import org.project.model.communication.converters.GameMessagesCreator;
import org.project.model.communication.converters.NodeRoleConverter;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.communication.gameplayers.GamePlayersStorage;
import org.project.model.communication.gameplayers.InetInfo;
import org.project.model.communication.gameplayers.PlayerInfo;
import org.project.model.communication.udp.Socket;
import org.project.model.communication.udp.UDPMessageReceiverAndSender;
import org.project.model.snake.Direction;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.project.model.communication.udp.MulticastConfig.MULTICAST_ADDRESS;
import static org.project.model.communication.udp.MulticastConfig.MULTICAST_PORT;

public class CommunicationManager {
    private final UnconfirmedGameMessagesStorage unconfirmedGameMessagesStorage = new UnconfirmedGameMessagesStorage();
    private static final int ATTEMPTS_TO_JOIN = 100;
    private final GamePlayersStorage gamePlayersStorage = new GamePlayersStorage();
    private final UDPMessageReceiverAndSender messageReceiverAndSender;
    private final Socket multicastSocket;
    private final EventBus modelEventBus;
    private final AtomicInteger msgSeq = new AtomicInteger(0);
    private final long delay;
    private final long tenthOfDelay;
    private final Map<Integer, Long> lastSteerMsgSeq = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // Adjust the number of threads as needed

    private CommunicationManager(UDPMessageReceiverAndSender messageReceiverAndSender,
                                 InetAddress multicastInetAddress, int delay, EventBus modelEventBus) {
        this.messageReceiverAndSender = messageReceiverAndSender;
        this.multicastSocket = new Socket(multicastInetAddress, MULTICAST_PORT);
        this.delay = delay;
        this.tenthOfDelay = delay / 10;
        this.modelEventBus = modelEventBus;
    }

    public static CommunicationManager create(int delay, EventBus modelEventBus) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(MULTICAST_ADDRESS);

        UDPMessageReceiverAndSender unicastMsgReceiverAndSender = UDPMessageReceiverAndSender.getInstance();

        return new CommunicationManager(unicastMsgReceiverAndSender, inetAddress, delay, modelEventBus);
    }

    public boolean handleGameStateMessage(GameState currentGameState, GameState newState, List<GamePlayer> gamePlayers,
                                          Socket senderSocket, PlayerInfo me, long msgSeq) {
        synchronized (gamePlayersStorage) {
            GamePlayer maybeMaster = gamePlayersStorage.findGamePlayerBySocket(senderSocket).orElse(null);
            if (maybeMaster == null) {
                return false;
            }

            maybeMaster.updateLastCommunication();

            if (newState.getStateOrder() > currentGameState.getStateOrder()) {
                gamePlayersStorage.setGamePlayers(gamePlayers.stream().peek(GamePlayer::updateLastCommunication).toList());
                GamePlayer master = gamePlayersStorage.findGamePlayerByRole(NodeRole.MASTER).orElse(null);
                if (master == null) {
                    return false;
                }
                gamePlayersStorage.delete(master);
                gamePlayersStorage.add(new GamePlayer(master.getPlayerInfo(), NodeRole.MASTER, new InetInfo(senderSocket, Instant.now().toEpochMilli())));
                sendAckMsg(me.getId(), master.getId(), senderSocket, msgSeq);
                return true;
            }
            gamePlayers.stream().filter(p -> p.getRole() == NodeRole.MASTER).findAny().ifPresent(m ->  sendAckMsg(me.getId(), m.getId(), senderSocket, msgSeq));
            return false;
        }
    }

    public void handleJoinMessage(SnakesProto.GameMessage.JoinMsg joinMsg, Socket senderSocket, long msgSeq,
                                  GameState gameState, PlayerInfo me) {

        NodeRole role = NodeRoleConverter.getInstance().snakesProtoToNodeRole(joinMsg.getRequestedRole());
        if (role != NodeRole.NORMAL && role != NodeRole.VIEWER) {
            return;
        }

        synchronized (gamePlayersStorage) {

            GamePlayer sender = gamePlayersStorage.findGamePlayerBySocket(senderSocket).orElse(null);
            if (sender != null) {
                sendAckMsg(me.getId(), sender.getId(), senderSocket, msgSeq);
                return;
            }

            if (isMyRole(NodeRole.MASTER, me.getId())) {
                String playerName = joinMsg.getPlayerName();
                InetInfo inetInfo = new InetInfo(senderSocket, -1);

                PlayerInfo playerInfo = new PlayerInfo(playerName);
                GamePlayer newGamePlayer = new GamePlayer(playerInfo, role, inetInfo);

                if (role != NodeRole.VIEWER) {
                    Coordinate freeAreaCoordinate = gameState.findFreeArea();
                    if (freeAreaCoordinate != null) {
                        if (gameState.addNewPlayer(playerInfo, freeAreaCoordinate)) {
                            if (gamePlayersStorage.findGamePlayerByRole(NodeRole.DEPUTY).isEmpty()) {
                                newGamePlayer.setRole(NodeRole.DEPUTY);
                            }
                        } else {
                            sendErrorMsg("Can't find free area", newGamePlayer.getSocket());
                            return;
                        }
                    } else {
                        sendErrorMsg("Can't find free area", newGamePlayer.getSocket());
                        return;
                    }
                }

                gamePlayersStorage.add(newGamePlayer);
                sendAckMsg(me.getId(), playerInfo.getId(), senderSocket, msgSeq);
            }
        }
    }

    public void handleSteerMsg(GameState gameState, Direction newDirection,
                               Socket senderSocket, PlayerInfo me, long msgSeq) {
        synchronized (gamePlayersStorage) {
            if (isMyRole(NodeRole.MASTER, me.getId())) {
                GamePlayer gamePlayer = gamePlayersStorage.findGamePlayerBySocket(senderSocket).orElse(null);
                if (gamePlayer == null) {
                    return;
                }
                Long lastMsgSeq = lastSteerMsgSeq.get(gamePlayer.getId());
                if (lastMsgSeq != null) {
                    if (msgSeq < lastMsgSeq) {
                        return;
                    }
                }
                lastSteerMsgSeq.put(gamePlayer.getId(), msgSeq);
                gameState.steerSnake(gamePlayer.getId(), newDirection);
                gamePlayer.updateLastCommunication();
                sendAckMsg(me.getId(), gamePlayer.getId(), senderSocket, msgSeq);
            }
        }
    }

    public void handleRoleChangeMsg(NodeRole senderRole, NodeRole receiverRole,
                                    int senderId, int receiverId, long messageSeq, PlayerInfo me,
                                    Socket senderSocket) {
        synchronized (gamePlayersStorage) {
            if (isMyRole(NodeRole.MASTER, me.getId()) && isMyRole(NodeRole.DEPUTY, senderId) && senderRole == NodeRole.VIEWER) {
                gamePlayersStorage.findGamePlayerByRole(NodeRole.NORMAL).ifPresent(p -> {
                    p.setRole(NodeRole.DEPUTY);
                    sendRoleChangeMsg
                            (NodeRole.MASTER, NodeRole.DEPUTY, me.getId(), p.getId(), p.getSocket());
                });
            } else if (isMyRole(NodeRole.DEPUTY, me.getId()) && receiverRole == NodeRole.MASTER) {
                modelEventBus.post(new StartMasterRoutineEvent());

                gamePlayersStorage.findGamePlayerByRole(NodeRole.NORMAL).ifPresent(p -> {
                    p.setRole(NodeRole.DEPUTY);
                    sendRoleChangeMsg(NodeRole.MASTER, NodeRole.DEPUTY, me.getId(), p.getId(), p.getSocket());
                });

            }

            gamePlayersStorage.findGamePlayerById(senderId).ifPresent(p -> {
                p.setRole(senderRole);
                p.updateLastCommunication();
            });

            gamePlayersStorage.findGamePlayerById(receiverId).ifPresent(p -> p.setRole(receiverRole));
            sendAckMsg(me.getId(), senderId, senderSocket, messageSeq);
        }
    }

    public void handlePingMsg(Socket senderSocket, PlayerInfo me, long messageSeq) {
        synchronized (gamePlayersStorage) {
            GamePlayer gamePlayer = gamePlayersStorage.findGamePlayerBySocket(senderSocket).orElse(null);
            if (gamePlayer == null) {
                return;
            }
            gamePlayer.updateLastCommunication();
            sendAckMsg(me.getId(), gamePlayer.getId(), senderSocket, messageSeq);
        }
    }

    public void handleAckMsg(int senderId, long messageSeq) {
        synchronized (unconfirmedGameMessagesStorage) {
            unconfirmedGameMessagesStorage.confirm(messageSeq);
        }
        synchronized (gamePlayersStorage) {
            gamePlayersStorage.findGamePlayerById(senderId).ifPresent(GamePlayer::updateLastCommunication);
        }
    }

    public void handleErrorMsg(Socket senderSocket, PlayerInfo me, long messageSeq) {
        synchronized (gamePlayersStorage) {
            GamePlayer gamePlayer = gamePlayersStorage.findGamePlayerBySocket(senderSocket).orElse(null);
            if (gamePlayer == null) {
                return;
            }
            gamePlayer.updateLastCommunication();
            sendAckMsg(me.getId(), gamePlayer.getId(), senderSocket, messageSeq);
        }
    }

    public void detectPlayersToSendPingMsg(PlayerInfo me) {
        synchronized (gamePlayersStorage) {
            if (!isMyRole(NodeRole.MASTER, me.getId())) {
                GamePlayer master = gamePlayersStorage.findGamePlayerByRole(NodeRole.MASTER).orElse(null);
                if (master != null) {
                    if (master.getLastCommunicationTime() > tenthOfDelay) {
                        sendPingMsg(me.getId(), master.getId(), master.getSocket());
                    }
                }
            } else {
                List<GamePlayer> toSendPingMsgPlayers =
                        gamePlayersStorage.findForLastCommunicationPlayer(tenthOfDelay, me.getId());

                for (GamePlayer player : toSendPingMsgPlayers) {
                    if (me.getId() != player.getId()) {
                        sendPingMsg(me.getId(), player.getId(), player.getSocket());
                    }
                }
            }
        }
    }

    public void findExpired(PlayerInfo me, GameState gameState) {
        synchronized (gamePlayersStorage) {
            List<GamePlayer> expired = gamePlayersStorage.findForLastCommunicationPlayer(delay, me.getId());
            expired.forEach(player -> {
                gameState.handlePlayerLeave(player.getId());
                this.handleExpire(player, me);
            });
        }
    }

    public void resendUnconfirmed() {
        synchronized (unconfirmedGameMessagesStorage) {
            unconfirmedGameMessagesStorage.getUnconfirmedMessages().forEach(message -> {
                long instant = Instant.now().toEpochMilli();
                if (instant - message.getSentAt() > tenthOfDelay) {
                    if (message.getSocket().port() == 0) {
                        unconfirmedGameMessagesStorage.confirm(message.getMessage().getMsgSeq());
                    } else {
                        executorService.submit(() -> sendMessage(message));
                    }
                }
            });
        }
    }

    public void addMaster(GamePlayer gamePlayer) {
        synchronized (gamePlayersStorage) {
            if (gamePlayersStorage.findGamePlayerByRole(NodeRole.MASTER).isEmpty()
                    && gamePlayersStorage.findGamePlayerById(gamePlayer.getId()).isEmpty()) {
                gamePlayer.setRole(NodeRole.MASTER);
                gamePlayersStorage.add(gamePlayer);
            }
        }
    }

    public void nextState(PlayerInfo me, GameState gameState) {
        if (isMyRole(NodeRole.MASTER, me.getId())) {
            List<Integer> ids = gameState.change();
            synchronized (gamePlayersStorage) {
                handleDeadPlayers(me, ids);
                gameState.addPlayerInfos(gamePlayersStorage.getGamePlayers().stream().map(GamePlayer::getPlayerInfo).toList());
                gamePlayersStorage.getGamePlayers().forEach(player -> {
                    if (player.getId() != me.getId()) {
                        sendGameStateMsg(me.getId(), player.getId(), gameState, player);
                    }
                });
            }
        }
    }

    public long getDelay() {
        return delay;
    }
    public long getTenthOfDelay() {
        return tenthOfDelay;
    }

    public void sendJoinMsg(GameAnnouncement gameAnnouncement, String nickname, NodeRole role) {
        int msgSeq = nextMsgSeq();
        SnakesProto.GameMessage joinMsg = GameMessagesCreator.getInstance().createJoinMsg(gameAnnouncement.gameName(), nickname, role, msgSeq);
        Message message = new Message(joinMsg, gameAnnouncement.senderSocket());
        synchronized (unconfirmedGameMessagesStorage) {
            unconfirmedGameMessagesStorage.add(message);
        }
        sendMessage(message);

    }

    public int receiveAskMsgAfterJoinMsg(NodeRole role) throws IOException {
        synchronized (gamePlayersStorage) {
            Message response = messageReceiverAndSender.receive();
            SnakesProto.GameMessage gameMsg = response.getMessage();
            for (int i = 0; i < ATTEMPTS_TO_JOIN; i++) {
                if (gameMsg.hasAck()) {
                    synchronized (unconfirmedGameMessagesStorage) {
                        unconfirmedGameMessagesStorage.confirm(gameMsg.getMsgSeq());
                    }
                    GamePlayer master = new GamePlayer(new PlayerInfo("", gameMsg.getSenderId()), NodeRole.MASTER,
                            new InetInfo(response.getSocket(), -1));
                    GamePlayer me = new GamePlayer(new PlayerInfo("", gameMsg.getReceiverId()), role,
                            new InetInfo(InetAddress.getLocalHost(), 0, -1));
                    addMaster(master);
                    if (!me.equals(master)) {
                        gamePlayersStorage.add(me);
                    }
                    return gameMsg.getReceiverId();
                }
                if (gameMsg.hasError()) {
                    handleErrorMsg(response.getSocket(), new PlayerInfo("", 0), gameMsg.getMsgSeq());
                    return -1;
                }
            }
            return -1;
        }
    }

    public void steer(Direction newDirection, PlayerInfo me, GameState currentGameState) {
        synchronized (gamePlayersStorage) {
            GamePlayer master = gamePlayersStorage.findGamePlayerByRole(NodeRole.MASTER).orElse(null);
            if (master == null) {
                return;
            }
            if (master.getId() == me.getId()) {
                currentGameState.steerSnake(me.getId(), newDirection);
            } else {
                sendSteerMsg(newDirection, me, master);
            }
        }

    }

    public void multicastGameAnnounce(GameState gameState, PlayerInfo me, String gameName) {
        if (gameState == null) {
            return;
        }
        if (isMyRole(NodeRole.MASTER, me.getId())) {
            sendGameAnnouncementMsg(multicastSocket, gameState, gameName);
        }
    }

    public void unicastGameAnnounce(Socket socket, PlayerInfo me, GameState gameState, String gameName) {
        if (gameState == null) {
            return;
        }
        if (isMyRole(NodeRole.MASTER, me.getId())) {
            sendGameAnnouncementMsg(socket, gameState, gameName);
        }
    }

    private boolean isMyRole(NodeRole role, int id) {
        synchronized (gamePlayersStorage) {
            GamePlayer me = gamePlayersStorage.findGamePlayerById(id).orElse(null);
            if (me == null) {
                return false;
            }
            return me.getRole() == role;
        }
    }

    public void close(PlayerInfo me) {
        synchronized (gamePlayersStorage) {
            GamePlayer master = gamePlayersStorage.findGamePlayerByRole(NodeRole.MASTER).orElse(null);
            if (master != null) {
                if (master.getId() == me.getId()) {
                    GamePlayer deputy = gamePlayersStorage.findGamePlayerByRole(NodeRole.DEPUTY).orElse(null);
                    if (deputy != null) {
                        deputy.setRole(NodeRole.MASTER);
                        sendRoleChangeMsg(NodeRole.VIEWER, NodeRole.MASTER, me.getId(), deputy.getId(), deputy.getSocket());
                    }
                } else {
                    sendRoleChangeMsg(NodeRole.VIEWER, NodeRole.MASTER, me.getId(), master.getId(), master.getSocket());
                }
            }
        }
        executorService.shutdownNow();
        messageReceiverAndSender.close();
    }

    private void sendSteerMsg(Direction newDirection, PlayerInfo me, GamePlayer master) {
        executorService.submit(() -> {
            synchronized (gamePlayersStorage) {
                SnakesProto.GameMessage steerMsg = GameMessagesCreator.getInstance().createSteerMsg(newDirection, me.getId(), nextMsgSeq());
                Message message = new Message(steerMsg, master.getSocket());
                synchronized (unconfirmedGameMessagesStorage) {
                    unconfirmedGameMessagesStorage.add(message);
                }
                sendMessage(message);
            }
        });
    }

    private void sendPingMsg(int senderId, int receiverId, Socket toSendSocket) {
        executorService.submit(() -> {
            synchronized (gamePlayersStorage) {
                SnakesProto.GameMessage pingMsg =
                        GameMessagesCreator.getInstance().createPingMsg(senderId, receiverId, nextMsgSeq());
                Message message = new Message(pingMsg, toSendSocket);
                synchronized (unconfirmedGameMessagesStorage) {
                    unconfirmedGameMessagesStorage.add(message);
                }
                sendMessage(message);
            }
        });
    }

    private void sendAckMsg(int senderId, int receiverId, Socket toSendSocket, long messageSeq) {
        executorService.submit(() -> {
            SnakesProto.GameMessage ackMsg =
                    GameMessagesCreator.getInstance().createAckMsg(senderId, receiverId, messageSeq);
            Message message = new Message(ackMsg, toSendSocket);
            sendMessage(message);
        });
    }


    @SuppressWarnings("SameParameterValue")
    private void sendRoleChangeMsg(NodeRole senderRole, NodeRole receiverRole, int senderId, int receiverId, Socket toSendSocket) {
        SnakesProto.GameMessage roleChangedMsg =
                GameMessagesCreator.getInstance().createRoleChangedMsg(senderRole, receiverRole, senderId, receiverId, nextMsgSeq());
        Message message = new Message(roleChangedMsg, toSendSocket);
        synchronized (unconfirmedGameMessagesStorage) {
            unconfirmedGameMessagesStorage.add(message);
        }
        sendMessage(message);
    }

    @SuppressWarnings("SameParameterValue")
    private void sendErrorMsg(String cause, Socket toSendSocket) {
        executorService.submit(() -> {
            SnakesProto.GameMessage errorMsg =
                    GameMessagesCreator.getInstance().createErrorMsg(cause, nextMsgSeq());
            Message message = new Message(errorMsg, toSendSocket);
            synchronized (unconfirmedGameMessagesStorage) {
                unconfirmedGameMessagesStorage.add(message);
            }
            sendMessage(message);
        });

    }

    private void sendGameAnnouncementMsg(Socket toSendSocket, GameState gameState, String currentGameName) {
        executorService.submit(() -> {
            synchronized (gamePlayersStorage) {
                SnakesProto.GameMessage announcementMsg = GameMessagesCreator.getInstance().createAnnouncementMsg(
                        gameState.getGameConfig(), gamePlayersStorage.getGamePlayers(), currentGameName, true, nextMsgSeq());
                Message message = new Message(announcementMsg, toSendSocket);
                sendMessage(message);
            }
        });
    }

    private void sendMessage(Message message) {
        try {
            messageReceiverAndSender.send(message);
            message.setSentAt(Instant.now().toEpochMilli());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int nextMsgSeq() {
        return msgSeq.incrementAndGet();
    }

    private void sendGameStateMsg(int senderID, int receiverID, GameState gameState, GamePlayer gamePlayer) {
        executorService.submit(() -> {
            synchronized (gamePlayersStorage) {
                SnakesProto.GameMessage gameStateMsg =
                        GameMessagesCreator.getInstance().createGameStateMsg(senderID, receiverID, gameState,
                                gamePlayersStorage.getGamePlayers(), nextMsgSeq());
                Message message = new Message(gameStateMsg, gamePlayer.getSocket());
                synchronized (unconfirmedGameMessagesStorage) {
                    unconfirmedGameMessagesStorage.add(message);
                }
                sendMessage(message);
            }
        });
    }

    private void handleExpire(GamePlayer gamePlayer, PlayerInfo me) {

        synchronized (gamePlayersStorage) {
            if (isMyRole(NodeRole.MASTER, me.getId())) {
                synchronized (unconfirmedGameMessagesStorage) {
                    unconfirmedGameMessagesStorage.cancelConfirmation(gamePlayer.getId());
                }
                if (gamePlayer.getRole() == NodeRole.DEPUTY) {
                    gamePlayersStorage.findGamePlayerByRole(NodeRole.NORMAL).ifPresent(p -> {
                        p.setRole(NodeRole.DEPUTY);
                        sendRoleChangeMsg(NodeRole.MASTER, NodeRole.DEPUTY, me.getId(), p.getId(), p.getSocket());
                    });
                }
                gamePlayersStorage.delete(gamePlayer);
                return;
            }

            if (isMyRole(NodeRole.NORMAL, me.getId()) && gamePlayer.getRole() == NodeRole.MASTER) {
                GamePlayer deputy = gamePlayersStorage.findGamePlayerByRole(NodeRole.DEPUTY).orElse(null);
                if (deputy == null) {
                    return;
                }
                synchronized (unconfirmedGameMessagesStorage) {
                    unconfirmedGameMessagesStorage.replaceDestination(gamePlayer.getId(), deputy.getSocket());
                }
                deputy.setRole(NodeRole.MASTER);
                gamePlayersStorage.delete(gamePlayer);
                return;
            }

            if (isMyRole(NodeRole.DEPUTY, me.getId()) && gamePlayer.getRole() == NodeRole.MASTER) {
                synchronized (unconfirmedGameMessagesStorage) {
                    unconfirmedGameMessagesStorage.cancelConfirmation(gamePlayer.getId());
                }
                GamePlayer mePlayer = gamePlayersStorage.findGamePlayerById(me.getId()).orElse(null);
                if (mePlayer == null) {
                    return;
                }
                mePlayer.setRole(NodeRole.MASTER);
                gamePlayersStorage.findGamePlayerByRole(NodeRole.NORMAL).ifPresent(p -> p.setRole(NodeRole.DEPUTY));
                gamePlayersStorage.getGamePlayers().forEach(player -> {
                    if (player.getId() != me.getId()) {
                        sendRoleChangeMsg(NodeRole.MASTER, player.getRole(), me.getId(), player.getId(), player.getSocket());
                        player.updateLastCommunication();
                    }
                });
                gamePlayersStorage.delete(gamePlayer);
                modelEventBus.post(new StartMasterRoutineEvent());
            }
        }
    }

    private void handleDeadPlayers(PlayerInfo me, List<Integer> ids) {
        synchronized (gamePlayersStorage) {
            for (int id : ids) {
                if (me.getId() != id) {
                    GamePlayer player = gamePlayersStorage.findGamePlayerById(id).orElse(null);
                    if (player == null) {
                        continue;
                    }
                    if (player.getRole() == NodeRole.DEPUTY) {
                        gamePlayersStorage.findGamePlayerByRole(NodeRole.NORMAL).ifPresent(p -> {
                            p.setRole(NodeRole.DEPUTY);
                            sendRoleChangeMsg(NodeRole.MASTER, NodeRole.DEPUTY, me.getId(), p.getId(), p.getSocket());
                        });
                    }
                    player.setRole(NodeRole.VIEWER);
                    sendRoleChangeMsg(NodeRole.MASTER, NodeRole.VIEWER, me.getId(), id, player.getSocket());
                }
            }
        }
    }
}

