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
import org.example.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegisterEnseignant {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterEnseignant.class);

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField telephoneField;
    @FXML private TextField dateNaissanceField;
    @FXML private TextField specialiteField;
    @FXML private ComboBox<String> experienceField;
    @FXML private TextField photoProfilField;
    @FXML private TextField verificationCodeField;
    @FXML private Button sendVerificationCodeButton;
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label telephoneError;
    @FXML private Label dateNaissanceError;
    @FXML private Label specialiteError;
    @FXML private Label experienceError;
    @FXML private Label photoProfilError;
    @FXML private Label verificationCodeError;

    private File selectedPhotoFile;
    private ServiceEnseignant serviceEnseignant;
    private EmailService emailService;
    private boolean isEmailVerified;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;
    private LocalDateTime lastEmailSentTime;
    private static final long CODE_EXPIRY_MINUTES = 10;
    private static final long EMAIL_COOLDOWN_SECONDS = 60;

    public RegisterEnseignant() {
        this.serviceEnseignant = new ServiceEnseignant();
        this.emailService = new EmailService();
        this.isEmailVerified = false;
    }

    @FXML
    private void initialize() {
        experienceField.setValue(null);
    }

    @FXML
    private void handlePhotoUpload() {
        if (nomField.getScene() == null || nomField.getScene().getWindow() == null) {
            LOGGER.error("Cannot open file chooser: Scene or window is null");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'interface",
                    "Impossible d'ouvrir le sélecteur de fichiers.");
            return;
        }

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
    private void handleSendVerificationCode() {
        clearErrors();
        String email = emailField.getText().trim();
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (email.isEmpty()) {
            emailError.setText("Requis");
            return;
        } else if (!email.matches(emailRegex)) {
            emailError.setText("Email invalide");
            return;
        }

        // Check email sending cooldown
        if (lastEmailSentTime != null &&
                LocalDateTime.now().isBefore(lastEmailSentTime.plusSeconds(EMAIL_COOLDOWN_SECONDS))) {
            emailError.setText("Attendez avant de renvoyer");
            return;
        }

        try {
            if (serviceEnseignant.emailExists(email)) {
                emailError.setText("Email déjà utilisé");
                return;
            }

            // Generate and store verification code
            verificationCode = emailService.generateVerificationCode();
            verificationCodeExpiry = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

            // Send verification email
            emailService.sendVerificationEmail(email, verificationCode);
            lastEmailSentTime = LocalDateTime.now();
            showAlert(Alert.AlertType.INFORMATION, "Code envoyé", "Vérifiez votre boîte de réception",
                    "Un code de vérification a été envoyé à " + email);
        } catch (MessagingException e) {
            emailError.setText("Erreur d'envoi d'email");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'envoi",
                    "Impossible d'envoyer l'email. Vérifiez votre connexion ou l'adresse email.");
            LOGGER.error("Failed to send verification email", e);
        } catch (SQLException e) {
            emailError.setText("Erreur de vérification");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données",
                    "Erreur lors de la vérification de l'email.");
            LOGGER.error("Database error during email verification", e);
        }
    }

    @FXML
    private void handleVerifyCode() {
        clearErrors();
        String enteredCode = verificationCodeField.getText().trim();

        if (enteredCode.isEmpty()) {
            verificationCodeError.setText("Requis");
            return;
        }

        if (verificationCodeExpiry == null || LocalDateTime.now().isAfter(verificationCodeExpiry)) {
            verificationCodeError.setText("Code expiré");
            verificationCode = null;
            verificationCodeExpiry = null;
            return;
        }

        if (enteredCode.equals(verificationCode)) {
            isEmailVerified = true;
            verificationCodeError.setText("Code vérifié");
            verificationCodeField.setDisable(true);
            sendVerificationCodeButton.setDisable(true);
            sendVerificationCodeButton.setText("Vérifié");
            verificationCode = null;
            verificationCodeExpiry = null;
        } else {
            verificationCodeError.setText("Code incorrect");
            isEmailVerified = false;
        }
    }

    @FXML
    private void handleRegister() {
        try {
            clearErrors();

            if (!isEmailVerified) {
                verificationCodeError.setText("Vérifiez votre email");
                return;
            }

            if (!validateFields()) {
                return;
            }

            Enseignant enseignant = new Enseignant();
            enseignant.setNom(nomField.getText().trim());
            enseignant.setPrenom(prenomField.getText().trim());
            enseignant.setEmail(emailField.getText().trim());
            enseignant.setPassword(passwordField.getText());
            enseignant.setTelephone(Integer.parseInt(telephoneField.getText().trim()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            enseignant.setDateNaissance(String.valueOf(LocalDate.parse(dateNaissanceField.getText().trim(), formatter)));
            enseignant.setSpecialite(specialiteField.getText().trim());
            enseignant.setExperience(experienceField.getValue());

            String photoPath = null;
            if (selectedPhotoFile != null) {
                photoPath = saveProfilePhoto(selectedPhotoFile, String.valueOf(generateNewId()));
                enseignant.setPhotoProfil(photoPath);
            }

            List<String> roles = new ArrayList<>();
            roles.add("ROLE_ENSEIGNANT");
            enseignant.setRoles(roles);

            enseignant.setId(generateNewId());
            serviceEnseignant.ajouter(enseignant);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie",
                    "L'enseignant a été enregistré avec succès! Statut: inactif (en attente de validation)");

            clearFields();
            isEmailVerified = false;
            verificationCodeField.setDisable(false);
            sendVerificationCodeButton.setDisable(false);
            sendVerificationCodeButton.setText("Envoyer Code");

        } catch (NumberFormatException e) {
            telephoneError.setText("Nombre invalide");
            LOGGER.error("Invalid telephone number format", e);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données",
                    "Erreur lors de l'enregistrement: " + e.getMessage());
            LOGGER.error("Database error during registration", e);
        } catch (IOException e) {
            photoProfilError.setText("Erreur photo");
            LOGGER.error("Error saving profile photo", e);
        }
    }

    private String saveProfilePhoto(File photoFile, String enseignantId) throws IOException {
        String targetDir = System.getProperty("user.home") + "/enseignant_photos";
        File directory = new File(targetDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + targetDir);
        }

        String extension = getFileExtension(photoFile.getName());
        String newFileName = enseignantId + "_profile" + extension;
        Path targetPath = Paths.get(targetDir, newFileName);

        try {
            Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to copy profile photo to {}", targetPath, e);
            throw e;
        }

        return targetPath.toString();
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private int generateNewId() {
        return UUID.randomUUID().hashCode() & Integer.MAX_VALUE;
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validate Nom
        String nameRegex = "^[a-zA-Z\\s]{2,50}$";
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            nomError.setText("Requis");
            isValid = false;
        } else if (!nom.matches(nameRegex)) {
            nomError.setText("2-50 lettres");
            isValid = false;
        }

        // Validate Prénom
        String prenom = prenomField.getText().trim();
        if (prenom.isEmpty()) {
            prenomError.setText("Requis");
            isValid = false;
        } else if (!prenom.matches(nameRegex)) {
            prenomError.setText("2-50 lettres");
            isValid = false;
        }

        // Validate Email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Requis");
            isValid = false;
        } else if (!email.matches(emailRegex)) {
            emailError.setText("Email invalide");
            isValid = false;
        }

        // Validate Password
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (passwordField.getText().isEmpty()) {
            passwordError.setText("Requis");
            isValid = false;
        } else if (!passwordField.getText().matches(passwordRegex)) {
            passwordError.setText("8+ chars, 1 maj, 1 min, 1 chiffre, 1 spécial");
            isValid = false;
        }

        // Validate Confirm Password
        if (confirmPasswordField.getText().isEmpty()) {
            confirmPasswordError.setText("Requis");
            isValid = false;
        } else if (!confirmPasswordField.getText().equals(passwordField.getText())) {
            confirmPasswordError.setText("Non identique");
            isValid = false;
        }

        // Validate Telephone
        String phoneRegex = "^\\+?\\d{8,12}$";
        String telephone = telephoneField.getText().trim();
        if (telephone.isEmpty()) {
            telephoneError.setText("Requis");
            isValid = false;
        } else if (!telephone.matches(phoneRegex)) {
            telephoneError.setText("8-12 chiffres");
            isValid = false;
        }

        // Validate Date of Birth
        String dateNaissance = dateNaissanceField.getText().trim();
        if (dateNaissance.isEmpty()) {
            dateNaissanceError.setText("Requis");
            isValid = false;
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate birthDate = LocalDate.parse(dateNaissance, formatter);
                LocalDate now = LocalDate.now();
                if (birthDate.isAfter(now.minusYears(18))) {
                    dateNaissanceError.setText("Âge minimum 18 ans");
                    isValid = false;
                }
            } catch (DateTimeParseException e) {
                dateNaissanceError.setText("Format YYYY-MM-DD");
                isValid = false;
            }
        }

        // Validate Spécialité
        String specialiteRegex = "^[a-zA-Z\\s-]{3,100}$";
        String specialite = specialiteField.getText().trim();
        if (specialite.isEmpty()) {
            specialiteError.setText("Requis");
            isValid = false;
        } else if (!specialite.matches(specialiteRegex)) {
            specialiteError.setText("3-100 lettres, espaces, tirets");
            isValid = false;
        }

        // Validate Expérience
        if (experienceField.getValue() == null) {
            experienceError.setText("Requis");
            isValid = false;
        }

        // Validate Photo (optional, max 5MB)
        if (selectedPhotoFile != null && selectedPhotoFile.length() > 5 * 1024 * 1024) {
            photoProfilError.setText("Max 5 Mo");
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        telephoneField.clear();
        dateNaissanceField.clear();
        specialiteField.clear();
        experienceField.setValue(null);
        photoProfilField.clear();
        verificationCodeField.clear();
        selectedPhotoFile = null;
    }

    private void clearErrors() {
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        telephoneError.setText("");
        dateNaissanceError.setText("");
        specialiteError.setText("");
        experienceError.setText("");
        photoProfilError.setText("");
        verificationCodeError.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/choix.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Error navigating to choice screen", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation impossible",
                    "Impossible de revenir à l'écran de choix.");
        }
    }
}