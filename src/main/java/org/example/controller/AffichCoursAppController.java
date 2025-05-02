package org.example.controller;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final Map<Integer, List<Integer>> progressData = new HashMap<>();
    private final Map<Integer, ScheduledExecutorService> fileTimers = new HashMap<>();
    private Voice ttsVoice;
    private boolean ttsInitialized = false;
    private boolean isSpeaking = false;
    private Button currentTtsButton = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTTS();

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

    private void initializeTTS() {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

        try {
            VoiceManager voiceManager = VoiceManager.getInstance();
            System.out.println("Available voices: " + Arrays.toString(voiceManager.getVoices()));

            String[] voiceNames = {"kevin16", "kevin", "alan", "cmu_us_kal"};

            for (String voiceName : voiceNames) {
                try {
                    ttsVoice = voiceManager.getVoice(voiceName);
                    if (ttsVoice != null) {
                        ttsVoice.allocate();
                        ttsVoice.setRate(150);
                        ttsVoice.setPitch(100);
                        ttsVoice.setVolume(1);
                        ttsInitialized = true;
                        System.out.println("TTS initialisé avec la voix: " + voiceName);
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur avec la voix " + voiceName + ": " + e.getMessage());
                }
            }

            if (!ttsInitialized) {
                showAlert(Alert.AlertType.WARNING, "Erreur TTS",
                        "Aucune voix TTS disponible. Vérifiez les dépendances FreeTTS.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Erreur TTS",
                    "Échec de l'initialisation TTS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void toggleSpeech(String text, Button button) {
        if (!ttsInitialized || ttsVoice == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur TTS", "TTS non initialisé.");
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur TTS", "Texte invalide.");
            return;
        }

        if (currentTtsButton != null && currentTtsButton != button) {
            ttsVoice.speak("");
            currentTtsButton.setText("🔊 Lire");
            currentTtsButton = button;
        }

        if (isSpeaking) {
            ttsVoice.speak("");
            button.setText("🔊 Lire");
            isSpeaking = false;
            currentTtsButton = null;
        } else {
            new Thread(() -> {
                try {
                    isSpeaking = true;
                    currentTtsButton = button;
                    javafx.application.Platform.runLater(() -> button.setText("■ Arrêter"));
                    ttsVoice.speak(prepareFrenchText(text));
                    isSpeaking = false;
                    javafx.application.Platform.runLater(() -> button.setText("🔊 Lire"));
                } catch (Exception e) {
                    isSpeaking = false;
                    javafx.application.Platform.runLater(() -> {
                        button.setText("🔊 Lire");
                        showAlert(Alert.AlertType.ERROR, "Erreur TTS", "Erreur lors de la lecture: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private String prepareFrenchText(String text) {
        return text.replaceAll("é", "e")
                .replaceAll("è", "e")
                .replaceAll("ê", "e")
                .replaceAll("à", "a")
                .replaceAll("ù", "u")
                .replaceAll("ç", "c");
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
        loadMatiereDetails();
        loadCours();
        loadEvaluations();
        loadCommentaires();
        loadCategories();
        loadProgressData();
    }

    private void loadProgressData() {
        progressData.clear();
    }

    private void loadMatiereDetails() {
        if (matiere == null) return;

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

    private void loadCours() {
        try {
            coursContainer.getChildren().clear();
            List<Cours> coursList = serviceCours.afficherParMatiere(matiere.getId());

            for (Cours cours : coursList) {
                coursContainer.getChildren().add(createCoursBox(cours));
            }
            applyFilters();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des cours échoué: " + e.getMessage());
        }
    }

    private VBox createCoursBox(Cours cours) {
        VBox coursBox = new VBox(5);
        coursBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-spacing: 5; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        coursBox.getProperties().put("type", cours.getType());
        coursBox.getProperties().put("level", cours.getNivC());

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(
                createStyledLabel(cours.getNomC(), "-fx-font-size: 18px; -fx-font-weight: bold;"),
                createStyledLabel("Enseigné par: " + (cours.getUser() != null ? cours.getUser().getNom() + " " + cours.getUser().getPrenom() : "Inconnu"), "-fx-text-fill: #666;"),
                createStyledLabel("Niveau: " + cours.getNivC(), "-fx-text-fill: #666;")
        );

        VBox objBox = new VBox(2);
        String[] objectives = cours.getObjC().split("\\.");
        StringBuilder objectivesText = new StringBuilder();
        for (String obj : objectives) {
            if (!obj.trim().isEmpty()) {
                objBox.getChildren().add(createStyledLabel("• " + obj.trim(), "-fx-text-fill: #666;"));
                objectivesText.append(obj.trim()).append(". ");
            }
        }

        List<Fichier> fichiers;
        try {
            fichiers = serviceFichier.getFichiersByCours(cours.getId());
        } catch (SQLException e) {
            fichiers = new ArrayList<>();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des fichiers: " + e.getMessage());
        }

        int pdfCount = (int) fichiers.stream().filter(f -> f.getType().equals("Pdf")).count();
        int wordCount = (int) fichiers.stream().filter(f -> f.getType().equals("Word")).count();
        int videoCount = (int) fichiers.stream().filter(f -> f.getType().equals("Video")).count();
        int imageCount = (int) fichiers.stream().filter(f -> f.getType().equals("Image")).count();
        String fileSummaryText = String.format("%d Vidéo(s), %d Word, %d Pdf, %d Image(s)", videoCount, wordCount, pdfCount, imageCount);
        Label fileSummary = createStyledLabel(fileSummaryText, "-fx-text-fill: #666; -fx-padding: 5 0;");

        String ttsText = String.format(
                "Cours: %s. Niveau: %s. Enseigné par: %s. Objectifs: %s. Résumé des fichiers: %s",
                cours.getNomC(),
                cours.getNivC(),
                cours.getUser() != null ? cours.getUser().getNom() + " " + cours.getUser().getPrenom() : "Inconnu",
                objectivesText.toString(),
                fileSummaryText
        );

        Button ttsBtn = new Button("🔊 Lire");
        ttsBtn.setOnAction(e -> toggleSpeech(ttsText, ttsBtn));
        ttsBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 10;");

        VBox fichiersContainer = createFichiersContainer(cours, fichiers);

        Button showFilesBtn = new Button("Afficher fichiers");
        showFilesBtn.setOnAction(e -> toggleFichiers(fichiersContainer));
        showFilesBtn.setStyle("-fx-background-color: #4B5EAA; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 10;");

        HBox progressBox = new HBox();
        progressBox.setStyle("-fx-background-color: #eee; -fx-border-radius: 5; -fx-pref-height: 20;");
        Label progressLabel = new Label("Progression: ");
        progressLabel.setStyle("-fx-text-fill: #666; -fx-padding: 0 5;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        progressBar.setId("progress-bar-" + cours.getId());

        Label progressPercent = new Label("0%");
        progressPercent.setId("progress-percent-" + cours.getId());

        progressBox.getChildren().addAll(progressLabel, progressBar, progressPercent);
        updateProgressBar(cours.getId(), fichiers.size());

        VBox contentBox = new VBox(5);
        contentBox.getChildren().addAll(infoBox, objBox, fileSummary, ttsBtn, progressBox, showFilesBtn, fichiersContainer);
        coursBox.getChildren().add(contentBox);

        return coursBox;
    }

    private void updateProgressBar(int coursId, int totalFiles) {
        List<Integer> completedFiles = progressData.getOrDefault(coursId, new ArrayList<>());
        double progress = totalFiles > 0 ? (double) completedFiles.size() / totalFiles : 0;

        ProgressBar progressBar = (ProgressBar) coursContainer.lookup("#progress-bar-" + coursId);
        Label progressPercent = (Label) coursContainer.lookup("#progress-percent-" + coursId);

        if (progressBar != null && progressPercent != null) {
            progressBar.setProgress(progress);
            progressPercent.setText(String.format("%.0f%%", progress * 100));
        }
    }

    private VBox createFichiersContainer(Cours cours, List<Fichier> fichiers) {
        VBox container = new VBox(2);
        container.setVisible(false);
        container.setStyle("-fx-padding: 5 0 0 10;");

        if (!fichiers.isEmpty()) {
            for (int i = 0; i < fichiers.size(); i++) {
                final int index = i;
                Fichier fichier = fichiers.get(i);
                HBox fichierBox = new HBox(10);
                fichierBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 5; -fx-border-radius: 5; -fx-spacing: 10;");

                Label fileLabel = createStyledLabel(fichier.getNomF() + " (" + fichier.getType() + ")", "-fx-font-size: 14px;");

                Button viewBtn = new Button("Voir");
                viewBtn.setOnAction(e -> viewFile(fichier, cours.getId(), index));
                viewBtn.setStyle("-fx-background-color: #4B5EAA; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 3 8;");

                Button downloadBtn = new Button("Télécharger");
                downloadBtn.setOnAction(e -> telechargerFichier(fichier));
                downloadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 3 8;");

                Button ttsBtn = new Button("🔊");
                ttsBtn.setOnAction(e -> toggleSpeech(fichier.getNomF() + ". Type: " + fichier.getType(), ttsBtn));
                ttsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 14px;");

                fichierBox.getChildren().addAll(fileLabel, viewBtn, downloadBtn, ttsBtn);
                container.getChildren().add(fichierBox);
            }
        } else {
            container.getChildren().add(createStyledLabel("Aucun fichier disponible", "-fx-font-style: italic; -fx-text-fill: #666;"));
        }

        return container;
    }

    private void viewFile(Fichier fichier, int coursId, int fileIndex) {
        if (fileIndex > 0) {
            try {
                List<Integer> completedFiles = progressData.getOrDefault(coursId, new ArrayList<>());
                Fichier previousFile = serviceFichier.getFichiersByCours(coursId).get(fileIndex - 1);

                if (!completedFiles.contains(previousFile.getId())) {
                    showAlert(Alert.AlertType.WARNING, "Accès restreint", "Veuillez ouvrir le fichier précédent (" + previousFile.getNomF() + ") pendant au moins 10 secondes avant d'accéder à celui-ci.");
                    return;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la vérification de la progression: " + e.getMessage());
                return;
            }
        }

        try {
            File file = new File(fichier.getUrlF());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable: " + fichier.getUrlF());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
        }

        startFileTimer(coursId, fichier.getId());
    }

    private void startFileTimer(int coursId, int fileId) {
        if (fileTimers.containsKey(fileId)) {
            fileTimers.get(fileId).shutdownNow();
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        fileTimers.put(fileId, executor);

        final List<Integer> completedFiles = progressData.getOrDefault(coursId, new ArrayList<>());
        executor.schedule(() -> {
            if (!completedFiles.contains(fileId)) {
                completedFiles.add(fileId);
                progressData.put(coursId, completedFiles);

                javafx.application.Platform.runLater(() -> {
                    try {
                        updateProgressBar(coursId, serviceFichier.getFichiersByCours(coursId).size());
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de la progression: " + e.getMessage());
                    }
                });
            }
        }, 10, TimeUnit.SECONDS);
    }

    private void toggleFichiers(VBox fichiersContainer) {
        fichiersContainer.setVisible(!fichiersContainer.isVisible());
    }

    private void telechargerFichier(Fichier fichier) {
        if (fichier.getUrlF() == null || fichier.getUrlF().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier associé");
            return;
        }

        try {
            File file = new File(fichier.getUrlF());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable: " + fichier.getUrlF());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
        }
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
            final int currentStar = star;
            long count = evaluations.stream().filter(e -> e.getNote() == currentStar).count();
            double percentage = evaluations.isEmpty() ? 0 : (count * 100.0 / evaluations.size());

            HBox barBox = new HBox(10);
            Label starLabel = createStyledLabel(currentStar + " étoiles:", "-fx-text-fill: #495057;");
            ProgressBar bar = new ProgressBar(percentage / 100);
            bar.setPrefWidth(200);
            Label percentLabel = createStyledLabel(String.format("%.0f%%", percentage), "-fx-text-fill: #495057;");
            barBox.getChildren().addAll(starLabel, bar, percentLabel);
            ratingBars.getChildren().add(barBox);
        }

        evaluationStars.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ImageView star = new ImageView(new Image("/images/star-empty.png"));
            star.setFitHeight(20);
            star.setFitWidth(20);
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
            star.setFitHeight(20);
            star.setFitWidth(20);
            star.setOnMouseClicked(e -> selectRating(currentRating));
            evaluationStars.getChildren().add(star);
        }
    }

    @FXML
    private void submitEvaluation() {
        if (selectedRating == 0) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner une note");
            return;
        }

        try {
            Evalu evalu = new Evalu();
            evalu.setNote(selectedRating);
            evalu.setMatiere(matiere);

            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
                return;
            }
            evalu.setUser(currentUser);

            serviceEvalu.ajouter(evalu);
            loadEvaluations();
            selectedRating = 0;
            selectRating(0);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation ajoutée");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'évaluation: " + e.getMessage());
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

            String ttsCommentText = String.format(
                    "Commentaire par %s %s, le %s. Sujet: %s. Contenu: %s",
                    commentaire.getUser().getNom(),
                    commentaire.getUser().getPrenom(),
                    commentaire.getDate().toString(),
                    commentaire.getSujet(),
                    commentaire.getContenu()
            );

            Button ttsCommentBtn = new Button("🔊 Lire");
            ttsCommentBtn.setOnAction(e -> toggleSpeech(ttsCommentText, ttsCommentBtn));
            ttsCommentBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 3 8;");

            commentBox.getChildren().addAll(
                    ratingBox,
                    createStyledLabel(commentaire.getSujet() + ":", "-fx-font-weight: bold;"),
                    createStyledLabel(commentaire.getContenu(), "-fx-text-fill: #6c757d;"),
                    createStyledLabel(
                            commentaire.getUser().getNom() + " " + commentaire.getUser().getPrenom() + " - " +
                                    commentaire.getDate().toString(),
                            "-fx-text-fill: #6c757d; -fx-font-style: italic;"
                    ),
                    ttsCommentBtn
            );
            commentairesContainer.getChildren().add(commentBox);
        }
    }

    @FXML
    private void submitCommentaire() {
        String sujet = commentaireSujet.getText().trim();
        String contenu = commentaireContenu.getText().trim();

        if (sujet.isEmpty() || contenu.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs");
            return;
        }

        Commentaire commentaire = new Commentaire();
        commentaire.setSujet(sujet);
        commentaire.setContenu(contenu);
        commentaire.setDate(LocalDateTime.now());
        commentaire.setMatiere(matiere);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }
        commentaire.setUser(currentUser);

        serviceCommentaire.ajouter(commentaire);
        loadCommentaires();
        commentaireSujet.clear();
        commentaireContenu.clear();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire ajouté");
    }

    private void loadCategories() {
        try {
            List<Matiere> matieres = serviceMatiere.afficher().stream().limit(6).toList();
            categoriesContainer.getChildren().clear();
            for (Matiere m : matieres) {
                CheckBox checkBox = new CheckBox(m.getNomM() + " (" + serviceCours.getCoursParMatiere(m.getId()).size() + ")");
                final Matiere currentMatiere = m;
                checkBox.setOnAction(e -> redirectToMatiere(currentMatiere));
                categoriesContainer.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des catégories échoué: " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée: " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des événements: " + e.getMessage());
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
    private void handleProfile() {
        loadPage("/org/example/view/ProfileApprenant.fxml", "Profil");
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation échouée: " + e.getMessage());
        }
    }
}