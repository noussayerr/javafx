package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.entity.Forum;
import org.example.services.ServiceForum;

import java.io.IOException;

public class AddForumController {

    @FXML
    private TextField forumNameField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private ServiceForum serviceForum = new ServiceForum();

    @FXML
    private void handleSave(ActionEvent event) {
        String forumName = forumNameField.getText().trim();

        if (forumName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champ vide", "Le nom du forum ne peut pas être vide.");
            return;
        }

        try {
            Forum forum = new Forum();
            forum.setName(forumName);
            serviceForum.ajouter(forum);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum créé", "Le forum a été créé avec succès.");
            handleCancel(event); // Return to dashboard
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/AdminDashboard.fxml"));
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}