package org.example.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Translator {
    public static String traduire(String texte, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(texte, "UTF-8");
            String urlStr = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + sourceLang + "|" + targetLang;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Extraire le texte traduit (simplement avec String)
            String json = response.toString();
            int start = json.indexOf("\"translatedText\":\"") + 18;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (UnsupportedEncodingException e) {
            return "Erreur d'encodage : " + e.getMessage();
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String traduit = traduire("Bonjour", "fr", "en");
        System.out.println("Traduction : " + traduit);
    }
}
