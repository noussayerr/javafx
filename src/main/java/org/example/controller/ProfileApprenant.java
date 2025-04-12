package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.User;
import org.example.services.ServiceApprenant;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;

public class ProfileApprenant {

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
    private ComboBox<String> niveauComboBox;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button handleAcceuil;
    @FXML
    private Button handleProfile;
    @FXML
    private Button logoutButton;

    private ServiceApprenant serviceApprenant;
    private User currentUser;

    public void initialize() {
        // Initialiser le service
        serviceApprenant = new ServiceApprenant();
        handleAcceuil.setOnAction(event -> loadAcceuil());
        handleProfile.setOnAction(event -> loadProfile());
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialiser les champs avec les données de l'utilisateur
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            emailField.setEditable(false);

            if (currentUser.getDateNaissance() != null) {
                dateNaissanceField.setText(currentUser.getDateNaissance());
            }

            telephoneField.setText(String.valueOf(currentUser.getTelephone()));

            // Initialiser la ComboBox des niveaux
            niveauComboBox.getItems().addAll("Débutant", "Intermédiaire", "Avancé");


            // Charger la photo de profil si elle existe
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
            // Convertir User en Apprenant
            Apprenant apprenant = convertUserToApprenant(currentUser);

            // Mettre à jour les données de l'apprenant avec les nouvelles valeurs
            apprenant.setNom(nomField.getText());
            apprenant.setPrenom(prenomField.getText());
            apprenant.setEmail(emailField.getText());


            apprenant.setTelephone(Integer.parseInt(telephoneField.getText()));
            apprenant.setNiveau(niveauComboBox.getValue());

            if (!passwordField.getText().isEmpty()) {
                apprenant.setPassword(passwordField.getText());
            }

            if (!photoProfilField.getText().isEmpty()) {
                apprenant.setPhotoProfil(photoProfilField.getText());
            }

            try {
                serviceApprenant.modifier(apprenant); // Maintenant on passe un Apprenant

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Profil mis à jour avec succès!");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de la mise à jour du profil.");
                alert.showAndWait();
            }
        }
    }

    private Apprenant convertUserToApprenant(User user) {
        Apprenant apprenant = new Apprenant();

        apprenant.setId(user.getId());
        apprenant.setNom(user.getNom());
        apprenant.setPrenom(user.getPrenom());
        apprenant.setEmail(user.getEmail());
        apprenant.setPassword(user.getPassword());
        apprenant.setTelephone(user.getTelephone());
        apprenant.setDateNaissance(dateNaissanceField.getText());
        apprenant.setNiveau(niveauComboBox.getValue());
        apprenant.setPhotoProfil(user.getPhotoProfil());
        apprenant.setRoles(user.getRoles());

        return apprenant;
    }

    private void loadAcceuil() {
        try {
            // Chargement de la page ApprenantDashboard
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));
            Stage stage = (Stage) handleAcceuil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page Accueil");
        }
    }

    private void loadProfile() {
        try {
            // Chargement de la page Profil
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/Profil.fxml"));
            Stage stage = (Stage) handleProfile.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page Profil");
        }
    }

    @FXML
    private void logout() {
        try {
            // Effacer la session utilisateur
            SessionManager.getInstance().logout();

            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle depuis le bouton existant
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();

            // Afficher un message de confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie",
                    "Vous avez été déconnecté avec succès.", "");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}