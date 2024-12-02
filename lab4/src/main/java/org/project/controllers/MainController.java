package org.project.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.project.Main;
import org.project.events.message.*;
import org.project.model.Model;

import java.io.IOException;

public class MainController {
    private final static String START_PAGE_FXML = "start_page.fxml";
    private final static String NEW_GAME_SETTINGS_FXML = "new_game_config.fxml";
    private final static String GAME_FIELD_FXML = "game_field.fxml";
    private final static String AVAILABLE_GAMES_LIST_FXML = "available_games_list.fxml";

    private Scene startPageScene;
    private Scene newGameSettingsScene;
    private Scene gameFieldScene;
    private Scene availableGamesListScene;
    private GameFieldController gameFieldController;
    private AvailableGamesListController availableGamesListController;
    private final static MainController INSTANCE = new MainController();
    private Stage stage;
    private final EventBus eventBus = new EventBus();

    private MainController() {
    }

    public static MainController getInstance() {
        return INSTANCE;
    }

    public void startApp() throws IOException {
        startPageScene = loadScene(START_PAGE_FXML);
        newGameSettingsScene = loadScene(NEW_GAME_SETTINGS_FXML);
        gameFieldScene = loadScene(GAME_FIELD_FXML);
        availableGamesListScene = loadScene(AVAILABLE_GAMES_LIST_FXML);
        eventBus.register(this);
        eventBus.post(new SwitchToStartPageEvent());
        stage.show();
    }

    @Subscribe
    public void renderField(RenderGameFieldEvent e) {
        Platform.runLater(() -> gameFieldController.renderField(e.gameState()));
    }

    @Subscribe
    public void startNewGameAnimation(StartNewGameAnimationEvent e){
        Platform.runLater(() -> gameFieldController.startAnimation(e.delay()));
    }

    @Subscribe
    public void switchToNewGameConfig(SwitchToNewGameConfigEvent e) {
        Platform.runLater(() -> stage.setScene(newGameSettingsScene));
    }

    @Subscribe
    public void switchToGameField(SwitchToGameFieldEvent e) {
        Platform.runLater(() -> stage.setScene(gameFieldScene));
    }

    @Subscribe
    public void switchToAvailableGamesList(SwitchToAvailableGamesListEvent e) {
        try {
            availableGamesListController.setModel(Model.create());
            Platform.runLater(() -> stage.setScene(availableGamesListScene));
        } catch (IOException ignored) {

        }
    }

    @Subscribe
    public void updateScores(UpdateGameScoresEvent e) {
        Platform.runLater(() -> gameFieldController.reloadGameScores());
    }

    @Subscribe
    public void switchToStartPage(SwitchToStartPageEvent e) {
        Platform.runLater(() -> stage.setScene(startPageScene));
    }

    @Subscribe
    public void exit(ExitAppEvent e) {
        stage.close();
        Platform.exit();
        System.exit(0);
    }

    @Subscribe
    public void updateAvailableList(UpdateAvailableGamesEvent e) {
        Platform.runLater(() -> availableGamesListController.updateListOfGames(e.availableGames()));
    }

    @Subscribe
    public void joinGame(JoinGameEvent e) {
        gameFieldController.joinGame(e.model(), e.gameAnnouncement(), e.nickname(), e.isViewer());
    }

    @Subscribe
    public void startGame(StartNewGameEvent e) {
        gameFieldController.createNewGame(e.gameState(), e.playerInfo(), e.gameName());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Scene loadScene(String sceneFxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(sceneFxml));
        Parent root = loader.load();
        setController(loader);
        return new Scene(root);
    }

    private void setController(FXMLLoader loader) {
        var controller = loader.getController();
        if (controller instanceof StartPageController c) {
            c.setEventBus(eventBus);
        } else if (controller instanceof GameFieldController c) {
            this.gameFieldController = c;
            this.gameFieldController.setEventBus(eventBus);
        } else if (controller instanceof NewGameConfigController c) {
            c.setEventBus(eventBus);
        } else if (controller instanceof AvailableGamesListController c) {
            this.availableGamesListController = c;
            this.availableGamesListController.setEventBus(eventBus);
        }
    }

}
