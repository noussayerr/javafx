package org.example.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneController {

    public static FXMLLoader switchTo(String fxmlFile, Stage stage, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("/org/example/" + fxmlFile));
            Parent root = loader.load();

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double targetWidth = stage.getWidth() > 0 ? stage.getWidth() : visualBounds.getWidth();
            double targetHeight = stage.getHeight() > 0 ? stage.getHeight() : visualBounds.getHeight();

            Scene scene = new Scene(root, targetWidth, targetHeight);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
            javafx.application.Platform.runLater(() -> {
                stage.setWidth(visualBounds.getWidth());
                stage.setHeight(visualBounds.getHeight());
                stage.setMaximized(true);
            });
            return loader;
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlFile);
            e.printStackTrace();
            return null;
        }
    }
}
