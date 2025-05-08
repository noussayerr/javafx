package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import org.example.model.giphy.GifData;
import org.example.model.giphy.GiphyResponse;
import org.example.services.GiphyService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GifController {
    @FXML private FlowPane gifContainer;
    @FXML private TextField searchField;

    private final GiphyService giphyService = new GiphyService();
    private final Random random = new Random();
    private final Map<String, String> gifCache = new HashMap<>();

    // Add default trending search terms
    private static final String[] TRENDING_CATEGORIES = {
            "funny", "reactions", "memes", "animals",
            "gaming", "celebrations", "sports", "tv"
    };

    @FXML
    private void initialize() {
        // Load trending GIFs when controller starts
        loadTrendingGifs();
    }

    @FXML
    private void searchGifs() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadTrendingGifs();
            return;
        }

        try {
            // Add random offset for variety
            int offset = random.nextInt(20);
            GiphyResponse response = giphyService.searchGifs(query, 10, offset);
            displayGifs(response);
            cacheGifs(response); // Cache the results
        } catch (IOException e) {
            showError("Search failed. Showing cached results if available.");
            showCachedGifs(query);
        }
    }

    @FXML
    private void loadTrendingGifs() {
        try {
            // Get random trending category
            String category = TRENDING_CATEGORIES[random.nextInt(TRENDING_CATEGORIES.length)];
            int offset = random.nextInt(20);

            GiphyResponse response = giphyService.getTrendingGifs(10);
            displayGifs(response);
            cacheGifs(response); // Cache the results
        } catch (IOException e) {
            showError("Couldn't load trending GIFs. Showing cached results.");
            showRandomCachedGifs();
        }
    }

    private void displayGifs(GiphyResponse response) {
        gifContainer.getChildren().clear();

        for (GifData gif : response.getData()) {
            ImageView imageView = createGifView(gif);
            gifContainer.getChildren().add(imageView);
        }
    }

    private ImageView createGifView(GifData gif) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        try {
            String gifUrl = gif.getImages().getFixedHeight().getUrl();
            imageView.setImage(new Image(gifUrl));

            // Add hover effect
            imageView.setOnMouseEntered(e -> {
                imageView.setFitWidth(220);
                imageView.setFitHeight(220);
            });

            imageView.setOnMouseExited(e -> {
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
            });

        } catch (Exception e) {
            imageView.setImage(new Image("/images/error.png")); // Default error image
        }

        return imageView;
    }

    private void cacheGifs(GiphyResponse response) {
        for (GifData gif : response.getData()) {
            String cacheKey = String.valueOf(response.getMeta());
            gifCache.put(cacheKey, gif.getImages().getFixedHeight().getUrl());
        }
    }

    private void showCachedGifs(String query) {
        gifContainer.getChildren().clear();
        gifCache.entrySet().stream()
                .filter(entry -> entry.getKey().contains(query))
                .limit(10)
                .forEach(entry -> {
                    ImageView imageView = new ImageView(entry.getValue());
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(200);
                    gifContainer.getChildren().add(imageView);
                });
    }

    private void showRandomCachedGifs() {
        gifContainer.getChildren().clear();
        gifCache.values().stream()
                .limit(10)
                .forEach(url -> {
                    ImageView imageView = new ImageView(url);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(200);
                    gifContainer.getChildren().add(imageView);
                });
    }

    private void showError(String message) {
        // You could implement a proper error display here
        System.err.println(message);
    }
}