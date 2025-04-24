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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Fichier;
import org.example.entity.Matiere;
import org.example.services.ServiceCours;
import org.example.services.ServiceFichier;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherCoursFrontController implements Initializable {

    @FXML private VBox coursContainer;
    @FXML private Label matiereNom, matiereTitre, matiereDesc, matiereObj;
    @FXML private ImageView matiereImage;

    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    private Matiere matiere;
    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceFichier serviceFichier = new ServiceFichier();

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
        loadMatiereDetails();
        loadCours();
    }

    private void loadMatiereDetails() {
        if (matiere != null) {
            matiereNom.setText(matiere.getNomM());
            matiereTitre.setText(matiere.getTitreM());
            matiereDesc.setText(matiere.getDescM());
            matiereObj.setText(matiere.getObjM());

            if (matiere.getImgM() != null && !matiere.getImgM().isEmpty()) {
                try {
                    // Essayer comme fichier
                    File file = new File(matiere.getImgM());
                    if (file.exists()) {
                        matiereImage.setImage(new Image(file.toURI().toString()));
                    }
                    // Essayer comme ressource
                    else {
                        InputStream stream = getClass().getResourceAsStream(matiere.getImgM());
                        if (stream != null) {
                            matiereImage.setImage(new Image(stream));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur de chargement d'image: " + e.getMessage());
                    matiereImage.setVisible(false);
                }
            } else {
                matiereImage.setVisible(false);
            }
        }
    }

    private void loadCours() {
        try {
            coursContainer.getChildren().clear();
            List<Cours> coursList = serviceCours.afficherParMatiere(matiere.getId());

            for (Cours cours : coursList) {
                VBox coursBox = createCoursBox(cours);
                coursContainer.getChildren().add(coursBox);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des cours échoué", e.getMessage());
        }
    }

    private VBox createCoursBox(Cours cours) {
        VBox coursBox = new VBox();
        coursBox.getStyleClass().add("cours-box");
        coursBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-spacing: 10;");

        // Header
        HBox header = new HBox(10);
        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(
                createStyledLabel(cours.getNomC(), "-fx-font-size: 18px; -fx-font-weight: bold;"),
                createStyledLabel("Objectif: " + cours.getObjC(), "-fx-text-fill: #666;"),
                createStyledLabel(String.format("Niveau: %s | Type: %s", cours.getNivC(), cours.getType()), "-fx-text-fill: #666;")
        );

        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(
                createButton("Afficher fichiers", e -> toggleFichiers(coursBox)),
                createButton("Ajouter fichier", e -> ajouterFichier(cours)),
                createButton("Modifier", e -> modifierCours(cours)),
                createButton("Supprimer", e -> supprimerCours(cours))
        );

        header.getChildren().addAll(infoBox, buttonBox);
        coursBox.getChildren().add(header);

        // Fichiers (cachés par défaut)
        VBox fichiersContainer = createFichiersContainer(cours);
        coursBox.getChildren().add(fichiersContainer);

        return coursBox;
    }

    private VBox createFichiersContainer(Cours cours) {
        VBox container = new VBox(5);
        container.setVisible(false);

        try {
            List<Fichier> fichiers = serviceFichier.getFichiersByCours(cours.getId());
            if (!fichiers.isEmpty()) {
                for (Fichier fichier : fichiers) {
                    HBox fichierBox = new HBox(10);
                    fichierBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 8;");

                    Label fileLabel = createStyledLabel(fichier.getNomF() + " (" + fichier.getType() + ")", "");
                    Button downloadBtn = createButton("Télécharger", e -> telechargerFichier(fichier));

                    fichierBox.getChildren().addAll(fileLabel, downloadBtn);
                    container.getChildren().add(fichierBox);
                }
            } else {
                container.getChildren().add(createStyledLabel("Aucun fichier disponible", "-fx-font-style: italic;"));
            }
        } catch (Exception e) {
            container.getChildren().add(createStyledLabel("Erreur de chargement des fichiers", "-fx-text-fill: red;"));
        }

        return container;
    }

    // Méthodes utilitaires
    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private Button createButton(String text, javafx.event.EventHandler<ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setOnAction(handler);
        return btn;
    }

    private void toggleFichiers(VBox coursBox) {
        VBox fichiersContainer = (VBox) coursBox.getChildren().get(1);
        fichiersContainer.setVisible(!fichiersContainer.isVisible());
    }

    private void ajouterFichier(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AjouterFichier.fxml"));
            Parent root = loader.load();

            AjouterFichierController controller = loader.getController();
            controller.setCours(cours);
            controller.setFichierAjouteListener(this::loadCours);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un fichier");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    private void telechargerFichier(Fichier fichier) {
        if (fichier.getUrlF() == null || fichier.getUrlF().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier associé", "");
            return;
        }

        File file = new File(fichier.getUrlF());
        if (file.exists()) {
            // Implémentez la logique de téléchargement ici
            showAlert(Alert.AlertType.INFORMATION, "Téléchargement",
                    "Prêt à télécharger", fichier.getNomF());
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Fichier introuvable", "Chemin: " + fichier.getUrlF());
        }
    }

    // Les autres méthodes restent inchangées...
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
            controller.setMatiere(matiere);
            controller.setCoursAjouteListener(this::loadCours);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Cours");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'ajout", e.getMessage());
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
