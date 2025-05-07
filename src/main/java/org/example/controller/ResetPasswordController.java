package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.services.ServiceUser;

import java.sql.SQLException;

public class ResetPasswordController {

    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private String email;
    private final ServiceUser serviceUser = new ServiceUser();

    // Method to set email
    public void setResetData(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        errorLabel.setText("");

        String enteredCode = codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (enteredCode.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Tous les champs sont requis");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passes ne correspondent pas");
            return;
        }

        // Validate password strength
        if (newPassword.length() < 8) {
            errorLabel.setText("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            // Verify reset code
            if (!serviceUser.verifyResetCode(email, enteredCode)) {
                errorLabel.setText("Code de réinitialisation incorrect ou expiré");
                return;
            }

            // Update the user's password
            serviceUser.updatePassword(email, newPassword);
            errorLabel.setText("Mot de passe réinitialisé avec succès");
            errorLabel.setStyle("-fx-text-fill: #2ecc71;");

            // Close the window after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) codeField.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (SQLException e) {
            errorLabel.setText("Erreur base de données");
            e.printStackTrace();
        } catch (Exception e) {
            errorLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}