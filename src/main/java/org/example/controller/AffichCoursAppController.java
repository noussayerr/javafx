package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.*;
import org.example.services.*;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AffichCoursAppController implements Initializable {

    @FXML private VBox coursContainer, objectivesContainer, categoriesContainer, ratingBars, commentairesContainer;
    @FXML private Label matiereNom, matiereTitre, matiereTitreNav, matiereDesc, averageRating, totalEvaluations;
    @FXML private ImageView matiereImage;
    @FXML private HBox starRating, evaluationStars;
    @FXML private Button profileButton, logoutButton, submitEvaluation, submitCommentaire;
    @FXML private TextField commentaireSujet;
    @FXML private TextArea commentaireContenu;
    @FXML private RadioButton filterAll, filterFree, filterPremium;
    @FXML private CheckBox filterAllLevels, filterBeginner, filterIntermediate, filterExpert;
    @FXML private TabPane tabPane;

    private Matiere matiere;
    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceFichier serviceFichier = new ServiceFichier();
    private final ServiceEvalu serviceEvalu = new ServiceEvalu();
    private final ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private int selectedRating = 0;

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
        loadMatiereDetails();
        loadCours();
        loadEvaluations();
        loadCommentaires();
        loadCategories();
    }

    private void loadMatiereDetails() {
        if (matiere != null) {
            matiereNom.setText(matiere.getNomM());
            matiereTitre.setText(matiere.getTitreM());
            matiereTitreNav.setText(matiere.getTitreM());
            matiereDesc.setText(matiere.getDescM());

            objectivesContainer.getChildren().clear();
            String[] objectives = matiere.getObjM().split("\\.");
            for (String obj : objectives) {
                if (!obj.trim().isEmpty()) {
                    Label objLabel = new Label("• " + obj.trim());
                    objLabel.setStyle("-fx-text-fill: #0e0e0e;-fx-font-size: 16px;");
                    objectivesContainer.getChildren().add(objLabel);
                }
            }

            if (matiere.getImgM() != null && !matiere.getImgM().isEmpty()) {
                try {
                    File file = new File("src/main/resources/matiere/" + matiere.getImgM());
                    if (file.exists()) {
                        matiereImage.setImage(new Image(file.toURI().toString()));
                    } else {
                        InputStream stream = getClass().getResourceAsStream("/matiere/" + matiere.getImgM());
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

    private void toggleFichiers(VBox coursBox) {
        VBox fichiersContainer = (VBox) coursBox.getChildren().get(3);
        fichiersContainer.setVisible(!fichiersContainer.isVisible());
    }

    private void loadCours() {
        try {
            coursContainer.getChildren().clear();
            List<Cours> coursList = serviceCours.afficherParMatiere(matiere.getId());

            for (Cours cours : coursList) {
                VBox coursBox = createCoursBox(cours);
                coursContainer.getChildren().add(coursBox);
                coursContainer.setStyle("-fx-text-fill: #0e0e0e;-fx-font-size: 14px");
            }
            applyFilters();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des cours échoué", e.getMessage());
        }
    }

    private VBox createCoursBox(Cours cours) throws SQLException {
        VBox coursBox = new VBox(10);
        coursBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-spacing: 10; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        coursBox.getProperties().put("type", cours.getType());
        coursBox.getProperties().put("level", cours.getNivC());

        HBox header = new HBox(10);
        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(
                createStyledLabel(cours.getNomC(), "-fx-font-size: 18px; -fx-font-weight: bold;"),
                createStyledLabel("Enseigné par: " + (cours.getUser() != null ? cours.getUser().getNom() + " " + cours.getUser().getPrenom() : "Inconnu"), "-fx-text-fill: #666;"),
                createStyledLabel("Niveau: " + cours.getNivC(), "-fx-text-fill: #666;")
        );

        VBox objBox = new VBox(5);
        String[] objectives = cours.getObjC().split("\\.");
        for (String obj : objectives) {
            if (!obj.trim().isEmpty()) {
                objBox.getChildren().add(createStyledLabel("• " + obj.trim(), "-fx-text-fill: #666;"));
            }
        }

        List<Fichier> fichiers = serviceFichier.getFichiersByCours(cours.getId());
        int pdfCount = (int) fichiers.stream().filter(f -> f.getType().equals("Pdf")).count();
        int wordCount = (int) fichiers.stream().filter(f -> f.getType().equals("Word")).count();
        int videoCount = (int) fichiers.stream().filter(f -> f.getType().equals("Video")).count();
        int imageCount = (int) fichiers.stream().filter(f -> f.getType().equals("Image")).count();
        Label fileSummary = createStyledLabel(
                String.format("%d Vidéo(s), %d Word, %d Pdf, %d Image(s)", videoCount, wordCount, pdfCount, imageCount),
                "-fx-text-fill: #666;"
        );

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(
                createButton("Afficher fichiers", e -> toggleFichiers(coursBox))
        );

        coursBox.getChildren().addAll(header, objBox, fileSummary);

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
                    fichierBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 8; -fx-border-radius: 5;");

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

    private void loadEvaluations() {
        List<Evalu> evaluations = serviceEvalu.getEvaluationsByMatiere(matiere.getId());
        double avgRating = evaluations.stream().mapToInt(Evalu::getNote).average().orElse(0);
        averageRating.setText(String.format("%.1f", avgRating));
        totalEvaluations.setText(evaluations.size() + " Évaluations");

        starRating.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(i <= avgRating ? new Image("/images/star-filled.png") : new Image("/images/star-empty.png"));
            star.setFitHeight(20);
            star.setFitWidth(20);
            starRating.getChildren().add(star);
        }

        ratingBars.getChildren().clear();
        for (int star = 5; star >= 1; star--) {
            final int finalStar = star;
            long count = evaluations.stream().filter(e -> e.getNote() == finalStar).count();
            double percentage = evaluations.isEmpty() ? 0 : (count * 100.0 / evaluations.size());

            HBox barBox = new HBox(10);
            Label starLabel = createStyledLabel(star + " étoiles:", "-fx-text-fill: #495057;");
            ProgressBar bar = new ProgressBar(percentage / 100);
            bar.setPrefWidth(200);
            Label percentLabel = createStyledLabel(String.format("%.0f%%", percentage), "-fx-text-fill: #495057;");
            barBox.getChildren().addAll(starLabel, bar, percentLabel);
            ratingBars.getChildren().add(barBox);
        }

        evaluationStars.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(new Image("/images/star-empty.png"));
            star.setFitHeight(20);
            star.setFitWidth(20);
            int rating = i;
            star.setOnMouseClicked(e -> selectRating(rating));
            evaluationStars.getChildren().add(star);
        }
    }

    private void selectRating(int rating) {
        selectedRating = rating;
        evaluationStars.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(i <= rating ? new Image("/images/star-filled.png") : new Image("/images/star-empty.png"));
            star.setFitHeight(20);
            star.setFitWidth(20);
            int finalI = i;
            star.setOnMouseClicked(e -> selectRating(finalI));
            evaluationStars.getChildren().add(star);
        }
    }

    @FXML
    private void submitEvaluation() {
        if (selectedRating == 0) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner une note", "");
            return;
        }

        try {
            Evalu evalu = new Evalu();
            evalu.setNote(selectedRating);
            evalu.setMatiere(matiere);

            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté", "");
                return;
            }
            evalu.setUser(currentUser);

            serviceEvalu.ajouter(evalu);
            loadEvaluations();
            selectedRating = 0;
            selectRating(0);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation ajoutée", "");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'évaluation", e.getMessage());
        }
    }

    private void loadCommentaires() {
        commentairesContainer.getChildren().clear();
        List<Commentaire> commentaires = serviceCommentaire.getCommentairesByMatiere(matiere.getId());

        for (Commentaire commentaire : commentaires) {
            VBox commentBox = new VBox(5);
            commentBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-border-radius: 5;");

            HBox ratingBox = new HBox(5);
            int note = serviceEvalu.getEvaluationByUserAndMatiere(commentaire.getUser().getId(), matiere.getId())
                    .map(Evalu::getNote).orElse(0);
            for (int i = 1; i <= 5; i++) {
                ImageView star = new ImageView(i <= note ? new Image("/images/star-filled.png") : new Image("/images/star-empty.png"));
                star.setFitHeight(16);
                star.setFitWidth(16);
                ratingBox.getChildren().add(star);
            }

            commentBox.getChildren().addAll(
                    ratingBox,
                    createStyledLabel(commentaire.getSujet() + ":", "-fx-font-weight: bold;"),
                    createStyledLabel(commentaire.getContenu(), "-fx-text-fill: #6c757d;"),
                    createStyledLabel(
                            commentaire.getUser().getNom() + " " + commentaire.getUser().getPrenom() + " - " +
                                    commentaire.getDate().toString(),
                            "-fx-text-fill: #6c757d; -fx-font-style: italic;"
                    )
            );
            commentairesContainer.getChildren().add(commentBox);
        }
    }

    @FXML
    private void submitCommentaire() {
        String sujet = commentaireSujet.getText().trim();
        String contenu = commentaireContenu.getText().trim();

        if (sujet.isEmpty() || contenu.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs", "");
            return;
        }

        try {
            Commentaire commentaire = new Commentaire();
            commentaire.setSujet(sujet);
            commentaire.setContenu(contenu);
            commentaire.setDate(LocalDateTime.now());
            commentaire.setMatiere(matiere);

            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté", "");
                return;
            }
            commentaire.setUser(currentUser);

            serviceCommentaire.ajouter(commentaire);
            loadCommentaires();
            commentaireSujet.clear();
            commentaireContenu.clear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire ajouté", "");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du commentaire", e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<Matiere> matieres = serviceMatiere.afficher().stream().limit(6).toList();
            categoriesContainer.getChildren().clear();
            for (Matiere m : matieres) {
                CheckBox checkBox = new CheckBox(m.getNomM() + " (" + serviceCours.getCoursParMatiere(m.getId()).size() + ")");
                checkBox.setOnAction(e -> redirectToMatiere(m));
                categoriesContainer.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des catégories échoué", e.getMessage());
        }
    }

    private void redirectToMatiere(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AffichCoursApp.fxml"));
            Parent root = loader.load();
            AffichCoursAppController controller = loader.getController();
            controller.setMatiere(matiere);
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée", e.getMessage());
        }
    }

    private void telechargerFichier(Fichier fichier) {
        if (fichier.getUrlF() == null || fichier.getUrlF().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier associé", "");
            return;
        }
        File file = new File(fichier.getUrlF());
        if (file.exists()) {
            showAlert(Alert.AlertType.INFORMATION, "Téléchargement", "Prêt à télécharger", fichier.getNomF());
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable", "Chemin: " + fichier.getUrlF());
        }
    }

    private void applyFilters() {
        String priceFilter = filterAll.isSelected() ? "all" : filterFree.isSelected() ? "free" : "premium";
        boolean allLevels = filterAllLevels.isSelected();
        boolean beginner = filterBeginner.isSelected();
        boolean intermediate = filterIntermediate.isSelected();
        boolean expert = filterExpert.isSelected();

        for (Node node : coursContainer.getChildren()) {
            VBox coursBox = (VBox) node;
            String type = (String) coursBox.getProperties().get("type");
            String level = (String) coursBox.getProperties().get("level");

            boolean matchesPrice = priceFilter.equals("all") ||
                    (priceFilter.equals("free") && type.equals("free")) ||
                    (priceFilter.equals("premium") && type.equals("premium"));

            boolean matchesLevel = allLevels ||
                    (beginner && level.equals("Débutant")) ||
                    (intermediate && level.equals("Intermédiaire")) ||
                    (expert && level.equals("Avancé"));

            coursBox.setVisible(matchesPrice && matchesLevel);
            coursBox.setManaged(matchesPrice && matchesLevel);
        }
    }

    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private Button createButton(String text, javafx.event.EventHandler<ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btn.setOnAction(handler);
        return btn;
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
        ToggleGroup priceGroup = new ToggleGroup();
        filterAll.setToggleGroup(priceGroup);
        filterFree.setToggleGroup(priceGroup);
        filterPremium.setToggleGroup(priceGroup);

        filterAll.setOnAction(e -> applyFilters());
        filterFree.setOnAction(e -> applyFilters());
        filterPremium.setOnAction(e -> applyFilters());
        filterAllLevels.setOnAction(e -> {
            if (filterAllLevels.isSelected()) {
                filterBeginner.setSelected(false);
                filterIntermediate.setSelected(false);
                filterExpert.setSelected(false);
            }
            applyFilters();
        });
        filterBeginner.setOnAction(e -> {
            filterAllLevels.setSelected(false);
            applyFilters();
        });
        filterIntermediate.setOnAction(e -> {
            filterAllLevels.setSelected(false);
            applyFilters();
        });
        filterExpert.setOnAction(e -> {
            filterAllLevels.setSelected(false);
            applyFilters();
        });
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        loadPage("/org/example/view/ProfileApprenant.fxml", "Profil");
    }

    @FXML
    private void logout() {
        try {
            SessionManager.getInstance().logout();
            loadPage("/org/example/view/Login.fxml", "Connexion");
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion", "Déconnecté avec succès", "");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de déconnexion", e.getMessage());
        }
    }

    @FXML
    private void goMatiereF() {
        loadPage("/org/example/view/MatiereFrontA.fxml", "Liste des Matières");
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée", e.getMessage());
        }
    }
}