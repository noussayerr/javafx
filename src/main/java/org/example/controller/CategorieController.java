package org.example.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    @FXML
    private TableColumn<Categorie, Void> colActions;


    private static final int ROWS_PER_PAGE = 6;
    private ObservableList<Categorie> allCategories;
    private FilteredList<Categorie> filteredData;
    private final ObservableList<Categorie> listCategories = FXCollections.observableArrayList();

    @FXML
    public void setParentController(CategorieController controller) {
        this.parentController = controller;
    }

    @FXML
    public void initialize() {
        CategorieDAO dao = new CategorieDAO();
        allCategories = FXCollections.observableArrayList(dao.getAllCategories());
        listCategories.setAll(allCategories);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("📝 Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");
            private final HBox actionBox = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: linear-gradient(to right, #8e44ad, #9b59b6); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25;");
                btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #74ebd5, #acb6e5); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25;");
                actionBox.setStyle("-fx-alignment: center;");

                btnModifier.setOnAction(e -> {
                    Categorie selected = getTableView().getItems().get(getIndex());
                    if (selected != null) {
                        tableCategories.getSelectionModel().select(selected);
                        modifierCategorie();
                    }
                });

                btnSupprimer.setOnAction(e -> {
                    Categorie selected = getTableView().getItems().get(getIndex());
                    if (selected != null) {
                        tableCategories.getSelectionModel().select(selected);
                        supprimerCategorie();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });


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
                        Image img = new Image(getClass().getResource("/images/" + imagePath).toExternalForm());
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        filteredData = new FilteredList<>(listCategories, p -> true);
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            pagination.setCurrentPageIndex(0);
            pagination.setPageFactory(this::createPage);
        });

        pagination.setPageCount((int) Math.ceil((double) allCategories.size() / ROWS_PER_PAGE));
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        String filtre = txtRecherche.getText().toLowerCase();
        filteredData.setPredicate(categorie ->
                filtre == null || filtre.isEmpty()
                        || categorie.getNom().toLowerCase().contains(filtre)
                        || categorie.getDescription().toLowerCase().contains(filtre)
        );

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredData.size());

        SortedList<Categorie> sorted = new SortedList<>(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        sorted.comparatorProperty().bind(tableCategories.comparatorProperty());
        tableCategories.setItems(sorted);

        int totalPages = (int) Math.ceil((double) filteredData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(totalPages, 1));

        return new AnchorPane();
    }

    public void rafraichirTable() {
        CategorieDAO dao = new CategorieDAO();
        ObservableList<Categorie> nouvellesCategories = FXCollections.observableArrayList(dao.getAllCategories());
        allCategories.setAll(nouvellesCategories);
        listCategories.setAll(nouvellesCategories);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ajouter-categorie.fxml"));
            Parent root = loader.load();
            AjouterCategorieController controller = loader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("➕ Ajouter une catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierCategorie() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", null, "Veuillez sélectionner une catégorie à modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/modifier-categorie.fxml"));
            Parent root = loader.load();
            ModifierCategorieController controller = loader.getController();
            controller.initData(selected, this);
            Stage stage = new Stage();
            stage.setTitle("Modifier catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
            alert.setContentText("Voulez-vous vraiment supprimer cette catégorie ?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    CategorieDAO dao = new CategorieDAO();
                    dao.supprimerCategorie(selected.getId());
                    rafraichirTable();
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
        filteredData.setPredicate(c -> true);
        SortedList<Categorie> sorted = new SortedList<>(filteredData);
        sorted.setComparator((c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));
        tableCategories.setItems(sorted);
    }

    @FXML
    private void trierParNomDesc() {
        lancerAnimationChargement(loadingIcon);
        filteredData.setPredicate(c -> true);
        SortedList<Categorie> sorted = new SortedList<>(filteredData);
        sorted.setComparator((c1, c2) -> c2.getNom().compareToIgnoreCase(c1.getNom()));
        tableCategories.setItems(sorted);
    }

    private void lancerAnimationChargement(Label label) {
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
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
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
}
