package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Matiere;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ModifierMatiereController {

    @FXML private Button logoutButton;
    @FXML private TextField nomMField;
    @FXML private TextField titreMField;
    @FXML private TextArea descMField;
    @FXML private TextArea objMField;
    @FXML private TextField imgMField;
    @FXML private Label errorLabel;
    @FXML private Label nomErrorLabel;

    @FXML
    private Label titreErrorLabel;

    @FXML
    private Label descErrorLabel;

    @FXML
    private Label objErrorLabel;


    private File selectedImageFile;
    private Matiere matiereAModifier;

    private final ServiceMatiere matiereService = new ServiceMatiere();

    /**
     * Cette méthode est appelée pour injecter la matière à modifier.
     */
    public void setMatiere(Matiere matiere) {
        this.matiereAModifier = matiere;

        // Pré-remplissage des champs
        nomMField.setText(matiere.getNomM());
        titreMField.setText(matiere.getTitreM());
        descMField.setText(matiere.getDescM());
        objMField.setText(matiere.getObjM());
        imgMField.setText(matiere.getImgM());
    }

    /**
     * Gère le bouton de modification.
     */
    @FXML
    private void handleModifierMatiere(ActionEvent event) {
        String nom = nomMField.getText().trim();
        String titre = titreMField.getText().trim();
        String description = descMField.getText().trim();
        String objectifs = objMField.getText().trim();
        String imagePath = imgMField.getText();

        // Réinitialiser les messages d'erreur
        nomErrorLabel.setText("");
        titreErrorLabel.setText("");
        descErrorLabel.setText("");
        objErrorLabel.setText("");
        errorLabel.setText("");

        boolean isValid = true;

        // Vérification nom (non vide et alphabétique)
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est obligatoire.");
            isValid = false;
        } else if (!nom.matches("[a-zA-Z ]+")) {
            nomErrorLabel.setText("Le nom doit contenir uniquement des lettres.");
            isValid = false;
        }

        // Vérification titre
        if (titre.isEmpty()) {
            titreErrorLabel.setText("Le titre est obligatoire.");
            isValid = false;
        }

        // Vérification description
        if (description.isEmpty()) {
            descErrorLabel.setText("La description est obligatoire.");
            isValid = false;
        }

        // Vérification objectifs
        if (objectifs.isEmpty()) {
            objErrorLabel.setText("Les objectifs sont obligatoires.");
            isValid = false;
        }

        if (!isValid) return;

        // Mise à jour de l'objet
        matiereAModifier.setNomM(nom);
        matiereAModifier.setTitreM(titre);
        matiereAModifier.setDescM(description);
        matiereAModifier.setObjM(objectifs);
        matiereAModifier.setImgM(imagePath);

        try {
            matiereService.modifier(matiereAModifier);

            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Matière modifiée avec succès !");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListeMatiere.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Matières");
            stage.centerOnScreen();
            stage.show();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Erreur lors de la modification.");
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        loadPage(event, "/org/example/view/ListeMatiere.fxml");
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imgMField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
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
    public void goToMatiere(ActionEvent event) {
        loadPage(event, "/org/example/view/ListeMatiere.fxml");
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

    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }

    @FXML
    public void afficherJeux(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/jeuxIndex.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

