package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entity.Score;
import org.example.services.ServiceScore;

import java.sql.SQLException;
import java.util.List;

public class ScoreController {

    @FXML
    private TableView<Score> scoreTable;
    @FXML
    private TableColumn<Score, Integer> idColumn;
    @FXML
    private TableColumn<Score, String> usernameColumn;
    @FXML
    private TableColumn<Score, String> jeuColumn;
    @FXML
    private TableColumn<Score, Integer> scoreColumn;

    @FXML
    private TextField filterField;
    @FXML
    private RadioButton byUserRadio;
    @FXML
    private RadioButton byGameRadio;
    private final ToggleGroup filterGroup = new ToggleGroup();

    private final ServiceScore serviceScore = new ServiceScore();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUser().getNom()));
        jeuColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJeux().getNom()));
        scoreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getHighScore()).asObject());

        byUserRadio.setToggleGroup(filterGroup);
        byGameRadio.setToggleGroup(filterGroup);
        byUserRadio.setSelected(true);
    }

    @FXML
    void loadScores() {
        int id;
        try {
            id = Integer.parseInt(filterField.getText());
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer un ID valide.");
            return;
        }

        try {
            List<Score> scores;
            if (byUserRadio.isSelected()) {
                scores = serviceScore.getScoresByUserId(id);
            } else {
                scores = serviceScore.getScoresByGameId(id);
            }

            ObservableList<Score> data = FXCollections.observableArrayList(scores);
            scoreTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des scores.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
