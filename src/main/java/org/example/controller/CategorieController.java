package org.example.controller;

import org.example.dao.CategorieDAO;
import org.example.entity.Categorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TextField;
import org.example.utils.Toast;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ButtonType;
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
    @FXML
    private CategorieController parentController;
    @FXML
    private Label loadingIcon;


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
                        File imageFile = new File("src/main/resources/images/" + imagePath);
                        if (imageFile.exists()) {
                            Image img = new Image(imageFile.toURI().toString());
                            imageView.setImage(img);
                            setGraphic(imageView);
                        } else {
                            System.out.println("⚠️ Image introuvable : " + imagePath);
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Erreur chargement image dans table : " + imagePath);
                        e.printStackTrace();
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
        allCategories.setAll(dao.getAllCategories());
        pagination.setPageCount((int) Math.ceil((double) allCategories.size() / ROWS_PER_PAGE));
        pagination.setPageFactory(this::createPage);
    }


    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firsttry/views/ajouter-categorie.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firsttry/views/modifier-categorie.fxml"));
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
                    listCategories.remove(selected);
                    tableCategories.refresh();
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
}


