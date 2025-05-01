package org.example.services;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.model.giphy.GiphyResponse;
import org.example.model.giphy.GifData;

import java.io.IOException;

public class GiphyService {
    private static final String API_KEY = "bCU9InpjJsee01XpF9PKquurgUpcVBbE"; // Replace with your key
    private static final String BASE_URL = "https://api.giphy.com/v1/gifs/";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public GiphyResponse searchGifs(String query, int limit, int offset) throws IOException {
        String url = String.format("%ssearch?api_key=%s&q=%s&limit=%d&offset=%d&rating=g",
                BASE_URL, API_KEY, query, limit, offset);

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            return gson.fromJson(response.body().string(), GiphyResponse.class);
        }
    }

    public GiphyResponse getTrendingGifs(int limit) throws IOException {
        String url = String.format("%strending?api_key=%s&limit=%d",
                BASE_URL, API_KEY, limit);

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            return gson.fromJson(response.body().string(), GiphyResponse.class);
        }
    }
}