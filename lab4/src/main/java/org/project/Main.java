package org.project;

import javafx.application.Application;
import javafx.stage.Stage;
import org.project.controllers.MainController;
import org.project.utils.Counter;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Counter.instance.setDaemon(true);
        Counter.instance.start();

//        stage.setResizable(false);
        MainController.getInstance().setStage(stage);
        MainController.getInstance().startApp();
    }

    public static void main(String[] args) {
        launch();
    }
}
