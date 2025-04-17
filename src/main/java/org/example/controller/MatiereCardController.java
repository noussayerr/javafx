package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.entity.Matiere;

import java.io.File;
import java.util.function.Consumer;

public class MatiereCardController {
    @FXML
    private Label nomLabel;
    @FXML private Label descLabel;
    @FXML private ImageView imageView;
    @FXML private Button voirCoursButton;

    private Matiere matiere;
    private Consumer<Matiere> onVoirCours;

    public void setData(Matiere matiere, Consumer<Matiere> onVoirCours) {
        this.matiere = matiere;
        this.onVoirCours = onVoirCours;

        nomLabel.setText(matiere.getNomM());
        descLabel.setText(matiere.getDescM());

        if (matiere.getImgM() != null) {
            File file = new File("src/main/resources/matiere/" + matiere.getImgM());
            imageView.setImage(new Image(file.toURI().toString()));
        }

        voirCoursButton.setOnAction(e -> {
            if (onVoirCours != null) onVoirCours.accept(matiere);
        });
    }
}

