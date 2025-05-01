package org.example.controller;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.entity.*;
import org.example.services.ServiceAbonnement;
import org.example.services.ServicePaiement;
import org.example.services.ServiceTransaction;
import org.example.utils.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class AbonnementApprenant {
    @FXML
    private HBox abonnementsContainer;  // HBox to hold the cards horizontally

    private ServiceAbonnement serviceAbonnement = new ServiceAbonnement();
    private ServiceTransaction serviceTransaction=new ServiceTransaction();// Service to fetch abonnements
    private ServicePaiement servicePaiement = new ServicePaiement();
    @FXML
    public void initialize() {
        try {
            // Fetch all abonnements
            List<Abonnement> abonnements = serviceAbonnement.afficher();
            Abonnement bestSellerId = serviceTransaction.getAbonnementLePlusVendu();
            // Add each abonnement as a card to the HBox
            for (Abonnement abonnement : abonnements) {
                Node card = createAbonnementCard(abonnement,bestSellerId.getId());
                abonnementsContainer.getChildren().add(card);  // Add card to the HBox
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les abonnements.", Alert.AlertType.ERROR);
        }
    }

    private Node createAbonnementCard(Abonnement abonnement, int bestSellerId) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-background-radius: 8; -fx-border-radius: 8;");
        card.setPrefWidth(220);

        Label titreLabel = createTitreLabel(abonnement);

        // 🔥 Ajouter le badge si c’est le plus vendu
        if (abonnement.getId() == bestSellerId) {
            Label hotDeal = new Label("🔥 Hot Deal");
            hotDeal.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: #ffe6e6; -fx-padding: 2 6; -fx-background-radius: 5;");
            card.getChildren().add(hotDeal);
        }

        Label descriptionLabel = createDescriptionLabel(abonnement);
        Label prixLabel = createPrixLabel(abonnement);
        Label prixReducedLabel = createReducedLabelIfPromotion(abonnement, prixLabel);
        Label dureeLabel = createDureeLabel(abonnement);
        Button souscrireBtn = createSouscrireButton(abonnement);

        card.getChildren().addAll(titreLabel, descriptionLabel, prixLabel);
        if (prixReducedLabel != null) card.getChildren().add(prixReducedLabel);
        card.getChildren().addAll(dureeLabel, souscrireBtn);

        return card;
    }

    private Label createTitreLabel(Abonnement abonnement) {
        Label titreLabel = new Label(abonnement.getTitreAbonnement());
        titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return titreLabel;
    }

    private Label createDescriptionLabel(Abonnement abonnement) {
        Label descriptionLabel = new Label(abonnement.getDescription());
        descriptionLabel.setWrapText(true);
        return descriptionLabel;
    }

    private Label createPrixLabel(Abonnement abonnement) {
        return new Label("Prix : " + abonnement.getPrix() + " DT");
    }

    private Label createReducedLabelIfPromotion(Abonnement abonnement, Label prixLabel) {
        Promotion promo = abonnement.getPromotion();
        if (promo != null) {
            double discount = promo.getReduction();
            double discountedPrice = abonnement.getPrix() - (abonnement.getPrix() * discount / 100);
            prixLabel.setText("Prix : " + abonnement.getPrix() + " DT");
            Label reducedLabel = new Label("Prix après réduction : " + String.format("%.2f", discountedPrice) + " DT");
            reducedLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return reducedLabel;
        }
        return null;
    }

    private Label createDureeLabel(Abonnement abonnement) {
        return new Label("Durée : " + abonnement.getDuration());
    }

    private Button createSouscrireButton(Abonnement abonnement) {
        Button souscrireBtn = new Button("Souscrire");
        souscrireBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        souscrireBtn.setOnAction(e -> handlePaiement(abonnement));
        return souscrireBtn;
    }

    private void handlePaiement(Abonnement abonnement) {
        int prix=abonnement.getPrix();
        if(abonnement.getPromotion() != null) {
            prix = (abonnement.getPrix()/100)*100-abonnement.getPromotion().getReduction();
            System.out.println(prix);
        }
        try {
            String responseJson = ServicePaiement.generatePayment(
                    "95e08372-1164-46a5-8725-0d23f628e2d5",
                    "96f12e3a-1d92-4b1f-9c17-3e3a975c4e22",
                    String.valueOf(prix * 100),
                    "https://example.website.com/success",
                    "https://example.website.com/fail",
                    "9b07e6ef-06ca-4a33-bc66-77a9eefd19c1"
            );

            JSONObject responseObj = new JSONObject(responseJson);
            String paymentLink = responseObj.getJSONObject("result").getString("link");

            openPaymentWebView(paymentLink,abonnement);

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors du paiement.", Alert.AlertType.ERROR);
        }
    }

    private void openPaymentWebView(String paymentLink,Abonnement abonnement) throws IOException {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(paymentLink);

        StackPane webViewPane = new StackPane(webView);
        Scene scene = new Scene(webViewPane, 800, 600);
        Stage paymentStage = new Stage();
        paymentStage.setTitle("Page de Paiement");
        paymentStage.setScene(scene);
        webEngine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            System.out.println("Navigating to: " + newLoc);

            if (newLoc.startsWith("https://example.website.com/success")) {
                //System.out.println("Payment succeeded.");
                String idpayement=getPaymentIdFromUrl(newLoc);
                try {
                    this.ajoutTransaction(abonnement,idpayement);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                loadFXMLAfterSuccess("/org/example/view/SuccesPaiement.fxml");
            } else if (newLoc.startsWith("https://example.website.com/fail")) {

            }
        });




        paymentStage.show();
    }


    private void loadFXMLAfterSuccess(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage mainStage = new Stage();
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to show alerts
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void ajoutTransaction(Abonnement abonnement,String idTransaction) throws SQLException {
        Transaction transaction=new Transaction();
        int prix=abonnement.getPrix();
        if(abonnement.getPromotion() != null) {
            prix = (abonnement.getPrix()/100)*100-abonnement.getPromotion().getReduction();
            System.out.println(prix);
        }
        transaction.setAmount(prix);
        transaction.setTransactionId(idTransaction);
        transaction.setStatus("success");
        transaction.setTransactionDate(LocalDateTime.now());

        User currentUser = SessionManager.getInstance().getCurrentUser();

        Apprenant currentApprenant=serviceTransaction.getApprenantById(currentUser.getId());
        if (currentApprenant != null) {
            transaction.setApprenant(currentApprenant);
        } else {
            System.out.println("Utilisateur non apprenant ou aucun utilisateur connecté.");
        }
        serviceTransaction.ajouter(transaction);
    }
    public static String getPaymentIdFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery(); // e.g. "payment_id=abc123"

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("payment_id")) {
                        return pair[1];
                    }
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null; // Or throw an exception if preferred
    }
    @FXML
    private void handleGoToTransactions(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListTransaction.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotodash(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
