package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Matiere;
import org.example.services.ServiceCours;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherCoursFrontController implements Initializable {

    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private VBox coursContainer;

    private Matiere matiere;
    private final ServiceCours serviceCours = new ServiceCours();

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
        loadCours();
    }

    private void loadCours() {
        try {
            List<Cours> coursList = serviceCours.afficherParMatiere(matiere.getId());
            coursContainer.getChildren().clear();

            for (Cours cours : coursList) {
                Label nomLabel = new Label("Nom : " + cours.getNomC());
                nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label descLabel = new Label("Objectif : " + cours.getObjC());
                Label userLabel = new Label("Ajouté par : " + cours.getUser().getNom());

                Button editButton = new Button("Modifier");
                editButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; -fx-background-radius: 6;");
                editButton.setOnAction(e -> modifierCours(cours));

                Button deleteButton = new Button("Supprimer");
                deleteButton.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-background-radius: 6;");
                deleteButton.setOnAction(e -> supprimerCours(cours));

                HBox buttonBox = new HBox(10, editButton, deleteButton);
                VBox box = new VBox(5, nomLabel, descLabel, userLabel, buttonBox);
                box.setStyle("-fx-background-color: #fff; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

                coursContainer.getChildren().add(box);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des cours", e.getMessage());
        }
    }

    private void supprimerCours(Cours cours) {
        try {
            serviceCours.supprimer(cours.getId());
            loadCours();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours supprimé", "Le cours a été supprimé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression échouée", e.getMessage());
        }
    }

    private void modifierCours(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ModifierCours.fxml"));
            Parent root = loader.load();

            ModifierCoursController controller = loader.getController();
            controller.setCours(cours);
            controller.setCoursModifieListener(this::loadCours);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Cours");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification", e.getMessage());
        }
    }

    @FXML
    private void ajouterCours(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AjouterCoursF.fxml"));
            Parent root = loader.load();

            AjouterCoursFController controller = loader.getController();
            controller.setMatiere(matiere); // lie la matière actuelle
            controller.setCoursAjouteListener(this::loadCours); // recharge les cours après ajout

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Cours");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d’ajout", e.getMessage());
        }
    }


    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileEnseignant.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil");
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement du profil échoué", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion", "Déconnecté avec succès", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }

    public void goMatiereF(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ListeMatiereF.fxml");
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation si nécessaire
    }
}
