package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Matiere;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

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

    @FXML private Label nomErrorLabel;
    @FXML private Label titreErrorLabel;
    @FXML private Label descErrorLabel;
    @FXML private Label objErrorLabel;
    @FXML private Label imgErrorLabel;
    @FXML private Label errorLabel;

    @FXML private Button logoutButton;

    private final ServiceMatiere matiereService = new ServiceMatiere();

    @FXML
    private void handleAjouterMatiere(ActionEvent event) throws SQLException {
        clearErrorLabels();
        boolean valid = true;

        String nom = nomMField.getText().trim();

        // Valider le nom : commence par une lettre, accepte lettres, chiffres, espaces, tirets, apostrophes
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Le nom est requis.");
            valid = false;
        } else if (!nom.matches("^[a-zA-Z][a-zA-Z0-9\\s\\-']*$")) {
            nomErrorLabel.setText("Le nom doit commencer par une lettre et contenir uniquement lettres, chiffres, tirets ou espaces.");
            valid = false;
        } else if (matiereService.checkNomExist(nom)) {
            nomErrorLabel.setText("Ce nom de matière existe déjà.");
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
            errorLabel.setTextFill(Color.RED);
            errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
            return;
        }

        Matiere matiere = new Matiere();
        matiere.setNomM(nom);
        matiere.setTitreM(titreMField.getText());
        matiere.setDescM(descMField.getText());
        matiere.setObjM(objMField.getText());

        File sourceImageFile = new File(imgMField.getText());
        if (sourceImageFile.exists()) {
            String imageName = sourceImageFile.getName();
            String projectRoot = System.getProperty("user.dir");
            Path targetImagePath = Path.of(projectRoot, "src", "main", "resources", "matiere", imageName);

            try {
                Files.copy(sourceImageFile.toPath(), targetImagePath, StandardCopyOption.REPLACE_EXISTING);
                matiere.setImgM(imageName);
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Erreur lors de la copie de l'image.");
                errorLabel.setTextFill(Color.RED);
                errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
                return;
            }
        } else {
            errorLabel.setText("Fichier image non trouvé.");
            errorLabel.setTextFill(Color.RED);
            errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
            return;
        }

        try {
            matiereService.ajouter(matiere);
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ajout de la matière.");
            errorLabel.setTextFill(Color.RED);
            errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
            return;
        }

        resetFields();

        // ✅ Message en gras et en vert
        errorLabel.setText("✅ Matière ajoutée avec succès !");
        errorLabel.setTextFill(Color.GREEN);
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
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
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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

    public void goToMatiere(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ListeMatiere.fxml");
    }

    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }

    public void goToUtilisateurs(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AdminDashboard.fxml");
    }

    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void afficherJeux(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/jeuxIndex.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
