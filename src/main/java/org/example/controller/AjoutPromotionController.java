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
import org.example.entity.Promotion;
import org.example.services.ServicePromotion;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjoutPromotionController implements Initializable {

    private ServicePromotion servicePromotion=new ServicePromotion();
    private Abonnement selected;
    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField reductionField;

    @FXML
    private TextField titreField;
    @FXML
    private Label titreError;
    @FXML
    private Label descriptionError;
    @FXML
    private Label reductionError;
    @FXML
    private Label dateDebutError;
    @FXML
    private Label dateFinError;

    @FXML
    void handleAjouterPromotion(ActionEvent event) throws SQLException {
        // Reset des erreurs
        titreError.setText("");
        descriptionError.setText("");
        reductionError.setText("");
        dateDebutError.setText("");
        dateFinError.setText("");

        boolean valid = true;

        String titre = titreField.getText();
        String description = descriptionField.getText();
        int reduction = 0;

        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Le titre est obligatoire.");
            valid = false;
        }

        if (description == null || description.trim().length() < 15) {
            descriptionError.setText("La description doit contenir au moins 15 caractères.");
            valid = false;
        }

        if (reductionField.getText() == null || reductionField.getText().trim().isEmpty()) {
            reductionError.setText("La réduction est obligatoire.");
            valid = false;
        } else {
            try {
                reduction = Integer.parseInt(reductionField.getText().trim());
                if (reduction < 0 || reduction > 100) {
                    reductionError.setText("La réduction doit être entre 0% et 100%.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                reductionError.setText("Veuillez entrer un nombre valide.");
                valid = false;
            }
        }

        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (dateDebut == null) {
            dateDebutError.setText("Veuillez choisir une date de début.");
            valid = false;
        }

        if (dateFin == null) {
            dateFinError.setText("Veuillez choisir une date de fin.");
            valid = false;
        }

        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            dateFinError.setText("La date de fin doit être après la date de début.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        Promotion promotion = new Promotion(titre, description, reduction, dateDebut, dateFin);
        servicePromotion.ajouterPromo(promotion, this.selected.getId());

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Promotion ajoutée avec succès !");
        this.goBackToListScene();
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public AjoutPromotionController(Abonnement selected) {
        this.selected = selected;
    }

    public AjoutPromotionController() {
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    private void goBackToListScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListeAbonnement.fxml"));
            Scene listScene = new Scene(loader.load());

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(listScene);
            stage.setTitle("Liste des Abonnements");
        } catch (IOException e) {
            e.printStackTrace();

        }
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
