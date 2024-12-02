package org.project.model.communication.converters;

import org.project.SnakesProto;
import org.project.model.Coordinate;
import org.project.model.GameConfig;
import org.project.model.GameState;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.communication.gameplayers.PlayerInfo;
import org.project.model.snake.Snake;

import java.util.List;

public class GameStateConverter {
    private static final GameStateConverter INSTANCE = new GameStateConverter();
    private GameStateConverter(){}
    public static GameStateConverter getInstance(){
        return INSTANCE;
    }

    public GameState snakesProtoToGameState(SnakesProto.GameState gameState, GameConfig gameConfig){

        Coordinate fieldSize = new Coordinate(gameConfig.width(), gameConfig.height());

        List<Snake> snakes = gameState.getSnakesList().stream()
                .map(snake -> SnakeConverter.getInstance().snakesProtoToSnake(snake, fieldSize)).toList();

        List<Coordinate> food = gameState.getFoodsList().stream()
                .map(CoordinateConverter.getInstance()::snakesProtoToCoordinate).toList();

        List<PlayerInfo> players = GamePlayersConverter.getInstance().snakesProtoToGamePlayers(gameState.getPlayers())
                .stream().map(GamePlayer::getPlayerInfo).toList();

        GameState modelGameState = new GameState(gameConfig);
        modelGameState.addFood(food);
        modelGameState.addSnakes(snakes);
        modelGameState.addPlayerInfos(players);
        modelGameState.setStateOrder(gameState.getStateOrder());

        return modelGameState;
    }

    public SnakesProto.GameState gameStateToSnakesProto(GameState modelState, List<GamePlayer> gamePlayers){
        SnakesProto.GameState.Builder builder = SnakesProto.GameState.newBuilder();
        return builder.setStateOrder(modelState.getStateOrder())
                .setPlayers(GamePlayersConverter.getInstance().gamePlayersToSnakesProto(gamePlayers))
                .addAllFoods(modelState.getFood().stream().map(CoordinateConverter.getInstance()::coordinateToSnakeProto).toList())
                .addAllSnakes(modelState.getSnakes().stream().map(SnakeConverter.getInstance()::snakeToSnakesProto).toList())
                .build();
    }

}
