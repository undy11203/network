package org.project.model;

import org.project.model.communication.gameplayers.PlayerInfo;
import org.project.model.field.FieldCreator;
import org.project.model.field.cell.Cell;
import org.project.model.field.cell.EmptyCell;
import org.project.model.field.cell.FoodCell;
import org.project.model.field.cell.SnakeCell;
import org.project.model.snake.*;

import java.util.*;

public class GameState {
    private final List<PlayerInfo> playerInfos = new ArrayList<>();
    private final List<Snake> snakes = new ArrayList<>();
    private final List<Coordinate> food = new ArrayList<>();
    private final GameConfig gameConfig;
    private int stateOrder = 0;

    public GameState(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public boolean addNewPlayer(PlayerInfo playerInfo, Coordinate coordinate) {
        int width = gameConfig.width();
        int height = gameConfig.height();

        Direction direction = chooseRandomDirection(coordinate);
        if (direction == null) {
            return false;
        }

        Snake snake = new Snake(coordinate, direction, playerInfo.getId());
        snake.grow(width, height);
        synchronized (snakes) {
            snakes.add(snake);
        }
        synchronized (playerInfos) {
            playerInfos.add(playerInfo);
        }
        return true;
    }

    public Coordinate findFreeArea() {
        Cell[][] field = FieldCreator.getInstance().createField(this);
        int width = gameConfig.width();
        int height = gameConfig.height();
        for (int i = 0; i < width - 4; ++i) {
            for (int j = 0; j < height - 4; ++j) {
                if (isFreeArea(field, i, j)) {
                    int maybeNewSnakeX = i + 2;
                    int maybeNewSnakeHeadY = j + 2;
                    if (field[maybeNewSnakeX][maybeNewSnakeHeadY] instanceof EmptyCell) {
                        return new Coordinate(i + 2, j + 2);
                    }
                }
            }
        }
        return null;
    }

    public void steerSnake(int snakeId, Direction newDirection) {
        Optional<Snake> optionalSnake = findSnake(snakeId);
        optionalSnake.ifPresent(snake -> snake.turn(newDirection));
    }

    public List<Integer> change() {
        int fieldWidth = gameConfig.width();
        int fieldHeight = gameConfig.height();

        detectAteFood(fieldWidth, fieldHeight);

        List<Integer> removedPlayers = detectCollisions();

        generateNewFood();
        stateOrder++;

        return removedPlayers;
    }

    public void setStateOrder(int stateOrder) {
        this.stateOrder = stateOrder;
    }

    public List<PlayerInfo> getPlayers() {
        return Collections.unmodifiableList(playerInfos);
    }

    public List<Snake> getSnakes() {
        return Collections.unmodifiableList(snakes);
    }

    public List<Coordinate> getFood() {
        return Collections.unmodifiableList(food);
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public int getStateOrder() {
        return stateOrder;
    }

    public void addFood(List<Coordinate> food) {
        this.food.addAll(food);
    }

    public void addSnakes(List<Snake> snakes) {
        this.snakes.addAll(snakes);
    }

    public void addPlayerInfos(List<PlayerInfo> playerInfos) {
        synchronized (this.playerInfos) {
            this.playerInfos.clear();
            this.playerInfos.addAll(playerInfos);
        }
    }

    public void handlePlayerLeave(int playerId) {
        synchronized (snakes) {
            snakes.stream()
                    .filter(s -> s.getPlayerId() == playerId).findAny()
                    .ifPresent(Snake::setZombie);
        }
        synchronized (playerInfos) {
            playerInfos.removeIf(player -> player.getId() == playerId);
        }
    }

    void generateNewFood() {
        Random random = new Random();
        Cell[][] field = FieldCreator.getInstance().createField(this);
        int foodStatic = gameConfig.foodStatic();
        int width = gameConfig.width();
        int height = gameConfig.height();
        int amountAliveSnakes = amountAliveSnakes();
        while (food.size() < foodStatic + amountAliveSnakes) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (field[x][y] instanceof EmptyCell) {
                field[x][y] = new FoodCell();
                food.add(new Coordinate(x, y));
            }
        }
    }

    private boolean isFreeArea(Cell[][] field, int leftX, int topY) {
        for (int x = leftX; x < leftX + 5; ++x) {
            for (int y = topY; y < topY + 5; ++y) {
                if (field[x][y] instanceof SnakeCell) {
                    return false;
                }
            }
        }
        return true;
    }

    private int amountAliveSnakes() {
        synchronized (snakes) {
            int count = 0;
            for (Snake snake : snakes) {
                if (snake.getState() == SnakeState.ALIVE) {
                    count++;
                }
            }
            return count;
        }
    }

    private List<Integer> detectCollisions() {
        List<Collision> collisions = findMurders();
        List<Integer> ids = new ArrayList<>();
        for (Collision collision : collisions) {
            ids.add(collision.victimId());
            if (collision.isSuicide()) {
                removeSnake(collision.killerId());
            } else {
                increasePlayerScore(collision.killerId());
                removeSnake(collision.victimId());
            }
        }
        return ids;
    }

    private void removeSnake(int id) {
        synchronized (snakes) {
            Random random = new Random();
            Snake snake = snakes.stream().filter(s -> s.getPlayerId() == id).findFirst().orElse(null);
            if (snake != null) {
                List<SnakeSegment> body = snake.getBody();
                int bodySize = body.size();
                for (int i = 1; i < bodySize; ++i) {
                    if (random.nextBoolean()) {
                        food.add(body.get(i).getCoordinate());
                    }
                }
                snakes.remove(snake);
            }
        }
    }

    private void detectAteFood(int fieldWidth, int fieldHeight) {
        synchronized (snakes) {
            Set<Coordinate> ateFood = new HashSet<>();
            for (Snake snake : snakes) {
                snake.replaceAll(fieldWidth, fieldHeight);
                SnakeSegment head = snake.getHead();
                if (food.contains(head.getCoordinate())) {
                    snake.grow(fieldWidth, fieldHeight);
                    ateFood.add(head.getCoordinate());
                    increasePlayerScore(snake.getPlayerId());
                }
            }
            food.removeAll(ateFood);
        }
    }

    private Optional<Snake> findSnake(int snakeId) {
        synchronized (snakes) {
            return snakes.stream().filter(snake -> snake.getPlayerId() == snakeId).findFirst();
        }
    }

    private List<Collision> findMurders() {
        synchronized (snakes) {
            List<Collision> collisions = new ArrayList<>();
            snakes.forEach(snake -> {
                for (Snake otherSnake : snakes) {
                    if (snake.isSuicide()) {
                        collisions.add(new Collision(snake.getPlayerId(), snake.getPlayerId()));
                        break;
                    }
                    if (otherSnake.getPlayerId() != snake.getPlayerId()
                            && otherSnake.isCollision(snake.getHead().getCoordinate())) {
                        collisions.add(new Collision(otherSnake.getPlayerId(), snake.getPlayerId()));
                    }
                }
            });
            return collisions;
        }
    }

    private void increasePlayerScore(int id) {
        PlayerInfo playerInfo = findPlayer(id).orElse(null);
        if (playerInfo == null) {
            return;
        }
        playerInfo.increaseScore();
    }

    private Optional<PlayerInfo> findPlayer(int id) {
        synchronized (playerInfos) {
            return playerInfos.stream().filter(player -> player.getId() == id).findFirst();
        }
    }

    private Direction chooseRandomDirection(Coordinate head) {
        Random random = new Random();
        int randomDirection = random.nextInt(4);
        int countTries = 0;

        while (countTries != 4) {
            switch (randomDirection) {
                case 0 -> {
                    if (!food.contains(new Coordinate(head.x(), head.y() + 1))) {
                        return Direction.UP;
                    } else {
                        randomDirection = 1;
                        countTries++;
                    }
                }
                case 1 -> {
                    if (!food.contains(new Coordinate(head.x(), head.y() - 1))) {
                        return Direction.DOWN;
                    } else {
                        randomDirection = 2;
                        countTries++;
                    }
                }
                case 2 -> {
                    if (!food.contains(new Coordinate(head.x() - 1, head.y()))) {
                        return Direction.LEFT;
                    } else {
                        randomDirection = 3;
                        countTries++;
                    }
                }
                case 3 -> {
                    if (!food.contains(new Coordinate(head.x() - 1, head.y()))) {
                        return Direction.RIGHT;
                    } else {
                        randomDirection = 0;
                        countTries++;
                    }
                }
            }
        }
        return null;
    }
}
