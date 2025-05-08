package org.example.services;

import okhttp3.*;

import java.io.IOException;

public class ServicePaiement {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://developers.flouci.com/api/generate_payment";

    public static String generatePayment(String appToken, String appSecret, String amount, String successLink, String failLink, String trackingId) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        String jsonBody = String.format(
                "{ \"app_token\": \"%s\", \"app_secret\": \"%s\", \"accept_card\": \"true\", \"amount\": \"%s\", \"success_link\": \"%s\", \"fail_link\": \"%s\", \"session_timeout_secs\": 1200, \"developer_tracking_id\": \"%s\" }",
                appToken, appSecret, amount, successLink, failLink, trackingId);

        RequestBody body = RequestBody.create(mediaType, jsonBody);

        Request request = new Request.Builder()
                .url(API_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();  // Return the response body if successful
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
}
