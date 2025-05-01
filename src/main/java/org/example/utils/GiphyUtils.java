package org.example.utils;

import javafx.scene.control.Alert;
import org.example.model.giphy.Meta;

public class GiphyUtils {
    public static void showApiError(Meta meta) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("GIPHY API Error");
        alert.setHeaderText("Error " + meta.getStatus());
        alert.setContentText(meta.getMsg());
        alert.showAndWait();
    }

    public static void showConnectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText("Network Problem");
        alert.setContentText("Could not connect to GIPHY service");
        alert.showAndWait();
    }
}