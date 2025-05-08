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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.*;
import org.example.services.ServiceCours;
import org.example.services.ServiceFichier;
import org.example.services.ServiceEvalu;
import org.example.services.ServiceCommentaire;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherCoursFrontController implements Initializable {

    @FXML private VBox coursContainer, objectivesContainer, categoriesContainer, ratingBars, commentairesContainer;
    @FXML private Label matiereNom, matiereTitre, matiereTitreNav, matiereDesc, averageRating, totalEvaluations;
    @FXML private ImageView matiereImage;
    @FXML private HBox starRating, evaluationStars;
    @FXML private Button profileButton, logoutButton, btnAjouterCours, submitEvaluation, submitCommentaire;
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



    // Functional interface for listeners
    @FunctionalInterface
    interface RefreshListener {
        void refresh();
    }

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

            // Load objectives
            objectivesContainer.getChildren().clear();
            String[] objectives = matiere.getObjM().split("\\.");
            for (String obj : objectives) {
                if (!obj.trim().isEmpty()) {
                    Label objLabel = new Label("• " + obj.trim());
                    objLabel.setStyle("-fx-text-fill: #0e0e0e;-fx-font-size: 16px;");
                    objectivesContainer.getChildren().add(objLabel);
                }
            }

            // Load image
            if (matiere.getImgM() != null && !matiere.getImgM().isEmpty()) {
                try {
                    File file = new File("src/main/resources/matiere/" + matiere.getImgM());
                    if (file.exists()) {
                        matiereImage.setImage(new Image(file.toURI().toString()));
                    } else {
                        InputStream stream = getClass().getResourceAsStream("/matiere/" + matiere.getImgM());
                        if (stream != null) {
                            matiereImage.setImage(new Image(stream));
                        } else {
                            matiereImage.setVisible(false);
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
            applyFilters();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des cours échoué", e.getMessage());
        }
    }

    private VBox createCoursBox(Cours cours) throws SQLException {
        VBox coursBox = new VBox(10);
        coursBox.getStyleClass().add("cours-card");
        coursBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        coursBox.getProperties().put("type", cours.getType());
        coursBox.getProperties().put("level", cours.getNivC());

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("cours-header");
        Label headerLabel = new Label(cours.getNomC());
        headerLabel.getStyleClass().add("cours-header-label");
        header.getChildren().add(headerLabel);

        // Content
        VBox content = new VBox(10);
        content.getStyleClass().add("cours-content");

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(
                createStyledLabel("Enseigné par: " + (cours.getUser() != null ? cours.getUser().getNom() + " " + cours.getUser().getPrenom() : "Inconnu"), "cours-info-label"),
                createStyledLabel("Niveau: " + cours.getNivC(), "cours-info-label")
        );

        // Objectives
        VBox objBox = new VBox(5);
        String[] objectives = cours.getObjC().split("\\.");
        for (String obj : objectives) {
            if (!obj.trim().isEmpty()) {
                objBox.getChildren().add(createStyledLabel("• " + obj.trim(), "cours-objectives-label"));
            }
        }

        // File counts
        List<Fichier> fichiers = serviceFichier.getFichiersByCours(cours.getId());
        int pdfCount = (int) fichiers.stream().filter(f -> "PDF".equals(f.getType())).count();
        int wordCount = (int) fichiers.stream().filter(f -> "Word".equals(f.getType())).count();
        int videoCount = (int) fichiers.stream().filter(f -> "Video".equals(f.getType())).count();
        int imageCount = (int) fichiers.stream().filter(f -> "Image".equals(f.getType())).count();
        Label fileSummary = createStyledLabel(
                String.format("%d Vidéos, %d Word, %d PDF, %d Images", videoCount, wordCount, pdfCount, imageCount),
                "cours-file-summary"
        );

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(
                createButton("Afficher fichiers", e -> toggleFichiers(coursBox), "#4CAF50"),
                createButton("Ajouter fichier", e -> ajouterFichier(cours), "#4CAF50"),
                createButton("Modifier", e -> modifierCours(cours), "#FF9800"),
                createButton("Supprimer", e -> supprimerCours(cours), "#DC3545")
        );

        content.getChildren().addAll(infoBox, objBox, fileSummary, buttonBox);
        coursBox.getChildren().addAll(header, content, createFichiersContainer(cours));

        return coursBox;
    }

    private VBox createFichiersContainer(Cours cours) {
        VBox container = new VBox(5);
        container.setVisible(false);
        container.setStyle("-fx-padding: 10 0 0 10;");

        try {
            List<Fichier> fichiers = serviceFichier.getFichiersByCours(cours.getId());
            if (!fichiers.isEmpty()) {
                for (Fichier fichier : fichiers) {
                    HBox fichierBox = new HBox(10);
                    fichierBox.getStyleClass().add("cours-file-box");

                    Label fileLabel = createStyledLabel(fichier.getNomF() + " (" + fichier.getType() + ")", "cours-info-label");
                    Button downloadBtn = createButton("Télécharger", e -> telechargerFichier(fichier), "#4CAF50");
                    Button editBtn = createButton("Modifier", e -> modifierFichier(fichier), "#FF9800");
                    Button deleteBtn = createButton("Supprimer", e -> supprimerFichier(fichier), "#DC3545");

                    fichierBox.getChildren().addAll(fileLabel, downloadBtn, editBtn, deleteBtn);
                    container.getChildren().add(fichierBox);
                }
            } else {
                container.getChildren().add(createStyledLabel("Aucun fichier disponible", "cours-info-label"));
            }
        } catch (SQLException e) {
            container.getChildren().add(createStyledLabel("Erreur de chargement des fichiers", "-fx-text-fill: red;"));
        }

        return container;
    }

    private void loadEvaluations() {
        List<Evalu> evaluations = serviceEvalu.getEvaluationsByMatiere(matiere.getId());
        double avgRating = evaluations.stream().mapToInt(Evalu::getNote).average().orElse(0);
        averageRating.setText(String.format("%.1f", avgRating));
        totalEvaluations.setText(evaluations.size() + " Évaluations");

        // Star rating
        starRating.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(i <= avgRating ? new Image("/images/star-filled.png") : new Image("/images/star-empty.png"));
            star.setFitHeight(24);
            star.setFitWidth(24);
            starRating.getChildren().add(star);
        }

        // Rating bars
        ratingBars.getChildren().clear();
        for (int star = 5; star >= 1; star--) {
            final int currentStar = star;
            long count = evaluations.stream().filter(e -> e.getNote() == currentStar).count();
            double percentage = evaluations.isEmpty() ? 0 : (count * 100.0 / evaluations.size());

            HBox barBox = new HBox(10);
            Label starLabel = createStyledLabel(currentStar + " étoiles:", "-fx-text-fill: #495057; -fx-font-size: 14px;");
            ProgressBar bar = new ProgressBar(percentage / 100);
            bar.setPrefWidth(250);
            bar.setStyle("-fx-accent: #1734a4;");
            Label percentLabel = createStyledLabel(String.format("%.0f%%", percentage), "-fx-text-fill: #495057; -fx-font-size: 14px;");
            barBox.getChildren().addAll(starLabel, bar, percentLabel);
            ratingBars.getChildren().add(barBox);
        }

        // Evaluation stars
        evaluationStars.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ImageView star = new ImageView(new Image("/images/star-empty.png"));
            star.setFitHeight(24);
            star.setFitWidth(24);
            star.setOnMouseClicked(e -> selectRating(rating));
            evaluationStars.getChildren().add(star);
        }
    }

    private void selectRating(int rating) {
        selectedRating = rating;
        evaluationStars.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            final int currentRating = i;
            ImageView star = new ImageView(i <= rating ? new Image("/images/star-filled.png") : new Image("/images/star-empty.png"));
            star.setFitHeight(24);
            star.setFitWidth(24);
            star.setOnMouseClicked(e -> selectRating(currentRating));
            evaluationStars.getChildren().add(star);
        }
    }

    @FXML
    private void submitEvaluation() {
        if (selectedRating == 0) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner une note", "");
            return;
        }

        Evalu evalu = new Evalu();
        evalu.setNote(selectedRating);
        if (matiere == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune matière sélectionnée", "");
            return;
        }
        evalu.setMatiere(matiere);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté", "");
            return;
        }
        evalu.setUser(currentUser);

        try {
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
            VBox commentBox = new VBox(10);
            commentBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
            commentBox.setOnMouseEntered(e -> commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);"));
            commentBox.setOnMouseExited(e -> commentBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"));

            HBox ratingBox = new HBox(6);
            int note = serviceEvalu.getEvaluationByUserAndMatiere(commentaire.getUser().getId(), matiere.getId())
                    .map(Evalu::getNote).orElse(0);
            for (int i = 1; i <= 5; i++) {
                ImageView star = new ImageView(i <= note ? new Image("/images/star-filled.png") : new Image("/images/star-empty.png"));
                star.setFitHeight(20);
                star.setFitWidth(20);
                ratingBox.getChildren().add(star);
            }

            commentBox.getChildren().addAll(
                    ratingBox,
                    createStyledLabel(commentaire.getSujet() + ":", "-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #050809;"),
                    createStyledLabel(commentaire.getContenu(), "-fx-text-fill: #495057; -fx-font-size: 16px;"),
                    createStyledLabel(
                            commentaire.getUser().getNom() + " " + commentaire.getUser().getPrenom() + " - " +
                                    commentaire.getDate().toString(),
                            "-fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-font-style: italic;"
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

        Commentaire commentaire = new Commentaire();
        commentaire.setSujet(sujet);
        commentaire.setContenu(contenu);
        commentaire.setDate(LocalDateTime.now());
        if (matiere == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune matière sélectionnée", "");
            return;
        }
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
    }

    private void loadCategories() {
        try {
            List<Matiere> matieres = serviceMatiere.afficher().stream().limit(6).toList();
            categoriesContainer.getChildren().clear();
            for (Matiere m : matieres) {
                CheckBox checkBox = new CheckBox(m.getNomM() + " (" + serviceCours.getCoursParMatiere(m.getId()).size() + ")");
                checkBox.setStyle("-fx-font-size: 14px; -fx-text-fill: #0e0e0e;");
                final Matiere currentMatiere = m;
                checkBox.setOnAction(e -> redirectToMatiere(currentMatiere));
                categoriesContainer.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des catégories échoué", e.getMessage());
        }
    }

    private void redirectToMatiere(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AfficherCoursFront.fxml"));
            Parent root = loader.load();
            AfficherCoursFrontController controller = loader.getController();
            controller.setMatiere(matiere);
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée", e.getMessage());
        }
    }

    private void toggleFichiers(VBox coursBox) {
        VBox fichiersContainer = (VBox) coursBox.getChildren().get(2);
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

    private void modifierFichier(Fichier fichier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ModifierFichier.fxml"));
            Parent root = loader.load();
            ModifierFichierController controller = loader.getController();
            controller.setFichier(fichier);
            controller.setFichierModifieListener(this::loadCours);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Fichier");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification", e.getMessage());
        }
    }

    private void supprimerFichier(Fichier fichier) {
        try {
            serviceFichier.supprimer(fichier.getId());
            loadCours();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fichier supprimé", "");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression échouée", e.getMessage());
        }
    }

    private void telechargerFichier(Fichier fichier) {
        if (fichier.getUrlF() == null || fichier.getUrlF().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier associé", "");
            return;
        }
        File file = new File(fichier.getUrlF());
        if (file.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier", e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable", "Chemin: " + fichier.getUrlF());
        }
    }

    private void supprimerCours(Cours cours) {
        try {
            serviceCours.supprimer(cours.getId());
            loadCours();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours supprimé", "");
        } catch (SQLException e) {
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification", e.getMessage());
        }
    }

    @FXML
    private void ajouterCours() {
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'ajout", e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        loadPage("/org/example/view/ProfileEnseignant.fxml", "Profil");
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
        loadPage("/org/example/view/ListeMatiereF.fxml", "Liste des Matières");
    }

    @FXML
    private void goAccueil() {
        loadPage("/org/example/view/EnseignantDashboard.fxml", "Accueil");
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
        if (!style.isEmpty()) {
            label.getStyleClass().add(style);
        }
        return label;
    }

    private Button createButton(String text, javafx.event.EventHandler<ActionEvent> handler, String backgroundColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15;");
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


}