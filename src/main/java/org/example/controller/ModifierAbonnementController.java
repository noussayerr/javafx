package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.sql.SQLException;
import java.util.Optional;

public class ModifierAbonnementController {
    private Abonnement abonnement;
    private ServiceAbonnement serviceAbonnement = new ServiceAbonnement();
    // Inject the selected Abonnement
    public ModifierAbonnementController(Abonnement abonnement) {
        this.abonnement = abonnement;
    }

    public ModifierAbonnementController() {
    }

    @FXML
    private TextField titreAbonnementField;
    @FXML
    private TextField prixField;
    @FXML
    private TextArea descriptionField;


    @FXML
    public void initialize() {
        // Populate the form with the data from the selected Abonnement
        titreAbonnementField.setText(abonnement.getTitreAbonnement());
        prixField.setText(String.valueOf(abonnement.getPrix()));
        descriptionField.setText(abonnement.getDescription());



    }

    @FXML
    public void handleSave() {

        String titre = titreAbonnementField.getText();
        String description = descriptionField.getText();
        String prixText = prixField.getText();

        // === Contrôles de validation ===
        if (titre == null || titre.trim().isEmpty()) {
            showAlert("Erreur de validation", "Le titre ne doit pas être vide.");
            return;
        }

        if (description == null || description.trim().length() < 15) {
            showAlert("Erreur de validation", "La description doit contenir au moins 15 caractères.");
            return;
        }

        int prix;
        try {
            prix = Integer.parseInt(prixText);
            if (prix < 0) {
                showAlert("Erreur de validation", "Le prix ne peut pas être négatif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Le prix doit être un nombre entier valide.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Voulez-vous vraiment modifier cet abonnement ?");
        confirmAlert.setContentText("Les changements seront enregistrés.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Proceed with modification if confirmed
            abonnement.setTitreAbonnement(titreAbonnementField.getText());
            abonnement.setPrix(Integer.parseInt(prixField.getText()));
            abonnement.setDescription(descriptionField.getText());

            try {
                serviceAbonnement.modifier(abonnement);

                // Show success alert
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Mise à jour réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("L'abonnement a été mis à jour avec succès !");
                successAlert.showAndWait();

                // Load the list scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
                Scene listScene = new Scene(loader.load());
                Stage stage = (Stage) titreAbonnementField.getScene().getWindow();
                stage.setScene(listScene);
                stage.setTitle("Liste des Abonnements");

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de sauvegarde");
                alert.setHeaderText("Erreur lors de la sauvegarde");
                alert.setContentText("Impossible de mettre à jour l'abonnement.");
                alert.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void handleAnnulerAction() throws IOException {
        // Close the current window (stage)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Scene listScene = new Scene(loader.load());
        Stage stage = (Stage) titreAbonnementField.getScene().getWindow();
        stage.setScene(listScene);
        stage.setTitle("Liste des Abonnements");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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

