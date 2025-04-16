package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.entity.Abonnement;
import org.example.entity.Promotion;
import org.example.services.ServicePromotion;

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
    void handleAjouterPromotion(ActionEvent event) throws SQLException {
        String titre = titreField.getText();
        String description = descriptionField.getText();
        int reduction;

        try {
            reduction = Integer.parseInt(reductionField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Réduction doit être un nombre entier.");
            return;
        }

        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (titre.isEmpty() || description.isEmpty() || dateDebut == null || dateFin == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (dateDebut.isAfter(dateFin)) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "La date de début ne peut pas être après la date de fin.");
            return;
        }

        Promotion promotion = new Promotion(titre,
                description,
                reduction,
                dateDebut,
                dateFin);


         servicePromotion.ajouterPromo(promotion,this.selected.getId());

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Promotion ajoutée avec succès !");


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

}
