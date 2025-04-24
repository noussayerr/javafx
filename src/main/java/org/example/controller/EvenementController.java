package org.example.controller;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.dao.EvenementDAO;
import org.example.entity.Evenement;
import org.example.utils.SessionManager;
import org.example.utils.Toast;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import org.example.services.ServicePDF;
import org.example.services.MailService;
import org.example.services.StripeLauncher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;



public class EvenementController {

    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String> colCategorie;

    @FXML private TableColumn<Evenement, String> colNom;
    @FXML private TableColumn<Evenement, String> colDescription;
    @FXML private TableColumn<Evenement, LocalDate> colDate;
    @FXML private TableColumn<Evenement, LocalTime> colHeureDebut;
    @FXML private TableColumn<Evenement, LocalTime> colHeureFin;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, String> colImage;
    @FXML private TextField searchField;
    @FXML private Button btnTheme;
    @FXML private Label loadingIcon;
    @FXML private Pagination pagination;
    @FXML private TextField txtEmail;
    @FXML private Button logoutButton;
    @FXML private TableColumn<Evenement, Void> colActions;


    private static final int ROWS_PER_PAGE = 6;
    private ObservableList<Evenement> allEvenements;

    @FXML
    public void initialize() {
        EvenementDAO dao = new EvenementDAO();
        allEvenements = FXCollections.observableArrayList(dao.getAllEvenements());
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("📝 Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");
            private final HBox hBox = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: linear-gradient(to right, #74ebd5, #acb6e5); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25;");
                btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #8e44ad, #9b59b6); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25;");

                btnModifier.setOnAction(e -> {
                    Evenement evt = getTableView().getItems().get(getIndex());
                    if (evt != null) {
                        tableEvenements.getSelectionModel().select(evt);
                        modifierEvenement();
                    }
                });

                btnSupprimer.setOnAction(e -> {
                    Evenement evt = getTableView().getItems().get(getIndex());
                    if (evt != null) {
                        tableEvenements.getSelectionModel().select(evt);
                        supprimerEvenement();
                    }
                });

                hBox.setStyle("-fx-alignment: center;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hBox);
            }
        });


        Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>> cellFactory = param -> new TableCell<>() {


            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
            }
        };

        colCategorie.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));



        colImage.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);

                // Clic sur la cellule → popup image
                this.setOnMouseClicked(event -> {
                    Evenement evt = getTableRow().getItem();
                    if (evt != null && evt.getImage() != null && !evt.getImage().isEmpty()) {
                        try {
                            String imagePath = evt.getImage();
                            Image fullImage = new Image(getClass().getResource("/images/" + imagePath).toExternalForm());

                            ImageView fullView = new ImageView(fullImage);
                            fullView.setPreserveRatio(true);
                            fullView.setSmooth(true);
                            fullView.setCache(true);
                            fullView.setFitHeight(600);

                            ScrollPane scrollPane = new ScrollPane(fullView);
                            scrollPane.setFitToWidth(true);
                            scrollPane.setFitToHeight(true);
                            scrollPane.setStyle("-fx-background-color: transparent;");

                            Button closeButton = new Button("Fermer");
                            closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

                            VBox contentBox = new VBox(20, scrollPane, closeButton);
                            contentBox.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: white;");
                            contentBox.setPrefSize(800, 700);

                            AnchorPane modalRoot = new AnchorPane();
                            modalRoot.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
                            AnchorPane.setTopAnchor(contentBox, 50.0);
                            AnchorPane.setLeftAnchor(contentBox, 100.0);
                            AnchorPane.setRightAnchor(contentBox, 100.0);
                            modalRoot.getChildren().add(contentBox);

                            Scene scene = new Scene(modalRoot, 1000, 800);
                            Stage popupStage = new Stage();
                            popupStage.setTitle("📸 Aperçu de l'image");
                            popupStage.setScene(scene);
                            popupStage.setResizable(true);

                            closeButton.setOnAction(e -> popupStage.close());

                            popupStage.show();

                            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.4), contentBox);
                            scale.setFromX(0.8);
                            scale.setFromY(0.8);
                            scale.setToX(1.0);
                            scale.setToY(1.0);
                            scale.play();

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("❌ Erreur popup image : " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = new Image(getClass().getResource("/images/" + imagePath).toExternalForm());
                        imageView.setImage(img);

                        // ✅ Centrage horizontal
                        setGraphic(imageView);
                        setStyle("-fx-alignment: CENTER;");
                    } catch (Exception e) {
                        setGraphic(null);
                        System.out.println("❌ Erreur miniature : " + e.getMessage());
                    }
                }
            }
        });




        // 🔢 Pagination initiale
        pagination.setPageCount((int) Math.ceil((double) allEvenements.size() / ROWS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);

        // 🔍 Mise à jour pagination à chaque saisie dans la recherche
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            pagination.setCurrentPageIndex(0); // 🆕 revenir à la première page
            pagination.setPageFactory(this::createPage); // 🆕 rafraîchir avec filtre
        });
    }
    private Comparator<Evenement> currentComparator = Comparator.comparing(Evenement::getDate);

    private Node createPage(int pageIndex) {
        String filtre = searchField.getText() != null ? searchField.getText().toLowerCase() : "";

        FilteredList<Evenement> filtered = new FilteredList<>(allEvenements, e ->
                filtre.isEmpty() || e.getNom().toLowerCase().contains(filtre)
        );

        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
        if (pagination.getPageCount() != totalPages) {
            pagination.setPageCount(Math.max(totalPages, 1));
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, totalItems);

        if (fromIndex >= toIndex || filtered.isEmpty()) {
            tableEvenements.setItems(FXCollections.observableArrayList());
        } else {
            // Tri de la sous-liste avec le comparateur actif
            List<Evenement> subList = new ArrayList<>(filtered.subList(fromIndex, toIndex));
            subList.sort(currentComparator);
            tableEvenements.setItems(FXCollections.observableArrayList(subList));
        }

        return new AnchorPane(); // requis pour la pagination
    }




    public void rafraichirTable() {
        EvenementDAO dao = new EvenementDAO();
        allEvenements.setAll(dao.getAllEvenements());
        pagination.setPageCount((int) Math.ceil((double) allEvenements.size() / ROWS_PER_PAGE));
        pagination.setPageFactory(this::createPage);
    }



    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ajouter-evenement.fxml"));
            Parent root = loader.load();
            AjouterEvenementController controller = loader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture formulaire : " + e.getMessage());
        }
    }


    @FXML
    private void modifierEvenement() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/modifier-evenement.fxml"));
                Parent root = loader.load();
                ModifierEvenementController controller = loader.getController();
                controller.initData(selected, this);
                Stage stage = new Stage();
                stage.setTitle("Modifier événement");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.out.println("❌ Erreur : " + e.getMessage());
            }
        } else {
            Toast.show((Stage) tableEvenements.getScene().getWindow(), "❗ Sélectionnez un événement à modifier !");
        }
    }

    @FXML
    private void supprimerEvenement() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            EvenementDAO dao = new EvenementDAO();
            dao.supprimerEvenement(selected.getId());
            allEvenements.remove(selected);
            pagination.setPageFactory(this::createPage);
            Toast.show((Stage) tableEvenements.getScene().getWindow(), "🗑️ Événement supprimé !");
        }
    }


    @FXML
    private void effacerRechercheEvenement() {
        searchField.clear();
    }



    private void lancerAnimationChargement(Label label) { //pagination        label.setVisible(true);
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setCycleCount(4);
        scale.setAutoReverse(true);
        scale.setOnFinished(e -> label.setVisible(false));
        scale.play();
    }
    @FXML
    private void trierDateAscendant() {
        lancerAnimationChargement(loadingIcon);
        allEvenements.sort(Comparator.comparing(Evenement::getDate));
        pagination.setPageFactory(this::createPage); // recharge la page avec les événements triés
        Toast.show((Stage) tableEvenements.getScene().getWindow(), "✔ Tri par date (⬆️) effectué !");
    }

    @FXML
    private void trierDateDescendant() {
        lancerAnimationChargement(loadingIcon);
        allEvenements.sort(Comparator.comparing(Evenement::getDate).reversed());
        pagination.setPageFactory(this::createPage);
        Toast.show((Stage) tableEvenements.getScene().getWindow(), "✔ Tri par date (⬇️) effectué !");
    }

    @FXML
    private void reinitialiserFiltresEtTri() {
        lancerAnimationChargement(loadingIcon);
        searchField.clear();
        allEvenements.sort(Comparator.comparing(Evenement::getDate)); // reset default sort
        pagination.setPageFactory(this::createPage);
        Toast.show((Stage) tableEvenements.getScene().getWindow(), "✔ Tri par date effectué !");
    }

    @FXML
    private void toggleTheme() {
        Scene scene = tableEvenements.getScene();//mode sombre et claire
        ObservableList<String> stylesheets = scene.getStylesheets();
        String light = getClass().getResource("/org/example/styles/mode-clair.css").toExternalForm();
        String dark = getClass().getResource("/org/example/styles/dark-theme.css").toExternalForm();

        stylesheets.removeIf(s -> s.contains("mode-clair.css") || s.contains("dark-theme.css"));
        if (btnTheme.getText().equals("🌙")) {
            stylesheets.add(dark);
            btnTheme.setText("☀️");
            Toast.show((Stage) scene.getWindow(), "🌙 Thème sombre activé !");
        } else {
            stylesheets.add(light);
            btnTheme.setText("🌙");
            Toast.show((Stage) scene.getWindow(), "☀️ Thème clair activé !");
        }
    }
    @FXML
    private void exporterPDF(ActionEvent event) {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String chemin = "Evenement_" + selected.getId() + ".pdf";
                ServicePDF.generateEvenementPDF(chemin, selected);
                Toast.show((Stage) tableEvenements.getScene().getWindow(), "✅ PDF généré !");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void exporterEtEnvoyerPDF() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        String email = txtEmail.getText();

        if (selected != null && email != null && !email.isEmpty()) {
            try {
                String chemin = "Evenement_" + selected.getId() + ".pdf";
                ServicePDF.generateEvenementPDF(chemin, selected);

                String sujet = "📎 Détails de votre événement : " + selected.getNom();
                String corps = "Bonjour,\n\nVeuillez trouver ci-joint les détails de l’événement : \"" +
                        selected.getNom() + "\".\n\nCordialement,\nL'équipe.";

                MailService.envoyerPDFParMail(email, sujet, corps, chemin);
                Toast.show((Stage) tableEvenements.getScene().getWindow(), "📧 PDF envoyé à " + email);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.show((Stage) tableEvenements.getScene().getWindow(), "❌ Erreur lors de l’envoi !");
            }
        } else {
            Toast.show((Stage) tableEvenements.getScene().getWindow(), "❌ Sélectionnez un événement et entrez un email !");
        }
    }

    @FXML
    private void envoyerPDFParMail() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        String email = txtEmail.getText();

        if (selected != null && email != null && !email.isEmpty()) {
            try {
                // ✅ Génération du PDF
                String chemin = "Evenement_" + selected.getId() + ".pdf";
                ServicePDF.generateEvenementPDF(chemin, selected);

                // ✅ Sujet et corps personnalisés
                String sujet = "📎 Détails de votre événement : " + selected.getNom();
                String corps = "Bonjour,\n\nVeuillez trouver ci-joint le PDF contenant les détails de l'événement : \""
                        + selected.getNom() + "\".\n\nCordialement,\nL'équipe.";

                // ✅ Envoi par mail
                MailService.envoyerPDFParMail(email, sujet, corps, chemin);

                // ✅ Confirmation visuelle
                Toast.show((Stage) tableEvenements.getScene().getWindow(),
                        "✅ PDF envoyé à " + email + " avec succès !");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.show((Stage) tableEvenements.getScene().getWindow(),
                        "❌ Une erreur est survenue lors de l'envoi du mail !");
            }
        } else {
            Toast.show((Stage) tableEvenements.getScene().getWindow(),
                    "❌ Veuillez sélectionner un événement et entrer un email !");
        }
    }

    @FXML
    private void handleLogout() {
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

    public void ouvrirCategorieView(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/categorie-view.fxml");
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
        }}


    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }

    @FXML
    private void ouvrirStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/statistiques-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📊 Statistiques");
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("✅ Statistiques ouvertes !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture statistiques : " + e.getMessage());
        }
    }
}
