package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private Label titreError;
    @FXML
    private Label prixError;
    @FXML
    private Label descriptionError;


    @FXML
    public void initialize() {
        // Populate the form with the data from the selected Abonnement
        titreAbonnementField.setText(abonnement.getTitreAbonnement());
        prixField.setText(String.valueOf(abonnement.getPrix()));
        descriptionField.setText(abonnement.getDescription());



    }

    @FXML
    public void handleSave() {

        String titre = titreAbonnementField.getText();
        String description = descriptionField.getText();
        String prixText = prixField.getText();

        // === Contrôles de validation ===
        titreError.setText("");
        prixError.setText("");
        descriptionError.setText("");

        boolean valid = true;

        if (titreAbonnementField.getText().isEmpty()) {
            titreError.setText("Le titre est obligatoire.");
            valid = false;
        }

        if (prixField.getText().isEmpty()) {
            prixError.setText("Le prix est obligatoire.");
            valid = false;
        } else {
            try {
                int prix = Integer.parseInt(prixField.getText());
                if (prix < 0) {
                    prixError.setText("Le prix doit être positif.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                prixError.setText("Prix invalide.");
                valid = false;
            }
        }

        if (descriptionField.getText().length() < 15) {
            descriptionError.setText("La description doit contenir au moins 15 caractères.");
            valid = false;
        }

        if (!valid) {
            return;
        }
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
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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

