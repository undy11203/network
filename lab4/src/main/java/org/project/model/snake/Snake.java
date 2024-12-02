package org.project.model.snake;

import org.project.SnakesProto;
import org.project.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private final List<SnakeSegment> body = new ArrayList<>();
    private SnakeState state = SnakeState.ALIVE;
    private final int playerId;

    public Snake(Coordinate headCoordinate, Direction headDirection, int playerId) {
        this.playerId = playerId;
        body.add(new SnakeSegment(headDirection, headCoordinate));
    }

    public boolean isSuicide() {
        Coordinate head = getHead().getCoordinate();
        for (int i = 1; i < body.size(); ++i) {
            if (head.equals(body.get(i).getCoordinate())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCollision(Coordinate coordinate) {
        return body.stream().anyMatch(segment -> segment.getCoordinate().equals(coordinate));
    }

    public void addNewSegment(Direction direction, int fieldWidth, int fieldHeight) {
        SnakeSegment lastSegment = body.get(body.size() - 1);
        Coordinate newCoordinate = direction.nextCoordinate(lastSegment.getCoordinate(), fieldWidth, fieldHeight);
        body.add(new SnakeSegment(direction.getOpposite(), newCoordinate));
    }

    public void grow(int fieldWidth, int fieldHeight) {
        Direction oppositeLastSegmentDirection = body.get(body.size() - 1).getDirection().getOpposite();
        addNewSegment(oppositeLastSegmentDirection, fieldWidth, fieldHeight);
    }

    public void replaceAll(int fieldWidth, int fieldHeight) {
        for (var segment : body) {
            segment.replace(fieldWidth, fieldHeight);
        }
        for (int i = body.size() - 1; i > 0; --i) {
            body.get(i).setDirection(body.get(i - 1).getDirection());
        }
    }

    public void turn(Direction newDirection) {
        if (state.ordinal() == SnakesProto.GameState.Snake.SnakeState.ALIVE.ordinal()) {
            SnakeSegment neck = body.get(1);
            if (neck.getDirection() == newDirection.getOpposite()) {
                return;
            }
        } else {
            return;
        }
        SnakeSegment head = body.get(0);
        head.setDirection(newDirection);
    }

    public SnakeSegment getHead() {
        return body.get(0);
    }

    public void setZombie() {
        state = SnakeState.ZOMBIE;
    }

    public SnakeState getState() {
        return state;
    }

    public List<SnakeSegment> getBody() {
        return body;
    }

    public int getPlayerId() {
        return playerId;
    }

}
