package org.example.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.EvenementDAO;
import org.example.dao.FavoriDAO;
import org.example.entity.CalendarView;
import org.example.entity.Evenement;
import org.example.utils.SessionManager;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import org.example.dao.CommentReactionDAO;
import org.example.dao.CommentDAO;
import org.example.entity.Comment;



public class EvenementListController {

    private static final String PASTEL_VIOLET = "#D9B3FF";
    private static final String PASTEL_BLUE = "#B3D9FF";
    private static final String PASTEL_PINK = "#FFB3E6";
    private static final String SOFT_WHITE = "#FFF9FE";

    @FXML private FlowPane eventContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane mainContainer;

    @FXML private Button btnFavoris;
    private Evenement currentEvent;

    @FXML
    private TextArea commentInput;

    @FXML
    private VBox commentBox;



    private final EvenementDAO evenementDAO = new EvenementDAO();

    @FXML
    public void initialize() {
        playTitleAnimation();
        loadEventsWithModernAnimations();
        configureScrollPane();
    }
    public void setCurrentEvent(Evenement event) {
        this.currentEvent = event;
    }

    private void configureScrollPane() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");

        javafx.application.Platform.runLater(() -> {
            Node verticalBar = scrollPane.lookup(".scroll-bar:vertical");
            if (verticalBar != null) {
                verticalBar.setStyle("-fx-background-color: transparent; -fx-padding: 2px; -fx-background-radius: 5em;");
            }
        });
    }



    private void loadEventsWithModernAnimations() {
        List<Evenement> events = evenementDAO.getAll();
        eventContainer.setPadding(new Insets(40, 30, 80, 30));
        eventContainer.setHgap(35);
        eventContainer.setVgap(35);

        for (int i = 0; i < events.size(); i++) {
            createModernEventCard(events.get(i), i * 150);
        }
    }

    private void createModernEventCard(Evenement event, int delayMillis) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/evenement-card.fxml"));
            Region card = loader.load();

            EvenementCardController controller = loader.getController();
            controller.setData(event);
            controller.setPastelColors(PASTEL_VIOLET, PASTEL_BLUE, PASTEL_PINK);

            Label heartLabel = new Label();
            heartLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #B388EB; -fx-cursor: hand;");
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            boolean estFavori = FavoriDAO.getInstance().estFavori(userId, event.getId());
            heartLabel.setText(estFavori ? "❤️" : "🤍");

            heartLabel.setOnMouseEntered(e -> heartLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8e44ad; -fx-cursor: hand;"));
            heartLabel.setOnMouseExited(e -> {
                boolean refresh = FavoriDAO.getInstance().estFavori(userId, event.getId());
                heartLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + (refresh ? "#B388EB" : "#CCCCCC") + "; -fx-cursor: hand;");
            });

            heartLabel.setOnMouseClicked(e -> {
                boolean current = FavoriDAO.getInstance().estFavori(userId, event.getId());
                if (current) {
                    FavoriDAO.getInstance().supprimerFavori(userId, event.getId());
                    heartLabel.setText("🤍");
                    showAnimatedToast("❌ Retiré des favoris");
                } else {
                    FavoriDAO.getInstance().ajouterFavori(userId, event.getId());
                    heartLabel.setText("❤️");
                    launchHeartExplosion(heartLabel, mainContainer);
                    playMagicSound();
                    showAnimatedToast("💜 Ajouté aux favoris !");
                }
                ScaleTransition scale = new ScaleTransition(Duration.seconds(0.3), heartLabel);
                scale.setFromX(1);
                scale.setFromY(1);
                scale.setToX(1.5);
                scale.setToY(1.5);
                scale.setAutoReverse(true);
                scale.setCycleCount(2);
                scale.play();
            });

            CommentDAO commentDAO = new CommentDAO();
            CommentReactionDAO commentReactionDAO = new CommentReactionDAO();
            TextArea commentInput = new TextArea();
            commentInput.setPromptText("📝 Écrivez votre commentaire ici...");
            commentInput.setPrefRowCount(2);
            commentInput.setWrapText(true);
            commentInput.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #dcdde1;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-padding: 8 10;
            -fx-font-size: 13px;
        """);

            Button commentBtn = new Button("💬 Commenter");
            commentBtn.setStyle("""
            -fx-background-color: #B388EB;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 15;
            -fx-padding: 6 18;
            -fx-font-size: 13px;
        """);

            VBox commentBox = new VBox();
            commentBox.setSpacing(6);
            commentBox.setStyle("-fx-padding: 4 0 0 0;");

            List<Comment> anciensCommentaires = commentDAO.getCommentairesByEvent(event.getId());

            for (Comment c : anciensCommentaires) {
                afficherCommentaireAvecReactions(c, commentBox, commentDAO, commentReactionDAO);
            }

            List<String> grosMots = List.of("merde", "con", "putain", "enculé", "salope", "nique", "batard", "fdp", "ta gueule");

            commentBtn.setOnAction(ev -> {
                ScaleTransition bounce = new ScaleTransition(Duration.seconds(0.1), commentBtn);
                bounce.setFromX(1);
                bounce.setFromY(1);
                bounce.setToX(1.1);
                bounce.setToY(1.1);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.play();

                String commentText = commentInput.getText().trim();
                boolean contientGrosMot = grosMots.stream().anyMatch(mot -> commentText.toLowerCase().contains(mot));
                if (contientGrosMot) {
                    showAnimatedToast("🚫 Les gros mots sont interdits !");
                    return;
                }
                if (!commentText.isEmpty()) {
                    int currentUserId = SessionManager.getInstance().getCurrentUser().getId();
                    String nomUtilisateur = SessionManager.getInstance().getCurrentUser().getNom();
                    Comment newComment = new Comment(event.getId(), currentUserId, commentText, nomUtilisateur);
                    int generatedId = commentDAO.ajouter(newComment);
                    newComment.setId(generatedId); // ✅ C'est ici que tu corriges le problème
                    afficherCommentaireAvecReactions(newComment, commentBox, commentDAO, commentReactionDAO);

                    commentInput.clear();
                    showAnimatedToast("💬 Commentaire ajouté !");
                }
            });

            VBox content = new VBox(card, heartLabel, commentInput, commentBtn, commentBox);
            content.setSpacing(10);
            content.setStyle("-fx-alignment: center;");
            content.setOpacity(0);
            content.setTranslateY(30);
            content.setRotate(1.5);
            content.setScaleX(0.97);
            content.setScaleY(0.97);

            playCardEntranceAnimation(content, delayMillis);
            setupModernHoverEffects(card);
            eventContainer.getChildren().add(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final String BUTTON_STYLE_NORMAL = """
    -fx-background-color: #dcd0ff;
    -fx-text-fill: #6c5ce7;
    -fx-font-weight: bold;
    -fx-font-size: 13px;
    -fx-background-radius: 20;
    -fx-border-radius: 20;
    -fx-padding: 6 14;
    -fx-cursor: hand;
    -fx-effect: dropshadow(three-pass-box, rgba(108, 92, 231, 0.2), 8, 0.3, 0, 3);
