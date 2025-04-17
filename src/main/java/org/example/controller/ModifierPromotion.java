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
import java.util.Date;
import java.util.ResourceBundle;

public class ModifierPromotion implements Initializable {

    private Abonnement selected;
    private final ServicePromotion servicePromotion = new ServicePromotion();
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
    public ModifierPromotion() {
    }

    public ModifierPromotion(Abonnement selected) {
        this.selected = selected;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (selected != null && selected.getPromotion() != null) {
            Promotion promo = selected.getPromotion();
            titreField.setText(promo.getTitre());
            descriptionField.setText(promo.getDescription());
            reductionField.setText(String.valueOf(promo.getReduction()));
            dateDebutPicker.setValue(promo.getDateDebut());
            dateFinPicker.setValue(promo.getDateFin());
        }
    }

    @FXML
    void handleModifierPromotion(ActionEvent event) {
        if (selected == null || selected.getPromotion() == null) return;

        try {
            String titre = titreField.getText();
            String description = descriptionField.getText();
            String reductionText = reductionField.getText();
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();

            // === Validation ===
            if (titre == null || titre.trim().isEmpty()) {
                showAlert("Champ invalide", "Le titre ne doit pas être vide.", Alert.AlertType.WARNING);
                return;
            }

            if (description == null || description.trim().length() < 15) {
                showAlert("Description invalide", "La description doit contenir au moins 15 caractères.", Alert.AlertType.WARNING);
                return;
            }

            int reduction;
            try {
                reduction = Integer.parseInt(reductionText);
                if (reduction < 0) {
                    showAlert("Valeur invalide", "La réduction ne peut pas être négative.", Alert.AlertType.WARNING);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "La réduction doit être un nombre entier.", Alert.AlertType.ERROR);
                return;
            }

            if (dateDebut == null || dateFin == null) {
                showAlert("Champs manquants", "Veuillez sélectionner les dates de début et de fin.", Alert.AlertType.WARNING);
                return;
            }

            if (dateDebut.isAfter(dateFin)) {
                showAlert("Date invalide", "La date de début ne peut pas être après la date de fin.", Alert.AlertType.WARNING);
                return;
            }

            // === Mise à jour de la promotion ===
            Promotion promo = selected.getPromotion();
            promo.setTitre(titre.trim());
            promo.setDescription(description.trim());
            promo.setReduction(reduction);
            promo.setDateDebut(dateDebut);
            promo.setDateFin(dateFin);

            servicePromotion.modifier(promo);

            showAlert("Succès", "Promotion modifiée avec succès.", Alert.AlertType.INFORMATION);
            this.goBackToListScene();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la modification.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    void supprimerPromotion(ActionEvent event) {
        if (selected == null || selected.getPromotion() == null) return;

        try {
            servicePromotion.supprimer(selected.getPromotion().getId());
            selected.setPromotion(null); // Detach promotion from abonnement

            showAlert("Suppression", "Promotion supprimée avec succès.", Alert.AlertType.INFORMATION);
            this.goBackToListScene();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la suppression.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            showAlert("Erreur", "Impossible de revenir à la liste des abonnements.", Alert.AlertType.ERROR);
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
