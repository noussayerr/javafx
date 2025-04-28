package org.example.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneTransitionUtil {

    public static void playFadeSlideIn(Node root) {
        root.setOpacity(0);
        root.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(400), root);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), root);
        slide.setFromY(30);
        slide.setToY(0);

        ParallelTransition transition = new ParallelTransition(fade, slide);
        transition.play();
    }

    public static void closeWithFadeSlideOut(Node node, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setFromValue(1);
        fade.setToValue(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
        slide.setByY(40);

        ParallelTransition transition = new ParallelTransition(fade, slide);
        transition.setOnFinished(e -> onFinished.run());
        transition.play();
    }
}
