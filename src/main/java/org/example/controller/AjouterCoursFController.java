package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entity.Cours;
import org.example.services.ServiceCours;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

import java.time.LocalDate;

public class AjouterCoursFController {

    @FXML private TextField nomField;

    @FXML private TextField objField;

    @FXML private DatePicker datePicker;

    @FXML private ComboBox<String> niveauCombo;

    @FXML private ComboBox<String> typeCombo;

    @FXML private Label nomErrorLabel;
    @FXML private Label objErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label niveauErrorLabel;
    @FXML private Label typeErrorLabel;

    private ServiceCours serviceCours;
    private ServiceMatiere serviceMatiere;

    private int matiereId;

    private CoursAjouteListener coursAjouteListener;

    public void setCoursAjouteListener(CoursAjouteListener listener) {
        this.coursAjouteListener = listener;
    }


    public AjouterCoursFController() {
        // Initialisation du service cours
        serviceCours = new ServiceCours();
        serviceMatiere = new ServiceMatiere();

    }

    // Méthode pour initialiser avec l'ID de la matière
    public void setMatiereId(int matiereId) {
        this.matiereId = matiereId;
    }

    // Méthode pour ajouter un cours
    @FXML
    private void ajouterCours() {
        clearErrorLabels();

        String nom = nomField.getText().trim();
        String objectif = objField.getText().trim();
        LocalDate date = datePicker.getValue();
        String niveau = niveauCombo.getValue();
        String type = typeCombo.getValue();

        boolean hasError = false;

        // Nom : lettres, chiffres, espaces - longueur 3 à 50
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est requis.");
            hasError = true;
        } else if (!nom.matches("[\\p{L}\\d\\s]{3,50}")) {
            nomErrorLabel.setText("Nom invalide (3-50 caractères, lettres/chiffres/espaces).");
            hasError = true;
        }

        // Objectif : min 10 caractères
        if (objectif.isEmpty()) {
            objErrorLabel.setText("L’objectif est requis.");
            hasError = true;
        } else if (objectif.length() < 10 || objectif.length() > 200) {
            objErrorLabel.setText("L’objectif doit faire entre 10 et 200 caractères.");
            hasError = true;
        }

        // Date : doit être dans le futur ou aujourd'hui
        if (date == null) {
            dateErrorLabel.setText("La date est requise.");
            hasError = true;
        } else if (date.isBefore(LocalDate.now())) {
            dateErrorLabel.setText("La date ne peut pas être dans le passé.");
            hasError = true;
        }

        // Niveau
        if (niveau == null || niveau.isEmpty()) {
            niveauErrorLabel.setText("Le niveau est requis.");
            hasError = true;
        }

        // Type
        if (type == null || type.isEmpty()) {
            typeErrorLabel.setText("Le type est requis.");
            hasError = true;
        }

        // Si erreur, arrêter là
        if (hasError) return;

        // Si tout est ok, ajouter le cours
        Cours cours = new Cours();
        cours.setNomC(nom);
        cours.setObjC(objectif);
        cours.setDateC(date.atStartOfDay());
        cours.setNivC(niveau);
        cours.setType(type);
        cours.setUser(SessionManager.getInstance().getCurrentUser());
        cours.setMatiereById(matiereId, serviceMatiere);

        try {
            serviceCours.ajouter(cours);
            if (coursAjouteListener != null) {
                coursAjouteListener.onCoursAjoute();
            }
            showSuccess("Cours ajouté avec succès !");
            resetFields();
        } catch (Exception e) {
            showError("Erreur lors de l'ajout du cours : " + e.getMessage());
        }
    }


    private void resetFields() {
        nomField.clear();
        objField.clear();
        datePicker.setValue(null);
        niveauCombo.getSelectionModel().clearSelection();
        typeCombo.getSelectionModel().clearSelection();
    }

    private void clearErrorLabels() {
        nomErrorLabel.setText("");
        objErrorLabel.setText("");
        dateErrorLabel.setText("");
        niveauErrorLabel.setText("");
        typeErrorLabel.setText("");
    }


    // Méthode pour afficher une alerte de succès
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Méthode pour afficher une alerte d'erreur
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAnnuler(ActionEvent actionEvent) {
        resetFields();
        clearErrorLabels();
    }

    public interface CoursAjouteListener {
        void onCoursAjoute();
    }


}
