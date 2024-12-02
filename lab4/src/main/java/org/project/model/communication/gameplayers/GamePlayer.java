package org.project.model.communication.gameplayers;

import org.project.model.communication.NodeRole;
import org.project.model.communication.udp.Socket;

public class GamePlayer {
    private final PlayerInfo playerInfo;
    private final InetInfo inetInfo;

    public GamePlayer(PlayerInfo playerInfo, NodeRole role, InetInfo inetInfo) {
        this.playerInfo = playerInfo;
        playerInfo.setRole(role);
        this.inetInfo = inetInfo;
    }

    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if (o instanceof GamePlayer gamePlayer){
            return gamePlayer.playerInfo.equals(this.playerInfo);
        }else {
            return false;
        }
    }

    public Socket getSocket() {
        return inetInfo.getSocket();
    }

    public NodeRole getRole() {
        return playerInfo.getRole();
    }

    public void setRole(NodeRole role) {
        playerInfo.setRole(role);
    }

    public int getId() {
        return playerInfo.getId();
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public long getLastCommunicationTime() {
        return inetInfo.getLastCommunicationTime();
    }

    public void updateLastCommunication() {
        inetInfo.updateLastCommunication();
    }
}
