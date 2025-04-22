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
import org.example.entity.Duration;
import org.example.services.ServiceAbonnement;
import org.example.utils.SessionManager;

import java.io.IOException;
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
        String titre = titreAbonnement.getText();
        String description = descriptionAbonnement.getText();
        String prixText = prixAbonnement.getText();
        Duration duration = periode.getValue();

        // Validate inputs
        if (titre == null || titre.trim().isEmpty()) {
            showAlert("Erreur", "Le titre ne doit pas être vide.");
            return;
        }

        if (description == null || description.trim().length() < 15) {
            showAlert("Erreur", "La description doit contenir au moins 15 caractères.");
            return;
        }

        int prix;
        try {
            prix = Integer.parseInt(prixText);
            if (prix < 0) {
                showAlert("Erreur", "Le prix ne peut pas être négatif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide.");
            return;
        }

        if (duration == null) {
            showAlert("Erreur", "Veuillez sélectionner une période.");
            return;
        }
        Abonnement abonnement=new Abonnement();
        abonnement.setDescription(description);
        abonnement.setTitreAbonnement(titre);
        abonnement.setDuration(duration);
        abonnement.setPrix(prix);
        serviceAbonnement.ajouter(abonnement);
        showAlert("Succès", "Abonnement ajouté avec succès.");

        // Navigation vers la page ListAbonnement.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        periode.getItems().setAll(Duration.values());
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
