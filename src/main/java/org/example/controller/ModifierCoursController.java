package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.services.ServiceCours;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierCoursController {

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

    private Cours coursAModifier;
    private CoursModifieListener coursModifieListener;
    private final ServiceCours serviceCours = new ServiceCours();

    public void setCours(Cours cours) {
        this.coursAModifier = cours;

        if (cours != null) {
            nomField.setText(cours.getNomC());
            objField.setText(cours.getObjC());
            datePicker.setValue(cours.getDateC().toLocalDate());
            niveauCombo.setValue(cours.getNivC());
            typeCombo.setValue(cours.getType());
        }
    }

    @FXML
    public void modifierCours(ActionEvent event) {
        clearErrors();

        String nom = nomField.getText().trim();
        String objectif = objField.getText().trim();
        LocalDate date = datePicker.getValue();
        String niveau = niveauCombo.getValue();
        String type = typeCombo.getValue();

        boolean isValid = true;

        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est obligatoire.");
            isValid = false;
        }

        if (objectif.isEmpty()) {
            objErrorLabel.setText("L'objectif est obligatoire.");
            isValid = false;
        }

        if (date == null) {
            dateErrorLabel.setText("La date est obligatoire.");
            isValid = false;
        }

        if (niveau == null || niveau.isEmpty()) {
            niveauErrorLabel.setText("Veuillez choisir un niveau.");
            isValid = false;
        }

        if (type == null || type.isEmpty()) {
            typeErrorLabel.setText("Veuillez choisir un type.");
            isValid = false;
        }

        if (!isValid) return;

        coursAModifier.setNomC(nom);
        coursAModifier.setObjC(objectif);
        coursAModifier.setDateC(date.atStartOfDay());
        coursAModifier.setNivC(niveau);
        coursAModifier.setType(type);

        try {
            serviceCours.modifier(coursAModifier);
            if (coursModifieListener != null) {
                coursModifieListener.onCoursModifie();
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du cours.");
        }
    }

    @FXML
    public void handleAnnuler(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AfficherCoursFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Cours");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearErrors() {
        nomErrorLabel.setText("");
        objErrorLabel.setText("");
        dateErrorLabel.setText("");
        niveauErrorLabel.setText("");
        typeErrorLabel.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public interface CoursModifieListener {
        void onCoursModifie();
    }
    public void setCoursModifieListener(CoursModifieListener listener) {
        this.coursModifieListener = listener;
    }

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        typeCombo.getItems().addAll("Vidéo", "Document", "Quiz", "Lien");
    }

}