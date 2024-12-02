package org.project.model.snake;

import org.project.model.Coordinate;

public class SnakeSegment {
    private Direction direction;
    private Coordinate coordinate;

    SnakeSegment(Direction direction, Coordinate coordinate) {
        this.direction = direction;
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Direction getDirection() {
        return direction;
    }

    void replace(int fieldWidth, int fieldHeight) {
        coordinate = direction.nextCoordinate(coordinate, fieldWidth, fieldHeight);
    }

    void setDirection(Direction direction) {
        this.direction = direction;
    }
}
