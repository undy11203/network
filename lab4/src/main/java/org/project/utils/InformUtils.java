package org.project.utils;

import javafx.scene.control.Alert;

public class InformUtils {
    public static void inform(String information) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Warning");
        alert.setContentText(information);
        alert.showAndWait();
    }

    public static void error(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(error);
        alert.showAndWait();
    }
}

