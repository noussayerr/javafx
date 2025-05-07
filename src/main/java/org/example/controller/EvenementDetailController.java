package org.example.controller;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import org.example.utils.QRCodeGenerator;
import org.example.services.MailService;
import org.example.utils.Toast;
import org.example.services.MailService;
import org.example.utils.Toast;
import org.example.services.ServicePDF;




public class EvenementDetailController {
    private Evenement evenementActuel;
    private String emailUtilisateur;

    @FXML private ImageView imageView;
    @FXML private Label categorieLabel;
    @FXML private Label nomLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label heureDebutLabel;
    @FXML private Label heureFinLabel;
    @FXML private Label lieuLabel;
    @FXML private Rectangle bgRect;
    @FXML private StackPane mainContainer;
    private Timeline bgAnimation;

    public void initialize() {
        setupBackgroundAnimation();
        playAppearAnimation();
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
    private void playAppearAnimation() {
        mainContainer.setOpacity(0);
        mainContainer.setScaleX(0.9);
        mainContainer.setScaleY(0.9);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), mainContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), mainContainer);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition appear = new ParallelTransition(fade, scale);
        appear.play();
    }
    @FXML
    private void retour() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), mainContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.5), mainContainer);
        slideDown.setByY(40);

        RotateTransition rotate = new RotateTransition(Duration.seconds(0.5), mainContainer);
        rotate.setByAngle(3); // petite rotation pour l’effet

        ParallelTransition closeAnim = new ParallelTransition(fadeOut, slideDown, rotate);
        closeAnim.setOnFinished(e -> {
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.close();
        });

        closeAnim.play();
    }


    public void setEvenement(Evenement evenement , String email ) {
        try { // Récupérer les données
            this.evenementActuel = evenement;
            this.emailUtilisateur = email;
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

            // Charger l'image
            String imageName = evenement.getImage();
            if (imageName == null || imageName.trim().isEmpty()) {
                throw new Exception("Aucune image définie !");
            }

            // Nettoyer le nom de l’image


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
    @FXML
    private void reserver() {
        try {
            if (evenementActuel == null) {
                System.out.println("❌ Aucune donnée d'événement !");
                return;
            }

            // 📂 Vérifier et créer les dossiers QR et PDF
            File qrDirectory = new File("C:/Users/nourb/Desktop/qr/");
            if (!qrDirectory.exists()) {
                qrDirectory.mkdirs();
            }

            File pdfDirectory = new File("C:/Users/nourb/Desktop/pdf/");
            if (!pdfDirectory.exists()) {
                pdfDirectory.mkdirs();
            }

            // 📂 Chemins dynamiques
            String qrPath = "C:/Users/nourb/Desktop/qr/" + evenementActuel.getId() + "_ticket.png";
            String pdfPath = "C:/Users/nourb/Desktop/pdf/" + evenementActuel.getId() + "_ticket.pdf";

            // 📦 Génération du QR code
            String contenuQR = "https://eventili.webnode.fr/";
            QRCodeGenerator.generateQRCode(contenuQR, qrPath);

            // 📦 Génération du PDF événement
            ServicePDF.generateEvenementPDF(pdfPath, evenementActuel);

            // 📧 Sujet du mail
            String sujet = "🎫 Réservation confirmée : " + evenementActuel.getNom();


            // 💬 Message HTML dynamique
            String messageTexte = String.format("""
            Bonjour,<br><br>
            Votre réservation pour l'événement <strong>%s</strong> est confirmée.<br>
            📍 <strong>Lieu :</strong> %s<br>
            📅 <strong>Date :</strong> %s<br><br>
            Veuillez trouver votre <strong>QR code</strong> ci-dessous ainsi que votre <strong>fiche PDF</strong> en pièce jointe.<br><br>
            Merci pour votre confiance 💜
        """,
                    evenementActuel.getNom(),
                    evenementActuel.getLieu(),
                    evenementActuel.getDate().toString());

            // 📩 Adresse du destinataire
            String destinataire = "nourbrahem275@gmail.com"; // ou txtEmail.getText().trim()

            // ✅ Envoi du mail avec QR code + PDF attaché
            MailService.envoyerTicketCompletParMail(destinataire, sujet, messageTexte, qrPath, pdfPath);



            // ✅ Confirmation visuelle
            Toast.showGradientToast(mainContainer, "✅ QR Code + PDF envoyés avec succès !");

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la réservation : " + e.getMessage());
            Toast.showError(mainContainer, "Échec de la réservation.");
            e.printStackTrace();
        }
    }

}
