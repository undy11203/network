package org.project.model;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.project.SnakesProto;
import org.project.events.message.HandleAckMsgEvent;
import org.project.events.message.HandleDiscoverMsgEvent;
import org.project.events.message.HandleErrorMsgEvent;
import org.project.events.message.HandleGameAnnouncementMsgEvent;
import org.project.events.message.HandleGameStateMsgEvent;
import org.project.events.message.HandleJoinMsgEvent;
import org.project.events.message.HandlePingMsgEvent;
import org.project.events.message.HandleRoleChangeMsgEvent;
import org.project.events.message.HandleSteerMsgEvent;
import org.project.events.message.RenderGameFieldEvent;
import org.project.events.message.StartMasterRoutineEvent;
import org.project.events.message.StartNewGameAnimationEvent;
import org.project.events.message.SwitchToGameFieldEvent;
import org.project.events.message.UpdateAvailableGamesEvent;
import org.project.events.message.UpdateGameScoresEvent;
import org.project.model.communication.CommunicationManager;
import org.project.model.communication.GameAnnouncementMsgHandler;
import org.project.model.communication.GameMessageHandler;
import org.project.model.communication.Message;
import org.project.model.communication.NodeRole;
import org.project.model.communication.converters.GameMessagesCreator;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.communication.gameplayers.InetInfo;
import org.project.model.communication.gameplayers.PlayerInfo;
import static org.project.model.communication.udp.MulticastConfig.MULTICAST_ADDRESS;
import static org.project.model.communication.udp.MulticastConfig.MULTICAST_PORT;
import org.project.model.communication.udp.Socket;
import org.project.model.communication.udp.UDPMessageReceiverAndSender;
import org.project.model.snake.Direction;
import org.project.utils.InformUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class Model {
    private static final long GAME_ANNOUNCEMENT_TTL = 2000;
    private String gameName;
    private final InetAddress multicastAddress;
    private final Map<GameAnnouncement, Long> gamesRepository = new HashMap<>();
    private EventBus controllersEventBus;
    private final EventBus modelEventBus;
    private GameState gameState;
    private PlayerInfo me;
    private CommunicationManager communicationManager;
    private GameAnnouncementMsgHandler gameAnnouncementMsgHandler;
    private GameMessageHandler gameMsgHandler;
    private final UDPMessageReceiverAndSender messageReceiverAndSender;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private Model(UDPMessageReceiverAndSender messageReceiverAndSender,
                  GameAnnouncementMsgHandler gameAnnouncementMsgHandler, EventBus modelEventBus,
                  InetAddress multicastAddress) {
        this.messageReceiverAndSender = messageReceiverAndSender;
        this.multicastAddress = multicastAddress;
        this.modelEventBus = modelEventBus;
        this.gameAnnouncementMsgHandler = gameAnnouncementMsgHandler;
    }

    public static Model create() throws IOException {
        EventBus modelEventBus = new EventBus();
        GameAnnouncementMsgHandler gameAnnouncementMsgHandler = new GameAnnouncementMsgHandler(modelEventBus);
        InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
        Model model = new Model(UDPMessageReceiverAndSender.getInstance(), gameAnnouncementMsgHandler, modelEventBus, multicastAddress);
        modelEventBus.register(model);
        gameAnnouncementMsgHandler.start();
        return model;
    }

    public void createNewGame(GameState gameState, PlayerInfo playerInfo, String gameName) throws IOException {
        this.gameState = gameState;
        this.me = playerInfo;
        this.gameName = gameName;
        if (addFirstPlayer(gameState, playerInfo)) {
            return;
        }
        setNewGameCommunicationManager(gameState, playerInfo);
        startGameMessageHandler(gameState);
        scheduler.scheduleAtFixedRate(this::multicastAnnounceGame, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::detectPlayersToSendPingMsg, 0, communicationManager.getTenthOfDelay() / 2, TimeUnit.MILLISECONDS);
        startPlayersExpiredDetectors(communicationManager);
    }

    public void joinGame(GameAnnouncement gameAnnouncement, String nickname, boolean isViewer) throws IOException, InterruptedException {
        gameAnnouncementMsgHandler.interrupt();
        NodeRole role = NodeRole.NORMAL;
        if (isViewer) {
            role = NodeRole.VIEWER;
        }
        this.gameName = gameAnnouncement.gameName();
        GameConfig gameConfig = gameAnnouncement.gameConfig();
        this.gameState = new GameState(gameConfig);
        this.communicationManager = CommunicationManager.create(gameConfig.delay(), modelEventBus);
        if (!startJoining(gameAnnouncement, nickname, role)) {
            return;
        }
        startGameMessageHandler(gameState);
        startPlayersExpiredDetectors(communicationManager);

    }

    @Subscribe
    public void handleJoinMsg(HandleJoinMsgEvent e) {
        communicationManager.handleJoinMessage(e.joinMsg(), e.senderSocket(), e.msgSeq(), gameState, me);
    }

    @Subscribe
    public void handleDiscoverMsg(HandleDiscoverMsgEvent e) {
        if (communicationManager != null) {
            communicationManager.unicastGameAnnounce(e.senderSocket(), me, gameState, gameName);
        }
    }

    @Subscribe
    public void handleGameStateMsg(HandleGameStateMsgEvent e) {
        if (communicationManager.handleGameStateMessage(gameState, e.newGameState(), e.players(), e.senderSocket(), me, e.msgSeq())) {
            gameState = e.newGameState();
            controllersEventBus.post(new RenderGameFieldEvent(gameState));
            controllersEventBus.post(new UpdateGameScoresEvent());
        }
    }

    @Subscribe
    public void handleAckMsg(HandleAckMsgEvent e) {
        communicationManager.handleAckMsg(e.senderID(), e.msgSeq());
    }

    @Subscribe
    public void handlePingMsg(HandlePingMsgEvent e) {
        communicationManager.handlePingMsg(e.senderSocket(), me, e.msgSeq());
    }

    @Subscribe
    public void handleSteerMsg(HandleSteerMsgEvent e) {
        communicationManager.handleSteerMsg(gameState, e.newDirection(), e.senderSocket(), me, e.msgSeq());
    }

    @Subscribe
    void handleRoleChangeMsg(HandleRoleChangeMsgEvent e) {
        communicationManager.handleRoleChangeMsg(e.senderRole(), e.receiverRole(), e.senderId(), e.receiverId(), e.msgSeq(), me, e.senderSocket());
    }

    @Subscribe
    public void handleGameAnnouncement(HandleGameAnnouncementMsgEvent e) {
        synchronized (gamesRepository) {
            gamesRepository.put(e.gameAnnouncement(), Instant.now().toEpochMilli());
            if (controllersEventBus != null) {
                controllersEventBus.post(new UpdateAvailableGamesEvent(gamesRepository.keySet().stream().toList()));
            }
        }
    }

    @Subscribe
    public void handleErrorMsg(HandleErrorMsgEvent e) {
        communicationManager.handleErrorMsg(e.senderSocket(), me, e.msgSeq());
    }

    @Subscribe
    public void startMasterRoutine(StartMasterRoutineEvent e) {
        gameAnnouncementMsgHandler = new GameAnnouncementMsgHandler(modelEventBus);
        gameAnnouncementMsgHandler.start();
        controllersEventBus.post(new StartNewGameAnimationEvent(gameState.getGameConfig().delay()));
        scheduler.scheduleAtFixedRate(this::detectPlayersToSendPingMsg, 0, communicationManager.getTenthOfDelay() / 2, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::multicastAnnounceGame, 1, 1, TimeUnit.SECONDS);
    }

    public void removeExpiredGames() {
        synchronized (gamesRepository) {
            long now = Instant.now().toEpochMilli();
            Set<Map.Entry<GameAnnouncement, Long>> toDelete = gamesRepository.entrySet()
                    .stream().filter(entry -> now - entry.getValue() > GAME_ANNOUNCEMENT_TTL).collect(Collectors.toSet());
            toDelete.forEach(entry -> gamesRepository.remove(entry.getKey()));
            if (!toDelete.isEmpty()) {
                controllersEventBus.post(new UpdateAvailableGamesEvent(gamesRepository.keySet().stream().toList()));
            }
        }
    }

    public void multicastAnnounceGame() {
        communicationManager.multicastGameAnnounce(gameState, me, gameName);
    }

    public void generateNewFood() {
        gameState.generateNewFood();
    }

    public void sendDiscoverMsg() throws IOException {
        SnakesProto.GameMessage gameMessage = GameMessagesCreator.getInstance().createDiscoverMsg(0);
        messageReceiverAndSender.send(new Message(gameMessage, new Socket(multicastAddress, MULTICAST_PORT)));
    }

    public List<PlayerInfo> getCurrentPlayers() {
        return gameState.getPlayers().stream().sorted(Comparator.comparing(PlayerInfo::getScore).reversed().thenComparing(PlayerInfo::getNickname)).toList();
    }

    public void steerSnake(Direction newDirection) {
        communicationManager.steer(newDirection, me, gameState);
    }

    public void changeGameState() {
        communicationManager.nextState(me, gameState);
    }

    public GameState getGameState() {
        return gameState;
    }

    public PlayerInfo getMe() {
        return me;
    }

    public void stopScheduler() {
        System.out.println("close scheduler");
        scheduler.shutdownNow();
        scheduler.close();
    }

    public void end() {
        stopScheduler();
        communicationManager.close(me);
        gameMsgHandler.interrupt();
        gameAnnouncementMsgHandler.interrupt();
        modelEventBus.unregister(this);
    }

    public void setControllersEventBus(EventBus controllersEventBus) {
        this.controllersEventBus = controllersEventBus;
    }

    private void detectPlayersToSendPingMsg() {
        communicationManager.detectPlayersToSendPingMsg(me);
    }

    private void findExpired() {
        communicationManager.findExpired(me, gameState);
    }

    private void resendUnconfirmedMsg() {
        communicationManager.resendUnconfirmed();
    }

    private void startPlayersExpiredDetectors(CommunicationManager communicationManager) {
        scheduler.scheduleAtFixedRate(this::resendUnconfirmedMsg, 0, communicationManager.getTenthOfDelay() / 2, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::findExpired, 0, communicationManager.getDelay() / 2, TimeUnit.MILLISECONDS);
    }

    private boolean startJoining(GameAnnouncement gameAnnouncement, String nickname, NodeRole role) throws IOException {
        communicationManager.sendJoinMsg(gameAnnouncement, nickname, role);
        int id = communicationManager.receiveAskMsgAfterJoinMsg(role);
        if (id == -1) {
            InformUtils.error("Can't join this game");
            return false;
        }
        this.me = new PlayerInfo(nickname, id);
        controllersEventBus.post(new SwitchToGameFieldEvent());
        return true;
    }

    private boolean addFirstPlayer(GameState gameState, PlayerInfo playerInfo) {
        this.generateNewFood();
        Coordinate free = gameState.findFreeArea();
        if (free != null) {
            gameState.addNewPlayer(playerInfo, free);
        } else {
            InformUtils.error("Can't create new game");
            return true;
        }
        return false;
    }

    private void startGameMessageHandler(GameState gameState) {
        gameMsgHandler = new GameMessageHandler(modelEventBus, gameState.getGameConfig());
        gameMsgHandler.start();
    }

    private void setNewGameCommunicationManager(GameState gameState, PlayerInfo playerInfo) throws IOException {
        CommunicationManager communicationManager = CommunicationManager.create(gameState.getGameConfig().delay(), modelEventBus);
        GamePlayer master = new GamePlayer(playerInfo, NodeRole.MASTER, new InetInfo(null, 0, 0));
        communicationManager.addMaster(master);
        this.communicationManager = communicationManager;
    }
}
