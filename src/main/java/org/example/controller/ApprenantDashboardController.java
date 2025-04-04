package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.utils.SessionManager;

import java.io.IOException;

public class ApprenantDashboardController {

    // Éléments du FXML
    @FXML
    private Button logoutButton;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label coursesCountLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private ListView<?> recentCoursesList;

    // Méthode d'initialisation (optionnelle)
    @FXML
    private void initialize() {
        // Vous pouvez initialiser vos éléments ici si besoin
        // Par exemple :
        // usernameLabel.setText(SessionManager.getInstance().getCurrentUsername());
        // coursesCountLabel.setText("12");
        // progressLabel.setText("75%");
    }

    @FXML
    private void logout() {
        try {
            // Effacer la session utilisateur
            SessionManager.getInstance().logout();

            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle depuis le bouton existant
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();

            // Afficher un message de confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie",
                    "Vous avez été déconnecté avec succès.", "");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}