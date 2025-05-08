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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Enseignant;
import org.example.services.PredictService;
import org.example.services.ServiceApprenant;
import org.example.services.ServiceEnseignant;
import org.example.services.ServiceUser;
import org.example.services.EmailService;
import org.example.services.GeminiRapport;
import org.example.services.ServicePDF;
import org.example.utils.SessionManager;
import org.json.JSONObject;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AdminDashboardController {

    @FXML private Button logoutButton;
    @FXML private TableView<Object> usersTable;
    @FXML private TableColumn<Object, String> nomColumn;
    @FXML private TableColumn<Object, String> prenomColumn;
    @FXML private TableColumn<Object, String> emailColumn;
    @FXML private TableColumn<Object, String> rolesColumn;
    @FXML private TableColumn<Object, String> etatColumn;
    @FXML private TableColumn<Object, String> dateNaissanceColumn;
    @FXML private TableColumn<Object, String> niveauColumn;
    @FXML private TableColumn<Object, String> specialiteColumn;
    @FXML private TableColumn<Object, String> experienceColumn;
    @FXML private TableColumn<Object, String> lastActivityColumn;
    @FXML private TableColumn<Object, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Button predictButton;

    private ObservableList<Object> usersList = FXCollections.observableArrayList();
    private FilteredList<Object> filteredUsersList;
    private ServiceApprenant serviceApprenant = new ServiceApprenant();
    private ServiceEnseignant serviceEnseignant = new ServiceEnseignant();
    private ServiceUser serviceUser = new ServiceUser();
    private PredictService predictService = new PredictService();
    private EmailService emailService = new EmailService();
    private GeminiRapport geminiRapport = new GeminiRapport(HttpClient.newHttpClient(), null);

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

        // Configure Action column with Toggle, Delete, and Rapport buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleButton = new Button("Toggle Status");
            private final Button deleteButton = new Button("Supprimer");
            private final Button rapportButton = new Button("Rapport");
            private final HBox buttonsContainer = new HBox(5);

            {
                toggleButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                rapportButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

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

                deleteButton.setOnAction(event -> {
                    Object user = getTableRow().getItem();
                    if (user != null) {
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

                rapportButton.setOnAction(event -> {
                    Object user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            String name = user instanceof Apprenant
                                    ? ((Apprenant) user).getPrenom() + " " + ((Apprenant) user).getNom()
                                    : ((Enseignant) user).getPrenom() + " " + ((Enseignant) user).getNom();
                            int sessionCount = user instanceof Apprenant ? ((Apprenant) user).getSessionsCount() : 0;
                            int interactionCount = user instanceof Apprenant ? ((Apprenant) user).getInteractionsCount() : 0;
                            LocalDateTime lastActivity = user instanceof Apprenant ? ((Apprenant) user).getLastActivity() : null;
                            if (lastActivity == null) {
                                lastActivity = LocalDateTime.now();
                            }

                            String reportBody = geminiRapport.analyzeUserConnectivity(name, sessionCount, interactionCount, lastActivity);

                            String reportsDir = "reports";
                            Files.createDirectories(Paths.get(reportsDir));

                            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                            String safeFileName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
                            String pdfPath = reportsDir + File.separator + safeFileName + "_" + timestamp + ".pdf";
                            ServicePDF.generateRapportPDF(pdfPath, reportBody);

                            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport généré",
                                    "Le rapport de connectivité a été généré avec succès à : " + pdfPath);
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du rapport", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });

                buttonsContainer.getChildren().addAll(toggleButton, deleteButton, rapportButton);
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

        // Initialize FilteredList for search
        filteredUsersList = new FilteredList<>(usersList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsersList.setPredicate(user -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (user instanceof Apprenant) {
                    Apprenant apprenant = (Apprenant) user;
                    return apprenant.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            apprenant.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                            apprenant.getEmail().toLowerCase().contains(lowerCaseFilter);
                } else if (user instanceof Enseignant) {
                    Enseignant enseignant = (Enseignant) user;
                    return enseignant.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            enseignant.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                            enseignant.getEmail().toLowerCase().contains(lowerCaseFilter);
                }
                return false;
            });
        });

        // Set FilteredList to TableView
        usersTable.setItems(filteredUsersList);

        // Load data
        loadUsersData();
    }

    private void loadUsersData() {
        try {
            usersList.clear();
            usersList.addAll(serviceApprenant.afficher());
            usersList.addAll(serviceEnseignant.afficher());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des utilisateurs", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePredictChurn() {
        try {
            for (Object user : usersList) {
                if (!(user instanceof Apprenant)) {
                    continue;
                }

                Apprenant apprenant = (Apprenant) user;
                String email = apprenant.getEmail();
                String name = apprenant.getPrenom();

                int sessionCount = apprenant.getSessionsCount();
                int interactionsCount = apprenant.getInteractionsCount();
                LocalDateTime lastActivity = apprenant.getLastActivity();

                int daysSinceLastActivity = lastActivity != null
                        ? (int) ChronoUnit.DAYS.between(lastActivity, LocalDateTime.now())
                        : 999;

                try {
                    String response = predictService.predictChurn(sessionCount, daysSinceLastActivity, interactionsCount);
                    JSONObject jsonResponse = new JSONObject(response);
                    int prediction = jsonResponse.getInt("prediction");

                    if (prediction == 1) {
                        emailService.sendChurnWarningEmail(email, name);
                        showAlert(Alert.AlertType.INFORMATION, "Email envoyé", "Avertissement de churn envoyé",
                                String.format("Un email a été envoyé à %s pour prévenir un risque de départ.", email));
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la prédiction ou de l'envoi d'email",
                            String.format("Erreur pour %s : %s", email, e.getMessage()));
                    e.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Prédiction terminée", "Prédictions effectuées",
                    "La prédiction de churn a été exécutée pour tous les apprenants.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la prédiction", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear the session
            SessionManager.getInstance().logout();

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();

            // Show logout confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", "Impossible de charger l'écran de connexion: " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page", e.getMessage());
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
    private void handleAbonnementsNavigation(ActionEvent event) {
        loadPage(event, "/org/example/view/ListAbonnement.fxml");
    }

    public void goToUtilisateurs(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AdminDashboard.fxml");
    }

    @FXML
    public void afficherJeux(ActionEvent event) {
        loadPage(event, "/org/example/view/jeuxIndex.fxml");
    }

    public void Statique(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/statistiques-view.fxml");
    }
}