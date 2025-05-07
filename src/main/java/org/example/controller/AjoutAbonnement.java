package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entity.Abonnement;
import org.example.entity.Duration;
import org.example.services.ServiceAbonnement;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjoutAbonnement implements Initializable {


    @FXML
    private ComboBox<Duration> periode;


    @FXML
    private Button AjoutButton;

    @FXML
    private TextField descriptionAbonnement;

    @FXML
    private TextField prixAbonnement;

    @FXML
    private TextField titreAbonnement;

    @FXML
    private Label titreError;
    @FXML
    private Label descriptionError;
    @FXML
    private Label prixError;
    @FXML
    private Label periodeError;

    private ServiceAbonnement serviceAbonnement=new ServiceAbonnement();
    @FXML
    void AjouterPersonneAction(ActionEvent event) throws SQLException {
        // Clear all previous errors
        titreError.setText("");
        descriptionError.setText("");
        prixError.setText("");
        periodeError.setText("");

        String titre = titreAbonnement.getText();
        String description = descriptionAbonnement.getText();
        String prixText = prixAbonnement.getText();
        Duration duration = periode.getValue();

        boolean hasError = false;

        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Le titre ne doit pas être vide.");
            hasError = true;
        }

        if (description == null || description.trim().length() < 15) {
            descriptionError.setText("La description doit contenir au moins 15 caractères.");
            hasError = true;
        }

        int prix = 0;
        try {
            prix = Integer.parseInt(prixText);
            if (prix < 0) {
                prixError.setText("Le prix ne peut pas être négatif.");
                hasError = true;
            }
        } catch (NumberFormatException e) {
            prixError.setText("Le prix doit être un nombre valide.");
            hasError = true;
        }

        if (duration == null) {
            periodeError.setText("Veuillez sélectionner une période.");
            hasError = true;
        }

        if (hasError) {
            return; // Stop execution if there are errors
        }

        // Otherwise continue
        Abonnement abonnement = new Abonnement();
        abonnement.setDescription(description);
        abonnement.setTitreAbonnement(titre);
        abonnement.setDuration(duration);
        abonnement.setPrix(prix);
        serviceAbonnement.ajouter(abonnement);

        showAlert("Succès", "Abonnement ajouté avec succès.");

        // Navigation vers la page ListAbonnement.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        periode.getItems().setAll(Duration.values());
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private Button logoutButton; // Inject the button with fx:id="logoutButton"

    @FXML
    private void handleLogout() {
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
    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) {
        try {
            // Load the listAbonnement view
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/listAbonnement.fxml"));
            Scene scene = new Scene(root);

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load Abonnements page");
            alert.setContentText("An error occurred while trying to navigate to the Abonnements page.");
            alert.showAndWait();
        }
    }
}
