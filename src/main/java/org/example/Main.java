package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML (Assurez-vous que le chemin est correct)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Home.fxml"));

        Scene scene = new Scene(loader.load(), 400, 500);
        primaryStage.setTitle("Home");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // La méthode main pour démarrer l'application
    public static void main(String[] args) {
        launch();  // Démarrer l'application JavaFX
    }
}
