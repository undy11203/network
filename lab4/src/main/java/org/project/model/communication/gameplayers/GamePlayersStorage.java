package org.project.model.communication.gameplayers;

import org.project.model.communication.NodeRole;
import org.project.model.communication.udp.Socket;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GamePlayersStorage {
    private final List<GamePlayer> gamePlayers = new ArrayList<>();

    public List<GamePlayer> getGamePlayers() {
        return Collections.unmodifiableList(gamePlayers);
    }

    public Optional<GamePlayer> findGamePlayerById(int playerId) {
        return gamePlayers.stream()
                .filter(player -> player.getId() == playerId)
                .findAny();
    }

    public Optional<GamePlayer> findGamePlayerBySocket(Socket socket) {
        return gamePlayers.stream()
                .filter(player -> socket.equals(player.getSocket()))
                .findAny();
    }

    public Optional<GamePlayer> findGamePlayerByRole(NodeRole role) {
        return gamePlayers.stream()
                .filter(player -> role.equals(player.getRole()))
                .findAny();
    }

    public void setGamePlayers(List<GamePlayer> gamePlayers) {
        this.gamePlayers.clear();
        this.gamePlayers.addAll(gamePlayers);
    }

    public void add(GamePlayer gamePlayer) {
        gamePlayers.add(gamePlayer);
    }

    public void delete(GamePlayer gamePlayer) {
        gamePlayers.remove(gamePlayer);
    }

    public void deleteAll(List<GamePlayer> toDelGamePlayers) {
        gamePlayers.removeAll(toDelGamePlayers);
    }

    public List<GamePlayer> findForLastCommunicationPlayer(long minDiffWithNow, int myId) {
        long instant = Instant.now().toEpochMilli();
        return gamePlayers.stream()
                .filter(player -> player.getPlayerInfo().getId() != myId)
                .filter(player -> player.getLastCommunicationTime()!=-1 && instant - player.getLastCommunicationTime() > minDiffWithNow)
                .toList();
    }
}
