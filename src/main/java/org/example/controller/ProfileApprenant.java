package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.User;
import org.example.services.ServiceApprenant;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;

public class ProfileApprenant {

    @FXML
    private ImageView profileImageView;
    @FXML
    private TextField photoProfilField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField dateNaissanceField;
    @FXML
    private TextField telephoneField;
    @FXML
    private ComboBox<String> niveauComboBox;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button handleAcceuil;
    @FXML
    private Button handleProfile;
    @FXML
    private Button logoutButton;

    private ServiceApprenant serviceApprenant;
    private User currentUser;
    @FXML private Button profileButton;

    public void initialize() {
        // Initialiser le service
        serviceApprenant = new ServiceApprenant();
        handleAcceuil.setOnAction(event -> loadAcceuil());
        handleProfile.setOnAction(event -> loadProfile());

        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialiser les champs avec les données de l'utilisateur
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            emailField.setEditable(false);

            if (currentUser.getDateNaissance() != null) {
                dateNaissanceField.setText(currentUser.getDateNaissance());
            }

            telephoneField.setText(String.valueOf(currentUser.getTelephone()));

            // Initialiser la ComboBox des niveaux
            niveauComboBox.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
            // Sélectionner le niveau actuel de l'utilisateur, si disponible
            if (currentUser instanceof Apprenant && ((Apprenant) currentUser).getNiveau() != null) {
                niveauComboBox.setValue(((Apprenant) currentUser).getNiveau());
            }

            // Charger la photo de profil si elle existe
            if (currentUser.getPhotoProfil() != null && !currentUser.getPhotoProfil().isEmpty()) {
                File imageFile = new File(currentUser.getPhotoProfil());
                if (imageFile.exists()) {
                    try {

                        String imagePath =currentUser.getPhotoProfil();
                        System.out.println(imagePath);
                        Image image = new Image(currentUser.getPhotoProfil());
                        profileImageView.setImage(image);
                        photoProfilField.setText(currentUser.getPhotoProfil() );
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Afficher une image par défaut si l'image ne peut pas être chargée
                        profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/event1.jpg")));
                    }
                } else {
                    // Si le fichier n'existe pas, utiliser une image par défaut
                    profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/event1.jpg")));
                }
            } else {
                // Si aucune photo n'est définie, utiliser une image par défaut
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/event1.jpg")));
            }
        }
    }
    @FXML
    private void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            photoProfilField.setText(selectedFile.getAbsolutePath());
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentUser != null) {
            // Convertir User en Apprenant
            Apprenant apprenant = convertUserToApprenant(currentUser);

            // Mettre à jour les données de l'apprenant avec les nouvelles valeurs
            apprenant.setNom(nomField.getText());
            apprenant.setPrenom(prenomField.getText());
            apprenant.setEmail(emailField.getText());
            try {
                apprenant.setTelephone(Integer.parseInt(telephoneField.getText()));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Téléphone invalide", "Veuillez entrer un numéro de téléphone valide.");
                return;
            }
            apprenant.setNiveau(niveauComboBox.getValue());
            apprenant.setDateNaissance(dateNaissanceField.getText());

            if (!passwordField.getText().isEmpty()) {
                apprenant.setPassword(passwordField.getText());
            }

            if (!photoProfilField.getText().isEmpty()) {
                File imageFile = new File(photoProfilField.getText());
                if (imageFile.exists()) {
                    apprenant.setPhotoProfil(photoProfilField.getText());
                } else {
                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Image introuvable", "La photo de profil sélectionnée n'existe pas.");
                }
            }

            try {
                serviceApprenant.modifier(apprenant);
                // Mettre à jour l'utilisateur dans la session
                SessionManager.getInstance().setCurrentUser(apprenant);
                currentUser = apprenant;

                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Profil mis à jour avec succès!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Une erreur est survenue lors de la mise à jour du profil.");
            }
        }
    }

    private Apprenant convertUserToApprenant(User user) {
        Apprenant apprenant = new Apprenant();

        apprenant.setId(user.getId());
        apprenant.setNom(user.getNom());
        apprenant.setPrenom(user.getPrenom());
        apprenant.setEmail(user.getEmail());
        apprenant.setPassword(user.getPassword());
        apprenant.setTelephone(user.getTelephone());
        apprenant.setDateNaissance(dateNaissanceField.getText());
        apprenant.setNiveau(niveauComboBox.getValue());
        apprenant.setPhotoProfil(user.getPhotoProfil());
        apprenant.setRoles(user.getRoles());

        return apprenant;
    }

    @FXML private void loadAcceuil() {
        try {
            // Chargement de la page ApprenantDashboard
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));
            Stage stage = (Stage) handleAcceuil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page Accueil");
        }
    }

     private void loadProfile() {
        try {
            // Chargement de la page Profil
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/Profil.fxml"));
            Stage stage = (Stage) handleProfile.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page Profil");
        }
    }

    @FXML
    private void logout() {
        try {
            // Effacer la session utilisateur
            SessionManager.getInstance().logout();

            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/Login.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle depuis le bouton existant
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();

            // Afficher un message de confirmation
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie",
                    "Vous avez été déconnecté avec succès.", "");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void showGames(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxApprenant.fxml"));
            AnchorPane listPane = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirListeEvenements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Liste des Événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
    public void goMatiereF(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/MatiereFrontA.fxml");
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
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileApprenant.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil");
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page de profil", e.getMessage());
        }
    }

    public void goAccueil(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ApprenantDashboard.fxml");
    }
}