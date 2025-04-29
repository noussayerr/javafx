package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.entity.Jeux;
import org.example.entity.Score;
import org.example.entity.User;
import org.example.services.ServiceJeux;
import org.example.services.ServiceScore;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ScoreController {

    @FXML private TableView<Score> scoreTable;
    @FXML private TableColumn<Score, Integer> idColumn;
    @FXML private TableColumn<Score, String> usernameColumn;
    @FXML private TableColumn<Score, String> jeuColumn;
    @FXML private TableColumn<Score, Integer> scoreColumn;
    @FXML private ComboBox<Jeux> gameFilter;

    private final ServiceScore serviceScore = new ServiceScore();
    private final ServiceJeux serviceJeux = new ServiceJeux();
    public static final String ROLE_APPRENANT = "APPRENANT";
    @FXML
    public void initialize() {
        try {
            // Initialize game filter dropdown
            List<Jeux> games = serviceJeux.afficher();
            gameFilter.setItems(FXCollections.observableArrayList(games));
            gameFilter.setCellFactory(param -> new ListCell<Jeux>() {
                @Override
                protected void updateItem(Jeux game, boolean empty) {
                    super.updateItem(game, empty);
                    if (empty || game == null) {
                        setText(null);
                    } else {
                        setText(game.getNom());
                    }
                }
            });
            gameFilter.setButtonCell(new ListCell<Jeux>() {
                @Override
                protected void updateItem(Jeux game, boolean empty) {
                    super.updateItem(game, empty);
                    if (empty || game == null) {
                        setText(null);
                    } else {
                        setText(game.getNom());
                    }
                }
            });

            // Initialize table columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            usernameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getUser().getNom()));
            jeuColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getJeux().getNom()));
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("highScore"));

            // Configure table row factory for styling
            scoreTable.setRowFactory(tv -> {
                TableRow<Score> row = new TableRow<>();
                row.setStyle("-fx-background-color: rgba(255,255,255,0.05);");

                row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                    if (isNowHovered) {
                        row.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
                    } else {
                        row.setStyle("-fx-background-color: rgba(255,255,255,0.05);");
                    }
                });

                row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        row.setStyle("-fx-background-color: rgba(76,161,175,0.4);");
                    } else {
                        row.setStyle(row.isHover() ?
                                "-fx-background-color: rgba(255,255,255,0.1);" :
                                "-fx-background-color: rgba(255,255,255,0.05);");
                    }
                });
                return row;
            });

        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des jeux: " + e.getMessage());
        }
    }

    @FXML
    void loadScores() {
        Jeux selectedGame = gameFilter.getValue();
        if (selectedGame == null) {
            showAlert("Veuillez sélectionner un jeu");
            return;
        }

        try {
            List<Score> scores = serviceScore.getScoresByGameId(selectedGame.getId());
            ObservableList<Score> data = FXCollections.observableArrayList(scores);
            scoreTable.setItems(data);

            if (scores.isEmpty()) {
                showAlert("Aucun score trouvé pour ce jeu");
            }
        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des scores: " + e.getMessage());
        }
    }

    @FXML
    void goBack() {
        try {
            // Get the current user from your session manager
            User currentUser = SessionManager.getInstance().getCurrentUser();
            String fxmlPath;


            // Check user role
            if (currentUser != null && currentUser.getRoles().contains("ROLE_APPRENANT")) {
                fxmlPath = "/org/example/view/jeuxApprenant.fxml"; // Path to Apprenant dashboard

            } else {
                fxmlPath = "/org/example/view/jeuxIndex.fxml"; // Path to Admin games list
            }

            // Load the appropriate FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) scoreTable.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Erreur lors du retour à la page précédente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}