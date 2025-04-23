package org.example.controller;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.dao.EvenementDAO;
import org.example.entity.Evenement;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;
import javafx.scene.control.Label;


public class EvenementListController {

    // Couleurs pastel modernes
    private static final String PASTEL_VIOLET = "#D9B3FF";
    private static final String PASTEL_BLUE = "#B3D9FF";
    private static final String PASTEL_PINK = "#FFB3E6";
    private static final String SOFT_WHITE = "#FFF9FE";

    @FXML private FlowPane eventContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane mainContainer;

    private final EvenementDAO evenementDAO = new EvenementDAO();

    @FXML
    public void initialize() {

        playTitleAnimation();

        loadEventsWithModernAnimations();
        configureScrollPane();
    }

    private void configureScrollPane() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");

        javafx.application.Platform.runLater(() -> {
            Node verticalBar = scrollPane.lookup(".scroll-bar:vertical");
            if (verticalBar != null) {
                verticalBar.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-padding: 2px;" +
                                "-fx-background-radius: 5em;"
                );
            }
        });
    }

    private void loadEventsWithModernAnimations() {
        List<Evenement> events = evenementDAO.getAll();
        eventContainer.setPadding(new Insets(40, 30, 80, 30));
        eventContainer.setHgap(35);
        eventContainer.setVgap(35);

        for (int i = 0; i < events.size(); i++) {
            createModernEventCard(events.get(i), i * 150);
        }
    }

    private void createModernEventCard(Evenement event, int delayMillis) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-card.fxml"));
            Region card = loader.load();

            EvenementCardController controller = loader.getController();
            controller.setData(event);
            controller.setPastelColors(PASTEL_VIOLET, PASTEL_BLUE, PASTEL_PINK);

            card.setOpacity(0);
            card.setTranslateY(30);
            card.setRotate(1.5);
            card.setScaleX(0.97);
            card.setScaleY(0.97);

            playCardEntranceAnimation(card, delayMillis);
            setupModernHoverEffects(card);

            eventContainer.getChildren().add(card);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playCardEntranceAnimation(Node card, int delayMillis) {
        Timeline entranceAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.opacityProperty(), 0),
                        new KeyValue(card.translateYProperty(), 30),
                        new KeyValue(card.rotateProperty(), 1.5),
                        new KeyValue(card.scaleXProperty(), 0.97),
                        new KeyValue(card.scaleYProperty(), 0.97)
                ),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(card.rotateProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleYProperty(), 1, Interpolator.EASE_BOTH)
                )
        );

        entranceAnimation.setDelay(Duration.millis(delayMillis));
        entranceAnimation.play();
    }

    private void setupModernHoverEffects(Node card) {
        RotateTransition rotateOnHover = new RotateTransition(Duration.millis(300), card);
        rotateOnHover.setFromAngle(0);
        rotateOnHover.setToAngle(-1.5);

        ScaleTransition scaleOnHover = new ScaleTransition(Duration.millis(300), card);
        scaleOnHover.setToX(1.03);
        scaleOnHover.setToY(1.03);

        TranslateTransition liftOnHover = new TranslateTransition(Duration.millis(300), card);
        liftOnHover.setToY(-8);

        ShadowTransition shadowOnHover = new ShadowTransition(Duration.millis(300), card, 15, 25);

        ParallelTransition hoverOn = new ParallelTransition(
                rotateOnHover, scaleOnHover, liftOnHover, shadowOnHover
        );

        ParallelTransition hoverOff = new ParallelTransition(
                new RotateTransition(Duration.millis(300), card),
                new ScaleTransition(Duration.millis(300), card),
                new TranslateTransition(Duration.millis(300), card),
                new ShadowTransition(Duration.millis(300), card, 25, 15)
        );

        card.setOnMouseEntered(e -> hoverOn.play());
        card.setOnMouseExited(e -> hoverOff.play());
    }

    public void toggleTheme(ActionEvent event) {
        ColorTransition transition = new ColorTransition(mainContainer);
        transition.toggleTheme();
    }

    // ✅ Classe pour effet d’ombre fluide
    private static class ShadowTransition extends Transition {
        private final Node node;
        private final double fromRadius;
        private final double toRadius;

        public ShadowTransition(Duration duration, Node node, double from, double to) {
            this.node = node;
            this.fromRadius = from;
            this.toRadius = to;
            setCycleDuration(duration);
        }

        @Override
        protected void interpolate(double frac) {
            double radius = fromRadius + (toRadius - fromRadius) * frac;
            node.setEffect(new javafx.scene.effect.DropShadow(radius,
                    Color.web(PASTEL_VIOLET).deriveColor(0, 1, 1, 0.15)));
        }
    }

    // ✅ Classe pour transition entre thèmes
    private static class ColorTransition {
        private final StackPane pane;
        private boolean isDark = false;

        public ColorTransition(StackPane pane) {
            this.pane = pane;
        }

        public void toggleTheme() {
            isDark = !isDark;
            Transition transition = createTransition();
            transition.play();
        }

        private Transition createTransition() {
            String fromColor = isDark ? SOFT_WHITE : PASTEL_VIOLET;
            String toColor = isDark ? "#2E2D4D" : SOFT_WHITE;

            Rectangle bgRect = new Rectangle();
            bgRect.widthProperty().bind(pane.widthProperty());
            bgRect.heightProperty().bind(pane.heightProperty());
            bgRect.setFill(Color.web(fromColor));

            pane.getChildren().add(0, bgRect); // Assure-toi d'ajouter ce rectangle en fond

            FillTransition ft = new FillTransition(Duration.seconds(0.8), bgRect, Color.web(fromColor), Color.web(toColor));


            ft.currentTimeProperty().addListener((obs, old, now) -> {
                if (now != null) {
                    double progress = now.toMillis() / ft.getDuration().toMillis();
                    Color current = Color.web(fromColor).interpolate(Color.web(toColor), progress);
                    pane.setStyle("-fx-background-color: #" + current.toString().substring(2, 8) + ";");
                }
            });

            return ft;
        }
    }
    private void playTitleAnimation() {
        Label titleLabel = (Label) mainContainer.lookup("#titleLabel");

        if (titleLabel != null) {
            titleLabel.setOpacity(0);
            titleLabel.setTranslateY(-30);

            Timeline animation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(titleLabel.opacityProperty(), 0),
                            new KeyValue(titleLabel.translateYProperty(), -30)
                    ),
                    new KeyFrame(Duration.seconds(1.2),
                            new KeyValue(titleLabel.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(titleLabel.translateYProperty(), 0, Interpolator.EASE_OUT)
                    )
            );
            animation.play();
        }
    }

}
