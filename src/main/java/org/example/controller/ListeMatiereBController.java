package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class ListeMatiereBController {

    @FXML
    private TableColumn<Matiere, Void> actionColumn;

    @FXML
    private TableColumn<Matiere, Void> coursColumn;

    @FXML
    private TableColumn<Matiere, String> descColumn;

    @FXML
    private TableColumn<Matiere, String> imgColumn;

    @FXML private Button logoutButton;

    @FXML
    private TableView<Matiere> matieresTable;

    @FXML
    private TableColumn<Matiere, String> nomColumn;

    @FXML
    private TableColumn<Matiere, String> objColumn;

    @FXML
    private TableColumn<Matiere, String> titreColumn;

    private final ServiceMatiere mt = new ServiceMatiere();

    private final ObservableList<Matiere> MatiereList = FXCollections.observableArrayList();

    public void initialize() {
        try {
            List<Matiere> matiereList = mt.afficher();
            ObservableList<Matiere> observableList = FXCollections.observableList(matiereList);
            matieresTable.setItems(observableList);

            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomM"));
            titreColumn.setCellValueFactory(new PropertyValueFactory<>("titreM"));
            descColumn.setCellValueFactory(new PropertyValueFactory<>("descM"));
            objColumn.setCellValueFactory(new PropertyValueFactory<>("objM"));

            imgColumn.setCellValueFactory(new PropertyValueFactory<>("imgM")); // contient juste le nom du fichier

            imgColumn.setCellFactory(column -> new TableCell<>() {
                private final ImageView imageView = new ImageView();
                private final HBox imageContainer = new HBox();

                {
                    imageContainer.setAlignment(Pos.CENTER); // Centrage horizontal
                    imageContainer.getChildren().add(imageView);
                }

                @Override
                protected void updateItem(String imageName, boolean empty) {
                    super.updateItem(imageName, empty);

                    if (empty || imageName == null || imageName.isEmpty()) {
                        setGraphic(null);
                    } else {
                        try {
                            // Si l'image est dans le dossier ressources
                            String path = "/matiere/" + imageName; // Utilisation d'un chemin relatif
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

                            // Récupérer le contrôleur de la vue ModifierMatiere.fxml
                            ModifierMatiereController controller = loader.getController();
                            controller.setMatiere(matiere); // passer la matière à modifier

                            // Récupérer la fenêtre actuelle et changer la scène
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
                                        getTableView().getItems().remove(matiere);
                                        getTableView().refresh();
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

                    // Centrer dans la HBox
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

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matières", e.getMessage());
        }
    }

    private void loadMatiereData() {
        try {
            MatiereList.clear();
            MatiereList.addAll(mt.afficher());
            matieresTable.setItems(MatiereList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des matières", e.getMessage());
        }
    }

    private void modifierMatiere(Matiere matiere) {

        System.out.println("Modifier : " + matiere.getNomM());
    }

    private void supprimerMatiere(Matiere matiere) {
        try {
            mt.supprimer(matiere.getId());
            matieresTable.getItems().remove(matiere);
            showAlert(Alert.AlertType.INFORMATION, "Suppression", "Matière supprimée avec succès", "");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la matière", e.getMessage());
        }
    }

    private void afficherCours(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AfficherCoursB.fxml"));
            Parent root = loader.load();

            // Récupération du contrôleur pour passer la matière
            AfficherCoursBController controller = loader.getController();
            controller.setMatiere(matiere);

            // Récupération de la scène actuelle
            Stage currentStage = (Stage) matieresTable.getScene().getWindow();

            // Remplacer le contenu de la scène
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

    public void handleAfficherAjoutMatiere(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AjoutMatiere.fxml");
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
}
