package org.example.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PredictService {

    private static final String PREDICT_API_URL = "http://127.0.0.1:5000/predict";

    /**
     * Calls the Flask /predict endpoint to predict user engagement (churn risk).
     *
     * @param sessionCount The number of user sessions.
     * @param daysSinceLastActivity Days since the user's last activity.
     * @param interactionsCount The number of user interactions.
     * @return A JSON string containing the prediction (e.g., {"prediction": 1}).
     * @throws Exception If an error occurs during the HTTP request.
     */
    public String predictChurn(int sessionCount, int daysSinceLastActivity, int interactionsCount) throws Exception {
        // Prepare the JSON payload
        String jsonInputString = String.format(
                "{\"sessionCount\": %d, \"daysSinceLastActivity\": %d, \"interactionsCount\": %d}",
                sessionCount, daysSinceLastActivity, interactionsCount);

        // Set up the HTTP connection
        URL url = new URL(PREDICT_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // Send the JSON payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read the response
        StringBuilder response = new StringBuilder();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } else {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        }

        conn.disconnect();
        return response.toString();
    }
}