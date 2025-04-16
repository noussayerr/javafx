package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;



import javafx.scene.control.Alert;
import org.example.utils.Toast;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private void handleClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText("Bravo Nounou, tu maîtrises JavaFX 🎉");
        alert.showAndWait();
    }

    @FXML
    private Button btnTheme;

    @FXML
    private void afficherEvenements(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firsttry/views/evenements-view.fxml"));

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirCategorieView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firsttry/views/categorie-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📂 Gestion des Catégories");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture catégories : " + e.getMessage());
        }
    }
    @FXML
    private void ouvrirStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firsttry/views/statistiques-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📊 Statistiques");
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("✅ Statistiques ouvertes !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture statistiques : " + e.getMessage());
        }
    }
    @FXML
    private void ouvrirListeEvenements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firsttry/views/evenement-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Liste des Événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
