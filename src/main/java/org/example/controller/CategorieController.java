package org.example.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import org.example.dao.CategorieDAO;
import org.example.entity.Categorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.example.utils.SessionManager;
import org.example.utils.Toast;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.io.File;


public class CategorieController {

    @FXML
    private TableView<Categorie> tableCategories;
    @FXML
    private TableColumn<Categorie, Integer> colId;
    @FXML
    private TableColumn<Categorie, String> colNom;
    @FXML
    private TableColumn<Categorie, String> colDescription;
    @FXML
    private TableColumn<Categorie, String> colImage;
    @FXML
    private TextField txtRecherche;
    @FXML private Button btnTheme;
    @FXML
    private CategorieController parentController;
    @FXML
    private Label loadingIcon;
    @FXML private Button logoutButton;


    @FXML
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 6;
    private ObservableList<Categorie> allCategories;

    private FilteredList<Categorie> filteredData;
    ;

    private final ObservableList<Categorie> listCategories = FXCollections.observableArrayList();

    @FXML
    public void setParentController(CategorieController controller) {
        this.parentController = controller;
    }

    @FXML
    public void initialize() {
        CategorieDAO dao = new CategorieDAO();
        allCategories = FXCollections.observableArrayList(dao.getAllCategories()); // 🔁 Récupère toutes les catégories
        listCategories.setAll(allCategories); // ✅ on utilise la même liste pour pagination


        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colImage.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        // ✅ Charge l’image à partir du dossier resources/images/
                        Image img = new Image(getClass().getResource("/images/" + imagePath).toExternalForm());
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.out.println("❌ Image introuvable : " + imagePath);
                        setGraphic(null);
                    }
                }
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));


// ✅ Initialiser la liste filtrée
        filteredData = new FilteredList<>(listCategories, p -> true);

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(categorie -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return categorie.getNom().toLowerCase().contains(lower)
                        || categorie.getDescription().toLowerCase().contains(lower);
            });
        });

        SortedList<Categorie> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableCategories.comparatorProperty());

        tableCategories.setItems(sortedData);
        pagination.setPageCount((int) Math.ceil((double) allCategories.size() / ROWS_PER_PAGE));
        pagination.setPageFactory(this::createPage);

    }

    public void rafraichirTable() {
        CategorieDAO dao = new CategorieDAO();
        ObservableList<Categorie> nouvellesCategories = FXCollections.observableArrayList(dao.getAllCategories());

        allCategories.setAll(nouvellesCategories);         // maj data principale
        listCategories.setAll(nouvellesCategories);        // maj pagination
        filteredData = new FilteredList<>(listCategories, p -> true); // ⚠️ recréer ici
        pagination.setPageCount((int) Math.ceil((double) allCategories.size() / ROWS_PER_PAGE));
        pagination.setCurrentPageIndex(0); // revenir à la page 1
        pagination.setPageFactory(this::createPage);
    }


    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ajouter-categorie.fxml"));
            Parent root = loader.load();

            // 🔁 Lien entre les deux contrôleurs
            AjouterCategorieController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("➕ Ajouter une catégorie");
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("✅ Formulaire d’ajout ouvert !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture formulaire ajout : " + e.getMessage());
        }
    }


    @FXML
    private void modifierCategorie() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une catégorie à modifier.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/modifier-categorie.fxml"));
            Parent root = loader.load();

            ModifierCategorieController controller = loader.getController();
            controller.initData(selected, this); // ⬅️ on passe les données

            Stage stage = new Stage();
            stage.setTitle("Modifier catégorie");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture formulaire modification : " + e.getMessage());
        }
    }

    @FXML
    private void effacerRecherche() {
        txtRecherche.clear();
    }

    @FXML
    private void supprimerCategorie() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer cette catégorie ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    CategorieDAO dao = new CategorieDAO();
                    dao.supprimerCategorie(selected.getId());

                    rafraichirTable(); // ✅ Rafraîchir TOUTES les listes, pagination, etc.
                    Toast.show((Stage) tableCategories.getScene().getWindow(), "✅ Catégorie supprimée !");
                }
            });
        } else {
            Toast.show((Stage) tableCategories.getScene().getWindow(), "⚠️ Veuillez sélectionner une catégorie.");
        }
    }


    @FXML
    private void trierParNom() {
        lancerAnimationChargement(loadingIcon);

        SortedList<Categorie> sortedList = new SortedList<>(filteredData);
        sortedList.setComparator((c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));
        tableCategories.setItems(sortedList);

        Toast.show((Stage) tableCategories.getScene().getWindow(), "✔ Tri A → Z effectué !");
    }

    @FXML
    private void trierParNomDesc() {
        lancerAnimationChargement(loadingIcon);

        SortedList<Categorie> sortedList = new SortedList<>(filteredData);
        sortedList.setComparator((c1, c2) -> c2.getNom().compareToIgnoreCase(c1.getNom()));
        tableCategories.setItems(sortedList);

        Toast.show((Stage) tableCategories.getScene().getWindow(), "✔ Tri Z → A effectué !");
    }

    private Node createPage(int pageIndex) {
        String filtre = txtRecherche.getText().toLowerCase();
        FilteredList<Categorie> filtered = new FilteredList<>(allCategories, c ->
                filtre == null || filtre.isEmpty()
                        || c.getNom().toLowerCase().contains(filtre)
                        || c.getDescription().toLowerCase().contains(filtre));

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filtered.size());

        SortedList<Categorie> sorted = new SortedList<>(FXCollections.observableArrayList(filtered.subList(fromIndex, toIndex)));
        sorted.comparatorProperty().bind(tableCategories.comparatorProperty());

        tableCategories.setItems(sorted);
        return new AnchorPane(); // Obligatoire pour Pagination
    }

    private void lancerAnimationChargement(Label label) {//animation icone
        label.setVisible(true);
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setCycleCount(4);
        scale.setAutoReverse(true);
        scale.setOnFinished(e -> label.setVisible(false));
        scale.play();
    }


    @FXML
    private void toggleTheme() {
        Scene scene = tableCategories.getScene();//mode sombre et claire
        ObservableList<String> stylesheets = scene.getStylesheets();
        String light = getClass().getResource("/org/example/styles/mode-clair.css").toExternalForm();
        String dark = getClass().getResource("/org/example/styles/dark-theme.css").toExternalForm();

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
