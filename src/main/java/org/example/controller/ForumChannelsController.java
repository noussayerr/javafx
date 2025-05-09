package org.example.controller;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entity.Channel;
import org.example.entity.Forum;
import org.example.services.ServiceChannel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ForumChannelsController {

    @FXML
    private Label titleLabel;

    @FXML
    private VBox channelsContainer;

    @FXML
    private Button closeButton;

    private ServiceChannel serviceChannel = new ServiceChannel();

    public void setForum(Forum forum) {
        titleLabel.setText("Channels de " + forum.getName());
        loadChannels(forum);
    }

    private void loadChannels(Forum forum) {
        try {
            List<Channel> channels = serviceChannel.afficher();
            channelsContainer.getChildren().clear();

            // Filter channels by forum
            channels.removeIf(channel -> channel.getForum().getId() != forum.getId());

            if (channels.isEmpty()) {
                Label noChannelsLabel = new Label("Aucun channel disponible.");
                noChannelsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7a7a7a;");
                channelsContainer.getChildren().add(noChannelsLabel);
            } else {
                for (Channel channel : channels) {
                    VBox channelCard = createChannelCard(channel);
                    channelsContainer.getChildren().add(channelCard);

                    // Apply animation
                    channelCard.setScaleX(0.8);
                    channelCard.setScaleY(0.8);
                    ScaleTransition scale = new ScaleTransition(Duration.millis(200), channelCard);
                    scale.setToX(1);
                    scale.setToY(1);
                    scale.setDelay(Duration.millis(channelsContainer.getChildren().indexOf(channelCard) * 50));
                    scale.play();
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des channels", e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createChannelCard(Channel channel) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(400);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0f4f8; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 0);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);"));

        Label nameLabel = new Label(channel.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3a4a6d;");

        Button viewButton = new Button("Voir");
        viewButton.setStyle("-fx-background-color: #4a6baf; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 15;");
        viewButton.setOnAction(e -> openMessagesWindow(channel));

        HBox buttonBox = new HBox(viewButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        card.getChildren().addAll(nameLabel, buttonBox);
        return card;
    }

    private void openMessagesWindow(Channel channel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ChannelMessages.fxml"));
            Parent root = loader.load();

            ChannelMessagesController controller = loader.getController();
            controller.setChannel(channel);

            Stage stage = new Stage();
            stage.setTitle("Discussion: " + channel.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre de messages", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}