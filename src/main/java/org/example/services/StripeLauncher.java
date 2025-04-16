package org.example.services;
import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StripeLauncher {

    public static void openStripeSession(String eventName, int priceInCents) {
        try {
            String urlPhp = "http://localhost/stripe-api/stripe-checkout.php?event="
                    + eventName.replace(" ", "+") + "&amount=" + priceInCents;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlPhp))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                            String checkoutUrl = json.get("url").getAsString();
                            Desktop.getDesktop().browse(new URI(checkoutUrl));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
