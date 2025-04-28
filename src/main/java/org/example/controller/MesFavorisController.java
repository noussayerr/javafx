package org.example.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.EvenementDAO;
import org.example.dao.FavoriDAO;
import org.example.entity.Evenement;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.util.List;

public class MesFavorisController {

    @FXML private VBox favorisContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane mainContainer;

    private final FavoriDAO favoriDAO = FavoriDAO.getInstance();
    private final EvenementDAO evenementDAO = new EvenementDAO();

    @FXML
    public void initialize() {
        scrollPane.setStyle("-fx-background-color: transparent;");
        afficherFavoris();  // Important au démarrage
        playAppearAnimation();  // Animation d’apparition
    }

    // ✅ rendre public pour pouvoir l'appeler depuis EvenementListController
    public void afficherFavoris() {
        favorisContainer.getChildren().clear();

        int userId = SessionManager.getInstance().getCurrentUser().getId();
        List<Integer> favorisIds = favoriDAO.getEventIdsFavorisByUser(userId);

        for (int id : favorisIds) {
            Evenement event = evenementDAO.findById(id);


            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-card.fxml"));
                Node card = loader.load(); // ✅ d’abord charger le FXML
                EvenementCardController controller = loader.getController(); // ✅ ensuite récupérer le controller
                controller.setData(event);
                controller.setPastelColors("#D9B3FF", "#B3D9FF", "#FFB3E6");
                favorisContainer.getChildren().add(card);

                FadeTransition fadeCard = new FadeTransition(Duration.seconds(0.4), card);
                fadeCard.setFromValue(0);
                fadeCard.setToValue(1);

                TranslateTransition slideCard = new TranslateTransition(Duration.seconds(0.4), card);
                slideCard.setFromY(20);
                slideCard.setToY(0);

                ParallelTransition cardAppear = new ParallelTransition(fadeCard, slideCard);
                cardAppear.setDelay(Duration.millis(150 * favorisContainer.getChildren().size()));
                cardAppear.play();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playAppearAnimation() {
        mainContainer.setOpacity(0);
        mainContainer.setScaleX(0.9);
        mainContainer.setScaleY(0.9);
        mainContainer.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), mainContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), mainContainer);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.5), mainContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        ParallelTransition appear = new ParallelTransition(fade, scale, slideUp);
        appear.play();
    }

    // ✅ Animation de fermeture avec fade out + slide
    @FXML
    private void retour() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), mainContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.5), mainContainer);
        slideDown.setByY(40);

        RotateTransition rotate = new RotateTransition(Duration.seconds(0.5), mainContainer);
        rotate.setByAngle(3);

        ParallelTransition closeAnim = new ParallelTransition(fadeOut, slideDown, rotate);
        closeAnim.setOnFinished(e -> {
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.close();
        });

        closeAnim.play();
    }

}
