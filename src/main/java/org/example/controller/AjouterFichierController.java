package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Fichier;
import org.example.services.ServiceFichier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class AjouterFichierController {

    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Label filePathLabel;

    private File selectedFile;
    private Cours cours;
    private Runnable fichierAjouteListener;

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    public void setFichierAjouteListener(Runnable listener) {
        this.fichierAjouteListener = listener;
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathLabel.setText(selectedFile.getName());
            // Déduire le type d'après l'extension
            String fileName = selectedFile.getName();
            if (fileName.toLowerCase().endsWith(".pdf")) {
                typeCombo.setValue("PDF");
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                typeCombo.setValue("Word");
            }
        }
    }

    @FXML
    private void ajouterFichier(ActionEvent event) {
        if (validateForm()) {
            try {
                // Créer le dossier uploads s'il n'existe pas
                Path uploadDir = Path.of("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectory(uploadDir);
                }

                // Copier le fichier
                String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destination = uploadDir.resolve(newFileName);
                Files.copy(selectedFile.toPath(), destination);

                // Sauvegarder en base
                Fichier fichier = new Fichier();
                fichier.setNomF(nomField.getText());
                fichier.setUrlF(destination.toString());
                fichier.setType(typeCombo.getValue());
                fichier.setCours(cours);

                new ServiceFichier().ajouter(fichier);

                if (fichierAjouteListener != null) {
                    fichierAjouteListener.run();
                }
                closeWindow();
            } catch (Exception e) {
                showAlert("Erreur lors de l'ajout du fichier: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        if (nomField.getText().isEmpty()) {
            showAlert("Le nom du fichier est requis");
            return false;
        }
        if (typeCombo.getValue() == null) {
            showAlert("Veuillez sélectionner un type de fichier");
            return false;
        }
        if (selectedFile == null) {
            showAlert("Veuillez sélectionner un fichier");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) nomField.getScene().getWindow()).close();
    }
}