package org.example.controller;

import org.example.entity.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class EvenementCardController {

    @FXML private ImageView eventImage;
    @FXML private Label eventName;
    @FXML private Label eventDate;
    @FXML private HBox eventCard;

    private Evenement evenement;

    public void setData(Evenement evenement) {
        this.evenement = evenement;

        eventName.setText(evenement.getNom());
        eventDate.setText(evenement.getDate().toString());

        try {
            if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
                String imagePath = "/images/" + evenement.getImage();
                Image image = new Image(getClass().getResource(imagePath).toExternalForm());
                eventImage.setImage(image);
                eventImage.setOpacity(0);
                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(600), eventImage);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        eventName.setOnMouseClicked(this::openDetails);
    }

    private void openDetails(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-detail.fxml"));
            Parent root = loader.load();
            EvenementDetailController controller = loader.getController();
            controller.setEvenement(evenement);
            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPastelColors(String... colors) {
        String style = """
            -fx-background-color: rgba(255, 255, 255, 0.25);
            -fx-border-color: rgba(255,255,255,0.3);
            -fx-border-width: 1.5px;
            -fx-background-radius: 20px;
            -fx-border-radius: 20px;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.2, 0, 4);
        """;
        eventCard.setStyle(style);
    }
}
