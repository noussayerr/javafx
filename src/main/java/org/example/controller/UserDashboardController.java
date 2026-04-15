package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.utils.SceneController;

import javafx.fxml.FXMLLoader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class UserDashboardController {
    private org.example.entity.User currentUser;


    @FXML
    private Label userNameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Text welcomeText;

    @FXML
    private Label dateLabel;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
    }

    public void setUser(org.example.entity.User user) {
        if (user != null) {
            this.currentUser = user;
            userNameLabel.setText(user.getName());
            roleLabel.setText(user.getRoleType().toUpperCase());
            welcomeText.setText("Welcome back, " + user.getName() + "!");
        }
    }

    @FXML
    void handleProfileAction() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        FXMLLoader loader = SceneController.switchTo("profil.fxml", stage, "My Profile");
        if (loader != null) {
            ProfilController controller = loader.getController();
            controller.setUser(currentUser);
        }
    }


    @FXML
    void handleLogout(MouseEvent event) {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneController.switchTo("login.fxml", stage, "Login");
    }
}
