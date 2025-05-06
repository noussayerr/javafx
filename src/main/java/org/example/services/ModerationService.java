package org.example.services;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModerationService {
    private static final String API_KEY = "AIzaSyC_xpErjX2ZMOkUBksRiQwqMdLGQ-FsDY4";

    public static boolean isToxicComment(String text) {
        try {
            String jsonInput = "{\n" +
                    "  \"comment\": {\"text\": \"" + text.replace("\"", "\\\"") + "\"},\n" +
                    "  \"languages\": [\"en\"],\n" +
                    "  \"requestedAttributes\": {\"TOXICITY\": {}}\n" +
                    "}";

            URL url = new URL("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + API_KEY);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(jsonInput.getBytes("utf-8"));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            JSONObject json = new JSONObject(response.toString());
            System.out.println("📦 JSON complet :\n" + json.toString(2));  // <-- Affiche TOUT

            double score = json.getJSONObject("attributeScores")
                    .getJSONObject("TOXICITY")
                    .getJSONObject("summaryScore")
                    .getDouble("value");

            System.out.println("🧠 TOXICITY SCORE: " + score);

            return score >= 0.5;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l’appel à Perspective :");
            e.printStackTrace();
            return false;
        }
    }

}
