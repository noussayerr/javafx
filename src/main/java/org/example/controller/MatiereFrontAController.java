package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.Cours;
import org.example.entity.Matiere;
import org.example.services.ServiceCours;
import org.example.services.ServiceMatiere;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MatiereFrontAController {
    @FXML private TilePane matiereContainer;
    @FXML private TextField searchField;
    @FXML private HBox pageButtons;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label displayInfoLabel;
    @FXML private VBox categoriesContainer;
    @FXML private Label totalCountLabel;
    @FXML private Label freeCountLabel;
    @FXML private Label premiumCountLabel;
    @FXML private Label totalLevelCountLabel;
    @FXML private Label beginnerCountLabel;
    @FXML private Label intermediateCountLabel;
    @FXML private Label expertCountLabel;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private final ServiceCours serviceCours = new ServiceCours();
    private List<Matiere> allMatieres;
    private List<Matiere> filteredMatieres;
    private int currentPage = 1;
    private final int itemsPerPage = 8;
    private int totalPages;

    @FXML
    public void initialize() {
        try {
            allMatieres = serviceMatiere.afficher();
            if (allMatieres == null) {
                allMatieres = new ArrayList<>();
            }
            filteredMatieres = new ArrayList<>(allMatieres);
            updatePagination();

            populateCategories();
            populateFilterCounts();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                filterMatieres(newValue.trim().toLowerCase());
                updatePagination();
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", "Problème lors du chargement des matières", e.getMessage());
        }
    }

    private void filterMatieres(String searchText) {
        if (searchText.isEmpty()) {
            filteredMatieres = new ArrayList<>(allMatieres);
        } else {
            filteredMatieres = allMatieres.stream()
                    .filter(matiere -> matiere.getNomM() != null && matiere.getNomM().toLowerCase().contains(searchText) ||
                            matiere.getTitreM() != null && matiere.getTitreM().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredMatieres.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int start = (currentPage - 1) * itemsPerPage + 1;
        int end = Math.min(currentPage * itemsPerPage, filteredMatieres.size());
        displayInfoLabel.setText(String.format("Affichage de %d à %d sur %d", start, end, filteredMatieres.size()));

        pageButtons.getChildren().clear();
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(String.valueOf(i));
            pageButton.setStyle("-fx-background-color: " + (i == currentPage ? "#4B5EAA" : "#E8EDFF") + "; -fx-text-fill: " + (i == currentPage ? "#FFFFFF" : "#4B5EAA") + "; -fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-background-radius: 5; -fx-padding: 5 10;");
            final int pageNum = i;
            pageButton.setOnAction(e -> {
                currentPage = pageNum;
                updatePagination();
            });
            pageButtons.getChildren().add(pageButton);
        }

        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);

        displayMatieres();
    }

    private void displayMatieres() {
        matiereContainer.getChildren().clear();
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredMatieres.size());

        for (int i = startIndex; i < endIndex; i++) {
            Matiere matiere = filteredMatieres.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/MatiereCard.fxml"));
                HBox card = loader.load();
                MatiereCardController controller = loader.getController();
                controller.setData(matiere, this::navigateToCourses);
                controller.setPastelColors("#E8EDFF", "#F3E8FF");
                matiereContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Error loading MatiereCard for " + (matiere.getNomM() != null ? matiere.getNomM() : "unknown") + ": " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la carte", e.getMessage());
            }
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    private void navigateToCourses(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/AffichCoursApp.fxml"));
            Parent root = loader.load();
            AffichCoursAppController controller = loader.getController();
            controller.setMatiere(matiere);
            Stage stage = (Stage) matiereContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des cours", e.getMessage());
        }
    }

    private void populateCategories() {
        categoriesContainer.getChildren().clear();
        List<Matiere> shuffledMatieres = new ArrayList<>(allMatieres);
        Collections.shuffle(shuffledMatieres);
        int maxCategories = Math.min(6, shuffledMatieres.size());
        for (int i = 0; i < maxCategories; i++) {
            Matiere matiere = shuffledMatieres.get(i);
            HBox categoryRow = new HBox(10);
            CheckBox checkBox = new CheckBox(matiere.getNomM() + " (" + getCourseCount(matiere) + ")");
            checkBox.setStyle("-fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-text-fill: #0e0e0e;");
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    navigateToCourses(matiere);
                }
            });
            categoryRow.getChildren().add(checkBox);
            categoriesContainer.getChildren().add(categoryRow);
        }
    }

    private int getCourseCount(Matiere matiere) {
        try {
            List<Cours> courses = serviceCours.getCoursParMatiere(matiere.getId());
            return courses != null ? courses.size() : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private void populateFilterCounts() {
        int totalCount = allMatieres.stream().mapToInt(this::getCourseCount).sum();
        totalCountLabel.setText("Tous (" + totalCount + ")");

        int freeCount = 0;
        int premiumCount = 0;
        int beginnerCount = 0;
        int intermediateCount = 0;
        int expertCount = 0;

        for (Matiere matiere : allMatieres) {
            try {
                List<Cours> courses = serviceCours.getCoursParMatiere(matiere.getId());
                if (courses != null) {
                    for (Cours cours : courses) {
                        if (cours.getType() != null && "free".equalsIgnoreCase(cours.getType())) {
                            freeCount++;
                        } else if (cours.getType() != null && "premium".equalsIgnoreCase(cours.getType())) {
                            premiumCount++;
                        }

                        if (cours.getNivC() != null && "Débutant".equalsIgnoreCase(cours.getNivC())) {
                            beginnerCount++;
                        } else if (cours.getNivC() != null && "Intermédiaire".equalsIgnoreCase(cours.getNivC())) {
                            intermediateCount++;
                        } else if (cours.getNivC() != null && "Avancé".equalsIgnoreCase(cours.getNivC())) {
                            expertCount++;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        freeCountLabel.setText("Cours gratuits (" + freeCount + ")");
        premiumCountLabel.setText("Cours Premium (" + premiumCount + ")");
        totalLevelCountLabel.setText("Tous les niveaux (" + totalCount + ")");
        beginnerCountLabel.setText("Débutant (" + beginnerCount + ")");
        intermediateCountLabel.setText("Intermédiaire (" + intermediateCount + ")");
        expertCountLabel.setText("Expert (" + expertCount + ")");
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
            showAlert(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", e.getMessage());
        }
    }

    public void effacerRecherche(ActionEvent actionEvent) {
        searchField.clear();
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

    public void goAccueil(ActionEvent actionEvent) {
        loadPage(actionEvent, "/org/example/view/ApprenantDashboard.fxml");
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
}