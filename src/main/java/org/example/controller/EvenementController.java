package org.example.controller;

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
import java.util.Comparator;


public class EvenementController {

    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, Integer> colCategorie;
    @FXML private TableColumn<Evenement, String> colNom;
    @FXML private TableColumn<Evenement, String> colDescription;
    @FXML private TableColumn<Evenement, LocalDate> colDate;
    @FXML private TableColumn<Evenement, LocalTime> colHeureDebut;
    @FXML private TableColumn<Evenement, LocalTime> colHeureFin;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, String> colImage;
    @FXML private TableColumn<Evenement, Float> colPrix;
    @FXML private TextField searchField;
    @FXML private Button btnTheme;
    @FXML private Label loadingIcon;
    @FXML private Pagination pagination;
    @FXML private TextField txtEmail;
    @FXML private Button logoutButton;

    @FXML
    private TableColumn<Evenement, Void> colPayer;



    private static final int ROWS_PER_PAGE = 10;
    private ObservableList<Evenement> allEvenements;

    @FXML
    public void initialize() {
        EvenementDAO dao = new EvenementDAO();
        allEvenements = FXCollections.observableArrayList(dao.getAllEvenements());
        // Colonne Paiement
        TableColumn<Evenement, Void> colPaiement = new TableColumn<>("Paiement");

        Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Evenement, Void> call(final TableColumn<Evenement, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button("💳 Payer");

                    {
                        btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 8;");
                        btn.setOnAction((event) -> {
                            Evenement evenement = getTableView().getItems().get(getIndex());
                            payerEvenement(evenement);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };

        colPaiement.setCellFactory(cellFactory);
        tableEvenements.getColumns().add(colPaiement);


        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        // Image en miniature
        colImage.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
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
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.out.println("❌ Erreur image : " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });

        pagination.setPageCount((int) Math.ceil((double) allEvenements.size() / ROWS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            pagination.setPageFactory(this::createPage);
        });
    }
    private Node createPage(int pageIndex) {
        String filtre = searchField.getText().toLowerCase();

        // Filtrage dynamique
        FilteredList<Evenement> filtered = new FilteredList<>(allEvenements, e ->
                filtre == null || filtre.isEmpty()
                        || e.getNom().toLowerCase().contains(filtre)
                        || e.getDescription().toLowerCase().contains(filtre)
                        || e.getLieu().toLowerCase().contains(filtre)
        );

        // 🆕 Mise à jour dynamique du nombre de pages après filtre
        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(totalPages, 1));


        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, totalItems);


        if (fromIndex > toIndex || filtered.isEmpty()) {
            tableEvenements.setItems(FXCollections.observableArrayList());
        } else {
            SortedList<Evenement> sorted = new SortedList<>(FXCollections.observableArrayList(filtered.subList(fromIndex, toIndex)));
            sorted.comparatorProperty().bind(tableEvenements.comparatorProperty());
            tableEvenements.setItems(sorted);
        }

        return new AnchorPane(); // requis par la pagination
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

            // ✅ IMPORTANT : injecter le parentController dans le contrôleur du formulaire
            AjouterEvenementController controller = loader.getController();
            controller.setParentController(this); // <--- C'est ça qui permet le rafraîchissement

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
    private void reinitialiserFiltresEtTri() {
        lancerAnimationChargement(loadingIcon);
        searchField.clear();
        tableEvenements.getSortOrder().clear();
        colDate.setSortType(TableColumn.SortType.ASCENDING);
        colPrix.setSortType(TableColumn.SortType.ASCENDING);
        tableEvenements.getSortOrder().addAll(colDate, colPrix);
        pagination.setPageFactory(this::createPage);
        Toast.show((Stage) tableEvenements.getScene().getWindow(), "✔ Tri par date & prix effectué !");
    }

    @FXML
    private void trierDateAscendant() {
        lancerAnimationChargement(loadingIcon);
        allEvenements.sort(Comparator.comparing(Evenement::getDate));
        pagination.setPageFactory(this::createPage);
        Toast.show((Stage) tableEvenements.getScene().getWindow(), "✔ Tri par date (⬆️) effectué !");
    }

    @FXML
    private void trierDateDescendant() {
        lancerAnimationChargement(loadingIcon);
        allEvenements.sort(Comparator.comparing(Evenement::getDate).reversed());
        pagination.setPageFactory(this::createPage);
        Toast.show((Stage) tableEvenements.getScene().getWindow(), "✔ Tri par date (⬇️) effectué !");
    }


    /*@FXML
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
    }*/
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

   /* @FXML
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
    }*/
    @FXML
    private void payerEvenement(Evenement evenement) {
        if (evenement != null) {
            StripeLauncher.openStripeSession(
                    evenement.getNom(),
                    (int) (evenement.getPrix() * 100)
            );
            Toast.show((Stage) tableEvenements.getScene().getWindow(),
                    "💳 Paiement ouvert dans le navigateur !");
        } else {
            Toast.show((Stage) tableEvenements.getScene().getWindow(),
                    "❌ Sélectionnez un événement à payer !");
        }
    }

    public void ouvrirCategorieView(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/categorie-view.fxml");
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
    public void goToMatiere(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ListeMatiere.fxml");
    }

    public void afficherEvenements(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/evenements-view.fxml");
    }

    public void goToUtilisateurs(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/AdminDashboard.fxml");
    }

    @FXML
    private void handleAbonnementsNavigation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ListAbonnement.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void afficherJeux(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/jeuxIndex.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}
