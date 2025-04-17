package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Matiere;
import org.example.services.ServiceMatiere;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AjoutMatiereController {

    @FXML private TextField nomMField;
    @FXML private TextField titreMField;
    @FXML private TextArea descMField;
    @FXML private TextArea objMField;
    @FXML private TextField imgMField;

    // Labels d'erreur sous les champs
    @FXML private Label nomErrorLabel;
    @FXML private Label titreErrorLabel;
    @FXML private Label descErrorLabel;
    @FXML private Label objErrorLabel;
    @FXML private Label imgErrorLabel;
    @FXML private Label errorLabel;

    private ServiceMatiere matiereService = new ServiceMatiere();
    @FXML
    private void handleAjouterMatiere(ActionEvent event) throws SQLException {
        // Nettoyer les messages d'erreur
        clearErrorLabels();

        boolean valid = true;

        String nom = nomMField.getText().trim();
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est requis.");
            valid = false;
        } else if (!nom.matches("[a-zA-ZàâçéèêëîïôûùüÿñæœÀÂÇÉÈÊËÎÏÔÛÙÜŸÑÆŒ\\s\\-']+")) {
            nomErrorLabel.setText("Le nom ne doit contenir que des lettres.");
            valid = false;
        }

        if (titreMField.getText().trim().isEmpty()) {
            titreErrorLabel.setText("Le titre est requis.");
            valid = false;
        }

        if (descMField.getText().trim().isEmpty()) {
            descErrorLabel.setText("La description est requise.");
            valid = false;
        }

        if (objMField.getText().trim().isEmpty()) {
            objErrorLabel.setText("Les objectifs sont requis.");
            valid = false;
        }

        if (imgMField.getText().trim().isEmpty()) {
            imgErrorLabel.setText("L'image est requise.");
            valid = false;
        }

        if (!valid) {
            errorLabel.setText("Veuillez corriger les erreurs ci-dessus.");
            return;
        }

        // Créez une nouvelle matière
        Matiere matiere = new Matiere();
        matiere.setNomM(nom);
        matiere.setTitreM(titreMField.getText());
        matiere.setDescM(descMField.getText());
        matiere.setObjM(objMField.getText());

        File sourceImageFile = new File(imgMField.getText()); // Image choisie par l'utilisateur
        if (sourceImageFile.exists()) {
            // Récupérer uniquement le nom du fichier
            String imageName = sourceImageFile.getName();

            // Chemin relatif vers le dossier 'resources/matiere'
            String projectRoot = System.getProperty("user.dir");
            Path targetImagePath = Path.of(projectRoot, "src", "main", "resources", "matiere", imageName);

            try {
                // Copier l'image vers le dossier resources/matiere
                Files.copy(sourceImageFile.toPath(), targetImagePath, StandardCopyOption.REPLACE_EXISTING);

                // Enregistrer uniquement le nom du fichier dans la base de données
                matiere.setImgM(  imageName); // Enregistrement relatif dans la base de données
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Erreur lors de la copie de l'image.");
                return;
            }
        } else {
            errorLabel.setText("Fichier image non trouvé.");
            return;
        }





        // Ajoutez la matière à la base de données
        try {
            matiereService.ajouter(matiere);
            System.out.println("Matière ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ajout de la matière.");
        }

        // Réinitialiser les champs
        resetFields();
        errorLabel.setText("✅ Matière ajoutée avec succès !");
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        resetFields();
        clearErrorLabels();
        errorLabel.setText("");
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            imgMField.setText(file.getAbsolutePath());
        }
    }

    private void clearErrorLabels() {
        nomErrorLabel.setText("");
        titreErrorLabel.setText("");
        descErrorLabel.setText("");
        objErrorLabel.setText("");
        imgErrorLabel.setText("");
        errorLabel.setText("");
    }

    private void resetFields() {
        nomMField.clear();
        titreMField.clear();
        descMField.clear();
        objMField.clear();
        imgMField.clear();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Déconnexion...");
        // Implémente ici la logique de déconnexion si nécessaire
    }

    @FXML
    private void goToMatiere(ActionEvent actionEvent) {
        System.out.println("Naviguer vers gestion des matières...");
        loadPage(actionEvent, "/org/example/view/ListeMatiere.fxml");
        // Implémente ici la navigation si nécessaire
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }

    @FXML
    public void afficherJeux(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/jeuxIndex.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
