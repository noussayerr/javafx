package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.entity.Promotion;
import org.example.services.ServicePromotion;

import java.time.LocalDate;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML (Assurez-vous que le chemin est correct)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));

        Scene scene = new Scene(loader.load(), 400, 500);
        primaryStage.setTitle("Inscription Apprenant");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    // La méthode main pour démarrer l'application
    public static void main(String[] args) {
        launch();
        // Démarrer l'application JavaFX
        /*try {
            ServicePromotion service = new ServicePromotion();

            // 🔹 1. Ajouter une promotion
            Promotion promo1 = new Promotion();
            promo1.setTitre("Promo Printemps");
            promo1.setDescription("Réduction de printemps");
            promo1.setReduction(20);
            promo1.setDateDebut(LocalDate.of(2025, 4, 1));
            promo1.setDateFin(LocalDate.of(2025, 4, 30));

            service.ajouter(promo1);
            System.out.println("✅ Promotion ajoutée !");

            // 🔹 2. Afficher toutes les promotions
            List<Promotion> promotions = service.afficher();
            System.out.println("📋 Liste des promotions :");
            for (Promotion p : promotions) {
                System.out.println(p.getId() + " | " + p.getTitre() + " | " + p.getReduction() + "%");
            }

            // 🔹 3. Modifier la première promotion (par ID)
            if (!promotions.isEmpty()) {
                Promotion first = promotions.get(0);
                first.setTitre("Promo Printemps Modifiée");
                first.setReduction(25);
                service.modifier(first);
                System.out.println("✏️ Promotion modifiée !");
            }

            // 🔹 4. Récupérer une promotion par ID
            int idToFind = promotions.get(0).getId();
            Promotion found = service.getById(idToFind);
            System.out.println("🔍 Promotion trouvée : " + found.getTitre() + " - " + found.getReduction() + "%");

            // 🔹 5. Supprimer la promotion
            service.supprimer(idToFind);
            System.out.println("🗑️ Promotion supprimée avec succès !");

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }*/
    }
}
