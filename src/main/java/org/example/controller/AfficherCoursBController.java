package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Matiere;
import org.example.services.ServiceCours;
import org.example.services.ServiceMatiere;
import org.example.utils.MyDatabase;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AfficherCoursBController {

    @FXML
    private TableView<Cours> CoursTable;
    @FXML
    private TableColumn<Cours, String> nomColumn;
    @FXML
    private TableColumn<Cours, String> objColumn;
    @FXML
    private TableColumn<Cours, String> nivColumn;
    @FXML
    private TableColumn<Cours, String> typeColumn;
    @FXML
    private TableColumn<Cours, String> fichierColumn;
    @FXML
    private TableColumn<Cours, Void> actionColumn;

    @FXML
    private Button logoutButton;
    private Connection connection;

    private Matiere selectedMatiere;
    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceMatiere matiereService =new ServiceMatiere();

    public void initialize() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomC"));
        objColumn.setCellValueFactory(new PropertyValueFactory<>("objC"));
        nivColumn.setCellValueFactory(new PropertyValueFactory<>("nivC"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        fichierColumn.setCellValueFactory(new PropertyValueFactory<>("fichC"));
        initActionColumn();
    }

    public void setMatiere(Matiere matiere) {
        this.selectedMatiere = matiere;
        loadCours();
    }

    private void loadCours() {
        try {
            List<Cours> coursList = serviceCours.getCoursParMatiere(selectedMatiere.getId());
            ObservableList<Cours> data = FXCollections.observableArrayList(coursList);
            CoursTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement des cours", e.getMessage());
        }
    }



    private void initActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    System.out.println("Modifier cours: " + cours.getNomC());
                    // Tu peux charger ici une autre interface pour modifier le cours
                });

                deleteButton.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    boolean confirmed = showConfirmation("Voulez-vous vraiment supprimer ce cours ?");
                    if (confirmed) {
                        try {
                            serviceCours.supprimer(cours.getId());
                            getTableView().getItems().remove(cours);
                            getTableView().refresh();
                            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours supprimé", "Le cours a été supprimé avec succès.");
                        } catch (SQLException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression", e.getMessage());
                            e.printStackTrace();
                        }
                        loadCours(); // rafraîchir
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, editButton, deleteButton);
                    setGraphic(hbox);
                }
            }
        });
    }

    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(message);
        alert.setContentText("Cliquez sur OK pour continuer.");
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    @FXML
    private void goToMatiere(ActionEvent event) {
        try {
            // Vérifiez si la connexion est valide avant de charger les matières
            if (connection == null || connection.isClosed()) {
                connection = MyDatabase.getInstance().getConnection(); // Ouvrir la connexion
            }

            List<Matiere> matieres = matiereService.afficher();
            // Afficher les matières dans l'interface
            loadPage(event, "/org/example/view/ListeMatiere.fxml");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'affichage des matières", e.getMessage());
        }
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
}
