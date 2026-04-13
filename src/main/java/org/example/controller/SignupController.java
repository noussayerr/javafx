package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.entity.User;
import org.example.services.UserService;
import org.example.utils.SceneController;

import java.sql.SQLException;

public class SignupController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nameField;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (roleChoiceBox != null) {
            roleChoiceBox.getItems().addAll("Admin", "User");
            roleChoiceBox.setValue("User");
        }
    }

    @FXML
    void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String roleType = roleChoiceBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRoleType(roleType);
        user.setRoles("[\"ROLE_USER\", \"ROLE_" + roleType.toUpperCase() + "\"]");

        try {
            userService.add(user);
            // After successful signup, redirect to login
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneController.switchTo("login.fxml", stage, "Login");
        } catch (SQLException e) {
            showError("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void switchToLogin(MouseEvent event) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneController.switchTo("login.fxml", stage, "Login");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
