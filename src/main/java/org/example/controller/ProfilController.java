package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.entity.User;
import org.example.services.UserService;
import org.example.utils.SceneController;

import java.sql.SQLException;

public class ProfilController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label profileHeaderName;

    @FXML
    private Label statusLabel;

    private User currentUser;
    private final UserService userService = new UserService();

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            profileHeaderName.setText(user.getName());
        }
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        if (currentUser == null) return;

        String newName = nameField.getText();
        String newEmail = emailField.getText();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            showStatus("Please fill all fields", true);
            return;
        }

        currentUser.setName(newName);
        currentUser.setEmail(newEmail);

        try {
            userService.update(currentUser);
            profileHeaderName.setText(newName);
            showStatus("Profile updated successfully!", false);
        } catch (SQLException e) {
            showStatus("Error updating profile: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    void handleBackToDashboardAction(ActionEvent event) {
        switchToDashboard();
    }

    @FXML
    void handleBackToDashboardClick(MouseEvent event) {
        switchToDashboard();
    }


    private void switchToDashboard() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        try {
            FXMLLoader loader = SceneController.switchTo("dashboardUser.fxml", stage, "User Dashboard");
            if (loader != null) {
                UserDashboardController controller = loader.getController();
                controller.setUser(currentUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(MouseEvent event) {
        Stage stage = (Stage) nameField.getScene().getWindow();
        SceneController.switchTo("login.fxml", stage, "Login");
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }
}
