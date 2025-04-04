package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EnseignantDashboardController implements Initializable {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label coursesCountLabel;
    @FXML
    private Label studentsCountLabel;

    @FXML
    private Button logoutButton;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les données de l'utilisateur
        usernameLabel.setText("Professeur Dupont");
        coursesCountLabel.setText("8");
        studentsCountLabel.setText("142");

        // Configurer la table des cours à venir


    }




    @FXML
    private void handleDashboardClick(MouseEvent event) {
        // Logique pour le tableau de bord
        System.out.println("Tableau de bord cliqué");
    }

    @FXML
    private void handleCoursesClick(MouseEvent event) {
        // Logique pour mes cours
        System.out.println("Mes cours cliqué");
    }

    @FXML
    private void handleStudentsClick(MouseEvent event) {
        // Logique pour étudiants
        System.out.println("Étudiants cliqué");
    }

    @FXML
    private void logout(MouseEvent event) {
        try {
            // Effacer la session utilisateur
            SessionManager.getInstance().logout();

            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle depuis le bouton existant
            Stage stage = (Stage) logoutButton.getScene().getWindow(); // NPE here if logoutButton is null

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();

            // Afficher un message de confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", e.getMessage());
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