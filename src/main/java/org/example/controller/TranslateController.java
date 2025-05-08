package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.utils.Translator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.animation.ScaleTransition;
import javafx.scene.effect.Glow;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

public class TranslateController {

    @FXML
    private TextArea txtSource;
    @FXML
    private VBox rootVBox;

    @FXML
    private TextArea txtResult;

    @FXML
    private ComboBox<String> comboFrom;

    @FXML
    private ComboBox<String> comboTo;
    @FXML
    private Button btnTraduire;




    @FXML
    public void initialize() {
        comboFrom.getItems().addAll("en", "fr", "de", "es", "it", "ar");
        comboTo.getItems().addAll("en", "fr", "de", "es", "it", "ar");
        comboFrom.setValue("en");
        comboTo.setValue("fr");

        // ✅ Animation de glow et zoom
        btnTraduire.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            btnTraduire.setEffect(new Glow(0.5));
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btnTraduire);
            st.setToX(1.1);
            st.setToY(1.1);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        });

        btnTraduire.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            btnTraduire.setEffect(null);
        });
    }


    @FXML
    private void handleTranslate() {
        String from = comboFrom.getValue();
        String to = comboTo.getValue();
        String text = txtSource.getText().trim();

        if (from == null || to == null || text.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champ manquant");
            alert.setHeaderText(null);
            alert.setContentText("Merci de sélectionner les langues et saisir du texte.");
            alert.showAndWait();
            return;
        }

        try {
            String translated = Translator.traduire(text, from, to);
            txtResult.setText(translated);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de traduction");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private String translateText(String text, String source, String target) throws IOException {
        URL url = new URL("https://libretranslate.com/translate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = String.format(
                "{\"q\":\"%s\",\"source\":\"%s\",\"target\":\"%s\",\"format\":\"text\"}",
                text.replace("\"", "\\\""), source, target
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();

        // Extraire le texte traduit (simple extraction manuelle)
        String result = response.toString();
        int start = result.indexOf(":\"") + 2;
        int end = result.indexOf("\"}", start);
        return result.substring(start, end);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
