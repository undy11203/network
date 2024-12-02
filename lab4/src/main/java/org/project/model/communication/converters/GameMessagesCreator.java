package org.project.model.communication.converters;

import org.project.SnakesProto;
import org.project.model.GameConfig;
import org.project.model.GameState;
import org.project.model.communication.NodeRole;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.snake.Direction;

import java.util.List;

public class GameMessagesCreator {
    private final static GameMessagesCreator INSTANCE = new GameMessagesCreator();

    private GameMessagesCreator() {
    }

    public static GameMessagesCreator getInstance() {
        return INSTANCE;
    }

    public SnakesProto.GameMessage createGameStateMsg(int senderId, int receiverId, GameState gameState, List<GamePlayer> gamePlayers,
                                                      long msgSeq){

        SnakesProto.GameState state = GameStateConverter.getInstance().gameStateToSnakesProto(gameState, gamePlayers);
        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GameMessage.StateMsg.Builder stateMsgBuilder = SnakesProto.GameMessage.StateMsg.newBuilder();

        return gameMsgBuilder.setState(stateMsgBuilder.setState(state).build())
                .setMsgSeq(msgSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public SnakesProto.GameMessage createSteerMsg(Direction direction, int senderId, long msgSeq) {

        SnakesProto.Direction spDirection = DirectionConverter.getInstance().directionToSnakesProto(direction);
        SnakesProto.GameMessage.SteerMsg steerMsg = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(spDirection)
                .build();
        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();
        return gameMsgBuilder.setSteer(steerMsg)
                .setSenderId(senderId)
                .setMsgSeq(msgSeq)
                .build();
    }

    public SnakesProto.GameMessage createAnnouncementMsg(GameConfig gameConfig, List<GamePlayer> players,
                                                         String gameName, boolean canJoin, long msgSeq) {

        SnakesProto.GameAnnouncement.Builder announcementBuilder = SnakesProto.GameAnnouncement
                .newBuilder();

        SnakesProto.GameAnnouncement announcement = announcementBuilder.setGameName(gameName)
                .setCanJoin(canJoin)
                .setConfig(GameConfigConverter.getInstance().gameConfigToSnakesProto(gameConfig))
                .setPlayers(GamePlayersConverter.getInstance().gamePlayersToSnakesProto(players))
                .build();

        SnakesProto.GameMessage.AnnouncementMsg.Builder announcementMsgBuilder =
                SnakesProto.GameMessage.AnnouncementMsg.newBuilder();

        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = announcementMsgBuilder.addGames(announcement)
                .build();

        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setAnnouncement(announcementMsg)
                .setMsgSeq(msgSeq)
                .build();
    }

    public SnakesProto.GameMessage createJoinMsg(String gameName, String playerNickname, NodeRole role, long msgSeq) {

        SnakesProto.GameMessage.JoinMsg.Builder joinMsgBuilder = SnakesProto.GameMessage.JoinMsg.newBuilder();
        SnakesProto.GameMessage.JoinMsg joinMsg = joinMsgBuilder.setGameName(gameName)
                .setPlayerName(playerNickname)
                .setPlayerType(SnakesProto.PlayerType.HUMAN)
                .setRequestedRole(NodeRoleConverter.getInstance().nodeRoleToSnakesProto(role))
                .build();

        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setJoin(joinMsg)
                .setMsgSeq(msgSeq)
                .build();
    }

    public SnakesProto.GameMessage createAckMsg(int senderId, int receiverId, long msgSeq) {

        SnakesProto.GameMessage.AckMsg.Builder ackMsgBuilder = SnakesProto.GameMessage.AckMsg.newBuilder();
        SnakesProto.GameMessage.AckMsg ackMsg = ackMsgBuilder.build();
        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setAck(ackMsg)
                .setMsgSeq(msgSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public SnakesProto.GameMessage createPingMsg(int senderId, int receiverId, long msgSeq) {

        SnakesProto.GameMessage.PingMsg.Builder pingMsgBuilder = SnakesProto.GameMessage.PingMsg.newBuilder();
        SnakesProto.GameMessage.PingMsg pingMsg = pingMsgBuilder.build();
        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setPing(pingMsg)
                .setMsgSeq(msgSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public SnakesProto.GameMessage createRoleChangedMsg(NodeRole senderRole, NodeRole receiverRole,
                                                        int senderId, int receiverId, long msgSeq) {

        SnakesProto.GameMessage.RoleChangeMsg.Builder roleChangeMsgBuilder = SnakesProto.GameMessage.RoleChangeMsg
                .newBuilder();
        SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = roleChangeMsgBuilder
                .setReceiverRole(NodeRoleConverter.getInstance().nodeRoleToSnakesProto(receiverRole))
                .setSenderRole(NodeRoleConverter.getInstance().nodeRoleToSnakesProto(senderRole))
                .build();

        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setRoleChange(roleChangeMsg)
                .setMsgSeq(msgSeq)
                .setReceiverId(receiverId)
                .setSenderId(senderId)
                .build();
    }

    public SnakesProto.GameMessage createErrorMsg(String cause, long msgSeq) {

        SnakesProto.GameMessage.ErrorMsg.Builder errorMsgBuilder = SnakesProto.GameMessage.ErrorMsg.newBuilder();
        SnakesProto.GameMessage.ErrorMsg errorMsg = errorMsgBuilder.setErrorMessage(cause).build();
        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setError(errorMsg)
                .setMsgSeq(msgSeq)
                .build();
    }

    public SnakesProto.GameMessage createDiscoverMsg(long msgSeq) {
        SnakesProto.GameMessage.DiscoverMsg discoverMsg = SnakesProto.GameMessage.DiscoverMsg.getDefaultInstance();
        SnakesProto.GameMessage.Builder gameMsgBuilder = SnakesProto.GameMessage.newBuilder();

        return gameMsgBuilder.setDiscover(discoverMsg)
                .setMsgSeq(msgSeq)
                .build();
    }

}

