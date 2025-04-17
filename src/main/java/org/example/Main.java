package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.entity.Promotion;
import org.example.services.ServicePromotion;

import java.time.LocalDate;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML (Assurez-vous que le chemin est correct)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));

        // Obtenir la taille de l'écran
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        // Créer la scène avec la taille de l'écran
        Scene scene = new Scene(loader.load(), screenWidth-100, screenHeight-100);

        primaryStage.setTitle("Inscription Apprenant");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    // La méthode main pour démarrer l'application
    public static void main(String[] args) {
        launch();

    }
}
