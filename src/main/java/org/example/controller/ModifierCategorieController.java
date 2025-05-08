package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.example.dao.CategorieDAO;
import org.example.entity.Categorie;
import org.example.utils.SessionManager;
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
    private Button logoutButton;

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

        // ✅ Vérification du nom
        if (nom.isEmpty()) {
            txtNom.setStyle("-fx-border-color: red;");
            lblErreurNom.setText("❌ Le nom est requis.");
            isValid = false;
        } else if (!nom.matches("[a-zA-Z ]+")) {
            txtNom.setStyle("-fx-border-color: red;");
            lblErreurNom.setText("❌ Le nom doit contenir uniquement des lettres.");
            isValid = false;

        } else {
            CategorieDAO dao = new CategorieDAO();
            boolean existe = dao.getAllCategories().stream()
                    .anyMatch(cat -> cat.getNom().equalsIgnoreCase(nom) && cat.getId() != selectedCategorie.getId());
            if (existe) {
                txtNom.setStyle("-fx-border-color: red;");
                lblErreurNom.setText("❌ Ce nom existe déjà.");
                isValid = false;
            }
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
        if (imagePathFinal != null && !imagePathFinal.isEmpty()) {
            selectedCategorie.setImage(imagePathFinal);
        } else {
            selectedCategorie.setImage(selectedCategorie.getImage());
        }


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