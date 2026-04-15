package javafx.forum1.controller;

import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.forum1.model.Poste;
import javafx.forum1.service.PosteService;
import javafx.forum1.service.ValidationException;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PosteController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label messageLabel;

    @FXML
    private Button modifierButton;

    @FXML
    private Button supprimerButton;

    @FXML
    private TableView<Poste> posteTable;

    @FXML
    private TableColumn<Poste, Number> idColumn;

    @FXML
    private TableColumn<Poste, String> titreColumn;

    @FXML
    private TableColumn<Poste, String> descriptionColumn;

    @FXML
    private TableColumn<Poste, String> dateCreationColumn;

    private final PosteService posteService = new PosteService();
    private final ObservableList<Poste> posteItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyLongWrapper(cell.getValue().id()));
        titreColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().titre()));
        descriptionColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().description()));
        dateCreationColumn.setCellValueFactory(cell -> {
            if (cell.getValue().dateCreation() == null) {
                return new ReadOnlyStringWrapper("");
            }
            return new ReadOnlyStringWrapper(cell.getValue().dateCreation().format(DATE_FORMAT));
        });

        posteTable.setItems(posteItems);
        posteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldPoste, newPoste) -> {
            boolean selected = newPoste != null;
            modifierButton.setDisable(!selected);
            supprimerButton.setDisable(!selected);
            if (selected) {
                titreField.setText(newPoste.titre());
                descriptionArea.setText(newPoste.description());
            }
        });

        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        refreshPostes();
    }

    @FXML
    private void onAjouterClick() {
        try {
            posteService.createPoste(titreField.getText(), descriptionArea.getText());
            refreshPostes();
            clearForm();
            messageLabel.setText("Poste ajoute avec succes.");
        } catch (ValidationException | SQLException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onModifierClick() {
        Poste selected = posteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Selectionnez un poste a modifier.");
            return;
        }

        try {
            posteService.updatePoste(selected.id(), titreField.getText(), descriptionArea.getText());
            refreshPostes();
            clearForm();
            messageLabel.setText("Poste modifie avec succes.");
        } catch (ValidationException | SQLException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onSupprimerClick() {
        Poste selected = posteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Selectionnez un poste a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le poste #" + selected.id());
        confirm.setContentText("Voulez-vous vraiment supprimer ce poste ?");

        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            posteService.deletePoste(selected.id());
            refreshPostes();
            clearForm();
            messageLabel.setText("Poste supprime avec succes.");
        } catch (SQLException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onNouveauClick() {
        clearForm();
        messageLabel.setText("Formulaire reinitialise.");
    }

    private void refreshPostes() {
        try {
            posteItems.setAll(posteService.listPostes());
        } catch (SQLException e) {
            messageLabel.setText("Erreur chargement: " + e.getMessage());
        }
    }

    private void clearForm() {
        titreField.clear();
        descriptionArea.clear();
        posteTable.getSelectionModel().clearSelection();
        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
    }
}

