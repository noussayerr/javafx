package javafx.forum1.controller;

import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.forum1.model.Commentaire;
import javafx.forum1.model.Poste;
import javafx.forum1.service.CommentaireService;
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
import javafx.scene.control.ToggleButton;

import java.sql.SQLException;
import java.util.Comparator;
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
    private TextArea commentaireArea;

    @FXML
    private Button modifierButton;

    @FXML
    private Button supprimerButton;

    @FXML
    private Button ajouterCommentaireButton;

    @FXML
    private Button modifierCommentaireButton;

    @FXML
    private Button supprimerCommentaireButton;

    @FXML
    private ToggleButton triCommentairesToggle;

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

    @FXML
    private TableView<Commentaire> commentaireTable;

    @FXML
    private TableColumn<Commentaire, Number> commentaireIdColumn;

    @FXML
    private TableColumn<Commentaire, String> commentaireContenuColumn;

    @FXML
    private TableColumn<Commentaire, String> commentaireDateColumn;

    private final PosteService posteService = new PosteService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final ObservableList<Poste> posteItems = FXCollections.observableArrayList();
    private final ObservableList<Commentaire> commentaireItems = FXCollections.observableArrayList();

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

        commentaireIdColumn.setCellValueFactory(cell -> new ReadOnlyLongWrapper(cell.getValue().id()));
        commentaireContenuColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().contenu()));
        commentaireDateColumn.setCellValueFactory(cell -> {
            if (cell.getValue().createdAt() == null) {
                return new ReadOnlyStringWrapper("");
            }
            return new ReadOnlyStringWrapper(cell.getValue().createdAt().format(DATE_FORMAT));
        });

        posteTable.setItems(posteItems);
        commentaireTable.setItems(commentaireItems);

        posteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldPoste, newPoste) -> {
            boolean selected = newPoste != null;
            modifierButton.setDisable(!selected);
            supprimerButton.setDisable(!selected);
            ajouterCommentaireButton.setDisable(!selected);
            if (selected) {
                titreField.setText(newPoste.titre());
                descriptionArea.setText(newPoste.description());
                refreshCommentaires(newPoste.id());
            } else {
                commentaireItems.clear();
            }
            clearCommentaireForm();
        });

        commentaireTable.getSelectionModel().selectedItemProperty().addListener((obs, oldCommentaire, newCommentaire) -> {
            boolean selected = newCommentaire != null;
            modifierCommentaireButton.setDisable(!selected);
            supprimerCommentaireButton.setDisable(!selected);
            if (selected) {
                commentaireArea.setText(newCommentaire.contenu());
            }
        });

        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        ajouterCommentaireButton.setDisable(true);
        modifierCommentaireButton.setDisable(true);
        supprimerCommentaireButton.setDisable(true);
        triCommentairesToggle.setSelected(false);
        triCommentairesToggle.setText("Tri: plus recents");
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

    @FXML
    private void onAjouterCommentaireClick() {
        Poste selectedPoste = posteTable.getSelectionModel().getSelectedItem();
        if (selectedPoste == null) {
            messageLabel.setText("Selectionnez un poste avant d'ajouter un commentaire.");
            return;
        }

        try {
            commentaireService.createCommentaire(selectedPoste.id(), commentaireArea.getText());
            refreshCommentaires(selectedPoste.id());
            clearCommentaireForm();
            messageLabel.setText("Commentaire ajoute avec succes.");
        } catch (ValidationException | SQLException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onModifierCommentaireClick() {
        Commentaire selectedCommentaire = commentaireTable.getSelectionModel().getSelectedItem();
        if (selectedCommentaire == null) {
            messageLabel.setText("Selectionnez un commentaire a modifier.");
            return;
        }

        try {
            commentaireService.updateCommentaire(selectedCommentaire.id(), commentaireArea.getText());
            refreshCommentaires(selectedCommentaire.posteId());
            clearCommentaireForm();
            messageLabel.setText("Commentaire modifie avec succes.");
        } catch (ValidationException | SQLException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onSupprimerCommentaireClick() {
        Commentaire selectedCommentaire = commentaireTable.getSelectionModel().getSelectedItem();
        if (selectedCommentaire == null) {
            messageLabel.setText("Selectionnez un commentaire a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le commentaire #" + selectedCommentaire.id());
        confirm.setContentText("Voulez-vous vraiment supprimer ce commentaire ?");

        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            commentaireService.deleteCommentaire(selectedCommentaire.id());
            refreshCommentaires(selectedCommentaire.posteId());
            clearCommentaireForm();
            messageLabel.setText("Commentaire supprime avec succes.");
        } catch (SQLException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onNouveauCommentaireClick() {
        clearCommentaireForm();
        messageLabel.setText("Saisie du commentaire reinitialisee.");
    }

    @FXML
    private void onToggleTriCommentairesClick() {
        applyCommentaireSort();
        if (triCommentairesToggle.isSelected()) {
            triCommentairesToggle.setText("Tri: plus anciens");
        } else {
            triCommentairesToggle.setText("Tri: plus recents");
        }
    }

    private void refreshPostes() {
        try {
            posteItems.setAll(posteService.listPostes());
        } catch (SQLException e) {
            messageLabel.setText("Erreur chargement: " + e.getMessage());
        }
    }

    private void refreshCommentaires(long posteId) {
        try {
            commentaireItems.setAll(commentaireService.listByPosteId(posteId));
            applyCommentaireSort();
        } catch (SQLException e) {
            messageLabel.setText("Erreur chargement commentaires: " + e.getMessage());
        }
    }

    private void applyCommentaireSort() {
        Comparator<Commentaire> comparator = Comparator
                .comparing(Commentaire::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Commentaire::id);

        if (!triCommentairesToggle.isSelected()) {
            comparator = comparator.reversed();
        }

        FXCollections.sort(commentaireItems, comparator);
    }

    private void clearForm() {
        titreField.clear();
        descriptionArea.clear();
        posteTable.getSelectionModel().clearSelection();
        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        commentaireItems.clear();
        clearCommentaireForm();
        ajouterCommentaireButton.setDisable(true);
    }

    private void clearCommentaireForm() {
        commentaireArea.clear();
        commentaireTable.getSelectionModel().clearSelection();
        modifierCommentaireButton.setDisable(true);
        supprimerCommentaireButton.setDisable(true);
    }
}

