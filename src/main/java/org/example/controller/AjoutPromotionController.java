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
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();
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

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToMatiere(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ListeMatiere.fxml");
    }

    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }

    public void goToUtilisateurs(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AdminDashboard.fxml");
    }

    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void afficherJeux(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/jeuxIndex.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}
