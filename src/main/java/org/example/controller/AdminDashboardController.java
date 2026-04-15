package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.utils.SceneController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    @FXML
    private Label adminNameLabel;

    @FXML
    private Text welcomeText;

    @FXML
    private Label dateLabel;

    private org.example.entity.User currentUser;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
    }

    public void setUser(org.example.entity.User user) {
        this.currentUser = user;
        if (user != null) {
            adminNameLabel.setText(user.getName());
            welcomeText.setText("Welcome back, " + user.getName() + "!");
        }
    }

    @FXML
    void handleUserManagement(MouseEvent event) {
        Stage stage = (Stage) adminNameLabel.getScene().getWindow();
        javafx.fxml.FXMLLoader loader = SceneController.switchTo("userManagement.fxml", stage, "User Management");
        UserManagementController controller = loader.getController();
        controller.setUser(currentUser);
    }

    @FXML
    void handleLogout(MouseEvent event) {
        Stage stage = (Stage) adminNameLabel.getScene().getWindow();
        SceneController.switchTo("login.fxml", stage, "Login");
    }
}
