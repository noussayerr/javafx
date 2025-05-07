package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
    private Label studentsCountLabel;
    @FXML
    private Button profileButton;
    @FXML
    private Button logoutButton;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
    private void logout() {
        try {
            // Clear the session
            SessionManager.getInstance().logout();

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();

            // Show logout confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", "Impossible de charger l'écran de connexion: " + e.getMessage());
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

    @FXML
    private void handleProfile() {
        try {
            // Load the profile page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileEnseignant.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil");
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page de profil", e.getMessage());
            e.printStackTrace();
        }
    }


    public void goMatiereF(ActionEvent actionEvent) {
        System.out.println("Naviguer vers gestion des matières...");
        loadPage(actionEvent, "/org/example/view/ListeMatiereF.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            System.out.println("Chargement du fichier : " + fxmlPath);
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // ← affiche l'erreur exacte dans la console
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // ← attrape aussi toute autre erreur de controller
            showAlert(Alert.AlertType.ERROR, "Erreur", "Exception générale", e.getMessage());
        }
    }

}