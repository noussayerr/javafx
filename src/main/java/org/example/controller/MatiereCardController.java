package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.example.entity.Matiere;

import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;

public class MatiereCardController {
    @FXML private Label nomLabel;
    @FXML private Label titreLabel;
    @FXML private ImageView imageView;
    @FXML private HBox matiereCard;
    private Matiere matiere;
    private Consumer<Matiere> onClick;

    public void setData(Matiere matiere, Consumer<Matiere> onClick) {
        this.matiere = matiere;
        this.onClick = onClick;

        nomLabel.setText(matiere.getNomM());
        titreLabel.setText(matiere.getTitreM());

        // Load image with fallback to default
        try {
            if (matiere.getImgM() != null && !matiere.getImgM().isEmpty()) {
                File file = new File("src/main/resources/matiere/" + matiere.getImgM());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    String imagePath = "/matiere/" + matiere.getImgM();
                    InputStream is = getClass().getResourceAsStream(imagePath);
                    if (is != null) {
                        imageView.setImage(new Image(is));
                    } else {
                        loadDefaultImage();
                    }
                }
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + matiere.getNomM() + ": " + e.getMessage());
            loadDefaultImage();
        }

        // Add click handler to the entire card
        matiereCard.setOnMouseClicked(e -> {
            if (onClick != null) onClick.accept(matiere);
        });
    }

    private void loadDefaultImage() {
        try {
            InputStream defaultStream = getClass().getResourceAsStream("/images/default-subject.png");
            if (defaultStream != null) {
                imageView.setImage(new Image(defaultStream));
            } else {
                System.err.println("Default image not found in resources");
            }
        } catch (Exception ex) {
            System.err.println("Failed to load default image: " + ex.getMessage());
        }
    }

    public void setPastelColors(String... colors) {
        String background = colors.length > 0 ? colors[0] : "#E8EDFF";
        String style = String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 15;
            -fx-padding: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);
            -fx-transition: all 0.3s ease;
        """, background);
        matiereCard.setStyle(style);

        // Add hover animation
        matiereCard.setOnMouseEntered(e -> {
            matiereCard.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 15;
                -fx-padding: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6);
                -fx-scale-x: 1.05;
                -fx-scale-y: 1.05;
            """, background));
        });
        matiereCard.setOnMouseExited(e -> {
            matiereCard.setStyle(style);
            matiereCard.setScaleX(1.0);
            matiereCard.setScaleY(1.0);
        });
    }
}