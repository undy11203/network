package org.project.model.field.cell;

public final class SnakeCell extends Cell {
    private final int playerId;

    public SnakeCell(int id){
        playerId = id;
    }

    public int getPlayerId() {
        return playerId;
    }
}
