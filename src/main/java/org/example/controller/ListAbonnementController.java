package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.entity.Abonnement;
import org.example.services.ServiceAbonnement;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListAbonnementController implements Initializable {

    @FXML
    private TableView<Abonnement> tableAbonnements;
    @FXML
    private TableColumn<Abonnement, Integer> colId;
    @FXML
    private TableColumn<Abonnement, String> colTitre;
    @FXML
    private TableColumn<Abonnement, Integer> colPrix;
    @FXML
    private TableColumn<Abonnement, String> colDescription;
    @FXML
    private TableColumn<Abonnement, String> colDuration;
    @FXML
    private TableColumn<Abonnement, Void> colActions;
    @FXML
    private TableColumn<Abonnement, Void> colPromotion;
    private ServiceAbonnement serviceAbonnement = new ServiceAbonnement();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des colonnes simples
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreAbonnement"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        // Remplir la table avec les abonnements
        try {
            List<Abonnement> abonnements = serviceAbonnement.afficher();
            ObservableList<Abonnement> observableList = FXCollections.observableArrayList(abonnements);
            tableAbonnements.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Colonne Promotion (ajouter / modifier)
        colPromotion.setCellFactory(param -> new TableCell<Abonnement, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Abonnement selected = getTableView().getItems().get(getIndex());

                if (selected != null) {
                    boolean hasPromo = selected.getPromotion() != null;
                    String label = hasPromo ? "Modifier Promotion" : "Ajouter Promotion";
                    Button btnPromo = new Button(label);

                    btnPromo.setOnAction(event -> {
                        try {
                            String fxmlFile = hasPromo ? "/org/example/view/ModifierPromotion.fxml"
                                    : "/org/example/view/AjoutPromotion.fxml";

                            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));

                            if (hasPromo) {
                                ModifierPromotion controller = new ModifierPromotion(selected);
                                loader.setController(controller);
                            } else {
                                AjoutPromotionController controller = new AjoutPromotionController(selected);
                                loader.setController(controller);
                            }

                            Scene promotionScene = new Scene(loader.load());
                            Stage stage = (Stage) getTableView().getScene().getWindow();
                            stage.setScene(promotionScene);
                            stage.setTitle(label);

                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorAlert("Erreur lors du chargement", "Impossible d'ouvrir la scène de promotion.");
                        }
                    });

                    setGraphic(btnPromo);
                } else {
                    setGraphic(null);
                }
            }
        });

        // Colonne Actions (Modifier / Supprimer)
        colActions.setCellFactory(param -> new TableCell<Abonnement, Void>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(10, btnModifier, btnSupprimer);

            {
                // Action du bouton Modifier
                btnModifier.setOnAction(event -> {
                    Abonnement selected = getTableView().getItems().get(getIndex());
                    if (selected != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ModifierAbonnement.fxml"));
                            ModifierAbonnementController controller = new ModifierAbonnementController(selected);
                            loader.setController(controller);

                            Scene modifierScene = new Scene(loader.load());
                            Stage stage = (Stage) getTableView().getScene().getWindow();
                            stage.setScene(modifierScene);
                            stage.setTitle("Modifier Abonnement");
                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorAlert("Erreur de chargement de la scène de modification",
                                    "Impossible de charger la scène de modification de l'abonnement.");
                        }
                    }
                });

                // Action du bouton Supprimer
                btnSupprimer.setOnAction(e -> {
                    Abonnement selected = tableAbonnements.getItems().get(getIndex());
                    if (selected != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Confirmation");
                        confirm.setHeaderText("Supprimer l’abonnement ?");
                        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l’abonnement : " + selected.getTitreAbonnement() + " ?");
                        Optional<ButtonType> result = confirm.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                serviceAbonnement.supprimer(selected.getId());
                                tableAbonnements.getItems().remove(selected);
                            } catch (SQLException ex) {
                                showErrorAlert("Erreur de suppression", "L'abonnement est utilisé par un ou plusieurs apprenants.");
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // Méthode utilitaire pour afficher les erreurs
    private void showErrorAlert(String header, String content) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Erreur");
        error.setHeaderText(header);
        error.setContentText(content);
        error.showAndWait();
    }
    @FXML
    private void goToAjoutAbonnement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AjoutAbonnement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private Button logoutButton; // Inject the button with fx:id="logoutButton"

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