package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.entity.Abonnement;
import org.example.entity.Duration;
import org.example.services.ServiceAbonnement;

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

    private ServiceAbonnement serviceAbonnement=new ServiceAbonnement();
    @FXML
    void AjouterPersonneAction(ActionEvent event) throws SQLException {
        int prix =Integer.parseInt(prixAbonnement.getText());
        String titre=titreAbonnement.getText();
        String description=descriptionAbonnement.getText();
        Duration duration=periode.getValue();
        Abonnement abonnement=new Abonnement();
        abonnement.setDescription(description);
        abonnement.setTitreAbonnement(titre);
        abonnement.setDuration(duration);
        abonnement.setPrix(prix);
        serviceAbonnement.ajouter(abonnement);


    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        periode.getItems().setAll(Duration.values());
    }


}
