package org.example.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class FaceVerificationService {

    private static final String FACEID_API_URL = "http://localhost:5000/faceid"; // Remplacez par l'URL de votre API Flask
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FaceVerificationService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Appelle la route /faceid de l'API Flask pour vérifier l'identité d'un utilisateur via reconnaissance faciale.
     *
     * @param email L'email de l'utilisateur à vérifier.
     * @return Une map contenant le statut de la vérification et le message associé.
     * @throws Exception Si une erreur survient lors de la requête ou du traitement de la réponse.
     */
    public Map<String, Object> verifyFace(String email) throws Exception {
        // Créer le payload JSON
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);

        // Convertir le payload en JSON
        String jsonPayload = objectMapper.writeValueAsString(payload);

        // Construire la requête POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FACEID_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Envoyer la requête et récupérer la réponse
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Parser la réponse JSON
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        // Vérifier le code de statut HTTP
        if (response.statusCode() == 200 && (boolean) responseBody.get("success")) {
            return responseBody; // Succès : {"success": true, "message": "..."}
        } else if (response.statusCode() == 200) {
            return responseBody; // Échec : {"success": false, "message": "..."}
        } else if (response.statusCode() == 404) {
            return Map.of("success", false, "message", responseBody.getOrDefault("message", "Utilisateur ou photo introuvable."));
        } else {
            return Map.of("success", false, "message", responseBody.getOrDefault("message", "Erreur serveur."));
        }
    }

    /**
     * Méthode utilitaire pour tester le service.
     */
    public static void main(String[] args) {
        FaceVerificationService service = new FaceVerificationService();
        try {
            Map<String, Object> result = service.verifyFace("test@example.com");
            System.out.println("Résultat: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}