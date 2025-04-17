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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Enseignant;
import org.example.services.ServiceApprenant;
import org.example.services.ServiceEnseignant;
import org.example.services.ServiceUser;
import org.example.utils.SessionManager;
import javafx.scene.layout.StackPane;


import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<Object> usersTable;

    @FXML
    private TableColumn<Object, String> nomColumn;

    @FXML
    private TableColumn<Object, String> prenomColumn;

    @FXML
    private TableColumn<Object, String> emailColumn;

    @FXML
    private TableColumn<Object, String> rolesColumn;

    @FXML
    private TableColumn<Object, String> etatColumn;

    @FXML
    private TableColumn<Object, String> dateNaissanceColumn;

    @FXML
    private TableColumn<Object, String> niveauColumn;

    @FXML
    private TableColumn<Object, String> specialiteColumn;

    @FXML
    private TableColumn<Object, String> experienceColumn;

    @FXML
    private TableColumn<Object, String> lastActivityColumn;

    @FXML
    private TableColumn<Object, Void> actionColumn;

    private ObservableList<Object> usersList = FXCollections.observableArrayList();
    private ServiceApprenant serviceApprenant = new ServiceApprenant();
    private ServiceEnseignant serviceEnseignant = new ServiceEnseignant();
    private ServiceUser serviceUser = new ServiceUser();


    @FXML
    public void initialize() {
        // Configure columns
        nomColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? ((Apprenant) user).getNom() : ((Enseignant) user).getNom());
        });

        prenomColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? ((Apprenant) user).getPrenom() : ((Enseignant) user).getPrenom());
        });

        emailColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? ((Apprenant) user).getEmail() : ((Enseignant) user).getEmail());
        });

        rolesColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? String.join(", ", ((Apprenant) user).getRoles())
                            : String.join(", ", ((Enseignant) user).getRoles()));
        });

        etatColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? ((Apprenant) user).getEtat() : ((Enseignant) user).getEtat());
        });

        dateNaissanceColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? ((Apprenant) user).getDateNaissance() : ((Enseignant) user).getDateNaissance());
        });

        niveauColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Apprenant ? ((Apprenant) user).getNiveau() : "N/A");
        });

        specialiteColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Enseignant ? ((Enseignant) user).getSpecialite() : "N/A");
        });

        experienceColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    user instanceof Enseignant ? ((Enseignant) user).getExperience() : "N/A");
        });

        lastActivityColumn.setCellValueFactory(cellData -> {
            Object user = cellData.getValue();
            String lastActivity = user instanceof Apprenant && ((Apprenant) user).getLastActivity() != null
                    ? ((Apprenant) user).getLastActivity().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : user instanceof Enseignant && ((Enseignant) user).getLastActivity() != null
                    ? ((Enseignant) user).getLastActivity().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "N/A";
            return new javafx.beans.property.SimpleStringProperty(lastActivity);
        });

        // Configure Action column with Toggle and Delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleButton = new Button("Toggle Status");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsContainer = new HBox(5); // Spacing between buttons

            {
                toggleButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Toggle Status button action
                toggleButton.setOnAction(event -> {
                    Object user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            int userId = user instanceof Apprenant ? ((Apprenant) user).getId() : ((Enseignant) user).getId();
                            boolean success = serviceUser.toggleUserStatus(userId);
                            if (success) {
                                String newState = user instanceof Apprenant
                                        ? ((Apprenant) user).getEtat().equalsIgnoreCase("actif") ? "inactif" : "actif"
                                        : ((Enseignant) user).getEtat().equalsIgnoreCase("actif") ? "inactif" : "actif";
                                if (user instanceof Apprenant) {
                                    ((Apprenant) user).setEtat(newState);
                                } else {
                                    ((Enseignant) user).setEtat(newState);
                                }
                                usersTable.refresh();
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Statut modifié", "Le statut de l'utilisateur a été mis à jour.");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification", "Impossible de modifier le statut.");
                            }
                        } catch (SQLException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur SQL", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });

                // Delete button action
                deleteButton.setOnAction(event -> {
                    Object user = getTableRow().getItem();
                    if (user != null) {
                        // Show confirmation dialog
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirmation de suppression");
                        confirmation.setHeaderText("Voulez-vous vraiment supprimer cet utilisateur ?");
                        confirmation.setContentText("Cette action est irréversible.");
                        confirmation.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    int userId = user instanceof Apprenant ? ((Apprenant) user).getId() : ((Enseignant) user).getId();
                                    if (user instanceof Apprenant) {
                                        serviceApprenant.supprimer(userId);
                                    } else {
                                        serviceEnseignant.supprimer(userId);
                                    }
                                    // Remove user from the list and refresh the table
                                    usersList.remove(user);
                                    usersTable.refresh();
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé", "L'utilisateur a été supprimé avec succès.");
                                } catch (SQLException e) {
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression", e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

                buttonsContainer.getChildren().addAll(toggleButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsContainer);
                }
            }
        });

        // Load data
        loadUsersData();
    }

    private void loadUsersData() {
        try {
            usersList.clear();
            usersList.addAll(serviceApprenant.afficher());
            usersList.addAll(serviceEnseignant.afficher());
            usersTable.setItems(usersList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des utilisateurs", e.getMessage());
            e.printStackTrace();
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

