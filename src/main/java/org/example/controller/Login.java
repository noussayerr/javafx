package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.entity.User;
import org.example.services.ServiceUser;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Login {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email et mot de passe sont requis");
            return;
        }

        try {
            User user = serviceUser.authenticate(email, password);

            if (user != null) {
                // Stocker l'utilisateur connecté dans la session
                SessionManager.getInstance().setCurrentUser(user);

                // Redirection selon le rôle
                redirectBasedOnRole(user.getRoles());
            } else {
                errorLabel.setText("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            errorLabel.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        } catch (IOException e) {
            errorLabel.setText("Erreur de chargement de l'interface");
            e.printStackTrace();
        }
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
            // Rôle par défaut si aucun rôle reconnu
            fxmlFile = "/org/example/view/UserDashboard.fxml";
            title = "Tableau de bord Utilisateur";
        }

        // Charger la nouvelle interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Configurer la nouvelle scène
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
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page d'inscription");
        }
    }
}