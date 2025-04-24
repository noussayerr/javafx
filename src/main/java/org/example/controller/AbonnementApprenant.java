package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.Abonnement;
import org.example.entity.Promotion;
import org.example.services.ServiceAbonnement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AbonnementApprenant {
    @FXML
    private HBox abonnementsContainer;  // HBox to hold the cards horizontally

    private ServiceAbonnement serviceAbonnement = new ServiceAbonnement();  // Service to fetch abonnements

    @FXML
    public void initialize() {
        try {
            // Fetch all abonnements
            List<Abonnement> abonnements = serviceAbonnement.afficher();

            // Add each abonnement as a card to the HBox
            for (Abonnement abonnement : abonnements) {
                Node card = createAbonnementCard(abonnement);
                abonnementsContainer.getChildren().add(card);  // Add card to the HBox
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les abonnements.", Alert.AlertType.ERROR);
        }
    }

    // Create a card for each abonnement
    private Node createAbonnementCard(Abonnement abonnement) {
        // Create VBox for each card with spacing between elements
        VBox card = new VBox(10);  // 10 is the spacing between elements
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-background-radius: 8; -fx-border-radius: 8;");
        card.setPrefWidth(220);  // Adjust the width as necessary

        // Title of the abonnement
        javafx.scene.control.Label titreLabel = new javafx.scene.control.Label(abonnement.getTitreAbonnement());
        titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Description of the abonnement
        javafx.scene.control.Label descriptionLabel = new javafx.scene.control.Label(abonnement.getDescription());
        descriptionLabel.setWrapText(true);

        // Price of the abonnement (before any reduction)
        javafx.scene.control.Label prixLabel = new javafx.scene.control.Label("Prix : " + abonnement.getPrix() + " DT");

        // Check if there is a promotion for the abonnement
        Promotion promo = abonnement.getPromotion();
        if (promo != null) {
            // There is a promotion, calculate the discounted price
            double discount = promo.getReduction(); // Assuming reduction is in percentage
            double discountedPrice = abonnement.getPrix() - (abonnement.getPrix() * discount / 100);

            // Display both original price and discounted price
            prixLabel.setText("Prix : " + abonnement.getPrix() + " DT");  // Original price
            javafx.scene.control.Label prixReducedLabel = new javafx.scene.control.Label(
                    "Prix après réduction : " + String.format("%.2f", discountedPrice) + " DT");
            prixReducedLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

            // Add the discounted price label below the original price
            card.getChildren().add(prixReducedLabel);
        }

        // Duration of the abonnement
        javafx.scene.control.Label dureeLabel = new javafx.scene.control.Label("Durée : " + abonnement.getDuration());

        // Button to subscribe to the abonnement
        Button souscrireBtn = new Button("Souscrire");
        souscrireBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        souscrireBtn.setOnAction(e -> {
            // Action for subscription
            System.out.println("Souscription à l'abonnement : " + abonnement.getTitreAbonnement());
            // You can add your subscription logic here
        });

        // Add all elements to the VBox card
        card.getChildren().addAll(titreLabel, descriptionLabel, prixLabel, dureeLabel, souscrireBtn);

        return card;
    }

    // Method to show alerts
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) {
        try {
            // Load the AbonnementApprenant view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/AbonnementApprenant.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page des abonnements.", Alert.AlertType.ERROR);
        }
    }

}
