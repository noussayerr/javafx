package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entity.Enseignant;
import org.example.services.ServiceEnseignant;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegisterEnseignant {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telephoneField;
    @FXML private TextField dateNaissanceField;
    @FXML private TextField specialiteField;
    @FXML private ComboBox<String> experienceField;
    @FXML private Label errorLabel;

    private ServiceEnseignant serviceEnseignant;

    public RegisterEnseignant() {
        this.serviceEnseignant = new ServiceEnseignant();
    }

    @FXML
    private void initialize() {
        // Initialisation supplémentaire si nécessaire
    }

    @FXML
    private void handleRegister() {
        try {
            // Validation des champs
            if (!validateFields()) {
                return;
            }

            // Création de l'enseignant
            Enseignant enseignant = new Enseignant();
            enseignant.setNom(nomField.getText());
            enseignant.setPrenom(prenomField.getText());
            enseignant.setEmail(emailField.getText());
            enseignant.setPassword(passwordField.getText());
            enseignant.setTelephone(Integer.parseInt(telephoneField.getText()));
            enseignant.setDateNaissance(dateNaissanceField.getText());
            enseignant.setSpecialite(specialiteField.getText());
            enseignant.setExperience(experienceField.getValue());

            // Définir les valeurs par défaut
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_ENSEIGNANT");
            enseignant.setRoles(roles);

            // Générer un ID
            enseignant.setId(generateNewId());

            // Ajout dans la base de données
            serviceEnseignant.ajouter(enseignant);

            // Message de succès
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie",
                    "L'enseignant a été enregistré avec succès! Statut: inactif (en attente de validation)");

            // Réinitialiser le formulaire
            clearFields();

        } catch (NumberFormatException e) {
            errorLabel.setText("Le téléphone doit être un nombre valide");
        } catch (SQLException e) {
            errorLabel.setText("Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                telephoneField.getText().isEmpty() || dateNaissanceField.getText().isEmpty() ||
                specialiteField.getText().isEmpty() || experienceField.getValue() == null) {

            errorLabel.setText("Tous les champs sont obligatoires!");
            return false;
        }

        // Validation supplémentaire de l'email
        if (!emailField.getText().contains("@")) {
            errorLabel.setText("Veuillez entrer un email valide");
            return false;
        }

        // Validation de la date de naissance (format simplifié)
        if (!dateNaissanceField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorLabel.setText("Format de date invalide (YYYY-MM-DD)");
            return false;
        }

        return true;
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        telephoneField.clear();
        dateNaissanceField.clear();
        specialiteField.clear();
        experienceField.getSelectionModel().clearSelection();
        errorLabel.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private int generateNewId() {
        // Implémentez votre propre logique de génération d'ID
        // Par exemple: récupérer le dernier ID de la base et incrémenter
        return (int) (Math.random() * 1000) + 1; // Exemple temporaire
    }
}