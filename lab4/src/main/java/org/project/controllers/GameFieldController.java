package org.project.controllers;

import com.google.common.eventbus.EventBus;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.project.events.message.SwitchToStartPageEvent;
import org.project.exceptions.GameException;
import org.project.model.GameAnnouncement;
import org.project.model.GameConfig;
import org.project.model.GameState;
import org.project.model.Model;
import org.project.model.communication.gameplayers.PlayerInfo;
import org.project.model.field.FieldCreator;
import org.project.model.field.cell.Cell;
import org.project.model.field.cell.EmptyCell;
import org.project.model.field.cell.FoodCell;
import org.project.model.field.cell.SnakeCell;
import org.project.model.snake.Direction;
import org.project.utils.InformUtils;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class GameFieldController {
    private final static Color EVEN_CELL_COLOR = Color.BLACK;
    private final static Color ODD_CELL_COLOR = Color.BLACK.brighter();
    private final static Color FOOD_COLOR = Color.RED;
    @FXML
    public Label gameNameLabel;
    @FXML
    private Canvas gameField;
    @FXML
    private ListView<PlayerInfo> gameScoresList;

    private final HashMap<Integer, Color> snakesColors = new HashMap<>();
    private final RandomColorGenerator randomColorGenerator = new RandomColorGenerator();
    private EventBus eventBus;
    private Model model;
    private Timeline animation;

    void createNewGame(GameState gameState, PlayerInfo playerInfo, String gameName) {
        try {
            gameNameLabel.setText(gameName);
            model = Model.create();
            model.setControllersEventBus(eventBus);
            model.createNewGame(gameState, playerInfo, gameName);
            renderField(gameState);
            gameScoresList.setCellFactory(list -> new ColorRectCell());
            startAnimation(gameState.getGameConfig().delay());
        } catch (IOException e) {
            InformUtils.error(GameException.startingGameError().getMessage());
        }
    }

    void joinGame(Model model, GameAnnouncement gameAnnouncement, String nickname, boolean isViewer) {
        try {
            gameNameLabel.setText(gameAnnouncement.gameName());
            this.model = model;
            gameScoresList.setCellFactory(list -> new ColorRectCell());
            model.joinGame(gameAnnouncement, nickname, isViewer);
        } catch (IOException | InterruptedException e) {
            InformUtils.error(GameException.startingGameError().getMessage());
        }
    }

    void renderField(GameState gameState) {
        GameConfig config = gameState.getGameConfig();
        int width = config.width();
        int height = config.height();

        double fieldWidth = gameField.getWidth();
        double fieldHeight = gameField.getHeight();

        double cellSize = Math.min(fieldWidth / width, fieldHeight / height);

        Cell[][] field = FieldCreator.getInstance().createField(gameState);

        drawField(field, width, height, cellSize);
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    void reloadGameScores() {
        ObservableList<PlayerInfo> playerInfos = FXCollections.observableList(model.getCurrentPlayers());

        gameScoresList.setItems(playerInfos);
    }

    void stopAnimation() {
        if (animation != null) {
            animation.stop();
        }
    }

    void startAnimation(int delay) {
        stopAnimation();
        animation = new Timeline(new KeyFrame(Duration.millis(delay), ae -> {
            model.changeGameState();
            reloadGameScores();
            renderField(model.getGameState());
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    private void drawField(Cell[][] field, int width, int height, double cellSize) {
        GraphicsContext gc = gameField.getGraphicsContext2D();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell cell = field[i][j];
                if (cell instanceof EmptyCell) {
                    gc.setFill((i + j) % 2 == 0 ? EVEN_CELL_COLOR : ODD_CELL_COLOR);
                } else if (cell instanceof FoodCell) {
                    gc.setFill(FOOD_COLOR);
                } else if (cell instanceof SnakeCell snakeCell) {
                    colorSnake(snakeCell, gc);
                }
                gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
    }

    private void colorSnake(SnakeCell snakeCell, GraphicsContext gc) {
//        int playerId = snakeCell.getPlayerId();
//        Color snakeColor;
//
//        if (playerId == model.getMe().getId()) {
//            // Assign a specific color for your snake
//            snakeColor = Color.BLUE; // Example color for your snake
//        } else {
//            // Assign a different color for other snakes
//            snakeColor = snakesColors.computeIfAbsent(playerId, id -> randomColorGenerator.generateRandomColor());
//        }
//
//        gc.setFill(snakeColor);
        Color random = randomColorGenerator.generateRandomColor();
        Color current = snakesColors.putIfAbsent(snakeCell.getPlayerId(), random);
        if (current == null) {
            gc.setFill(random);
        } else {
            gc.setFill(current);
        }
    }

    @FXML
    private void endGame() {
        if (animation != null) {
            animation.stop();
        }
        model.end();
        model = null;
        eventBus.post(new SwitchToStartPageEvent());
    }

    @FXML
    private void steerSnake(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case W -> model.steerSnake(Direction.UP);
            case A -> model.steerSnake(Direction.LEFT);
            case S -> model.steerSnake(Direction.DOWN);
            case D -> model.steerSnake(Direction.RIGHT);
        }
    }

    private class ColorRectCell extends ListCell<PlayerInfo> {
        @Override
        public void updateItem(PlayerInfo item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                Rectangle rect = new Rectangle(10, 10);
                rect.setFill(snakesColors.get(item.getId()));
                setGraphic(rect);
                setText(item.toString());
                setBackground(Background.fill(Color.WHITE));
                if (item.getId() == model.getMe().getId()) {
                    setBackground(Background.fill(Color.GRAY.brighter()));
                }
            } else {
                setGraphic(null);
                setText(null);
                setBackground(Background.fill(Color.WHITE));
            }
        }
    }
}
