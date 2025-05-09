package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.entity.Forum;
import org.example.services.ServiceForum;

public class EditForumController {

    @FXML
    private TextField forumNameField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Forum forum;
    private ServiceForum serviceForum = new ServiceForum();
    private Runnable refreshCallback;

    public void setForum(Forum forum) {
        this.forum = forum;
        forumNameField.setText(forum.getName());
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String newName = forumNameField.getText().trim();

        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champ vide", "Le nom du forum ne peut pas être vide.");
            return;
        }

        try {
            forum.setName(newName);
            serviceForum.modifier(forum);

            if (refreshCallback != null) {
                refreshCallback.run();
            }

            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}