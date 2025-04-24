package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Enseignant;
import org.example.entity.User;
import org.example.services.ServiceEnseignant;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;

public class ProfileEnseignant {

    @FXML
    private ImageView profileImageView;
    @FXML
    private TextField photoProfilField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField dateNaissanceField;
    @FXML
    private TextField telephoneField;
    @FXML
    private ComboBox<String> specialiteComboBox;
    @FXML
    private TextField experienceField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button logoutButton;


    private ServiceEnseignant serviceEnseignant;
    private User currentUser;

    public void initialize() {
        serviceEnseignant = new ServiceEnseignant();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Remplir les champs avec les données de l'utilisateur
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            emailField.setEditable(false);

            if (currentUser.getDateNaissance() != null) {
                dateNaissanceField.setText(currentUser.getDateNaissance());
            }

            telephoneField.setText(String.valueOf(currentUser.getTelephone()));

            // Initialiser la ComboBox des spécialités
            specialiteComboBox.getItems().addAll(
                    "Lecture et écriture",
                    "Mathématiques",
                    "Orthophonie",
                    "Psychopédagogie",
                    "Technologies d'assistance"
            );

            // Si c'est un enseignant, charger les données spécifiques
            if (currentUser instanceof Enseignant) {
                Enseignant enseignant = (Enseignant) currentUser;
                specialiteComboBox.setValue(enseignant.getSpecialite());
                experienceField.setText(String.valueOf(enseignant.getExperience()));
            }

            // Charger la photo de profil
            if (currentUser.getPhotoProfil() != null && !currentUser.getPhotoProfil().isEmpty()) {
                photoProfilField.setText(currentUser.getPhotoProfil());
                try {
                    Image image = new Image(new File(currentUser.getPhotoProfil()).toURI().toString());
                    profileImageView.setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @FXML
    private void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            photoProfilField.setText(selectedFile.getAbsolutePath());
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentUser != null) {
            try {
                // Convertir User en Enseignant
                Enseignant enseignant = convertUserToEnseignant(currentUser);

                // Mettre à jour les données
                enseignant.setNom(nomField.getText());
                enseignant.setPrenom(prenomField.getText());
                enseignant.setEmail(emailField.getText());
                enseignant.setDateNaissance(dateNaissanceField.getText());
                enseignant.setTelephone(Integer.parseInt(telephoneField.getText()));
                enseignant.setSpecialite(specialiteComboBox.getValue());
                enseignant.setExperience(experienceField.getText());

                if (!passwordField.getText().isEmpty()) {
                    enseignant.setPassword(passwordField.getText());
                }

                if (!photoProfilField.getText().isEmpty()) {
                    enseignant.setPhotoProfil(photoProfilField.getText());
                }

                // Sauvegarder les modifications
                serviceEnseignant.modifier(enseignant);

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Profil mis à jour avec succès!", "");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Veuillez entrer une valeur numérique valide pour l'expérience et le téléphone", "");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la mise à jour du profil", e.getMessage());
            }
        }
    }

    private Enseignant convertUserToEnseignant(User user) {
        Enseignant enseignant = new Enseignant();

        // Copier les propriétés communes
        enseignant.setId(user.getId());
        enseignant.setNom(user.getNom());
        enseignant.setPrenom(user.getPrenom());
        enseignant.setEmail(user.getEmail());
        enseignant.setPassword(user.getPassword());
        enseignant.setTelephone(user.getTelephone());
        enseignant.setDateNaissance(user.getDateNaissance());
        enseignant.setPhotoProfil(user.getPhotoProfil());
        enseignant.setRoles(user.getRoles());

        // Si c'est déjà un enseignant, copier les propriétés spécifiques
        if (user instanceof Enseignant) {
            Enseignant existing = (Enseignant) user;
            enseignant.setSpecialite(existing.getSpecialite());
            enseignant.setExperience(existing.getExperience());
        }

        return enseignant;
    }

    @FXML
    private void loadDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/EnseignantDashboard.fxml"));
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement du tableau de bord", e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ProfileEnseignant.fxml"));
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement du profil", e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            SessionManager.getInstance().logout();
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie",
                    "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void goMatiereF(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ListeMatiereF.fxml");
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