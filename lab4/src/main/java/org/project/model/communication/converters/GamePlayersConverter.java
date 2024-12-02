package org.project.model.communication.converters;

import org.project.SnakesProto;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.communication.gameplayers.InetInfo;
import org.project.model.communication.gameplayers.PlayerInfo;
import org.project.model.communication.udp.Socket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;

public class GamePlayersConverter {
    private final static GamePlayersConverter INSTANCE = new GamePlayersConverter();

    private GamePlayersConverter() {
    }

    public static GamePlayersConverter getInstance() {
        return INSTANCE;
    }

    public SnakesProto.GamePlayers gamePlayersToSnakesProto(List<GamePlayer> gamePlayers) {
        SnakesProto.GamePlayers.Builder builder = SnakesProto.GamePlayers.newBuilder();
        return builder.addAllPlayers(gamePlayers.stream().map(this::gamePlayerToSnakesProto).toList())
                .build();
    }

    public List<GamePlayer> snakesProtoToGamePlayers(SnakesProto.GamePlayers gamePlayers) {
        return gamePlayers.getPlayersList().stream().map(this::snakesProtoToGamePlayer).toList();
    }

    private GamePlayer snakesProtoToGamePlayer(SnakesProto.GamePlayer player) {
        PlayerInfo playerInfo = new PlayerInfo(player.getName(), player.getId(), player.getScore());
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(player.getIpAddress());
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
        InetInfo inetInfo = new InetInfo(inetAddress, player.getPort(), Instant.now().toEpochMilli());
        return new GamePlayer(playerInfo,
                NodeRoleConverter.getInstance().snakesProtoToNodeRole(player.getRole()),
                inetInfo);
    }

    private SnakesProto.GamePlayer gamePlayerToSnakesProto(GamePlayer gamePlayer) {
        Socket socket = gamePlayer.getSocket();
        SnakesProto.GamePlayer.Builder builder = SnakesProto.GamePlayer
                .newBuilder();
        builder.setName(gamePlayer.getPlayerInfo().getNickname())
                .setId(gamePlayer.getPlayerInfo().getId())
                .setPort(socket.port())
                .setRole(NodeRoleConverter.getInstance().nodeRoleToSnakesProto(gamePlayer.getRole()))
                .setScore(gamePlayer.getPlayerInfo().getScore());

        if (socket.address() != null) {
            builder.setIpAddress(socket.address().getHostAddress());
        }
        return builder.build();
    }
}