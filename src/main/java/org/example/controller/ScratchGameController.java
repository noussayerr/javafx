package org.example.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class ScratchGameController {

    @FXML private StackPane card1, card2, card3;
    @FXML private Label resultLabel;

    @FXML
    private StackPane mainGamePane;


    private final String[] SYMBOLS = {"🍀", "⭐", "💎", "🎁", "🔔"};
    private final Random random = new Random();
    private String[] currentSymbols = new String[3];

    public void initialize() {
        resultLabel.setText("");

        // 🧠 40% de chance d’avoir 3 symboles identiques
        boolean shouldWin = random.nextDouble() < 0.4;

        if (shouldWin) {
            String winningSymbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            currentSymbols[0] = winningSymbol;
            currentSymbols[1] = winningSymbol;
            currentSymbols[2] = winningSymbol;
        } else {
            // S’assurer que les 3 sont différents
            currentSymbols[0] = SYMBOLS[random.nextInt(SYMBOLS.length)];
            do {
                currentSymbols[1] = SYMBOLS[random.nextInt(SYMBOLS.length)];
            } while (currentSymbols[1].equals(currentSymbols[0]));

            do {
                currentSymbols[2] = SYMBOLS[random.nextInt(SYMBOLS.length)];
            } while (currentSymbols[2].equals(currentSymbols[0]) || currentSymbols[2].equals(currentSymbols[1]));
        }

        // 🃏 Initialisation des cartes avec les bons symboles
        setupCard(card1, 0, 0);
        setupCard(card2, 1, 150);
        setupCard(card3, 2, 300);
    }

    private void setupCard(StackPane card, int index, int delayMs) {
        card.getChildren().clear();
        Text symbol = new Text(currentSymbols[index]);
        symbol.setFont(Font.font("Arial", 38));
        symbol.setFill(Color.web("#8e44ad"));
        symbol.setVisible(false);

        StackPane overlay = new StackPane();
        overlay.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #dcd6f7, #f5f5ff);
            -fx-background-radius: 25;
        """);
        overlay.setOpacity(1);
        overlay.setPrefSize(120, 140);

        card.getChildren().addAll(symbol, overlay);

        FadeTransition appear = new FadeTransition(Duration.millis(600), card);
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.setDelay(Duration.millis(delayMs));
        appear.play();

        card.setOnMouseClicked(e -> {
            if (overlay.isVisible()) {
                playShake(card);
                playGlow(card);
                playDustEffect(card); // 💨 effet poussière ajouté ici

                FadeTransition fade = new FadeTransition(Duration.millis(400), overlay);
                fade.setFromValue(1);
                fade.setToValue(0);
                fade.setOnFinished(ev -> {
                    overlay.setVisible(false);
                    symbol.setVisible(true);
                    checkWin();
                });
                fade.play();
            }
        });

    }

    private void checkWin() {
        if (card1.getChildren().get(1).isVisible() ||
                card2.getChildren().get(1).isVisible() ||
                card3.getChildren().get(1).isVisible()) {
            return;
        }

        boolean win = currentSymbols[0].equals(currentSymbols[1]) && currentSymbols[1].equals(currentSymbols[2]);
        if (win) {
            resultLabel.setText("🎉 Bravo ! Vous avez gagné !");
            launchEmojiExplosion(card2);
            playVictorySound();
        } else {
            resultLabel.setText("❌ Essayez encore !");
            shakeLabel(resultLabel);
            playSadParticles(card2);
        }
    }

    @FXML
    public void rejouer() {
        initialize();
    }

    private void playShake(StackPane card) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(120), card);
        shake.setFromX(-5);
        shake.setToX(5);
        shake.setAutoReverse(true);
        shake.setCycleCount(4);
        shake.play();
    }

    private void playGlow(StackPane card) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#8e44ad"));
        glow.setRadius(25);
        card.setEffect(glow);

        Timeline glowOut = new Timeline(new KeyFrame(Duration.millis(700),
                new KeyValue(glow.radiusProperty(), 0),
                new KeyValue(glow.colorProperty(), Color.TRANSPARENT)));
        glowOut.setOnFinished(e -> card.setEffect(null));
        glowOut.play();
    }

    private void shakeLabel(Label label) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), label);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void launchEmojiExplosion(StackPane origin) {
        String[] emojis = {"💜", "💖", "🎉", "✨", "⭐", "🔮"};
        Random rand = new Random();

        for (int i = 0; i < 12; i++) {
            Text boom = new Text(emojis[rand.nextInt(emojis.length)]);
            boom.setFont(Font.font(24));
            boom.setFill(Color.web("#8e44ad"));
            boom.setTranslateX(0);
            boom.setTranslateY(0);
            origin.getChildren().add(boom);

            double dx = rand.nextDouble() * 100 - 50;
            double dy = rand.nextDouble() * -80;

            TranslateTransition move = new TranslateTransition(Duration.seconds(1.2), boom);
            move.setByX(dx);
            move.setByY(dy);

            FadeTransition fade = new FadeTransition(Duration.seconds(1.2), boom);
            fade.setFromValue(1);
            fade.setToValue(0);

            ParallelTransition animation = new ParallelTransition(move, fade);
            animation.setOnFinished(e -> origin.getChildren().remove(boom));
            animation.play();
        }
    }

    private void playVictorySound() {
        try {
            String soundPath = getClass().getResource("/sounds/win.mp3").toExternalForm();
            javafx.scene.media.AudioClip clip = new javafx.scene.media.AudioClip(soundPath);
            clip.setVolume(0.5);
            clip.play();
        } catch (Exception e) {
            System.out.println("🎵 Erreur lecture son : " + e.getMessage());
        }
    }

    @FXML
    private void retour() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), mainGamePane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.5), mainGamePane);
        slideDown.setByY(40);

        RotateTransition rotate = new RotateTransition(Duration.seconds(0.5), mainGamePane);
        rotate.setByAngle(3);

        ParallelTransition closeAnim = new ParallelTransition(fadeOut, slideDown, rotate);
        closeAnim.setOnFinished(e -> {
            Stage stage = (Stage) mainGamePane.getScene().getWindow();
            stage.close();
        });

        closeAnim.play();
    }

    private void playDustEffect(StackPane card) {
        Random rand = new Random();
        for (int i = 0; i < 12; i++) {
            Label particle = new Label("•");
            particle.setStyle("-fx-text-fill: #8e44ad; -fx-font-size: " + (6 + rand.nextInt(6)) + "px;");

            particle.setOpacity(0.8);
            card.getChildren().add(particle);

            double angle = rand.nextDouble() * 2 * Math.PI;
            double radius = 30 + rand.nextDouble() * 20;
            double dx = Math.cos(angle) * radius;
            double dy = Math.sin(angle) * radius;

            TranslateTransition move = new TranslateTransition(Duration.seconds(0.6), particle);
            move.setByX(dx);
            move.setByY(dy);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.6), particle);
            fade.setFromValue(0.8);
            fade.setToValue(0.0);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.6), particle);
            scale.setFromX(1.2);
            scale.setFromY(1.2);
            scale.setToX(0.4);
            scale.setToY(0.4);

            ParallelTransition anim = new ParallelTransition(move, fade, scale);
            anim.setOnFinished(e -> card.getChildren().remove(particle));
            anim.play();
        }
    }
    private void playSadParticles(StackPane centerPane) {
        Random rand = new Random();

        for (int i = 0; i < 10; i++) {
            Label tear = new Label("💧");
            tear.setStyle("-fx-font-size: 20px;");
            tear.setTranslateX(rand.nextInt(40) - 20); // Léger décalage horizontal
            tear.setTranslateY(0);
            tear.setOpacity(0.7);

            centerPane.getChildren().add(tear);

            TranslateTransition fall = new TranslateTransition(Duration.seconds(1.2), tear);
            fall.setByY(120 + rand.nextInt(30));

            FadeTransition fade = new FadeTransition(Duration.seconds(1.2), tear);
            fade.setFromValue(0.7);
            fade.setToValue(0.0);

            ParallelTransition anim = new ParallelTransition(fall, fade);
            anim.setOnFinished(e -> centerPane.getChildren().remove(tear));
            anim.play();
        }
    }


}
