package org.example.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {

    public static void show(Stage ownerStage, String message) {
        // Création du Label du toast
        Label label = new Label(message);
        label.setStyle("""
                -fx-background-color: rgba(30, 30, 30, 0.9);
                -fx-text-fill: white;
                -fx-padding: 14px 28px;
                -fx-font-size: 15px;
                -fx-background-radius: 20px;
                -fx-font-weight: bold;
                """);
        label.setEffect(new DropShadow(10, Color.BLACK));

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setScene(scene);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        // Position : en haut à droite
        toastStage.setX(ownerStage.getX() + ownerStage.getWidth() - 320);
        toastStage.setY(ownerStage.getY() + 50);

        toastStage.show();

        // Animation de fade-in/out
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> toastStage.close());

        fadeIn.play();
        fadeIn.setOnFinished(e -> pause.play());
        pause.setOnFinished(e -> fadeOut.play());

        // Lecture du son de notification
        if (Toast.class.getResource("/sounds/notification.mp3") == null) {
            System.out.println("❌ Le fichier MP3 n’est pas trouvé !");
        } else {
            System.out.println("✅ Le fichier MP3 est trouvé !");
            AudioClip sound = new AudioClip(Toast.class.getResource("/sounds/notification.mp3").toString());
            sound.play();
        }
    }
    // Toast visuel intégré dans un StackPane (comme mainContainer)
    public static void showSuccess(StackPane root, String message) {
        showFromPane(root, message, Color.web("#00b894")); // vert clair
    }

    public static void showError(StackPane root, String message) {
        showFromPane(root, message, Color.web("#d63031")); // rouge clair
    }

    private static void showFromPane(StackPane root, String message, Color bgColor) {
        Label label = new Label(message);
        label.setStyle("""
            -fx-text-fill: white;
            -fx-padding: 10px 20px;
            -fx-font-size: 14px;
            -fx-background-radius: 15px;
            -fx-font-weight: bold;
            """);
        label.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(bgColor, new javafx.scene.layout.CornerRadii(15), null)));

        StackPane toastContainer = new StackPane(label);
        toastContainer.setOpacity(0);
        StackPane.setMargin(toastContainer, new javafx.geometry.Insets(10));
        root.getChildren().add(toastContainer);

        // Animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> root.getChildren().remove(toastContainer));

        new javafx.animation.SequentialTransition(fadeIn, pause, fadeOut).play();
    }

}
