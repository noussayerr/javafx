package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.example.entity.Jeux;
import org.example.entity.Score;
import org.example.entity.User;
import org.example.model.giphy.GiphyResponse;
import org.example.services.GiphyService;
import org.example.services.ServiceJeux;
import org.example.services.ServiceScore;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public abstract class BaseGameController {

    protected int score = 0;
    protected int totalRounds = 3;
    protected int roundsPlayed = 0;
    protected Map<String, List<String>> wordPool;

    protected StackPane gifContainer;
    protected final GiphyService giphyService = new GiphyService();
    protected int previousHighScore = 0;

    // GIF-related constants
    protected static final String[] CELEBRATION_TERMS = {
            "celebration", "winner", "congratulations", "happy dance",
            "fireworks", "party", "success", "trophy"
    };

    protected static final String[] SAD_TERMS = {
            "sad", "game over", "try again", "disappointed",
            "oh no", "failure", "losing", "cry"
    };

    protected final Random random = new Random();


    protected int loadHighScore(String gameName, Label highScoreLabel) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            ServiceJeux serviceJeux = new ServiceJeux();
            Jeux foundGame = serviceJeux.getByNom(gameName);

            ServiceScore serviceScore = new ServiceScore();
            Score existingScore = serviceScore.findByUserAndGame(currentUser.getId(), foundGame.getId());

            if (existingScore != null) {
                int highScore = existingScore.getHighScore();
                highScoreLabel.setText("🏆 High Score: " + highScore);
                animateHighScore(highScoreLabel);
                return highScore;  // Return the high score value
            } else {
                highScoreLabel.setText("No high score yet.");
                return 0;  // Return 0 if no score exists
            }
        } catch (Exception e) {
            highScoreLabel.setText("?");
            e.printStackTrace();
            return 0;  // Return 0 on error
        }
    }

    protected void saveScore(String gameName, Label feedbackLabel,int score) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();

            ServiceJeux serviceJeux = new ServiceJeux();
            Jeux foundGame = serviceJeux.getByNom(gameName);

            ServiceScore scoreService = new ServiceScore();
            Score existingScore = scoreService.findByUserAndGame(currentUser.getId(), foundGame.getId());

            if (existingScore != null) {
                if (score > existingScore.getHighScore()) {
                    existingScore.setHighScore(score);
                    scoreService.modifier(existingScore);
                    feedbackLabel.setText("🔄 Score Updated!");
                } else {
                    feedbackLabel.setText("✅ You already have a better score!");
                }
            } else {
                Score newScore = new Score();
                newScore.setHighScore(score);
                newScore.setUser(currentUser);
                newScore.setJeux(foundGame);
                scoreService.ajouter(newScore);
                feedbackLabel.setText("🎯 New Score Saved!");
            }
        } catch (Exception e) {
            feedbackLabel.setText("❌ Failed to save score.");
            e.printStackTrace();
        }
    }

    private void animateHighScore(Label label) {
        ScaleTransition st = new ScaleTransition(Duration.millis(400), label);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.4);
        st.setToY(1.4);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }
    protected void loadWordsFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream is = getClass().getResourceAsStream("/org/example/data/words.json");
            if (is != null) {
                wordPool = mapper.readValue(is, new com.fasterxml.jackson.core.type.TypeReference<Map<String, List<String>>>() {});
                System.out.println("Successfully loaded words.json from resources");
                return;
            }
        } catch (IOException e) {
            System.err.println("Error loading from resources: " + e.getMessage());
        }

        try {
            File file = new File("src/main/resources/org/example/data/words.json");
            if (file.exists()) {
                wordPool = mapper.readValue(file, new com.fasterxml.jackson.core.type.TypeReference<Map<String, List<String>>>() {});
                System.out.println("Successfully loaded words.json from file system");
                return;
            }
        } catch (IOException e) {
            System.err.println("Error loading from file: " + e.getMessage());
        }

        System.err.println("Failed to load words.json. Using fallback words.");
        wordPool = new HashMap<>();
        wordPool.put("easy", Arrays.asList("winter", "castle", "planet", "flower"));
        wordPool.put("medium", Arrays.asList("adventure", "beautiful", "mountain", "elephant"));
        wordPool.put("hard", Arrays.asList("extravaganza", "hippopotamus", "kaleidoscope", "magnificent"));
    }
    protected void setGifContainer(StackPane container) {
        this.gifContainer = container;
    }

    protected void showCelebrationGif() {
        try {
            String term = CELEBRATION_TERMS[random.nextInt(CELEBRATION_TERMS.length)];
            int offset = random.nextInt(50);
            GiphyResponse response = giphyService.searchGifs(term, 1, offset);
            displayGif(response.getData().get(0).getImages().getFixedHeight().getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showSadGif() {
        try {
            String term = SAD_TERMS[random.nextInt(SAD_TERMS.length)];
            int offset = random.nextInt(50);
            GiphyResponse response = giphyService.searchGifs(term, 1, offset);
            displayGif(response.getData().get(0).getImages().getFixedHeight().getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void displayGif(String gifUrl) {
        if (gifContainer == null) return;

        javafx.application.Platform.runLater(() -> {
            gifContainer.getChildren().clear();
            ImageView gifView = new ImageView(gifUrl);
            gifView.setFitWidth(300);
            gifView.setFitHeight(300);
            gifView.setPreserveRatio(true);
            gifContainer.getChildren().add(gifView);

            // Auto-hide after 3 seconds
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() ->
                                    gifContainer.getChildren().clear());
                        }
                    },
                    3000
            );
        });
    }
}
