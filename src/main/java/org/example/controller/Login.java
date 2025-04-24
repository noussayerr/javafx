package org.example.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import java.util.UUID;

public class Login {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField captchaAnswerField;
    @FXML private ImageView captchaQuestionImageView;
    @FXML private Button faceIdButton;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label captchaError;
    private final EmailService emailService = new EmailService();

    private final ServiceUser serviceUser = new ServiceUser();
    private final FaceVerificationService faceVerificationService = new FaceVerificationService();
    private static final String FACEID_API_URL = "http://localhost:5000/faceid";
    private final boolean BYPASS_CAPTCHA_FOR_TESTING = false; // Set to false to enforce CAPTCHA
    private int captchaCorrectAnswer;
    private final Random random = new Random();

    @FXML
    private void initialize() {
        // Generate initial CAPTCHA question
        generateCaptchaQuestion();

        // Bind FaceID button's disable property to emailField's text property
        faceIdButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> emailField.getText().trim().isEmpty(),
                        emailField.textProperty()
                )
        );
    }

    // Generate a random math CAPTCHA question as an image (e.g., "5 + 3 = ?" or "7 - 2 = ?")
    private void generateCaptchaQuestion() {
        int num1 = random.nextInt(10) + 1; // 1 to 10
        int num2 = random.nextInt(10) + 1; // 1 to 10
        boolean isAddition = random.nextBoolean();
        String question;

        if (isAddition) {
            question = num1 + " + " + num2 + " = ?";
            captchaCorrectAnswer = num1 + num2;
        } else {
            // Ensure num1 >= num2 to avoid negative results
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            question = num1 + " - " + num2 + " = ?";
            captchaCorrectAnswer = num1 - num2;
        }

        // Generate image for the question
        try {
            Image captchaImage = createCaptchaImage(question);
            captchaQuestionImageView.setImage(captchaImage);
        } catch (IOException e) {
            captchaError.setText("Erreur génération CAPTCHA");
            e.printStackTrace();
        }

        captchaAnswerField.clear();
    }

    // Create an image with the CAPTCHA question
    private Image createCaptchaImage(String question) throws IOException {
        int width = 150;
        int height = 40;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Set background and text properties
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        // Draw the question text
        g2d.drawString(question, 10, 25);

        // Add some noise to make it harder for bots
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.drawLine(x, y, x, y);
        }

        g2d.dispose();

        // Convert BufferedImage to JavaFX Image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new Image(bais);
    }

    // Validate the CAPTCHA answer
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
        // Clear previous errors
        clearErrors();

        String email = emailField.getText().trim();

        // Validate email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (email.isEmpty()) {
            emailError.setText("Email requis");
            return;
        } else if (!email.matches(emailRegex)) {
            emailError.setText("Email invalide");
            return;
        }

        // Verify CAPTCHA
        String captchaAnswer = captchaAnswerField.getText();
        if (!BYPASS_CAPTCHA_FOR_TESTING) {
            if (captchaAnswer == null || captchaAnswer.trim().isEmpty()) {
                captchaError.setText("Veuillez répondre au CAPTCHA pour continuer");
                return;
            }
            if (!verifyCaptcha(captchaAnswer)) {
                captchaError.setText("Réponse CAPTCHA incorrecte. Réessayez.");
                generateCaptchaQuestion(); // Generate new question
                return;
            }
        } else {
            System.out.println("CAPTCHA bypassed for testing");
        }

        try {
            // Check if user exists
            User user = serviceUser.findByEmail(email);
            if (user == null) {
                emailError.setText("Aucun utilisateur trouvé avec cet email");
                generateCaptchaQuestion(); // Generate new CAPTCHA
                return;
            }

            // Generate a 6-digit reset code
            String resetCode = String.format("%06d", random.nextInt(999999));

            // Store the reset code (assumes ServiceUser has a method to handle this)
            serviceUser.storePasswordResetCode(email, resetCode);

            emailService.sendResetCodeEmail(email, resetCode);

            // Open the reset password window and pass the email and reset code
            openResetPasswordWindow(email, resetCode);

            // Provide feedback to the user
            emailError.setText("Vérifiez le code affiché et entrez-le dans la fenêtre de réinitialisation");
            emailError.setStyle("-fx-text-fill: #2ecc71;"); // Green color for success

            // Generate new CAPTCHA
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

    // Open a new window for resetting the password
    private void openResetPasswordWindow(String email, String resetCode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ResetPassword.fxml"));
        Parent root = loader.load();

        // Get the controller and pass the email and reset code
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
        // Clear previous errors
        clearErrors();

        String email = emailField.getText().trim();

        // Validate email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (email.isEmpty()) {
            emailError.setText("Email requis");
            return;
        } else if (!email.matches(emailRegex)) {
            emailError.setText("Email invalide");
            return;
        }

        // Verify CAPTCHA
        String captchaAnswer = captchaAnswerField.getText();
        if (!BYPASS_CAPTCHA_FOR_TESTING) {
            if (captchaAnswer == null || captchaAnswer.trim().isEmpty()) {
                captchaError.setText("Veuillez répondre au CAPTCHA pour continuer");
                return;
            }
            if (!verifyCaptcha(captchaAnswer)) {
                captchaError.setText("Réponse CAPTCHA incorrecte. Réessayez.");
                generateCaptchaQuestion(); // Generate new question
                return;
            }
        } else {
            System.out.println("CAPTCHA bypassed for testing");
        }

        try {
            // Call FaceVerificationService to verify face
            Map<String, Object> result = faceVerificationService.verifyFace(email);

            boolean success = (boolean) result.get("success");
            String message = (String) result.get("message");

            if (success) {
                // Fetch user by email
                User user = serviceUser.findByEmail(email);
                if (user != null) {
                    // Store user in session
                    SessionManager.getInstance().setCurrentUser(user);
                    // Redirect based on role
                    redirectBasedOnRole(user.getRoles());
                } else {
                    emailError.setText("Utilisateur non trouvé");
                }
            } else {
                emailError.setText(message);
            }
            generateCaptchaQuestion(); // Generate new question after attempt
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
        // Clear previous errors
        clearErrors();

        String email = emailField.getText();
        String password = passwordField.getText();

        // Validate inputs
        if (!validateFields()) {
            return;
        }

        // Verify CAPTCHA
        String captchaAnswer = captchaAnswerField.getText();
        if (!BYPASS_CAPTCHA_FOR_TESTING) {
            if (captchaAnswer == null || captchaAnswer.trim().isEmpty()) {
                captchaError.setText("Veuillez répondre au CAPTCHA pour continuer");
                return;
            }
            if (!verifyCaptcha(captchaAnswer)) {
                captchaError.setText("Réponse CAPTCHA incorrecte. Réessayez.");
                generateCaptchaQuestion(); // Generate new question
                return;
            }
        } else {
            System.out.println("CAPTCHA bypassed for testing");
        }

        try {
            // Authenticate user
            User user = serviceUser.authenticate(email, password);

            if (user != null) {
                // Store user in session
                SessionManager.getInstance().setCurrentUser(user);
                // Redirect based on role
                redirectBasedOnRole(user.getRoles());
            } else {
                emailError.setText("Email ou mot de passe incorrect");
                passwordError.setText("Email ou mot de passe incorrect");
            }
            generateCaptchaQuestion(); // Generate new question after attempt
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

        // Validate Email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (emailField.getText().isEmpty()) {
            emailError.setText("Requis");
            isValid = false;
        } else if (!emailField.getText().matches(emailRegex)) {
            emailError.setText("Email invalide");
            isValid = false;
        }

        // Validate Password
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