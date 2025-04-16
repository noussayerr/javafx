package org.example.controller;

import org.example.dao.CategorieDAO;
import org.example.entity.Categorie;
import org.example.utils.Toast;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.scene.Scene;
import javafx.collections.ObservableList;
import javafx.animation.PauseTransition;
import javafx.util.Duration;



public class ModifierCategorieController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtImage;
    @FXML
    private Button btnImage;

    private Categorie selectedCategorie;
    private CategorieController parentController;
    private String imagePathFinal;
    @FXML
    private Button btnTheme;
    @FXML
    private Label lblErreurNom;

    @FXML
    private Label lblErreurDescription;

    public void initData(Categorie categorie, CategorieController controller) {
        this.selectedCategorie = categorie;
        this.parentController = controller;

        txtNom.setText(categorie.getNom());
        txtDescription.setText(categorie.getDescription());
        txtImage.setText(categorie.getImage());
        imagePathFinal = categorie.getImage();
    }


    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String destinationDir = "src/main/resources/images/";
                File destDir = new File(destinationDir);
                if (!destDir.exists()) destDir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, fileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imagePathFinal = "images/" + fileName;
                txtImage.setText(imagePathFinal);
                System.out.println("📸 Image copiée : " + imagePathFinal);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Erreur image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void enregistrerModification() {
        String nom = txtNom.getText().trim();
        String description = txtDescription.getText().trim();
        boolean isValid = true;

        // 🔄 Réinitialiser les styles et erreurs
        txtNom.setStyle("");
        txtDescription.setStyle("");
        lblErreurNom.setText("");
        lblErreurDescription.setText("");

        // ✅ Validation : nom non vide
        if (nom.isEmpty()) {
            lblErreurNom.setText("❗ Le nom est requis.");
            txtNom.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        // ✅ Validation : lettres uniquement
        else if (!nom.matches("[a-zA-Z ]+")) {
            lblErreurNom.setText("❗ Le nom doit contenir uniquement des lettres.");
            txtNom.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        // ✅ Validation : description
        if (description.isEmpty()) {
            lblErreurDescription.setText("❗ La description est requise.");
            txtDescription.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        if (!isValid) return;

        // ✅ Mise à jour
        selectedCategorie.setNom(nom);
        selectedCategorie.setDescription(description);
        selectedCategorie.setImage(imagePathFinal);

        CategorieDAO dao = new CategorieDAO();
        dao.modifierCategorie(selectedCategorie);

        Toast.show((Stage) txtNom.getScene().getWindow(), "✅ Catégorie modifiée !");
        parentController.rafraichirTable();

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> ((Stage) txtNom.getScene().getWindow()).close());
        pause.play();
    }

    @FXML
    private void toggleTheme() {
        Scene scene = btnTheme.getScene(); // ou n’importe quel autre élément de la scène
        ObservableList<String> stylesheets = scene.getStylesheets();

        String light = getClass().getResource("/com/example/firsttry/styles/mode-clair.css").toExternalForm();
        String dark = getClass().getResource("/com/example/firsttry/styles/dark-theme.css").toExternalForm();

        // Supprimer les anciens thèmes
        stylesheets.removeIf(s -> s.contains("mode-clair.css") || s.contains("dark-theme.css"));

        if ("🌙".equals(btnTheme.getText())) {
            stylesheets.add(dark);
            btnTheme.setText("☀️");
        } else {
            stylesheets.add(light);
            btnTheme.setText("🌙");
        }
    }
}