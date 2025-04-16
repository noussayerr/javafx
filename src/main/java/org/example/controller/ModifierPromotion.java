package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.entity.Abonnement;
import org.example.entity.Promotion;
import org.example.services.ServicePromotion;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
            Promotion promo = selected.getPromotion();
            promo.setTitre(titreField.getText());
            promo.setDescription(descriptionField.getText());
            promo.setReduction(Integer.parseInt(reductionField.getText()));
            promo.setDateDebut((dateDebutPicker.getValue()));
            promo.setDateFin((dateFinPicker.getValue()));

            servicePromotion.modifier(promo);

            showAlert("Succès", "Promotion modifiée avec succès.", Alert.AlertType.INFORMATION);
    this.goBackToListScene();

        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "La réduction doit être un nombre.", Alert.AlertType.ERROR);
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
}
