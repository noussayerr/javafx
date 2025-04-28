package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.example.dao.EvenementDAO;
import org.example.entity.Evenement;
import org.example.utils.MyDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.utils.SessionManager;
import org.example.utils.Toast;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;


import javafx.animation.PauseTransition;
import javafx.util.Duration;


public class AjouterEvenementController {
    @FXML
    private Button btnTheme;
    @FXML
    private Button logoutButton;


    @FXML
    private ComboBox<String> comboCategorie;
    @FXML
    private Label lblErreurCategorie;
    @FXML
    private TextField txtTitre;
    @FXML
    private Label lblErreurTitre;
    @FXML
    private TextField txtLieu;
    @FXML
    private Label lblErreurLieu;
    @FXML
    private TextField txtImage;
    @FXML
    private TextField txtPrix;
    @FXML
    private Label lblErreurPrix;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label lblErreurDate;
    @FXML
    private TextArea txtDescription; // ou TextField si tu utilises un champ simple

    @FXML
    private Label lblErreurDescription;
    @FXML
    private Spinner<LocalTime> spinnerHeureDebut;
    @FXML
    private Spinner<LocalTime> spinnerHeureFin;
    @FXML
    private Label lblErreurHeureDebut;
    @FXML
    private Label lblErreurHeureFin;


    @FXML
    private TextField txtHeureDebut;
    @FXML
    private TextField txtHeureFin;


    private EvenementController parentController;
    private Map<String, Integer> mapCategories = new HashMap<>();

    public void setParentController(EvenementController controller) {
        this.parentController = controller;
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(txtImage.getScene().getWindow());
        if (file != null) {
            txtImage.setText(file.getName());
        }
    }

    @FXML
    public void initialize() {
        chargerCategoriesDepuisBDD();

        ObservableList<LocalTime> horaires = FXCollections.observableArrayList(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                LocalTime.of(17, 0),
                LocalTime.of(18, 30)
        );

        spinnerHeureDebut.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(horaires));
        spinnerHeureFin.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(horaires));
    }


    @FXML
    private void chargerCategoriesDepuisBDD() {
        try (Connection connection = MyDatabase.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nom FROM category")) {

            ObservableList<String> categories = FXCollections.observableArrayList();
            mapCategories.clear();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                categories.add(nom);
                mapCategories.put(nom, id);
            }

            comboCategorie.setItems(categories);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Chargement des catégories échoué.");
        }
    }


    @FXML
    private void ajouterEvenement() {
        clearErrors();
        boolean isValid = true;

        // Titre
        String titre = txtTitre.getText().trim();
        if (titre.isEmpty() || titre.length() < 3 || !titre.matches("[a-zA-Z ]+")) {
            txtTitre.setStyle("-fx-border-color: red;");
            lblErreurTitre.setText("Titre requis (≥ 3 lettres, lettres uniquement).");
            isValid = false;
        }

        // Description
        String description = txtDescription.getText().trim();
        if (description.isEmpty()) {
            txtDescription.setStyle("-fx-border-color: red;");
            lblErreurDescription.setText("Description requise.");
            isValid = false;
        }

        // Lieu
        String lieu = txtLieu.getText().trim();
        if (lieu.isEmpty()) {
            txtLieu.setStyle("-fx-border-color: red;");
            lblErreurLieu.setText("Lieu requis.");
            isValid = false;
        }

        // Date
        LocalDate date = datePicker.getValue();
        if (date == null || date.isBefore(LocalDate.now())) {
            datePicker.setStyle("-fx-border-color: red;");
            lblErreurDate.setText("Date invalide ou passée.");
            isValid = false;
        }

        // Heures
        LocalTime heureDebut = spinnerHeureDebut.getValue();
        LocalTime heureFin = spinnerHeureFin.getValue();

        if (heureDebut == null || heureFin == null) {
            if (heureDebut == null) {
                spinnerHeureDebut.setStyle("-fx-border-color: red;");
                lblErreurHeureDebut.setText("Heure début requise.");
            }
            if (heureFin == null) {
                spinnerHeureFin.setStyle("-fx-border-color: red;");
                lblErreurHeureFin.setText("Heure fin requise.");
            }
            isValid = false;
        } else if (!heureDebut.isBefore(heureFin)) {
            spinnerHeureDebut.setStyle("-fx-border-color: red;");
            spinnerHeureFin.setStyle("-fx-border-color: red;");
            lblErreurHeureDebut.setText("Heure début doit être avant heure fin.");
            lblErreurHeureFin.setText("Heure fin doit être après heure début.");
            isValid = false;
        }

        // Catégorie
        String nomCategorie = comboCategorie.getValue();
        Integer categoryId = mapCategories.get(nomCategorie);
        if (categoryId == null) {
            comboCategorie.setStyle("-fx-border-color: red;");
            lblErreurCategorie.setText("Catégorie requise.");
            isValid = false;
        }

        // Prix
        float prix = 0;
        try {
            prix = Float.parseFloat(txtPrix.getText().trim());
            if (prix < 0) {
                txtPrix.setStyle("-fx-border-color: red;");
                lblErreurPrix.setText("Prix doit être positif.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            txtPrix.setStyle("-fx-border-color: red;");
            lblErreurPrix.setText("Prix invalide (ex: 25.0).");
            isValid = false;
        }

        if (!isValid) return;

        Evenement event = new Evenement(
                categoryId,
                titre,
                description,
                date,
                heureDebut,
                heureFin,
                lieu,
                txtImage.getText()
        );

        EvenementDAO dao = new EvenementDAO();
        dao.ajouterEvenement(event);

        Toast.show((Stage) txtTitre.getScene().getWindow(), "✅ Événement ajouté !");
        parentController.rafraichirTable();

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> fermerFenetre());
        pause.play();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
    }

    private void clearErrors() {
        txtTitre.setStyle("");
        lblErreurTitre.setText("");
        txtLieu.setStyle("");
        lblErreurLieu.setText("");
        datePicker.setStyle("");
        lblErreurDate.setText("");
        spinnerHeureDebut.setStyle("");
        spinnerHeureFin.setStyle("");
        lblErreurHeureDebut.setText("");
        lblErreurHeureFin.setText("");

        comboCategorie.setStyle("");
        lblErreurCategorie.setText("");
        txtPrix.setStyle("");
        lblErreurPrix.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void viderFormulaire() {
        txtTitre.clear();
        txtLieu.clear();
        datePicker.setValue(null);
        spinnerHeureDebut.getValueFactory().setValue(null);
        spinnerHeureFin.getValueFactory().setValue(null);

        comboCategorie.setValue(null);
        txtImage.clear();
        txtPrix.clear();

        clearErrors(); // Pour supprimer les erreurs si présentes
    }

    @FXML
    private void toggleTheme() {
        Scene scene = txtTitre.getScene(); // ✅ Utilisation d'un champ valide
        ObservableList<String> stylesheets = scene.getStylesheets();

        String light = getClass().getResource("/org/example/styles/mode-clair.css").toExternalForm();
        String dark = getClass().getResource("/org/example//styles/dark-theme.css").toExternalForm();

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
        }}
    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }
}