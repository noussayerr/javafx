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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.entity.*;
import org.example.services.ServiceAbonnement;
import org.example.services.ServiceApprenant;
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
    @FXML
    private Button logoutButton;
    @FXML
    private Button profileButton;

    private ServiceAbonnement serviceAbonnement = new ServiceAbonnement();
    private ServiceTransaction serviceTransaction=new ServiceTransaction();// Service to fetch abonnements
    private ServicePaiement servicePaiement = new ServicePaiement();
    private ServiceApprenant serviceApprenant = new ServiceApprenant();
    @FXML
    public void initialize() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();

            Apprenant currentApprenant = serviceTransaction.getApprenantById(currentUser.getId());
            Apprenant currentApprenant2 = serviceTransaction.getApprenantById2(currentUser.getId());
            System.out.println(currentApprenant2.toString());
            System.out.println(currentApprenant.toString());

            // Fetch all abonnements
            List<Abonnement> abonnements = serviceAbonnement.afficher();
            Abonnement bestSeller = serviceTransaction.getAbonnementLePlusVendu();

            // Determine best-seller ID (use -1 or another sentinel value if null)
            int bestSellerId = (bestSeller != null) ? bestSeller.getId() : -1;

            // Add each abonnement as a card to the HBox
            for (Abonnement abonnement : abonnements) {
                Node card = createAbonnementCard(abonnement, bestSellerId);
                abonnementsContainer.getChildren().add(card); // Add card to the HBox
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

        // 🔥 Add the badge if it’s the best seller
        if (bestSellerId != -1 && abonnement.getId() == bestSellerId) {
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
                loadFXMLAfterSuccess("/org/example/view/FailurePaiement.fxml");
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
        //Apprenant currentApprenant2=serviceTransaction.getApprenantById2(currentUser.getId());
        //System.out.println(currentApprenant2.toString());
        //serviceApprenant.modifier(currentApprenant);
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

    @FXML
    private void showGames(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goMatiereF(ActionEvent actionEvent) {
        System.out.println("Naviguer vers gestion des matières...");
        loadPage(actionEvent, "/org/example/view/MatiereFrontA.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            System.out.println("Chargement du fichier : " + fxmlPath);
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // ← affiche l'erreur exacte dans la console
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // ← attrape aussi toute autre erreur de controller
            showAlert(Alert.AlertType.ERROR, "Erreur", "Exception générale", e.getMessage());
        }
    }


    @FXML
    private void goAbonnement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AbonnementApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goReclamation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AjoutReclamation.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goAccueil(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ApprenantDashboard.fxml");
    }

    @FXML
    private void handleProfile() {
        try {
            // Load the profile page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileApprenant.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil");
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page de profil", e.getMessage());
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
    private void logout() {
        try {
            // Clear the session
            SessionManager.getInstance().logout();

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();

            // Show logout confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", "Impossible de charger l'écran de connexion: " + e.getMessage());
            e.printStackTrace();
        }


    }

    @FXML
    private void ouvrirListeEvenements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Liste des Événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
