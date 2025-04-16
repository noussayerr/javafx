package org.example.controller;

import org.example.dao.CategorieDAO;
import org.example.entity.Evenement;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;

public class EvenementDetailController {

    @FXML private ImageView imageView;
    @FXML private Label categorieLabel;
    @FXML private Label nomLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label heureDebutLabel;
    @FXML private Label heureFinLabel;
    @FXML private Label lieuLabel;
    @FXML private Label prixLabel;
    @FXML private Rectangle bgRect;

    private Timeline bgAnimation;

    public void initialize() {
        setupBackgroundAnimation();
    }

    private void setupBackgroundAnimation() {
        // ✅ Corrigé : liaison avec le parent si c'est un Region
        if (bgRect.getParent() instanceof Region parent) {
            bgRect.widthProperty().bind(parent.widthProperty());
            bgRect.heightProperty().bind(parent.heightProperty());
        }

        // ✅ Dégradé animé (rose → bleu → violet → rose)
        bgAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bgRect.fillProperty(), Color.web("#FF9A9E"))),
                new KeyFrame(Duration.seconds(10),
                        new KeyValue(bgRect.fillProperty(), Color.web("#A1C4FD"))),
                new KeyFrame(Duration.seconds(20),
                        new KeyValue(bgRect.fillProperty(), Color.web("#C2AFF0"))),
                new KeyFrame(Duration.seconds(30),
                        new KeyValue(bgRect.fillProperty(), Color.web("#FF9A9E")))
        );

        bgAnimation.setCycleCount(Timeline.INDEFINITE);
        bgAnimation.setAutoReverse(true);
        bgAnimation.play();
    }

    public void setEvenement(Evenement evenement) {
        try {
            // Récupérer les données
            CategorieDAO categorieDAO = new CategorieDAO();
            String nomCategorie = categorieDAO.getNomCategorieById(evenement.getCategoryId());

            categorieLabel.setText(nomCategorie);
            nomLabel.setText(evenement.getNom());
            descriptionLabel.setText(evenement.getDescription());
            dateLabel.setText(evenement.getDate().toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH'h'mm");
            heureDebutLabel.setText(evenement.getHeureDebut().format(formatter));
            heureFinLabel.setText(evenement.getHeureFin().format(formatter));
            lieuLabel.setText(evenement.getLieu());
            prixLabel.setText(evenement.getPrix() + " TND");

            // Charger l'image
            String imageName = evenement.getImage();
            if (imageName == null || imageName.trim().isEmpty()) {
                throw new Exception("Aucune image définie !");
            }

            // Nettoyer le nom de l’image
            imageName = imageName.replaceAll("[^a-zA-Z0-9._-]", "_");

            URL imageUrl = getClass().getResource("/images/" + imageName);
            if (imageUrl == null) throw new Exception("Image introuvable : " + imageName);

            Image image = new Image(imageUrl.toExternalForm());
            imageView.setImage(image);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), imageView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            System.out.println("⚠️ Erreur dans setEvenement : " + e.getMessage());
            try {
                // Toujours afficher une image même si tout échoue
                Image defaultImage = new Image(getClass().getResource("/images/default-event.jpg").toExternalForm());
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                System.out.println("❌ Image par défaut manquante !");
            }
        }
    }


    public void stopAnimations() {
        if (bgAnimation != null) {
            bgAnimation.stop();
        }
    }
}
