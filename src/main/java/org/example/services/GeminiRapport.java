package org.example.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GeminiRapport {

    private final HttpClient httpClient;
    private final String apiKey = "AIzaSyCVQHI_ArRIWqOmqDwi0D1cC5kBKXq-gVI"; // 🔴 Replace with your actual API key
    private final Logger logger;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=";

    public GeminiRapport(HttpClient httpClient, Logger logger) {
        this.httpClient = httpClient;
        this.logger = logger != null ? logger : LoggerFactory.getLogger(GeminiRapport.class);
    }

    public String analyzeUserConnectivity(String name, int sessionCount, int interactionCount, LocalDateTime lastActivity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedLastActivity = lastActivity.format(formatter);

        String prompt = """
                You are an expert analyst specializing in user engagement and platform connectivity. 
                Your task is to analyze the provided user activity data and generate a concise report on the user's connectivity and engagement level on the platform.

                ### Instructions:
                - Analyze the user's connectivity based on:
                  - **Name**: %s
                  - **Number of sessions**: %d
                  - **Number of interactions**: %d
                  - **Last activity**: %s
                - Provide a professional assessment of the user's engagement level (e.g., highly active, moderately active, low activity, inactive).
                - Identify any potential issues (e.g., prolonged inactivity, low interaction relative to sessions).
                - Recommend actions to improve user engagement if applicable (e.g., re-engagement campaigns, feature suggestions).
                - If data is insufficient to make a clear assessment, state: **"Insufficient data for comprehensive analysis."**

                ### Guidelines:
                - Use a **professional and concise tone**.
                - Avoid assumptions not supported by the provided data.
                - Structure the response as a clear, readable report.
                - If the last activity is older than 30 days, flag the user as potentially inactive.
                - Consider a user with fewer than 5 sessions or 10 interactions in the last 30 days as having low engagement.

                ### Response Format:
                **User Connectivity Report for %s**  
                - **Engagement Level**: [Highly Active/Moderately Active/Low Activity/Inactive]  
                - **Analysis**: [Detailed analysis based on the data]  
                - **Recommendations**: [Specific recommendations or "None required" if highly active]  

                Analyze the provided data and generate the report accordingly.
                """.formatted(name, sessionCount, interactionCount, formattedLastActivity, name);

        String url = API_URL + apiKey;
        int maxRetries = 3;
        int retryDelaySeconds = 2;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String jsonPayload = """
                        {
                            "contents": [
                                {
                                    "parts": [
                                        {
                                            "text": "%s"
                                        }
                                    ]
                                }
                            ]
                        }
                        """.formatted(prompt.replace("\"", "\\\""));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                String responseBody = response.body();

                logger.info("Gemini API Response (Attempt: {}), Status: {}, Response: {}", attempt, statusCode, responseBody);

                if (responseBody.contains("\"candidates\"") && responseBody.contains("\"text\"")) {
                    String text = extractTextFromResponse(responseBody);
                    if (text != null) {
                        return text.trim();
                    }
                }

                logger.warn("Gemini API returned an unexpected response: {}", responseBody);

                if (attempt < maxRetries) {
                    Thread.sleep(retryDelaySeconds * 1000);
                    continue;
                }

                return "Unable to generate connectivity report at this time. Please try again later.";

            } catch (Exception e) {
                logger.error("Gemini API Exception (Attempt: {}): {}", attempt, e.getMessage(), e);

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelaySeconds * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("Retry interrupted", ie);
                    }
                    continue;
                }

                return "A technical error occurred. Please try again later.";
            }
        }

        return "Unable to process your request at this time.";
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            String marker = "\"text\": \"";
            int startIndex = responseBody.indexOf(marker) + marker.length();
            int endIndex = responseBody.indexOf("\"", startIndex);
            if (startIndex > marker.length() && endIndex > startIndex) {
                return responseBody.substring(startIndex, endIndex);
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to parse response: {}", e.getMessage());
            return null;
        }
    }
}