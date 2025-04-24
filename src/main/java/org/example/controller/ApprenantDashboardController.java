package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
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
    @FXML
    private Button profileButton;
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

    public void handleExploreCourses(ActionEvent actionEvent) {
    }
    @FXML
    private void handleProfile() {
        try {
            // Load the profile page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileApprenant.fxml"));
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


    @FXML
    private void ouvrirListeEvenements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Liste des Événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void showGames(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
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
