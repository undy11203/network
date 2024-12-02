package org.project.model.communication.converters;

import org.project.SnakesProto;
import org.project.model.snake.Direction;

public class DirectionConverter {

    private final static DirectionConverter INSTANCE = new DirectionConverter();

    private DirectionConverter() {
    }

    public static DirectionConverter getInstance() {
        return INSTANCE;
    }

    public Direction snakesProtoToDirection(SnakesProto.Direction direction) {
        return switch (direction) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
        };
    }

    public SnakesProto.Direction directionToSnakesProto(Direction direction) {
        return switch (direction) {
            case UP -> SnakesProto.Direction.UP;
            case DOWN -> SnakesProto.Direction.DOWN;
            case LEFT -> SnakesProto.Direction.LEFT;
            case RIGHT -> SnakesProto.Direction.RIGHT;
        };
    }
}
