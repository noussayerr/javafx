package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entity.Channel;
import org.example.entity.Forum;
import org.example.services.ServiceChannel;
import org.example.services.ServiceForum;

import java.sql.SQLException;

public class EditChannelController {

    @FXML private TextField channelNameField;
    @FXML private ComboBox<Forum> forumComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Channel channel;
    private ServiceChannel serviceChannel = new ServiceChannel();
    private ServiceForum serviceForum = new ServiceForum();
    private Runnable refreshCallback;

    public void setChannel(Channel channel) {
        this.channel = channel;
        channelNameField.setText(channel.getName());

        try {
            ObservableList<Forum> forums = FXCollections.observableArrayList(serviceForum.afficher());
            forumComboBox.setItems(forums);

            // Configurer l'affichage des éléments dans la liste déroulante
            forumComboBox.setCellFactory(param -> new ListCell<Forum>() {
                @Override
                protected void updateItem(Forum forum, boolean empty) {
                    super.updateItem(forum, empty);
                    if (empty || forum == null) {
                        setText(null);
                    } else {
                        setText(forum.getName());
                    }
                }
            });

            // Configurer l'affichage de l'élément sélectionné
            forumComboBox.setButtonCell(new ListCell<Forum>() {
                @Override
                protected void updateItem(Forum forum, boolean empty) {
                    super.updateItem(forum, empty);
                    if (empty || forum == null) {
                        setText(null);
                    } else {
                        setText(forum.getName());
                    }
                }
            });

            // Sélectionner le forum actuel du channel
            if (channel.getForum() != null) {
                for (Forum forum : forums) {
                    if (forum.getId() == channel.getForum().getId()) {
                        forumComboBox.getSelectionModel().select(forum);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement", e.getMessage());
            e.printStackTrace();
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String newName = channelNameField.getText().trim();
        Forum selectedForum = forumComboBox.getSelectionModel().getSelectedItem();

        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champ vide", "Le nom du channel ne peut pas être vide.");
            return;
        }

        if (selectedForum == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Sélection vide", "Veuillez sélectionner un forum.");
            return;
        }

        try {
            channel.setName(newName);
            channel.setForum(selectedForum);
            serviceChannel.modifier(channel);

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