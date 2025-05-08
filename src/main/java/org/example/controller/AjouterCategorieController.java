package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.example.dao.CategorieDAO;
import org.example.entity.Categorie;
import org.example.utils.SessionManager;
import org.example.utils.Toast;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.example.utils.Toast;import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AjouterCategorieController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtImage;
    @FXML private Label lblNomImage;
    @FXML private ImageView imagePreview;
    @FXML
    private Button logoutButton;

    @FXML
    private Button btnTheme;

    private String imagePathFinal;
    @FXML private Label lblErreurNom;
    @FXML private Label lblErreurDescription;

    private CategorieController parentController; // 💡 Pour rafraîchir la TableView
    private boolean validerNom(String nom) {
        // Vérifie si le nom contient uniquement des lettres et des espaces
        if (!nom.matches("[a-zA-Z ]+")) {
            Toast.show((Stage) txtNom.getScene().getWindow(), "❌ Le nom ne doit contenir que des lettres !");
            txtNom.setStyle("-fx-border-color: red;");
            return false;
        }

        // Vérifie unicité du nom dans la BDD
        CategorieDAO dao = new CategorieDAO();
        if (dao.nomCategorieExiste(nom)) {
            Toast.show((Stage) txtNom.getScene().getWindow(), "❌ Ce nom de catégorie existe déjà !");
            txtNom.setStyle("-fx-border-color: red;");
            return false;
        }

        txtNom.setStyle(""); // Remet le style normal si tout est bon
        return true;
    }


    public void setParentController(CategorieController controller) {
        this.parentController = controller;
    }



    private String nomImageFinale = ""; // ce nom sera sauvegardé dans la base

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Nom unique
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                nomImageFinale = System.currentTimeMillis() + "_" + selectedFile.getName(); // ex: 17445056_event1.jpg

                // Chemin de destination dans /resources/images/
                Path destinationPath = Paths.get("src/main/resources/images/" + nomImageFinale);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                lblNomImage.setText(nomImageFinale);
                txtImage.setText(nomImageFinale);


                // Prévisualisation
                Image img = new Image(destinationPath.toUri().toString());
                imagePreview.setImage(img);

                System.out.println("✅ Image copiée : " + destinationPath);

            } catch (IOException e) {
                System.out.println("❌ Erreur lors de la copie de l'image");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void ajouterCategorie() {
        String nom = txtNom.getText().trim();
        String description = txtDescription.getText().trim();

        // ✅ Réinitialiser styles et erreurs
        txtNom.setStyle("");
        lblErreurNom.setText("");
        txtDescription.setStyle("");
        lblErreurDescription.setText("");

        boolean isValid = true;

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
                    .anyMatch(cat -> cat.getNom().equalsIgnoreCase(nom));
            if (existe) {
                txtNom.setStyle("-fx-border-color: red;");
                lblErreurNom.setText("❌ Ce nom existe déjà.");
                isValid = false;
            }
        }

        // ✅ Vérification de la description
        if (description.isEmpty()) {
            txtDescription.setStyle("-fx-border-color: red;");
            lblErreurDescription.setText("❌ La description est requise.");
            isValid = false;
        }

        // ✅ Vérification image
        // ✅ Image optionnelle : on garde le champ vide si aucune image n’est choisie
        if (nomImageFinale == null) {
            nomImageFinale = "";
        }
        if (!isValid) return;

        // ✅ Création et enregistrement
        imagePathFinal = nomImageFinale; // 🔁 On sauvegarde bien le nom de l’image
        Categorie newCategorie = new Categorie(nom, description, imagePathFinal);
        CategorieDAO dao = new CategorieDAO();
        dao.ajouterCategorie(newCategorie);

        // ✅ Affichage succès
        Toast.show((Stage) txtNom.getScene().getWindow(), "✅ Catégorie ajoutée avec succès !");

        // ✅ Rafraîchir la vue parent (TableView mise à jour avec image)
        if (parentController != null) {
            parentController.rafraichirTable();
        }

        // ✅ Fermeture après 2.5 secondes
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> fermerFenetre());
        pause.play();
    }


    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void toggleTheme() {
        Scene scene = txtNom.getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();

        String light = getClass().getResource("/org/example/styles/mode-clair.css").toExternalForm();
        String dark = getClass().getResource("/org/example/styles/dark-theme.css").toExternalForm();

        stylesheets.removeIf(s -> s.contains("mode-clair.css") || s.contains("dark-theme.css"));

        if (btnTheme.getText().equals("🌙")) {
            stylesheets.add(dark);
            Toast.show((Stage) scene.getWindow(), "🌙 Thème sombre activé !");
            btnTheme.setText("☀️");
        } else {
            stylesheets.add(light);
            Toast.show((Stage) scene.getWindow(), "☀️ Thème clair activé !");
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
