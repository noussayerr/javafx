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
import org.example.entity.Apprenant;
import org.example.services.ServiceApprenant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    @FXML private TextField photoProfilField;

    private File selectedPhotoFile;

    @FXML
    private void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (file != null) {
            selectedPhotoFile = file;
            photoProfilField.setText(file.getName());
        }
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

            // Handle photo upload if selected
            String photoPath = null;
            if (selectedPhotoFile != null) {
                photoPath = saveProfilePhoto(selectedPhotoFile, String.valueOf(generateNewId()));
                apprenant.setPhotoProfil(photoPath);
            }

            // Définir les valeurs par défaut
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_APPRENANT");
            apprenant.setRoles(roles);

            // Générer un ID
            apprenant.setId(generateNewId());

            // Ajout dans la base de données
            serviceApprenant.ajouter(apprenant);

            // Message de succès
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie",
                    "L'apprenant a été enregistré avec succès!");

            // Réinitialiser le formulaire
            clearFields();
            photoProfilField.clear();
            selectedPhotoFile = null;

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

    private String saveProfilePhoto(File photoFile, String apprenantId) throws IOException {
        // Define the target directory (resources/pdp)
        String targetDir = "src/main/resources/pdp";
        File directory = new File(targetDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create a unique filename using apprenant ID and original extension
        String extension = getFileExtension(photoFile.getName());
        String newFileName = apprenantId + "_profile" + extension;
        Path targetPath = Paths.get(targetDir, newFileName);

        // Copy the file to the target directory
        Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path to store in database
        return "/pdp/" + newFileName;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
    private boolean validateFields      () {
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

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Retour à la page précédente
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/choix.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}