""";

    private static final String BUTTON_STYLE_SELECTED = """
    -fx-background-color: #b388eb;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-font-size: 13px;
    -fx-background-radius: 20;
    -fx-border-radius: 20;
    -fx-padding: 6 14;
    -fx-cursor: hand;
    -fx-effect: dropshadow(three-pass-box, rgba(179, 136, 235, 0.5), 10, 0.4, 0, 4);
""";


    private void launchHeartExplosion(Node origin, Pane container) {
        String[] colors = {
                "#e74c3c", "#f39c12", "#8e44ad", "#3498db",
                "#1abc9c", "#e84393", "#fd79a8", "#6c5ce7", "#00cec9"
        };
        Random rand = new Random();

        for (int i = 0; i < 18; i++) {
            Platform.runLater(() -> {
                Bounds bounds = origin.localToScene(origin.getBoundsInLocal());
                Bounds localBounds = container.sceneToLocal(bounds);

                double startX = localBounds.getMinX() + localBounds.getWidth() / 2;
                double startY = localBounds.getMinY() + localBounds.getHeight() / 2;

                Label heart = new Label("❤");
                heart.setStyle("-fx-font-size: 22px;");
                heart.setTextFill(Color.web(colors[rand.nextInt(colors.length)]));
                heart.setTranslateX(startX - 10);
                heart.setTranslateY(startY - 10);
                heart.setOpacity(0.8);

                DropShadow glow = new DropShadow();
                glow.setRadius(10);
                glow.setColor(Color.web(colors[rand.nextInt(colors.length)]));
                heart.setEffect(glow);

                container.getChildren().add(heart);
                heart.toFront(); // ✅ le cœur passe DEVANT tous les autres enfants du container

                double angle = rand.nextDouble() * 2 * Math.PI;
                double distance = 100 + rand.nextDouble() * 80;
                double dx = Math.cos(angle) * distance;
                double dy = Math.sin(angle) * distance;

                TranslateTransition move = new TranslateTransition(Duration.seconds(1.5), heart);
                move.setByX(dx);
                move.setByY(dy);

                RotateTransition rotate = new RotateTransition(Duration.seconds(1.5), heart);
                rotate.setByAngle(180 + rand.nextDouble() * 180);

                ScaleTransition scale = new ScaleTransition(Duration.seconds(0.4), heart);
                scale.setFromX(1);
                scale.setFromY(1);
                scale.setToX(1.6);
                scale.setToY(1.6);
                scale.setAutoReverse(true);
                scale.setCycleCount(2);

                FadeTransition fade = new FadeTransition(Duration.seconds(1.5), heart);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);

                ParallelTransition animation = new ParallelTransition(heart, move, rotate, fade, scale);
                animation.setOnFinished(e -> container.getChildren().remove(heart));
                animation.play();
            });
        }

        playPopSound();
    }
    private void showSpecialToast(String message) {
        Label toast = new Label(message);
        toast.setStyle("""
        -fx-background-color: #6c5ce7;
        -fx-text-fill: white;
        -fx-padding: 12px 20px;
        -fx-background-radius: 20px;
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-effect: dropshadow(gaussian, black, 5, 0.3, 0, 2);
    """);

        StackPane root = (StackPane) commentBox.getScene().getRoot();
        root.getChildren().add(toast);

        toast.setTranslateY(-150);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> root.getChildren().remove(toast));

        SequentialTransition sequence = new SequentialTransition(fadeIn, stay, fadeOut);
        sequence.play();
    }
    private void afficherCommentaireAvecReactions(Comment c, VBox commentBox, CommentDAO commentDAO, CommentReactionDAO commentReactionDAO) {
        Label commentLabel = new Label("👤 " + c.getNomUtilisateur() + " : " + c.getContent());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("""
        -fx-background-color: #f5f6fa;
        -fx-padding: 10;
        -fx-background-radius: 10;
        -fx-border-color: #dcdde1;
        -fx-font-size: 13px;
        -fx-text-fill: #2d3436;
    """);

        Button likeBtn = new Button();
        Button dislikeBtn = new Button();
        likeBtn.setStyle(BUTTON_STYLE_NORMAL);
        dislikeBtn.setStyle(BUTTON_STYLE_NORMAL);

        // Création de la ligne commentaire
        HBox commentRow = new HBox(commentLabel, likeBtn, dislikeBtn);
        commentRow.setSpacing(10);
        commentRow.setStyle("-fx-alignment: CENTER_LEFT;");

        // Ajouter bouton "supprimer" uniquement si c'est SON commentaire
        int currentUserId = SessionManager.getInstance().getCurrentUser().getId();
        if (currentUserId == c.getUserId()) {
            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e74c3c;
            -fx-font-size: 14px;
            -fx-cursor: hand;
            -fx-padding: 2 6;
        """);

            commentRow.getChildren().add(deleteBtn);

            deleteBtn.setOnAction(e -> {

                if (currentUserId != c.getUserId()) {
                    deleteBtn.setVisible(false);
                    return;
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "🗑️ Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText(null);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), commentRow);
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(ev -> {
                            commentBox.getChildren().remove(commentRow);

                            // 👉 Ici on n'a plus besoin de redéclarer `currentUserId` !!
                            CommentDAO freshCommentDAO = new CommentDAO();
                            freshCommentDAO.supprimer(c);

                            showAnimatedToast("💬 Commentaire supprimé !");
                        });
                        fadeOut.play();
                    }
                });
            });

        }

        // Rafraîchir stats (likes/dislikes)
        Runnable refreshStats = () -> {
            try {
                int likes = commentReactionDAO.countReactions(c.getId(), "like");
                int dislikes = commentReactionDAO.countReactions(c.getId(), "dislike");

                likeBtn.setText("👍 " + likes);
                dislikeBtn.setText("👎 " + dislikes);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };
        refreshStats.run();

        likeBtn.setOnAction(e -> {
            animateModernButton(likeBtn);
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            try {
                if (commentReactionDAO.aDéjàRéagi(userId, c.getId())) {
                    String currentReaction = commentReactionDAO.getReactionType(userId, c.getId());
                    if ("like".equals(currentReaction)) {
                        commentReactionDAO.supprimerReaction(userId, c.getId());
                        showAnimatedToast("👍 Like retiré !");
                        likeBtn.setStyle(BUTTON_STYLE_NORMAL);
                        dislikeBtn.setStyle(BUTTON_STYLE_NORMAL);
                    } else {
                        commentReactionDAO.enregistrerReaction(userId, c.getId(), "like");
                        showAnimatedToast("👍 Like changé !");
                        likeBtn.setStyle(BUTTON_STYLE_SELECTED);
                        dislikeBtn.setStyle(BUTTON_STYLE_NORMAL);
                    }
                } else {
                    commentReactionDAO.enregistrerReaction(userId, c.getId(), "like");
                    showAnimatedToast("👍 Like ajouté !");
                    likeBtn.setStyle(BUTTON_STYLE_SELECTED);
                    dislikeBtn.setStyle(BUTTON_STYLE_NORMAL);
                }
                refreshStats.run();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        dislikeBtn.setOnAction(e -> {
            animateModernButton(dislikeBtn);
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            try {
                if (commentReactionDAO.aDéjàRéagi(userId, c.getId())) {
                    String currentReaction = commentReactionDAO.getReactionType(userId, c.getId());
                    if ("dislike".equals(currentReaction)) {
                        commentReactionDAO.supprimerReaction(userId, c.getId());
                        showAnimatedToast("👎 Dislike retiré !");
                        likeBtn.setStyle(BUTTON_STYLE_NORMAL);
                        dislikeBtn.setStyle(BUTTON_STYLE_NORMAL);
                    } else {
                        commentReactionDAO.enregistrerReaction(userId, c.getId(), "dislike");
                        showAnimatedToast("👎 Dislike changé !");
                        likeBtn.setStyle(BUTTON_STYLE_NORMAL);
                        dislikeBtn.setStyle(BUTTON_STYLE_SELECTED);
                    }
                } else {
                    commentReactionDAO.enregistrerReaction(userId, c.getId(), "dislike");
                    showAnimatedToast("👎 Dislike ajouté !");
                    likeBtn.setStyle(BUTTON_STYLE_NORMAL);
                    dislikeBtn.setStyle(BUTTON_STYLE_SELECTED);
                }
                refreshStats.run();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        commentBox.getChildren().add(commentRow);
    }


    private void animateModernButton(Button btn) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.15), btn);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.15), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        SequentialTransition bounce = new SequentialTransition(scaleUp, scaleDown);
        bounce.play();
    }
    private void envoyerCommentaire(ActionEvent actionEvent, Evenement event, TextArea commentInput, VBox commentBox) {
        String commentText = commentInput.getText().trim();
        List<String> grosMots = List.of("merde", "con", "putain", "enculé", "salope", "nique", "batard", "fdp", "ta gueule");

        if (grosMots.stream().anyMatch(mot -> commentText.toLowerCase().contains(mot))) {
            showAnimatedToast("🚫 Les gros mots sont interdits !");
            return;
        }

        if (!commentText.isEmpty()) {
            int eventId = event.getId();
            int currentUserId = SessionManager.getInstance().getCurrentUser().getId();
            String currentUserNom = SessionManager.getInstance().getCurrentUser().getNom();

            Comment newComment = new Comment(eventId, currentUserId, commentText, currentUserNom);
            CommentDAO commentDAO = new CommentDAO();
            CommentReactionDAO commentReactionDAO = new CommentReactionDAO();

            commentDAO.ajouter(newComment);

            // 🔄 Recharge tous les commentaires (juste une fois)
            commentBox.getChildren().clear();
            List<Comment> updatedComments = commentDAO.getCommentairesByEvent(eventId);
            for (Comment c : updatedComments) {
                afficherCommentaireAvecReactions(c, commentBox, commentDAO, commentReactionDAO);
            }

            commentInput.clear();
            showAnimatedToast("💬 Commentaire ajouté !");
        }
    }



    private void playPopSound() {
        try {
            String soundPath = getClass().getResource("/sounds/pop.mp3").toString();
            javafx.scene.media.AudioClip sound = new javafx.scene.media.AudioClip(soundPath);
            sound.play();
        } catch (Exception e) {
            System.out.println("❌ Son non trouvé ou erreur de lecture : " + e.getMessage());
        }
    }
    @FXML
    public void ouvrirMesFavoris(ActionEvent event) {
        try {
            System.out.println("👉 Clic sur Mes Favoris détecté !");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/mes-favoris-view.fxml"));
            Parent root = loader.load();

            MesFavorisController controller = loader.getController();
            controller.afficherFavoris();

            Stage stage = new Stage();
            stage.setTitle("💖 Mes événements favoris");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l’ouverture de Mes Favoris : " + e.getMessage());
        }
    }


    private void showAnimatedToast(String message) {
        Label toast = new Label(message);
        toast.setStyle("""
        -fx-background-color: #8e44ad;
        -fx-text-fill: white;
        -fx-font-size: 14px;
        -fx-padding: 10px 20px;
        -fx-background-radius: 30px;
        -fx-font-weight: bold;
    """);
        toast.setOpacity(0);
        StackPane.setMargin(toast, new Insets(20));
        StackPane.setAlignment(toast, javafx.geometry.Pos.TOP_CENTER);
        mainContainer.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(2));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, stay, fadeOut);
        sequence.setOnFinished(e -> mainContainer.getChildren().remove(toast));
        sequence.play();
    }

    private void playMagicSound() {
        try {
            String soundPath = getClass().getResource("/sounds/bling.mp3").toExternalForm();
            javafx.scene.media.AudioClip clip = new javafx.scene.media.AudioClip(soundPath);
            clip.setVolume(0.4);
            clip.play();
        } catch (Exception e) {
            System.out.println("🔇 Erreur chargement du son : " + e.getMessage());
        }
    }



    private void playCardEntranceAnimation(Node card, int delayMillis) {
        Timeline entranceAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.opacityProperty(), 0),
                        new KeyValue(card.translateYProperty(), 30),
                        new KeyValue(card.rotateProperty(), 1.5),
                        new KeyValue(card.scaleXProperty(), 0.97),
                        new KeyValue(card.scaleYProperty(), 0.97)
                ),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(card.rotateProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleYProperty(), 1, Interpolator.EASE_BOTH)
                )
        );
        entranceAnimation.setDelay(Duration.millis(delayMillis));
        entranceAnimation.play();
    }

    private void setupModernHoverEffects(Node node) {
        RotateTransition rotateOnHover = new RotateTransition(Duration.millis(300), node);
        rotateOnHover.setFromAngle(0);
        rotateOnHover.setToAngle(-1.5);

        ScaleTransition scaleOnHover = new ScaleTransition(Duration.millis(300), node);
        scaleOnHover.setToX(1.03);
        scaleOnHover.setToY(1.03);

        TranslateTransition liftOnHover = new TranslateTransition(Duration.millis(300), node);
        liftOnHover.setToY(-8);

        ShadowTransition shadowOnHover = new ShadowTransition(Duration.millis(300), node, 15, 25);

        ParallelTransition hoverOn = new ParallelTransition(
                rotateOnHover, scaleOnHover, liftOnHover, shadowOnHover
        );

        ParallelTransition hoverOff = new ParallelTransition(
                new RotateTransition(Duration.millis(300), node),
                new ScaleTransition(Duration.millis(300), node),
                new TranslateTransition(Duration.millis(300), node),
                new ShadowTransition(Duration.millis(300), node, 25, 15)
        );

        node.setOnMouseEntered(e -> hoverOn.play());
        node.setOnMouseExited(e -> hoverOff.play());
    }

    public void toggleTheme(ActionEvent event) {
        ColorTransition transition = new ColorTransition(mainContainer);
        transition.toggleTheme();
    }

    private static class ShadowTransition extends Transition {
        private final Node node;
        private final double fromRadius;
        private final double toRadius;

        public ShadowTransition(Duration duration, Node node, double from, double to) {
            this.node = node;
            this.fromRadius = from;
            this.toRadius = to;
            setCycleDuration(duration);
        }


        @Override
        protected void interpolate(double frac) {
            double radius = fromRadius + (toRadius - fromRadius) * frac;
            node.setEffect(new javafx.scene.effect.DropShadow(radius,
                    Color.web(PASTEL_VIOLET).deriveColor(0, 1, 1, 0.15)));
        }
    }

    private static class ColorTransition {
        private final StackPane pane;
        private boolean isDark = false;

        public ColorTransition(StackPane pane) {
            this.pane = pane;
        }

        public void toggleTheme() {
            isDark = !isDark;
            Transition transition = createTransition();
            transition.play();
        }

        private Transition createTransition() {
            String fromColor = isDark ? SOFT_WHITE : PASTEL_VIOLET;
            String toColor = isDark ? "#2E2D4D" : SOFT_WHITE;

            Rectangle bgRect = new Rectangle();
            bgRect.widthProperty().bind(pane.widthProperty());
            bgRect.heightProperty().bind(pane.heightProperty());
            bgRect.setFill(Color.web(fromColor));

            pane.getChildren().add(0, bgRect);

            FillTransition ft = new FillTransition(Duration.seconds(0.8), bgRect, Color.web(fromColor), Color.web(toColor));
            ft.currentTimeProperty().addListener((obs, old, now) -> {
                if (now != null) {
                    double progress = now.toMillis() / ft.getDuration().toMillis();
                    Color current = Color.web(fromColor).interpolate(Color.web(toColor), progress);
                    pane.setStyle("-fx-background-color: #" + current.toString().substring(2, 8) + ";");
                }
            });

            return ft;
        }
    }

    private void playTitleAnimation() {
        Label titleLabel = (Label) mainContainer.lookup("#titleLabel");
        if (titleLabel != null) {
            titleLabel.setOpacity(0);
            titleLabel.setTranslateY(-30);

            Timeline animation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(titleLabel.opacityProperty(), 0),
                            new KeyValue(titleLabel.translateYProperty(), -30)
                    ),
                    new KeyFrame(Duration.seconds(1.2),
                            new KeyValue(titleLabel.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(titleLabel.translateYProperty(), 0, Interpolator.EASE_OUT)
                    )
            );
            animation.play();
        }
    }
    @FXML
    public void ouvrirScratchGame(ActionEvent event) {
        System.out.println("✅ Méthode ouvrirScratchGame appelée");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/scratch-game-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("🎲 Jeu de cartes à gratter");
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("✅ scratch-game-view.fxml chargé avec succès");

        } catch (IOException e) {
            System.out.println("❌ Erreur lors du chargement de scratch-game-view.fxml");
            e.printStackTrace();

            // Optionnel : alerte visuelle
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                    "Erreur ouverture jeu : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void ouvrirCalendrier() {
        try {
            Stage calendarStage = new Stage();

            EvenementDAO dao = new EvenementDAO();
            List<Evenement> evenementList = dao.getAll();

            CalendarView calendarView = new CalendarView(evenementList);

            Scene scene = new Scene(calendarView);
            calendarStage.setTitle("📅 Calendrier des Événements");
            calendarStage.setScene(scene);
            calendarStage.setWidth(850);
            calendarStage.setHeight(600);
            calendarStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture du calendrier : " + e.getMessage()).showAndWait();
        }
    }
    @FXML
    private void ouvrirTraduction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/translate-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("🌍 Traduction");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirMap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/map-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Carte des événements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}