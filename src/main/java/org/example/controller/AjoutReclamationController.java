package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Reclamation;
import org.example.entity.TitreRec;
import org.example.entity.User;
import org.example.services.*;
import org.example.utils.MyDatabase;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AjoutReclamationController implements Initializable {
    @FXML private ComboBox<TitreRec> comboTitreRec;
    @FXML private TextArea txtDescription;
    @FXML private Label lblTitreErreur;
    @FXML private Label lblDescriptionErreur;
    @FXML private TabPane tabPane;
    private ServiceTitreRec serviceTitreRec=new ServiceTitreRec();
    private ServiceReclamation serviceReclamation=new ServiceReclamation();
    private ServiceApprenant serviceApprenant=new ServiceApprenant();
    private ServiceTransaction serviceTransaction=new ServiceTransaction();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            serviceTitreRec.promouvoirTitresFrequents();
            List<TitreRec> titres = serviceTitreRec.afficher();
            comboTitreRec.getItems().addAll(titres);

            // Ajouter une option spéciale "Autre..."
            TitreRec autre = new TitreRec(0, "Autre...");
            comboTitreRec.getItems().add(autre);

            comboTitreRec.setOnAction(event -> {
                TitreRec selected = comboTitreRec.getValue();
                if (selected != null && "Autre...".equals(selected.getTitrerec())) {
                    // Afficher une boîte de dialogue pour saisir un nouveau titre
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Nouveau titre");
                    dialog.setHeaderText("Ajouter un nouveau titre de réclamation");
                    dialog.setContentText("Titre :");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(titre -> {
                        String trimmedTitre = titre.trim();
                        if (!trimmedTitre.isEmpty()) {
                            try {
                                // Ajouter le nouveau titre dans la base
                                TitreRec nouveauTitre = new TitreRec();
                                nouveauTitre.setTitrerec(trimmedTitre);
                                //serviceTitreRec.ajouter(nouveauTitre); // tu dois avoir cette méthode

                                // Récupérer l'objet avec ID si nécessaire
                                List<TitreRec> titresMisAJour = serviceTitreRec.afficher();
                                comboTitreRec.getItems().clear();
                                comboTitreRec.getItems().addAll(titresMisAJour);
                                comboTitreRec.getItems().add(autre);

                                // Sélectionner le nouveau titre
                                for (TitreRec t : titresMisAJour) {
                                    if (t.getTitrerec().equals(trimmedTitre)) {
                                        comboTitreRec.setValue(t);
                                        break;
                                    }
                                }

                            } catch (SQLException e) {
                                showAlert("Erreur", "Impossible d'ajouter le titre", Alert.AlertType.ERROR);
                                e.printStackTrace();
                            }
                        } else {
                            comboTitreRec.setValue(null); // Réinitialiser si vide
                        }
                    });
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les titres", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void ajouterReclamation(ActionEvent event) {
        TitreRec titreSelectionne = comboTitreRec.getValue();
        String description = txtDescription.getText().trim();

        boolean hasError = false;

        // Réinitialiser les styles et les messages
        comboTitreRec.setStyle("");
        txtDescription.setStyle("");
        lblTitreErreur.setVisible(false);
        lblDescriptionErreur.setVisible(false);

        // Vérification du titre
        if (titreSelectionne == null) {
            comboTitreRec.setStyle("-fx-border-color: red;");
            lblTitreErreur.setText("Veuillez sélectionner un titre.");
            lblTitreErreur.setVisible(true);
            hasError = true;
        }

        // Vérification de la description
        if (description.isEmpty() || description.length() < 10) {
            txtDescription.setStyle("-fx-border-color: red;");
            lblDescriptionErreur.setText("La description doit contenir au moins 10 caractères.");
            lblDescriptionErreur.setVisible(true);
            hasError = true;
        }

        if (hasError) {
           // showAlert("Champs invalides", "Veuillez corriger les erreurs avant de soumettre.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Reclamation rec = new Reclamation();
            rec.setTitle(titreSelectionne.getTitrerec());
            rec.setDescription(description);
            rec.setEtat("en attente");
            User currentUser = SessionManager.getInstance().getCurrentUser();

            Apprenant currentApprenant=serviceTransaction.getApprenantById(currentUser.getId());
            rec.setApprenant(currentApprenant);
            ServiceReclamation serviceReclamation = new ServiceReclamation();
            serviceReclamation.ajouter(rec);

            showAlert("Succès", "Réclamation ajoutée avec succès.", Alert.AlertType.INFORMATION);

            comboTitreRec.setValue(null);
            txtDescription.clear();
            comboTitreRec.setStyle("");
            txtDescription.setStyle("");
            lblTitreErreur.setVisible(false);
            lblDescriptionErreur.setVisible(false);

        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void gotoReclamation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ListReclamationApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
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
