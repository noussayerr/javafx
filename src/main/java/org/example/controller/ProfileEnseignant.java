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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ProfileEnseignant {

    @FXML private ImageView profileImageView;
    @FXML private TextField photoProfilField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField dateNaissanceField;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<String> specialiteComboBox;
    @FXML private TextField experienceField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton;
    @FXML private Button dashboardButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    private ServiceEnseignant serviceEnseignant;
    private User currentUser;
    private File selectedPhotoFile;

    public void initialize() {
        serviceEnseignant = new ServiceEnseignant();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            emailField.setEditable(false);

            if (currentUser.getDateNaissance() != null) {
                dateNaissanceField.setText(currentUser.getDateNaissance());
            }

            telephoneField.setText(String.valueOf(currentUser.getTelephone()));

            specialiteComboBox.getItems().addAll(
                    "Lecture et écriture",
                    "Mathématiques",
                    "Orthophonie",
                    "Psychopédagogie",
                    "Technologies d'assistance"
            );

            if (currentUser instanceof Enseignant) {
                Enseignant enseignant = (Enseignant) currentUser;
                specialiteComboBox.setValue(enseignant.getSpecialite());
                experienceField.setText(enseignant.getExperience());
            }

            if (currentUser.getPhotoProfil() != null && !currentUser.getPhotoProfil().isEmpty()) {
                File imageFile = new File(currentUser.getPhotoProfil());
                if (imageFile.exists()) {
                    try {
                        Image image = new Image(imageFile.toURI().toString());
                        profileImageView.setImage(image);
                        photoProfilField.setText(currentUser.getPhotoProfil());
                    } catch (Exception e) {
                        e.printStackTrace();
                        profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.jpg")));
                    }
                } else {
                    profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.jpg")));
                }
            } else {
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.jpg")));
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

        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            if (file.length() > 5 * 1024 * 1024) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier trop grand", "La photo ne doit pas dépasser 5 Mo.");
                return;
            }
            selectedPhotoFile = file;
            photoProfilField.setText(file.getAbsolutePath());
            try {
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement de l'image", "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté", "Aucun utilisateur n'est connecté.");
            return;
        }

        try {
            if (!validateFields()) {
                return;
            }

            Enseignant enseignant = convertUserToEnseignant(currentUser);
            enseignant.setNom(nomField.getText().trim());
            enseignant.setPrenom(prenomField.getText().trim());
            enseignant.setEmail(emailField.getText().trim());
            enseignant.setDateNaissance(dateNaissanceField.getText().trim());
            enseignant.setTelephone(Integer.parseInt(telephoneField.getText().trim()));
            enseignant.setSpecialite(specialiteComboBox.getValue());
            enseignant.setExperience(experienceField.getText().trim());

            if (!passwordField.getText().isEmpty()) {
                enseignant.setPassword(passwordField.getText());
            }

            if (selectedPhotoFile != null) {
                if (!selectedPhotoFile.exists()) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable", "La photo de profil sélectionnée n'existe pas.");
                    return;
                }
                enseignant.setPhotoProfil(selectedPhotoFile.getAbsolutePath());
            } else if (!photoProfilField.getText().isEmpty()) {
                File existingPhoto = new File(photoProfilField.getText());
                if (existingPhoto.exists()) {
                    enseignant.setPhotoProfil(photoProfilField.getText());
                } else {
                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Photo introuvable", "La photo de profil spécifiée n'existe pas.");
                    enseignant.setPhotoProfil(null);
                }
            }

            serviceEnseignant.modifier(enseignant);
            SessionManager.getInstance().setCurrentUser(enseignant);
            currentUser = enseignant;

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour", "Profil mis à jour avec succès !");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Données invalides", "Veuillez entrer un numéro de téléphone valide.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données", "Une erreur est survenue lors de la mise à jour du profil.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue", "Une erreur est survenue : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        String nameRegex = "^[a-zA-Z\\s]{2,50}$";
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Nom requis", "Le nom est requis.");
            isValid = false;
        } else if (!nom.matches(nameRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Nom invalide", "Le nom doit contenir 2-50 lettres.");
            isValid = false;
        }

        String prenom = prenomField.getText().trim();
        if (prenom.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Prénom requis", "Le prénom est requis.");
            isValid = false;
        } else if (!prenom.matches(nameRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Prénom invalide", "Le prénom doit contenir 2-50 lettres.");
            isValid = false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email requis", "L'email est requis.");
            isValid = false;
        } else if (!email.matches(emailRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email invalide", "L'email n'est pas valide.");
            isValid = false;
        }

        String dateNaissance = dateNaissanceField.getText().trim();
        if (!dateNaissance.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate birthDate = LocalDate.parse(dateNaissance, formatter);
                LocalDate now = LocalDate.now();
                if (birthDate.isAfter(now.minusYears(18))) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Âge invalide", "L'âge minimum est 18 ans.");
                    isValid = false;
                }
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Date invalide", "Le format de la date doit être YYYY-MM-DD.");
                isValid = false;
            }
        }

        String phoneRegex = "^\\+?\\d{8,12}$";
        String telephone = telephoneField.getText().trim();
        if (telephone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Téléphone requis", "Le numéro de téléphone est requis.");
            isValid = false;
        } else if (!telephone.matches(phoneRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Téléphone invalide", "Le numéro doit contenir 8-12 chiffres.");
            isValid = false;
        }

        if (specialiteComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Spécialité requise", "Veuillez sélectionner une spécialité.");
            isValid = false;
        }

        String experience = experienceField.getText().trim();
        if (experience.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Expérience requise", "L'expérience est requise.");
            isValid = false;
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!passwordField.getText().isEmpty() && !passwordField.getText().matches(passwordRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe invalide", "Le mot de passe doit contenir 8+ caractères, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.");
            isValid = false;
        }

        return isValid;
    }

    private Enseignant convertUserToEnseignant(User user) {
        Enseignant enseignant = new Enseignant();
        enseignant.setId(user.getId());
        enseignant.setNom(user.getNom());
        enseignant.setPrenom(user.getPrenom());
        enseignant.setEmail(user.getEmail());
        enseignant.setPassword(user.getPassword());
        enseignant.setTelephone(user.getTelephone());
        enseignant.setDateNaissance(user.getDateNaissance());
        enseignant.setPhotoProfil(user.getPhotoProfil());
        enseignant.setRoles(user.getRoles());

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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du tableau de bord", e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du profil", e.getMessage());
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
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}