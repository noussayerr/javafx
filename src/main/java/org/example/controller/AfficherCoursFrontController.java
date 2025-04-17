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
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Matiere;
import org.example.services.ServiceCours;
import org.example.utils.SessionManager;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;


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

            coursContainer.getChildren().clear(); // vider avant d'ajouter pour éviter les doublons

            for (Cours cours : coursList) {
                Label nomLabel = new Label("Nom : " + cours.getNomC());
                nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                Label descLabel = new Label("Objectif : " + cours.getObjC());
                Label userLabel = new Label("Ajouté par : " +
                        (cours.getUser() != null ? cours.getUser().getNom() : "Inconnu"));



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
        }
    }

    private void supprimerCours(Cours cours) {
        try {
            serviceCours.supprimer(cours.getId()); // Supprimer de la base
            loadCours(); // Recharger après suppression
            showAlert(Alert.AlertType.INFORMATION, "Suppression", "Matière supprimée avec succès", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void modifierCours(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ModifierCours.fxml"));
            Parent root = loader.load();
            ModifierCoursController controller = loader.getController();
            controller.setCours(cours);
            controller.setCoursModifieListener(() -> { loadCours();
            });
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Cours");
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Rien ici pour l'instant
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
        }
    }

    @FXML
    private void handleProfile() {
        try {
            // Load the profile page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileEnseignant.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil");
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page de profil", e.getMessage());
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

    public void ajouterCours(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AjouterCoursF.fxml"));
            Parent root = loader.load();

            AjouterCoursFController controller = loader.getController();
            controller.setMatiereId(matiere.getId());

            // 👇 Ici on définit le callback qui recharge les cours quand un cours est ajouté
            controller.setCoursAjouteListener(() -> {
                loadCours(); // recharge les cours dès qu'un cours est ajouté
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Cours");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
