package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entity.Forum;
import org.example.services.ServiceForum;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ApprenantDashboardController {
    @FXML
    public Button btnReclamation;
    // Éléments du FXML
    @FXML
    private Button logoutButton;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label coursesCountLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private ListView<?> recentCoursesList;
    @FXML
    private Button profileButton;

    // Forum-related elements
    @FXML
    private Button forumsButton;
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox dashboardContent;
    @FXML
    private VBox forumsContent;
    @FXML
    private VBox forumsContainer;

    private ServiceForum serviceForum = new ServiceForum();

    // Méthode d'initialisation (optionnelle)
    @FXML
    private void initialize() {
        // Vous pouvez initialiser vos éléments ici si besoin
        // Par exemple :
        // usernameLabel.setText(SessionManager.getInstance().getCurrentUsername());
        // coursesCountLabel.setText("12");
        // progressLabel.setText("75%");
    }

    @FXML
    private void logout() {
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

    public void handleExploreCourses(ActionEvent actionEvent) {
        System.out.println("Naviguer vers gestion des matières...");
        loadPage(actionEvent, "/org/example/view/MatiereFrontA.fxml");
    }

    @FXML
    private void showForums() {
        try {
            // Clear previous forums
            forumsContainer.getChildren().clear();

            // Fetch forums
            List<Forum> forums = serviceForum.afficher();
            if (forums.isEmpty()) {
                Label noForumsLabel = new Label("Aucun forum disponible.");
                noForumsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7a7a7a;");
                forumsContainer.getChildren().add(noForumsLabel);
            } else {
                for (Forum forum : forums) {
                    VBox forumCard = createForumCard(forum);
                    forumsContainer.getChildren().add(forumCard);
                }
            }

            // Transition to forums content
            transitionToContent(dashboardContent, forumsContent);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des forums", e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createForumCard(Forum forum) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(600);

        Label nameLabel = new Label(forum.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3a4a6d;");

        Button viewButton = new Button("Voir");
        viewButton.setStyle("-fx-background-color: #4a6baf; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 15;");
        viewButton.setOnAction(e -> openChannelsWindow(forum));

        HBox buttonBox = new HBox(viewButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        card.getChildren().addAll(nameLabel, buttonBox);
        return card;
    }

    private void openChannelsWindow(Forum forum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ForumChannels.fxml"));
            Parent root = loader.load();

            ForumChannelsController controller = loader.getController();
            controller.setForum(forum);

            Stage stage = new Stage();
            stage.setTitle("Channels de " + forum.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // Apply transition
            root.setOpacity(0);
            root.setScaleX(0.8);
            root.setScaleY(0.8);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), root);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            ParallelTransition transition = new ParallelTransition(fadeIn, scaleIn);
            transition.play();

            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre", e.getMessage());
            e.printStackTrace();
        }
    }

    private void transitionToContent(Node fromNode, Node toNode) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), fromNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), fromNode);
        slideOut.setFromX(0);
        slideOut.setToX(-200);

        ParallelTransition parallelOut = new ParallelTransition(fadeOut, slideOut);
        parallelOut.setOnFinished(e -> {
            fromNode.setVisible(false);
            fromNode.setManaged(false);
            toNode.setVisible(true);
            toNode.setManaged(true);

            toNode.setOpacity(0);
            toNode.setTranslateX(200);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toNode);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toNode);
            slideIn.setFromX(200);
            slideIn.setToX(0);

            ParallelTransition parallelIn = new ParallelTransition(fadeIn, slideIn);
            parallelIn.play();
        });
        parallelOut.play();
    }

    @FXML
    private void handleProfile() {
        try {
            // Load the profile page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileApprenant.fxml"));
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
    private void showGames(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goMatiereF(ActionEvent actionEvent) {
        System.out.println("Naviguer vers gestion des matières...");
        loadPage(actionEvent, "/org/example/view/MatiereFrontA.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            System.out.println("Chargement du fichier : " + fxmlPath);
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // ← affiche l'erreur exacte dans la console
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // ← attrape aussi toute autre erreur de controller
            showAlert(Alert.AlertType.ERROR, "Erreur", "Exception générale", e.getMessage());
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

    public void goAccueil(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ApprenantDashboard.fxml");
    }
}