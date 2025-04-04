package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entity.Apprenant;
import org.example.services.ServiceApprenant;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegisterApprenant {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telephoneField;
    @FXML private TextField dateNaissanceField;
    @FXML private ComboBox<String> niveauField;
    @FXML private Label errorLabel;

    private ServiceApprenant serviceApprenant;

    public RegisterApprenant() {
        this.serviceApprenant = new ServiceApprenant();
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

            // Création de l'apprenant
            Apprenant apprenant = new Apprenant();
            apprenant.setNom(nomField.getText());
            apprenant.setPrenom(prenomField.getText());
            apprenant.setEmail(emailField.getText());
            apprenant.setPassword(passwordField.getText());
            apprenant.setTelephone(Integer.parseInt(telephoneField.getText()));
            apprenant.setDateNaissance(dateNaissanceField.getText());
            apprenant.setNiveau(niveauField.getValue());

            // Définir les valeurs par défaut
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_APPRENANT");
            apprenant.setRoles(roles);

            // Générer un ID (à adapter selon votre logique)
            apprenant.setId(generateNewId());

            // Ajout dans la base de données
            serviceApprenant.ajouter(apprenant);

            // Message de succès
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie",
                    "L'apprenant a été enregistré avec succès!");

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
                niveauField.getValue() == null) {

            errorLabel.setText("Tous les champs sont obligatoires!");
            return false;
        }

        // Validation supplémentaire de l'email
        if (!emailField.getText().contains("@")) {
            errorLabel.setText("Veuillez entrer un email valide");
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
        niveauField.getSelectionModel().clearSelection();
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