package org.example.services;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationService {

    public static void showFloatingNotification(StackPane mainContainer, String title, String message) {
        HBox notificationBox = new HBox(10);
        notificationBox.setAlignment(Pos.CENTER_LEFT);
        notificationBox.setPadding(new Insets(10));
        notificationBox.setMaxWidth(320);
        notificationBox.setStyle("""
            -fx-background-color: #ffffff;
            -fx-border-color: #ccc;
            -fx-border-radius: 12;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0.2, 0, 3);
        """);

        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 20px;");

        VBox textContainer = new VBox();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 13px;");
        textContainer.getChildren().addAll(titleLabel, messageLabel);

        notificationBox.getChildren().addAll(icon, textContainer);

        StackPane.setAlignment(notificationBox, Pos.TOP_CENTER);
        mainContainer.getChildren().add(notificationBox);

        notificationBox.setTranslateY(-200);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), notificationBox);
        slideIn.setFromY(-200);
        slideIn.setToY(30);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationBox);
        slideOut.setFromY(30);
        slideOut.setToY(-200);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        slideOut.setOnFinished(e -> mainContainer.getChildren().remove(notificationBox));

        SequentialTransition seq = new SequentialTransition(slideIn, pause, slideOut);
        seq.play();
    }
}
