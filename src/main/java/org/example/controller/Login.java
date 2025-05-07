package org.example.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.entity.User;
import org.example.services.EmailService;
import org.example.services.FaceVerificationService;
import org.example.services.ServiceUser;
import org.example.utils.SessionManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.prefs.Preferences;

public class Login {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField captchaAnswerField;
    @FXML private ImageView captchaQuestionImageView;
    @FXML private Button faceIdButton;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label captchaError;
    @FXML private CheckBox rememberMeCheckBox;
    private final EmailService emailService = new EmailService();
    private final ServiceUser serviceUser = new ServiceUser();
    private final FaceVerificationService faceVerificationService = new FaceVerificationService();
    private static final String FACEID_API_URL = "http://localhost:5000/faceid";
    private final boolean BYPASS_CAPTCHA_FOR_TESTING = false;
    private int captchaCorrectAnswer;
    private final Random random = new Random();
    private final Preferences prefs = Preferences.userNodeForPackage(Login.class);

    @FXML
    private void initialize() {
        generateCaptchaQuestion();
        faceIdButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> emailField.getText().trim().isEmpty(),
                        emailField.textProperty()
                )
        );
        // Defer loading remembered credentials until the scene is ready
        Platform.runLater(this::loadRememberedCredentials);
    }

    private void loadRememberedCredentials() {
        String rememberedEmail = prefs.get("remembered_email", "");
        String rememberedToken = prefs.get("remembered_token", "");

        if (!rememberedEmail.isEmpty() && !rememberedToken.isEmpty()) {
            try {
                // Validate the token to ensure it's valid before pre-filling
                User user = serviceUser.authenticateWithToken(rememberedEmail, rememberedToken);
                if (user != null) {
                    // Pre-fill the email field
                    emailField.setText(rememberedEmail);
                    passwordField.setText(rememberedToken);
                    // Optionally check the "Remember Me" checkbox
                    rememberMeCheckBox.setSelected(true);
                    // Do NOT set the current user or redirect
                } else {
                    // If token is invalid, clear the stored credentials
                    prefs.remove("remembered_email");
                    prefs.remove("remembered_token");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Clear credentials on error to prevent repeated failures
                prefs.remove("remembered_email");
                prefs.remove("remembered_token");
            }
        }
    }

    private void generateCaptchaQuestion() {
        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        boolean isAddition = random.nextBoolean();
        String question;

        if (isAddition) {
            question = num1 + " + " + num2 + " = ?";
            captchaCorrectAnswer = num1 + num2;
        } else {
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            question = num1 + " - " + num2 + " = ?";
            captchaCorrectAnswer = num1 - num2;
        }

        try {
            Image captchaImage = createCaptchaImage(question);
            captchaQuestionImageView.setImage(captchaImage);
        } catch (IOException e) {
            captchaError.setText("Erreur génération CAPTCHA");
            e.printStackTrace();
        }

        captchaAnswerField.clear();
    }

    private Image createCaptchaImage(String question) throws IOException {
        int width = 150;
        int height = 40;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(question, 10, 25);

        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.drawLine(x, y, x, y);
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new Image(bais);
    }

    private boolean verifyCaptcha(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }
        try {
            int userAnswer = Integer.parseInt(answer.trim());
            return userAnswer == captchaCorrectAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        clearErrors();
        String email = emailField.getText().trim();
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (email.isEmpty()) {
            emailError.setText("Email requis");
            return;
        } else if (!email.matches(emailRegex)) {
            emailError.setText("Email invalide");
            return;
        }

        String captchaAnswer = captchaAnswerField.getText();
        if (!BYPASS_CAPTCHA_FOR_TESTING) {
            if (captchaAnswer == null || captchaAnswer.trim().isEmpty()) {
                captchaError.setText("Veuillez répondre au CAPTCHA pour continuer");
                return;
            }
            if (!verifyCaptcha(captchaAnswer)) {
                captchaError.setText("Réponse CAPTCHA incorrecte. Réessayez.");
                generateCaptchaQuestion();
                return;
            }
        } else {
            System.out.println("CAPTCHA bypassed for testing");
        }

        try {
            User user = serviceUser.findByEmail(email);
            if (user == null) {
                emailError.setText("Aucun utilisateur trouvé avec cet email");
                generateCaptchaQuestion();
                return;
            }

            String resetCode = String.format("%06d", random.nextInt(999999));
            serviceUser.storePasswordResetCode(email, resetCode);
            emailService.sendResetCodeEmail(email, resetCode);
            openResetPasswordWindow(email, resetCode);
            emailError.setText("Vérifiez le code affiché et entrez-le dans la fenêtre de réinitialisation");
            emailError.setStyle("-fx-text-fill: #2ecc71;");
            generateCaptchaQuestion();
        } catch (SQLException e) {
            emailError.setText("Erreur base de données");
            e.printStackTrace();
        } catch (IOException e) {
            emailError.setText("Erreur ouverture fenêtre réinitialisation");
            e.printStackTrace();
        } catch (Exception e) {
            emailError.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openResetPasswordWindow(String email, String resetCode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ResetPassword.fxml"));
        Parent root = loader.load();
        ResetPasswordController controller = loader.getController();
        controller.setResetData(email);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Réinitialiser le mot de passe");
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void handleFaceId() {
        clearErrors();
        String email = emailField.getText().trim();
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (email.isEmpty()) {
            emailError.setText("Email requis");
            return;
        } else if (!email.matches(emailRegex)) {
            emailError.setText("Email invalide");
            return;
        }

        String captchaAnswer = captchaAnswerField.getText();
        if (!BYPASS_CAPTCHA_FOR_TESTING) {
            if (captchaAnswer == null || captchaAnswer.trim().isEmpty()) {
                captchaError.setText("Veuillez répondre au CAPTCHA pour continuer");
                return;
            }
            if (!verifyCaptcha(captchaAnswer)) {
                captchaError.setText("Réponse CAPTCHA incorrecte. Réessayez.");
                generateCaptchaQuestion();
                return;
            }
        } else {
            System.out.println("CAPTCHA bypassed for testing");
        }

        try {
            Map<String, Object> result = faceVerificationService.verifyFace(email);
            boolean success = (boolean) result.get("success");
            String message = (String) result.get("message");

            if (success) {
                User user = serviceUser.findByEmail(email);
                if (user != null) {
                    SessionManager.getInstance().setCurrentUser(user);
                    redirectBasedOnRole(user.getRoles());
                } else {
                    emailError.setText("Utilisateur non trouvé");
                }
            } else {
                emailError.setText(message);
            }
            generateCaptchaQuestion();
        } catch (SQLException e) {
            emailError.setText("Erreur base de données");
            e.printStackTrace();
        } catch (IOException e) {
            emailError.setText("Erreur de connexion au serveur, veuillez réessayer ultérieurement.");
            e.printStackTrace();
        } catch (Exception e) {
            emailError.setText("Erreur vérification faciale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        clearErrors();
        String email = emailField.getText().trim();
        String input = passwordField.getText().trim(); // Could be password or token

        if (!validateFields()) {
            return;
        }

        String captchaAnswer = captchaAnswerField.getText();
        if (!BYPASS_CAPTCHA_FOR_TESTING) {
            if (captchaAnswer == null || captchaAnswer.trim().isEmpty()) {
                captchaError.setText("Veuillez répondre au CAPTCHA pour continuer");
                return;
            }
            if (!verifyCaptcha(captchaAnswer)) {
                captchaError.setText("Réponse CAPTCHA incorrecte. Réessayez.");
                generateCaptchaQuestion();
                return;
            }
        } else {
            System.out.println("CAPTCHA bypassed for testing");
        }

        try {
            User user = null;
            // First, try password-based authentication
            user = serviceUser.authenticate(email, input);
            // If password authentication fails, try token-based authentication
            if (user == null) {
                user = serviceUser.authenticateWithToken(email, input);
            }

            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);
                if (rememberMeCheckBox.isSelected()) {
                    String token = serviceUser.generateRememberMeToken(email);
                    prefs.put("remembered_email", email);
                    prefs.put("remembered_token", token);
                } else {
                    prefs.remove("remembered_email");
                    prefs.remove("remembered_token");
                }
                redirectBasedOnRole(user.getRoles());
            } else {
                emailError.setText("Email, mot de passe ou jeton incorrect");
                passwordError.setText("Email, mot de passe ou jeton incorrect");
            }
            generateCaptchaQuestion();
        } catch (SQLException e) {
            emailError.setText("Erreur base de données");
            e.printStackTrace();
        } catch (IOException e) {
            emailError.setText("Erreur interface");
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (emailField.getText().isEmpty()) {
            emailError.setText("Requis");
            isValid = false;
        } else if (!emailField.getText().matches(emailRegex)) {
            emailError.setText("Email invalide");
            isValid = false;
        }

        if (passwordField.getText().isEmpty()) {
            passwordError.setText("Requis");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        emailError.setText("");
        emailError.setStyle("-fx-text-fill: #e74c3c;");
        passwordError.setText("");
        captchaError.setText("");
    }

    private void redirectBasedOnRole(List<String> roles) throws IOException {
        String fxmlFile;
        String title;

        if (roles.contains("ROLE_ADMIN")) {
            fxmlFile = "/org/example/view/AdminDashboard.fxml";
            title = "Tableau de bord Admin";
        } else if (roles.contains("ROLE_ENSEIGNANT")) {
            fxmlFile = "/org/example/view/EnseignantDashboard.fxml";
            title = "Tableau de bord Enseignant";
        } else if (roles.contains("ROLE_APPRENANT")) {
            fxmlFile = "/org/example/view/ApprenantDashboard.fxml";
            title = "Tableau de bord Apprenant";
        } else {
            fxmlFile = "/org/example/view/UserDashboard.fxml";
            title = "Tableau de bord Utilisateur";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.setMaximized(true);
    }

    @FXML
    private void redirectToChoice(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/choix.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            emailError.setText("Erreur chargement inscription");
            e.printStackTrace();
        }
    }
}