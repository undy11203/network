package org.project.model.field;

import org.project.model.Coordinate;
import org.project.model.GameConfig;
import org.project.model.GameState;
import org.project.model.field.cell.Cell;
import org.project.model.field.cell.EmptyCell;
import org.project.model.field.cell.FoodCell;
import org.project.model.field.cell.SnakeCell;
import org.project.model.snake.Snake;
import org.project.model.snake.SnakeSegment;

import java.util.List;

public class FieldCreator {
    private static final FieldCreator INSTANCE = new FieldCreator();
    private FieldCreator(){}
    public static FieldCreator getInstance(){
        return INSTANCE;
    }
    public Cell[][] createField(GameState state){
        GameConfig config = state.getGameConfig();
        final int width = config.width();
        final int height = config.height();

        Cell[][] field = new Cell[width][height];

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                field[i][j] = new EmptyCell();
            }
        }

        List<Snake> snakes = state.getSnakes();
        snakes.forEach(snake -> {
            List<SnakeSegment> body = snake.getBody();
            body.forEach(snakeSegment -> {
                        int x = snakeSegment.getCoordinate().x();
                        int y = snakeSegment.getCoordinate().y();
                        field[x][y] = new SnakeCell(snake.getPlayerId());
                    }
            );
        });

        List<Coordinate> foods = state.getFood();
        foods.forEach(food -> {
            int x = food.x();
            int y = food.y();
            field[x][y] = new FoodCell();
        });

        return field;
    }
}

