package org.project.controllers;

import com.google.common.eventbus.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.project.events.message.JoinGameEvent;
import org.project.events.message.SwitchToStartPageEvent;
import org.project.exceptions.UserInputException;
import org.project.model.GameAnnouncement;
import org.project.model.Model;
import org.project.utils.CheckUserInputUtils;
import org.project.utils.InformUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AvailableGamesListController {

    @FXML
    private ListView<GameAnnouncement> gamesList;
    @FXML
    private TextField nicknameField;
    @FXML
    private CheckBox viewerModeCheckBox;
    private EventBus eventBus;
    private Model model;
    private ScheduledExecutorService scheduler;

    void updateListOfGames(List<GameAnnouncement> gameAnnouncementList) {
        ObservableList<GameAnnouncement> items = FXCollections.observableArrayList(gameAnnouncementList);
        gamesList.setItems(items);
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    void setModel(Model model) {
        model.setControllersEventBus(eventBus);
        this.model = model;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(model::removeExpiredGames, 1, 1, TimeUnit.SECONDS);
    }

    @FXML
    private void back() {
        scheduler.shutdownNow();
        eventBus.post(new SwitchToStartPageEvent());
    }

    @FXML
    private void chooseGame() {
        try {
            GameAnnouncement chosenGame = gamesList.getSelectionModel().getSelectedItem();
            if (chosenGame != null) {
                String nickname = nicknameField.getText();
                CheckUserInputUtils.checkGameName(nickname);
                eventBus.post(new JoinGameEvent(model, chosenGame, nickname, viewerModeCheckBox.isSelected()));
                scheduler.shutdownNow();
            }
        } catch (UserInputException e) {
            InformUtils.inform(e.getMessage());
        }
    }

    @FXML
    private void discover() throws IOException {
        model.sendDiscoverMsg();
    }
}
