package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entity.Apprenant;
import org.example.entity.Channel;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.services.ServiceMessage;
import org.example.utils.SessionManager;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ChannelMessagesController {

    @FXML
    private Label titleLabel;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button closeButton;

    private ServiceMessage serviceMessage = new ServiceMessage();
    private Channel channel;
    private User currentApprenant;
    private Clip alertClip;


    // Liste des mots interdits (à adapter selon vos besoins)
    private static final String[] BAD_WORDS = {
            "merde", "putain", "connard", "salope", "nique",
            "fuck", "shit", "bitch", "asshole",
            "كلب", "عاهر" // mots arabes inappropriés
    };

    public void setChannel(Channel channel) {

        this.channel = channel;
        this.currentApprenant = SessionManager.getInstance().getCurrentUser();
        if (this.currentApprenant == null) {
            redirectToLogin();
            return;
        }
        titleLabel.setText("Discussion: " + channel.getName());
        loadMessages();

        // Intégration du code pour la détection des mots interdits
        messageInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (containsBadWords(newValue)) {
                showBadWordAlert();
                highlightBadWords();
            }
        });
    }

    private void loadMessages() {
        try {
            List<Message> messages = serviceMessage.getMessagesByChannel(channel.getId());
            messagesContainer.getChildren().clear();

            if (messages.isEmpty()) {
                Label noMessagesLabel = new Label("Aucun message dans ce channel.");
                noMessagesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7a7a7a;");
                messagesContainer.getChildren().add(noMessagesLabel);
            } else {
                for (Message message : messages) {
                    VBox messageCard = createMessageCard(message);
                    messagesContainer.getChildren().add(messageCard);

                    // Apply fade-in animation
                    messageCard.setOpacity(0);
                    FadeTransition fade = new FadeTransition(Duration.millis(300), messageCard);
                    fade.setToValue(1);
                    fade.setDelay(Duration.millis(messagesContainer.getChildren().indexOf(messageCard) * 50));
                    fade.play();
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des messages", e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createMessageCard(Message message) {
        VBox card = new VBox(5);
        boolean isCurrentUser = currentApprenant != null && message.getApprenant() != null &&
                message.getApprenant().getId() == currentApprenant.getId();

        // Style basé sur l'utilisateur courant
        String backgroundColor = isCurrentUser ? "#4a90e2" : "#e4e7eb";
        String alignment = isCurrentUser ? "-fx-alignment: center-right;" : "-fx-alignment: center-left;";
        card.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 1); " +
                alignment);
        card.setPrefWidth(500);
        card.setMaxWidth(500);

        // Contenu du message existant
        String senderName = message.getApprenant() != null
                ? message.getApprenant().getPrenom() + " " + message.getApprenant().getNom()
                : "Utilisateur inconnu";
        Label senderLabel = new Label(senderName);
        senderLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + (isCurrentUser ? "#ffffff" : "#3a4a6d") + ";");

        Label contentLabel = new Label(message.getContent());
        contentLabel.setStyle("-fx-font-size: 14px; " +
                "-fx-text-fill: " + (isCurrentUser ? "#ffffff" : "#333") + ";");
        contentLabel.setWrapText(true);

        Label timestampLabel = new Label(message.getCreatedAt().toString());
        timestampLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (isCurrentUser ? "#d1e7ff" : "#7a7a7a") + ";");

        // Bouton de lecture vocale
        Button readButton = new Button("Lire");
        readButton.setStyle("-fx-background-color: " + (isCurrentUser ? "#3a7bd5" : "#a5b1c2") + "; " +
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 10;");
        readButton.setOnAction(e -> speakMessage(message.getContent()));

        HBox bottomBox = new HBox(10, timestampLabel, readButton);
        bottomBox.setStyle("-fx-alignment: center-right;");

        card.getChildren().addAll(senderLabel, contentLabel, bottomBox);
        return card;
    }

    private void speakMessage(String text) {
        try {
            String language = detectLanguage(text);
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows - utilise SAPI avec la langue détectée
                String command = "powershell.exe -Command \"" +
                        "Add-Type -AssemblyName System.Speech; " +
                        "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        getWindowsLanguageSwitch(language) +
                        "$speak.Speak('" + escapeForWindows(text) + "');\"";
                Runtime.getRuntime().exec(command);
            } else if (os.contains("mac")) {
                // Mac - utilise la commande 'say' avec la voix appropriée
                String voice = getMacVoice(language);
                if (!voice.isEmpty()) {
                    Runtime.getRuntime().exec(new String[]{"say", "-v", voice, text});
                } else {
                    Runtime.getRuntime().exec(new String[]{"say", text});
                }
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Linux - utilise espeak avec la langue appropriée
                String langParam = getEspeakLanguage(language);
                Runtime.getRuntime().exec(new String[]{"espeak", "-v", langParam, text});
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Lecture vocale", getErrorMessage());
        }
    }

    private String detectLanguage(String text) {
        // Détection simple basée sur les caractères
        if (text.matches(".*[\u0600-\u06FF].*")) {
            return "ar"; // Arabe
        } else if (text.matches(".*[a-zA-Z].*") &&
                text.toLowerCase().matches(".*\\b(the|and|to|of|in|that|for)\\b.*")) {
            return "en"; // Anglais
        }
        return "fr"; // Français par défaut
    }

    private String getWindowsLanguageSwitch(String language) {
        switch (language) {
            case "en":
                return "$speak.SelectVoice('Microsoft David Desktop');"; // Voix anglaise
            case "ar":
                return "$speak.SelectVoice('Microsoft Naayf');"; // Voix arabe
            default:
                return "$speak.SelectVoice('Microsoft Hortense Desktop');"; // Voix française
        }
    }

    private String getMacVoice(String language) {
        switch (language) {
            case "en":
                return "Alex"; // Voix anglaise
            case "ar":
                return "Maged"; // Voix arabe (disponible sur les versions récentes)
            default:
                return "Amelie"; // Voix française
        }
    }

    private String getEspeakLanguage(String language) {
        switch (language) {
            case "en":
                return "en"; // Anglais
            case "ar":
                return "ar"; // Arabe
            default:
                return "fr"; // Français
        }
    }


    private String escapeForWindows(String text) {
        return text.replace("'", "''")
                .replace("\"", "\\\"")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String getErrorMessage() {
        String os = System.getProperty("os.name").toLowerCase();
        String languageError = "\n\nAssurez-vous que les voix pour les langues sont installées:";

        if (os.contains("win")) {
            return "Assurez-vous que:\n" +
                    "- PowerShell est disponible\n" +
                    "- .NET Framework est installé" +
                    languageError +
                    "\n- Français: Microsoft Hortense Desktop" +
                    "\n- Anglais: Microsoft David Desktop" +
                    "\n- Arabe: Microsoft Naayf";
        } else if (os.contains("mac")) {
            return "Assurez-vous que:\n" +
                    "- La commande 'say' est disponible" +
                    languageError +
                    "\n- Français: Amelie" +
                    "\n- Anglais: Alex" +
                    "\n- Arabe: Maged (macOS 10.15+)";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "Assurez-vous que:\n" +
                    "- espeak est installé: sudo apt-get install espeak" +
                    languageError +
                    "\n- Français: espeak -v fr" +
                    "\n- Anglais: espeak -v en" +
                    "\n- Arabe: espeak -v ar";
        }
        return "Synthèse vocale multilingue non supportée sur votre système";
    }

    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();

        // Vérification des mots interdits
        if (containsBadWords(content)) {
            showBadWordAlert();
            return;
        }

        if (content.isEmpty()) {
            showAlert("Erreur", "Message vide", "Veuillez entrer un message.");
            return;
        }

        if (currentApprenant == null) {
            showAlert("Erreur", "Non connecté", "Vous devez être connecté pour envoyer un message.");
            redirectToLogin();
            return;
        }

        Message message = new Message(content, channel, currentApprenant);
        try {
            serviceMessage.ajouter(message);
            messageInput.clear();
            loadMessages(); // Refresh the message list
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'envoi du message", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean containsBadWords(String text) {
        String lowerText = text.toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (lowerText.contains(badWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void showBadWordAlert() {
        // Jouer le son d'alerte
        playAlertSound();

        // Trouver le(s) mot(s) interdit(s) dans le texte
        String text = messageInput.getText().toLowerCase();
        StringBuilder badWordsFound = new StringBuilder();

        for (String badWord : BAD_WORDS) {
            if (text.contains(badWord.toLowerCase())) {
                if (badWordsFound.length() > 0) {
                    badWordsFound.append(", ");
                }
                // Masquer le mot avec des étoiles (ou flouter)
                String maskedWord = maskBadWord(badWord);
                badWordsFound.append(maskedWord);
            }
        }

        // Création de l'alerte visuelle
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("⚠ Alerte de contenu");
        alert.setHeaderText("Mot(s) inapproprié(s) détecté(s) !");
        alert.setContentText("Votre message contient le(s) terme(s) non autorisé(s): " + badWordsFound.toString() +
                "\nVeuillez modifier votre texte.");

        // Style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/styles/alert.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("bad-word-alert");

        // Animation
        FadeTransition ft = new FadeTransition(Duration.millis(1000), dialogPane);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.setCycleCount(3);
        ft.setAutoReverse(true);

        alert.showAndWait();
        ft.play();
    }

    private String maskBadWord(String badWord) {
        // Version avec étoiles (*****)
        // return "*".repeat(badWord.length());

        // Version avec premier caractère + étoiles (p****)
        return badWord.charAt(0) + "*".repeat(badWord.length() - 1);

        // Version floutée (peu claire)
        // return badWord.substring(0, 1) + "~" + badWord.substring(badWord.length() - 1);
    }

    private void playAlertSound() {
        try {
            // Essayer deux méthodes de chargement différentes
            java.net.URL soundURL = getClass().getResource("/sounds/alert.wav");

            if (soundURL == null) {
                // Essayer une autre méthode si la première échoue
                soundURL = ClassLoader.getSystemResource("sounds/alert.wav");
                if (soundURL == null) {
                    System.err.println("Fichier audio non trouvé à aucun des emplacements testés");
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Format audio non supporté: " + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
        } catch (LineUnavailableException e) {
            System.err.println("Ligne audio non disponible: " + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier audio: " + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Erreur inattendue avec le son: " + e.getMessage());
            Toolkit.getDefaultToolkit().beep();
        }
    }
    private void highlightBadWords() {
        String text = messageInput.getText().toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (text.contains(badWord.toLowerCase())) {
                // Animation de vibration du champ de texte
                TranslateTransition tt = new TranslateTransition(Duration.millis(50), messageInput);
                tt.setFromX(0);
                tt.setByX(10);
                tt.setCycleCount(6);
                tt.setAutoReverse(true);
                tt.play();

                // Changement de couleur temporaire
                messageInput.setStyle("-fx-background-color: #ffebee; -fx-border-color: #ff6b6b;");
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> messageInput.setStyle(""));
                pause.play();
                break;
            }
        }
    }



    @FXML
    private void handleClose() {
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

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion");
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setScene(new Scene(root));
            loginStage.show();

            // Close the current messages window
            Stage currentStage = (Stage) closeButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors du chargement de la page de connexion", e.getMessage());
            e.printStackTrace();
        }
    }
}