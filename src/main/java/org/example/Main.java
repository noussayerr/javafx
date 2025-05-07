package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Home.fxml"));
        Parent root = loader.load();

        // Configurer la scène
        Scene scene = new Scene(root, 1000, 600);

        // Configurer la fenêtre principale
        primaryStage.setTitle("Dyscover - Plateforme d'apprentissage");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}