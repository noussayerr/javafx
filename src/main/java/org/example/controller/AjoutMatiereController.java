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

import java.io.File;
import java.io.IOException;

public class AjoutMatiereController {

    @FXML private TextField nomMField;
    @FXML private TextField titreMField;
    @FXML private TextArea descMField;
    @FXML private TextArea objMField;
    @FXML private TextField imgMField;

    // Labels d'erreur sous les champs
    @FXML private Label nomErrorLabel;
    @FXML private Label titreErrorLabel;
    @FXML private Label descErrorLabel;
    @FXML private Label objErrorLabel;
    @FXML private Label imgErrorLabel;
    @FXML private Label errorLabel;

    @FXML
    private void handleAjouterMatiere(ActionEvent event) {
        // Nettoyer les messages d'erreur
        clearErrorLabels();

        boolean valid = true;

        String nom = nomMField.getText().trim();
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est requis.");
            valid = false;
        } else if (!nom.matches("[a-zA-ZàâçéèêëîïôûùüÿñæœÀÂÇÉÈÊËÎÏÔÛÙÜŸÑÆŒ\\s\\-']+")) {
            nomErrorLabel.setText("Le nom ne doit contenir que des lettres.");
            valid = false;
        }


        if (titreMField.getText().trim().isEmpty()) {
            titreErrorLabel.setText("Le titre est requis.");
            valid = false;
        }

        if (descMField.getText().trim().isEmpty()) {
            descErrorLabel.setText("La description est requise.");
            valid = false;
        }

        if (objMField.getText().trim().isEmpty()) {
            objErrorLabel.setText("Les objectifs sont requis.");
            valid = false;
        }

        if (imgMField.getText().trim().isEmpty()) {
            imgErrorLabel.setText("L'image est requise.");
            valid = false;
        }

        if (!valid) {
            errorLabel.setText("Veuillez corriger les erreurs ci-dessus.");
            return;
        }

        // Simuler l'ajout de la matière
        System.out.println("Matière ajoutée avec succès !");
        System.out.println("Nom : " + nomMField.getText());
        System.out.println("Titre : " + titreMField.getText());
        System.out.println("Description : " + descMField.getText());
        System.out.println("Objectifs : " + objMField.getText());
        System.out.println("Image : " + imgMField.getText());

        // Réinitialiser les champs
        resetFields();
        errorLabel.setText("✅ Matière ajoutée avec succès !");
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        resetFields();
        clearErrorLabels();
        errorLabel.setText("");
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            imgMField.setText(file.getAbsolutePath());
        }
    }

    private void clearErrorLabels() {
        nomErrorLabel.setText("");
        titreErrorLabel.setText("");
        descErrorLabel.setText("");
        objErrorLabel.setText("");
        imgErrorLabel.setText("");
        errorLabel.setText("");
    }

    private void resetFields() {
        nomMField.clear();
        titreMField.clear();
        descMField.clear();
        objMField.clear();
        imgMField.clear();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Déconnexion...");
        // Implémente ici la logique de déconnexion si nécessaire
    }

    @FXML
    private void goToMatiere(ActionEvent actionEvent) {
        System.out.println("Naviguer vers gestion des matières...");
        loadPage(actionEvent, "/org/example/view/ListeMatiere.fxml");
        // Implémente ici la navigation si nécessaire
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
}
