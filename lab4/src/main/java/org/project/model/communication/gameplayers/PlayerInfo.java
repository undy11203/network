package org.project.model.communication.gameplayers;


import org.project.model.communication.NodeRole;

public class PlayerInfo {
    private final String nickname;
    private final int id;
    private int score = 0;
    private NodeRole role = NodeRole.NORMAL;

    public PlayerInfo(String nickname){
        this.nickname=nickname;
        this.id = IDGenerator.getInstance().generate();
    }

    public PlayerInfo(String nickname, int id, int score){
        this.nickname=nickname;
        this.id = id;
        this.score = score;
    }

    public PlayerInfo(String nickname, int id){
        this.nickname=nickname;
        this.id = id;
    }

    public int getScore() {
        return score;
    }
    public void increaseScore(){
        score++;
    }

    @Override
    public String toString(){
        String role = switch (this.role){
            case DEPUTY -> "DEPUTY";
            case MASTER -> "MASTER";
            case NORMAL -> "NORMAL";
            case VIEWER -> "VIEWER";
        };
        if (this.role == NodeRole.VIEWER){
            return role + " " + nickname;
        }
        return role + " " + nickname + ": " + score + " points";
    }

    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if (o instanceof PlayerInfo playerInfo){
            return playerInfo.id == this.id;
        }else {
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public NodeRole getRole() {
        return role;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }
}
