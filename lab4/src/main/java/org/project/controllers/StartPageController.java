package org.project.controllers;

import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;
import org.project.events.message.ExitAppEvent;
import org.project.events.message.SwitchToAvailableGamesListEvent;
import org.project.events.message.SwitchToNewGameConfigEvent;

public class StartPageController {
    private EventBus eventBus;

    @FXML
    private void createNewGame(){
        eventBus.post(new SwitchToNewGameConfigEvent());
    }
    @FXML
    private void joinGame(){
        eventBus.post(new SwitchToAvailableGamesListEvent());
    }
    @FXML
    private void exit(){
        eventBus.post(new ExitAppEvent());
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}