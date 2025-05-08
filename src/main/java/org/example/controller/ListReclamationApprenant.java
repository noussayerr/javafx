package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Reclamation;
import org.example.entity.User;
import org.example.services.ServiceReclamation;
import org.example.services.ServiceTransaction;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListReclamationApprenant implements Initializable {
    @FXML
    private TableView<Reclamation> tableReclamations;
    @FXML
    private TableColumn<Reclamation, String> colTitre;
    @FXML
    private TableColumn<Reclamation, String> colDescription;
    @FXML
    private TableColumn<Reclamation, String> colEtat;

    @FXML private TabPane tabPane;

    private final ServiceReclamation serviceReclamation = new ServiceReclamation();
    private final ServiceTransaction serviceTransaction = new ServiceTransaction();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        int apprenantId;

        try {
            Apprenant currentApprenant = serviceTransaction.getApprenantById(currentUser.getId());
            apprenantId = currentApprenant.getId();

            List<Reclamation> toutes = serviceReclamation.afficher();
            List<Reclamation> reclamationsFiltrees = toutes.stream()
                    .filter(rec -> rec.getApprenant() != null && rec.getApprenant().getId() == apprenantId)
                    .collect(Collectors.toList());

            tableReclamations.getItems().addAll(reclamationsFiltrees);

        } catch (SQLException e) {
            e.printStackTrace();
            // Optionnel : afficher une alerte utilisateur
        }
    }

    public void gotodash(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void goAccueil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goMatiereF(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/MatiereFrontA.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée: " + e.getMessage());
        }
    }

    @FXML
    private void goAbonnement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AbonnementApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goReclamation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AjoutReclamation.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showGames(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxApprenant.fxml"));
            AnchorPane listPane = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des jeux: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            SessionManager.getInstance().logout();
            loadPage("/org/example/view/Login.fxml", "Connexion");
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion", "Déconnecté avec succès");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de déconnexion: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void ouvrirListeEvenements(ActionEvent event) throws IOException {
        loadView("Evenement-list.fxml", event);
    }

    private void loadView(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/" + fxmlFile));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    private void handleProfile(ActionEvent event) throws IOException {
        loadView("ProfileApprenant.fxml", event);
    }

}

