package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.example.dao.EvenementDAO;
import org.example.entity.Evenement;
import org.example.utils.MyDatabase;
import org.example.utils.SessionManager;
import org.example.utils.Toast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ModifierEvenementController {

    @FXML private TextField txtTitre;
    @FXML private Label lblErreurTitre;
    @FXML private TextField txtLieu;
    @FXML private TextArea txtDescription;
    @FXML private Label lblErreurLieu;
    @FXML private Label lblErreurDescription;
    @FXML private DatePicker datePicker;
    @FXML private Label lblErreurDate;
    @FXML private TextField txtHeureDebut;
    @FXML private TextField txtHeureFin;
    @FXML private Label lblErreurHeureDebut;
    @FXML private Label lblErreurHeureFin;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private Label lblErreurCategorie;
    @FXML private TextField txtImage;
    @FXML
    private Button logoutButton;
    @FXML private Button btnTheme;

    private Evenement evenementActuel;
    private EvenementController parentController;
    private Map<String, Integer> mapCategories = new HashMap<>();



    private void remplirChamps() {
        txtTitre.setText(evenementActuel.getTitre());
        txtDescription.setText(evenementActuel.getDescription());
        txtLieu.setText(evenementActuel.getLieu());
        txtImage.setText(evenementActuel.getImage());
        txtHeureDebut.setText(evenementActuel.getHeureDebut().toString());
        txtHeureFin.setText(evenementActuel.getHeureFin().toString());
    }



    public void initData(Evenement event, EvenementController controller) {
        this.evenementActuel = event;
        this.parentController = controller;

        txtTitre.setText(event.getTitre());
        txtDescription.setText(evenementActuel.getDescription());

        txtLieu.setText(event.getLieu());
        txtImage.setText(event.getImage());
        txtHeureDebut.setText(event.getHeureDebut().toString());
        txtHeureFin.setText(event.getHeureFin().toString());
        datePicker.setValue(event.getDateEvent());

        chargerCategoriesDepuisBDD();
    }

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

            for (Map.Entry<String, Integer> entry : mapCategories.entrySet()) {
                if (entry.getValue() == evenementActuel.getCategoryId()) {
                    comboCategorie.setValue(entry.getKey());
                    break;
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Échec du chargement des catégories.");
        }
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(txtImage.getScene().getWindow());
        if (file != null) {
            txtImage.setText(file.getName());
        }
    }

    @FXML
    private void enregistrerModification() {
        clearErrors();
        boolean isValid = true;

        String titre = txtTitre.getText().trim();
        if (titre.isEmpty() || titre.length() < 3 || !titre.matches("[a-zA-Z ]+")) {
            txtTitre.setStyle("-fx-border-color: red;");
            lblErreurTitre.setText("Min 3 lettres. Lettres uniquement.");
            isValid = false;
        }
        String description = txtDescription.getText().trim();
        if (description.isEmpty()) {
            txtDescription.setStyle("-fx-border-color: red;");
            lblErreurDescription.setText("Description requise.");
            isValid = false;
        }


        String lieu = txtLieu.getText().trim();
        if (lieu.isEmpty()) {
            txtLieu.setStyle("-fx-border-color: red;");
            lblErreurLieu.setText("Lieu requis.");
            isValid = false;
        }
        evenementActuel.setDescription(txtDescription.getText().trim());
        if (lieu.isEmpty()) {
            txtDescription.setStyle("-fx-border-color: red;");
            lblErreurDescription.setText("Description requis.");
            isValid = false;
        }
        LocalDate date = datePicker.getValue();
        if (date == null || date.isBefore(LocalDate.now())) {
            datePicker.setStyle("-fx-border-color: red;");
            lblErreurDate.setText("Date invalide ou passée.");
            isValid = false;
        }

        LocalTime heureDebut = null;
        LocalTime heureFin = null;

        try {
            heureDebut = LocalTime.parse(txtHeureDebut.getText().trim());
            heureFin = LocalTime.parse(txtHeureFin.getText().trim());

            if (!heureDebut.isBefore(heureFin)) {
                txtHeureDebut.setStyle("-fx-border-color: red;");
                txtHeureFin.setStyle("-fx-border-color: red;");
                lblErreurHeureDebut.setText("Heure début doit être avant heure fin.");
                lblErreurHeureFin.setText("Heure fin doit être après heure début.");
                isValid = false;
            }

        } catch (Exception e) {
            txtHeureDebut.setStyle("-fx-border-color: red;");
            txtHeureFin.setStyle("-fx-border-color: red;");
            lblErreurHeureDebut.setText("Format invalide (ex : 14:00).");
            lblErreurHeureFin.setText("Format invalide (ex : 16:30).");
            isValid = false;
        }


        String nomCategorie = comboCategorie.getValue();
        Integer categoryId = mapCategories.get(nomCategorie);
        if (categoryId == null) {
            comboCategorie.setStyle("-fx-border-color: red;");
            lblErreurCategorie.setText("Catégorie requise.");
            isValid = false;
        }


        if (!isValid) return;

        evenementActuel.setNom(titre);

        evenementActuel.setLieu(lieu);
        evenementActuel.setDate(date);

        evenementActuel.setHeureDebut(heureDebut);
        evenementActuel.setHeureFin(heureFin);
        evenementActuel.setImage(txtImage.getText());
        evenementActuel.setCategoryId(categoryId);

        EvenementDAO dao = new EvenementDAO();
        dao.modifierEvenement(evenementActuel);

        Toast.show((Stage) txtTitre.getScene().getWindow(), "✅ Événement modifié avec succès !");
        parentController.rafraichirTable();

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
        pause.setOnFinished(event -> fermerFenetre());
        pause.play();
    }

    private void clearErrors() {
        txtTitre.setStyle(""); lblErreurTitre.setText("");
        txtDescription.setStyle(""); lblErreurDescription.setText("");

        txtLieu.setStyle(""); lblErreurLieu.setText("");
        datePicker.setStyle(""); lblErreurDate.setText("");
        txtHeureDebut.setStyle(""); lblErreurHeureDebut.setText("");
        txtHeureFin.setStyle(""); lblErreurHeureFin.setText("");
        comboCategorie.setStyle(""); lblErreurCategorie.setText("");
    }

    private void fermerFenetre() {
        ((Stage) txtTitre.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String msg) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void viderChamps() {
        txtHeureDebut.clear(); txtHeureFin.clear();
        datePicker.setValue(null);
        comboCategorie.setValue(null);
        clearErrors();
        Toast.show((Stage) txtTitre.getScene().getWindow(), "🧹 Champs vidés !");
    }

    @FXML
    private void toggleTheme() {
        Scene scene = txtTitre.getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();
        String light = getClass().getResource("/com/example/firsttry/styles/mode-clair.css").toExternalForm();
        String dark = getClass().getResource("/com/example/firsttry/styles/dark-theme.css").toExternalForm();

        stylesheets.removeIf(s -> s.contains("mode-clair.css") || s.contains("dark-theme.css"));
        if (btnTheme.getText().equals("🌙")) {
            stylesheets.add(dark);
            btnTheme.setText("☀️");
            Toast.show((Stage) scene.getWindow(), "🌙 Thème sombre activé !");
        } else {
            stylesheets.add(light);
            btnTheme.setText("🌙");
            Toast.show((Stage) scene.getWindow(), "☀️ Thème clair activé !");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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

    @FXML


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
