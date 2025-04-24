package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
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
    @FXML private TextArea commentaireField;
    private Evenement event;

    @FXML private VBox root;


    public void setData(Evenement evenement) {
        this.event = evenement; // Stocker pour l’utiliser dans d'autres méthodes

        eventName.setText(evenement.getNom());
        eventDate.setText(evenement.getDate().toString());

        try {
            if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
                String imagePath = "/images/" + evenement.getImage();
                Image image = new Image(getClass().getResource(imagePath).toExternalForm());
                eventImage.setImage(image);

                // Animation d’apparition (fade-in)
                eventImage.setOpacity(0);
                FadeTransition fade = new FadeTransition(Duration.millis(600), eventImage);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement de l’image : " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ Action sur le nom pour ouvrir les détails
        eventName.setOnMouseClicked(this::openDetails);
    }

    private void openDetails(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-detail.fxml"));
            Parent root = loader.load();

            EvenementDetailController controller = loader.getController();
            controller.setEvenement(event, "email@example.com");


            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VBox getRoot() {
        return root;
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
