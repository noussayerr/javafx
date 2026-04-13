package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/login.fxml"));
        Parent root = loader.load();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        // Configurer la scène
        Scene scene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());

        // Configurer la fenêtre principale
        primaryStage.setTitle("Sports Management - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}