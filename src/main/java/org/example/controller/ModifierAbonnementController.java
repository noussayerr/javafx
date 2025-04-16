package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entity.Abonnement;
import org.example.entity.Duration;
import org.example.services.ServiceAbonnement;

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
        // Show confirmation alert before modifying
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
}

