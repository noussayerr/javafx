package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.entity.User;
import org.example.services.UserService;
import org.example.utils.SceneController;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        try {
            if (userService.authenticate(email, password)) {
                User user = userService.getByEmail(email);
                redirectToDashboard(user);
            } else {
                showError("Invalid email or password.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToDashboard(User user) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        try {
            if ("admin".equalsIgnoreCase(user.getRoleType())) {
                FXMLLoader loader = SceneController.switchTo("dashboardAdmin.fxml", stage, "Admin Dashboard");
                AdminDashboardController controller = loader.getController();
                controller.setUser(user);
            } else {
                FXMLLoader loader = SceneController.switchTo("dashboardUser.fxml", stage, "User Dashboard");
                UserDashboardController controller = loader.getController();
                controller.setUser(user);
            }
        } catch (Exception e) {
            showError("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void switchToSignup(MouseEvent event) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneController.switchTo("signup.fxml", stage, "Sign Up");
    }

    @FXML
    void handleForgotPassword(MouseEvent event) {
        System.out.println("Forgot password clicked");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
