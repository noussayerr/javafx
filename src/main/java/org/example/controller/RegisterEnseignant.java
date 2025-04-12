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
import org.example.entity.Enseignant;
import org.example.services.ServiceEnseignant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    @FXML private TextField photoProfilField;

    private File selectedPhotoFile;
    private ServiceEnseignant serviceEnseignant;

    public RegisterEnseignant() {
        this.serviceEnseignant = new ServiceEnseignant();
    }

    @FXML
    private void initialize() {
        // Initialisation du ComboBox d'expérience
        experienceField.getItems().addAll("Débutant", "Intermédiaire", "Expérimenté");
    }

    @FXML
    private void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedPhotoFile = fileChooser.showOpenDialog(photoProfilField.getScene().getWindow());
        if (selectedPhotoFile != null) {
            photoProfilField.setText(selectedPhotoFile.getName());
        }
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

            // Gestion de la photo de profil
            if (selectedPhotoFile != null) {
                String photoPath = saveProfilePhoto(selectedPhotoFile);
                enseignant.setPhotoProfil(photoPath);
            }

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
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de l'enregistrement de la photo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String saveProfilePhoto(File photoFile) throws IOException {
        // Créer le dossier pdp s'il n'existe pas
        File pdpDir = new File("src/main/resources/pdp");
        if (!pdpDir.exists()) {
            pdpDir.mkdirs();
        }

        // Générer un nom de fichier unique
        String extension = getFileExtension(photoFile.getName());
        String newFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(pdpDir.getAbsolutePath(), newFileName);

        // Copier le fichier
        Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner le chemin relatif
        return "pdp/" + newFileName;
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
        photoProfilField.clear();
        selectedPhotoFile = null;
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
        return (int) (Math.random() * 1000) + 1; // Exemple temporaire
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/choix.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}