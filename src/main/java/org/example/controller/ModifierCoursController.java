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
import org.example.services.ServiceMatiere;

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

    private int matiereId;

    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceMatiere  serviceMatiere = new ServiceMatiere();
    private Cours coursAModifier;
    private CoursModifieListener coursModifieListener;


    // Injecter le cours à modifier
    // Méthode pour initialiser avec l'ID de la matière
    public void setMatiereId(int matiereId) {
        this.matiereId = matiereId;
    }
    public void setCours(Cours cours) {
        this.coursAModifier = cours;

        if (cours.getMatiere() == null) {
            System.err.println("⚠ Le cours n'a pas de matière associée !");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le cours n'a pas de matière associée.");
            return;  // Bloque la modification si la matière est manquante
        }

        nomField.setText(cours.getNomC());
        objField.setText(cours.getObjC());
        datePicker.setValue(cours.getDateC().toLocalDate());
        niveauCombo.setValue(cours.getNivC());
        typeCombo.setValue(cours.getType());


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
            objErrorLabel.setText("L’objectif est obligatoire.");
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

        // 🔒 Sécurité supplémentaire avant l'appel au service
        if (coursAModifier.getMatiere() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le cours n’a pas de matière associée.");
            return;
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
            // Fermer la fenêtre actuelle
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


}