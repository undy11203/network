package org.project.model.communication;

import com.google.common.eventbus.EventBus;
import org.project.SnakesProto;
import org.project.events.message.*;
import org.project.model.GameConfig;
import org.project.model.GameState;
import org.project.model.communication.converters.DirectionConverter;
import org.project.model.communication.converters.GamePlayersConverter;
import org.project.model.communication.converters.GameStateConverter;
import org.project.model.communication.converters.NodeRoleConverter;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.communication.udp.Socket;
import org.project.model.communication.udp.UDPMessageReceiverAndSender;
import org.project.model.snake.Direction;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameMessageHandler extends Thread{
    private final EventBus modelEventBus;
    private final GameConfig gameConfig;
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    public GameMessageHandler(EventBus modelEventBus, GameConfig gameConfig){
        this.gameConfig = gameConfig;
        this.modelEventBus = modelEventBus;
    }

    public void run(){

        try {
            UDPMessageReceiverAndSender messageReceiverAndSender = UDPMessageReceiverAndSender.getInstance();

            while (!this.isInterrupted()) {

                Message message = messageReceiverAndSender.receive();

                SnakesProto.GameMessage gameMessage = message.getMessage();

                Socket senderSocket =  message.getSocket();

                if (gameMessage.hasSteer() && !this.isInterrupted()) {
                    SnakesProto.GameMessage.SteerMsg steer = gameMessage.getSteer();
                    Direction newDirection = DirectionConverter.getInstance().snakesProtoToDirection(steer.getDirection());
                    executor.submit(() -> modelEventBus.post(new HandleSteerMsgEvent(newDirection, senderSocket, gameMessage.getMsgSeq())));
                    continue;
                }

                if (gameMessage.hasState() && !this.isInterrupted()) {
                    SnakesProto.GameState snakesProtoState = gameMessage.getState().getState();
                    GameState newState = GameStateConverter.getInstance().snakesProtoToGameState(snakesProtoState, gameConfig);
                    List<GamePlayer> players = GamePlayersConverter.getInstance().snakesProtoToGamePlayers(snakesProtoState.getPlayers());

                    executor.submit(() -> modelEventBus.post(new HandleGameStateMsgEvent(newState, players, senderSocket, gameMessage.getMsgSeq())));
                    continue;
                }

                if (gameMessage.hasJoin() && !this.isInterrupted()) {
                    SnakesProto.GameMessage.JoinMsg joinMsg = gameMessage.getJoin();
                    executor.submit(() ->modelEventBus.post(new HandleJoinMsgEvent(joinMsg, senderSocket, gameMessage.getMsgSeq())));
                    continue;
                }

                if (gameMessage.hasPing() && !this.isInterrupted()) {
                    executor.submit(() ->modelEventBus.post(new HandlePingMsgEvent(senderSocket, gameMessage.getMsgSeq())));
                    continue;
                }

                if (gameMessage.hasAck() && !this.isInterrupted()) {
                    executor.submit(() ->modelEventBus.post(new HandleAckMsgEvent(gameMessage.getSenderId(), gameMessage.getMsgSeq())));
                    continue;
                }

                if (gameMessage.hasError() && !this.isInterrupted()) {
                    executor.submit(() ->modelEventBus.post(new HandleErrorMsgEvent(senderSocket, gameMessage.getMsgSeq())));
                    continue;
                }

                if (gameMessage.hasRoleChange() && !this.isInterrupted()) {
                    SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = gameMessage.getRoleChange();
                    NodeRole senderRole = NodeRoleConverter.getInstance().snakesProtoToNodeRole(roleChangeMsg.getSenderRole());
                    NodeRole receiverRole = NodeRoleConverter.getInstance().snakesProtoToNodeRole(roleChangeMsg.getReceiverRole());
                    executor.submit(() ->modelEventBus.post(new HandleRoleChangeMsgEvent(senderRole, receiverRole, gameMessage.getSenderId(), gameMessage.getReceiverId(), senderSocket, gameMessage.getMsgSeq())));
                }
            }
        } catch (IOException ignored){

        }
    }

}
