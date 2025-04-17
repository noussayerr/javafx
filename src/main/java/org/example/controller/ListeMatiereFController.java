package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Matiere;
import org.example.services.ServiceCours;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ListeMatiereFController{

    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private final ServiceCours serviceCours = new ServiceCours();
    private Matiere selectedMatiere;

    @FXML private TilePane matiereContainer;


    @FXML
    public void initialize() {
        try {
            List<Matiere> matieres = serviceMatiere.afficher();

            for (Matiere matiere : matieres) {
                VBox card = new VBox();
                card.setSpacing(10);
                card.setPadding(new Insets(10));
                card.setAlignment(Pos.CENTER);
                card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

                ImageView imageView = new ImageView(new Image("/matiere/" + matiere.getImgM()));
                imageView.setFitWidth(150);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);

                Label nom = new Label(matiere.getNomM());
                nom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label titre = new Label(matiere.getTitreM());
                titre.setStyle("-fx-text-fill: #666;");

                card.getChildren().addAll(imageView, nom, titre);
                card.setOnMouseClicked(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AfficherCoursFront.fxml"));
                        Parent root = loader.load();
                        AfficherCoursFrontController controller = loader.getController();
                        controller.setMatiere(matiere);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setMaximized(true);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                matiereContainer.getChildren().add(card);
            }



        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Problème lors du chargement des matières", e.getMessage());
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


}
