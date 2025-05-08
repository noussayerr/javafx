package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.entity.Matiere;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public class ListeMatiereBController {

    @FXML private TableColumn<Matiere, Void> actionColumn;
    @FXML private TableColumn<Matiere, Void> coursColumn;
    @FXML private TableColumn<Matiere, String> descColumn;
    @FXML private TableColumn<Matiere, String> imgColumn;
    @FXML private Button logoutButton;
    @FXML private TableView<Matiere> matieresTable;
    @FXML private TableColumn<Matiere, String> nomColumn;
    @FXML private TableColumn<Matiere, String> objColumn;
    @FXML private TableColumn<Matiere, String> titreColumn;
    @FXML private TextField searchField;

    // Pagination controls
    @FXML private ComboBox<Integer> itemsPerPageComboBox;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label currentPageLabel;
    @FXML private Label totalPagesLabel;

    private final ServiceMatiere mt = new ServiceMatiere();
    private final ObservableList<Matiere> allMatieres = FXCollections.observableArrayList();
    private ObservableList<Matiere> currentPageMatieres = FXCollections.observableArrayList();
    private FilteredList<Matiere> filteredMatieres;

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    public void initialize() {
        // Initialize items per page combo box
        itemsPerPageComboBox.getItems().addAll(5,10, 30, 50, 100);
        itemsPerPageComboBox.setValue(itemsPerPage);
        itemsPerPageComboBox.setOnAction(event -> {
            itemsPerPage = itemsPerPageComboBox.getValue();
            currentPage = 1;
            loadMatiereData();
        });

        // Initialize table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomM"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titreM"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("descM"));
        objColumn.setCellValueFactory(new PropertyValueFactory<>("objM"));

        imgColumn.setCellValueFactory(new PropertyValueFactory<>("imgM"));
        imgColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final HBox imageContainer = new HBox();

            {
                imageContainer.setAlignment(Pos.CENTER);
                imageContainer.getChildren().add(imageView);
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);

                if (empty || imageName == null || imageName.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        String path = "/matiere/" + imageName;
                        Image image = new Image(getClass().getResourceAsStream(path), 80, 80, true, true);
                        imageView.setImage(image);
                        imageView.setFitWidth(80);
                        imageView.setFitHeight(80);
                        setGraphic(imageContainer);
                    } catch (Exception e) {
                        System.out.println("Erreur image : " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });

        // Action column (Modifier + Supprimer)
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsContainer = new HBox(10, editButton, deleteButton);

            {
                Image editImage = new Image(getClass().getResourceAsStream("/images/mod.png"));
                ImageView editIcon = new ImageView(editImage);
                editIcon.setFitWidth(16);
                editIcon.setFitHeight(16);
                editButton.setGraphic(editIcon);
                editButton.setContentDisplay(ContentDisplay.LEFT);

                Image deleteImage = new Image(getClass().getResourceAsStream("/images/supp.png"));
                ImageView deleteIcon = new ImageView(deleteImage);
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setContentDisplay(ContentDisplay.LEFT);

                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Matiere matiere = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ModifierMatiere.fxml"));
                        Parent root = loader.load();

                        ModifierMatiereController controller = loader.getController();
                        controller.setMatiere(matiere);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Modifier Matière");
                        stage.centerOnScreen();
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                deleteButton.setOnAction(event -> {
                    Matiere matiere = getTableView().getItems().get(getIndex());

                    if (matiere != null) {
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirmation de suppression");
                        confirmation.setHeaderText("Voulez-vous vraiment supprimer cette matière ?");
                        confirmation.setContentText("Cette action est irréversible.");

                        confirmation.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    mt.supprimer(matiere.getId());
                                    allMatieres.remove(matiere);
                                    updatePagination();
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Matière supprimée", "La matière a été supprimée avec succès.");
                                } catch (SQLException e) {
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression", e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

                buttonsContainer.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsContainer);
            }
        });

        // Cours column
        coursColumn.setCellFactory(param -> new TableCell<>() {
            private final Button coursButton = new Button("Voir Liste Cours");
            private final HBox container = new HBox(coursButton);

            {
                coursButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                coursButton.setOnAction(event -> {
                    Matiere matiere = getTableView().getItems().get(getIndex());
                    afficherCours(matiere);
                });
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    container.prefWidthProperty().bind(widthProperty());
                    setGraphic(container);
                }
            }
        });

        // Setup search functionality
        setupSearch();

        // Load initial data
        loadMatiereData();
    }

    private void setupSearch() {
        filteredMatieres = new FilteredList<>(allMatieres, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMatieres.setPredicate(createPredicate(newValue));
            currentPage = 1;
            updatePagination();
        });

        SortedList<Matiere> sortedData = new SortedList<>(filteredMatieres);
        sortedData.comparatorProperty().bind(matieresTable.comparatorProperty());
        matieresTable.setItems(sortedData);
    }

    private Predicate<Matiere> createPredicate(String searchText) {
        return matiere -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();

            if (matiere.getNomM().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (matiere.getTitreM().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        };
    }

    private void loadMatiereData() {
        try {
            List<Matiere> matiereList = mt.afficher();
            allMatieres.setAll(matiereList);
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matières", e.getMessage());
        }
    }

    private void updatePagination() {
        // Calculate total pages based on filtered data
        totalPages = (int) Math.ceil((double) filteredMatieres.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        // Ensure current page is within bounds
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        // Calculate start and end indices
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredMatieres.size());

        // Update current page data
        currentPageMatieres.setAll(filteredMatieres.subList(fromIndex, toIndex));
        matieresTable.setItems(currentPageMatieres);

        // Update page info labels
        currentPageLabel.setText(String.valueOf(currentPage));
        totalPagesLabel.setText(String.valueOf(totalPages));

        // Enable/disable pagination buttons
        firstPageButton.setDisable(currentPage == 1);
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage == totalPages);
        lastPageButton.setDisable(currentPage == totalPages);
    }

    @FXML
    private void firstPage(ActionEvent event) {
        currentPage = 1;
        updatePagination();
    }

    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void nextPage(ActionEvent event) {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    @FXML
    private void lastPage(ActionEvent event) {
        currentPage = totalPages;
        updatePagination();
    }

    public void handleAfficherAjoutMatiere(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AjoutMatiere.fxml");
    }

    private void afficherCours(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AfficherCoursB.fxml"));
            Parent root = loader.load();

            AfficherCoursBController controller = loader.getController();
            controller.setMatiere(matiere);

            Stage currentStage = (Stage) matieresTable.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Cours de : " + matiere.getNomM());
            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les cours", e.getMessage());
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
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

    @FXML
    public void goToMatiere(ActionEvent event) {
        loadPage(event, "/org/example/view/ListeMatiere.fxml");
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



    public void goToUtilisateurs(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AdminDashboard.fxml");
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

    public void effacerRecherche(ActionEvent actionEvent) {
        searchField.clear();
    }
}