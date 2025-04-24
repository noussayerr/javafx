package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entity.Cours;
import org.example.entity.Matiere;
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

    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private Matiere matiere;
    private Runnable coursAjouteListener;

    @FXML
    public void initialize() {
        // Initialiser les ComboBox

    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public void setCoursAjouteListener(Runnable listener) {
        this.coursAjouteListener = listener;
    }

    @FXML
    private void ajouterCours() {
        clearErrorLabels();

        String nom = nomField.getText().trim();
        String objectif = objField.getText().trim();
        LocalDate date = datePicker.getValue();
        String niveau = niveauCombo.getValue();
        String type = typeCombo.getValue();

        boolean hasError = false;

        // Validation du nom
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est requis.");
            hasError = true;
        } else if (!nom.matches("[\\p{L}\\d\\s]{3,50}")) {
            nomErrorLabel.setText("Nom invalide (3-50 caractères, lettres/chiffres/espaces).");
            hasError = true;
        }

        // Validation de l'objectif
        if (objectif.isEmpty()) {
            objErrorLabel.setText("L'objectif est requis.");
            hasError = true;
        } else if (objectif.length() < 10 || objectif.length() > 200) {
            objErrorLabel.setText("L'objectif doit faire entre 10 et 200 caractères.");
            hasError = true;
        }

        // Validation de la date
        if (date == null) {
            dateErrorLabel.setText("La date est requise.");
            hasError = true;
        } else if (date.isBefore(LocalDate.now())) {
            dateErrorLabel.setText("La date ne peut pas être dans le passé.");
            hasError = true;
        }

        // Validation du niveau
        if (niveau == null || niveau.isEmpty()) {
            niveauErrorLabel.setText("Le niveau est requis.");
            hasError = true;
        }

        // Validation du type
        if (type == null || type.isEmpty()) {
            typeErrorLabel.setText("Le type est requis.");
            hasError = true;
        }

        if (hasError) return;

        // Création et ajout du cours
        Cours cours = new Cours();
        cours.setNomC(nom);
        cours.setObjC(objectif);
        cours.setDateC(date.atStartOfDay());
        cours.setNivC(niveau);
        cours.setType(type);
        cours.setUser(SessionManager.getInstance().getCurrentUser());
        cours.setMatiere(matiere);

        try {
            serviceCours.ajouter(cours);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté", "Le cours a été ajouté avec succès.");
            if (coursAjouteListener != null) {
                coursAjouteListener.run();
            }
            resetFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent actionEvent) {
        resetFields();
        clearErrorLabels();
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

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}