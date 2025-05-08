package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Reclamation;
import org.example.services.ServiceReclamation;
import org.example.services.ServiceTransaction;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML
      /*  ServiceReclamation serviceReclamation = new ServiceReclamation();
        ServiceTransaction serviceTransaction = new ServiceTransaction();

        try {

            Apprenant currentApprenant = serviceTransaction.getApprenantById(4);

            for (int i = 1; i <= 6; i++) {
                Reclamation rec = new Reclamation();
                rec.setTitle("Problème en java"); // Titre contenant "physique"
                rec.setDescription("Ceci est un test de recommandation pour physique. Description n°" + i);
                rec.setEtat("en attente");
                rec.setApprenant(currentApprenant);

                serviceReclamation.ajouter(rec);
            }

            System.out.println("10 réclamations ajoutées avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/home.fxml"));
        Parent root = loader.load();

        // Configurer la scène
        Scene scene = new Scene(root, 1000, 600);

        // Configurer la fenêtre principale
        primaryStage.setTitle("Dyscover - Plateforme d'apprentissage");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1050);
        primaryStage.setMinHeight(700);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}