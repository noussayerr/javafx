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
import java.io.InputStream;
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


    //Ajoutez cette méthode helper pour charger l'image par défaut
    private void loadDefaultImage(ImageView imageView) {
        try {
            InputStream defaultStream = getClass().getResourceAsStream("/images/default-subject.png");
            if (defaultStream != null) {
                imageView.setImage(new Image(defaultStream));
            } else {
                System.err.println("Default image not found in resources");
            }
        } catch (Exception ex) {
            System.err.println("Failed to load default image: " + ex.getMessage());
        }
    }

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

                // Create ImageView outside the try-catch
                ImageView imageView = new ImageView();
                imageView.setFitWidth(150);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);

                // Dans la méthode initialize(), remplacez le bloc try-catch d'image par ceci :
                try {
                    // Try to load the subject image
                    if (matiere.getImgM() != null && !matiere.getImgM().isEmpty()) {
                        // Solution 1: Chemin absolu depuis le système de fichiers
                        File file = new File("src/main/resources/matiere/" + matiere.getImgM());
                        if (file.exists()) {
                            imageView.setImage(new Image(file.toURI().toString()));
                        }
                        // Solution 2: Chemin relatif depuis les ressources
                        else {
                            String imagePath = "/matiere/" + matiere.getImgM();
                            InputStream is = getClass().getResourceAsStream(imagePath);
                            if (is != null) {
                                imageView.setImage(new Image(is));
                            } else {
                                // Solution 3: Image par défaut si les autres échouent
                                loadDefaultImage(imageView);
                            }
                        }
                    } else {
                        loadDefaultImage(imageView);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image for " + matiere.getNomM() + ": " + e.getMessage());
                    loadDefaultImage(imageView);
                }

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
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des cours", e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue", e.getMessage());
                    }
                });

                matiereContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", "Problem loading subjects", e.getMessage());
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
