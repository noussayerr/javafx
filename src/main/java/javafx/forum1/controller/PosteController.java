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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;

import java.sql.SQLException;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.UnaryOperator;

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
    private Button ajouterButton;

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
        setupInputValidation();

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
            if (selected) {
                titreField.setText(newPoste.titre());
                descriptionArea.setText(newPoste.description());
                refreshCommentaires(newPoste.id());
            } else {
                commentaireItems.clear();
            }
            clearCommentaireForm();
            updatePosteButtonsState();
            updateCommentaireButtonsState();
        });

        commentaireTable.getSelectionModel().selectedItemProperty().addListener((obs, oldCommentaire, newCommentaire) -> {
            if (newCommentaire != null) {
                commentaireArea.setText(newCommentaire.contenu());
            }
            updateCommentaireButtonsState();
        });

        titreField.textProperty().addListener((obs, oldValue, newValue) -> updatePosteButtonsState());
        descriptionArea.textProperty().addListener((obs, oldValue, newValue) -> updatePosteButtonsState());

        triCommentairesToggle.setSelected(false);
        triCommentairesToggle.setText("Tri: plus recents");
        updatePosteButtonsState();
        updateCommentaireButtonsState();
        refreshPostes();
    }

    @FXML
    private void onAjouterClick() {
        String posteHelp = getPosteInputHelpMessage();
        if (posteHelp != null) {
            messageLabel.setText(posteHelp);
            return;
        }

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

        String posteHelp = getPosteInputHelpMessage();
        if (posteHelp != null) {
            messageLabel.setText(posteHelp);
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

        String commentaireHelp = getCommentaireInputHelpMessage();
        if (commentaireHelp != null) {
            messageLabel.setText(commentaireHelp);
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

        String commentaireHelp = getCommentaireInputHelpMessage();
        if (commentaireHelp != null) {
            messageLabel.setText(commentaireHelp);
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
        commentaireItems.clear();
        clearCommentaireForm();
        updatePosteButtonsState();
        updateCommentaireButtonsState();
    }

    private void clearCommentaireForm() {
        commentaireArea.clear();
        commentaireTable.getSelectionModel().clearSelection();
        updateCommentaireButtonsState();
    }

    private void setupInputValidation() {
        limitInputLength(titreField, PosteService.TITRE_MAX_LENGTH);
        limitInputLength(descriptionArea, PosteService.DESCRIPTION_MAX_LENGTH);
        limitInputLength(commentaireArea, CommentaireService.CONTENU_MAX_LENGTH);
    }

    private void limitInputLength(TextInputControl input, int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String nextText = change.getControlNewText();
            return nextText.length() <= maxLength ? change : null;
        };
        input.setTextFormatter(new TextFormatter<>(filter));
    }

    private void updatePosteButtonsState() {
        boolean hasPosteSelected = posteTable.getSelectionModel().getSelectedItem() != null;

        // On garde la validation stricte dans le service pour eviter de bloquer le CRUD cote UI.
        ajouterButton.setDisable(false);
        modifierButton.setDisable(!hasPosteSelected);
        supprimerButton.setDisable(!hasPosteSelected);
    }

    private void updateCommentaireButtonsState() {
        boolean hasPosteSelected = posteTable.getSelectionModel().getSelectedItem() != null;
        boolean hasCommentaireSelected = commentaireTable.getSelectionModel().getSelectedItem() != null;

        // Meme logique UX que les postes: ne pas bloquer l'action sur la saisie cote UI.
        ajouterCommentaireButton.setDisable(!hasPosteSelected);
        modifierCommentaireButton.setDisable(!hasCommentaireSelected);
        supprimerCommentaireButton.setDisable(!hasCommentaireSelected);
    }

    private String getCommentaireInputHelpMessage() {
        String contenu = commentaireArea.getText();
        if (contenu == null || contenu.isBlank()) {
            return "Aide: saisissez un commentaire avant de continuer.";
        }

        int longueur = contenu.trim().length();
        if (longueur < CommentaireService.CONTENU_MIN_LENGTH) {
            return "Aide: le commentaire doit contenir au moins "
                    + CommentaireService.CONTENU_MIN_LENGTH + " caracteres.";
        }
        if (longueur > CommentaireService.CONTENU_MAX_LENGTH) {
            return "Aide: le commentaire ne doit pas depasser "
                    + CommentaireService.CONTENU_MAX_LENGTH + " caracteres.";
        }

        return null;
    }

    private String getPosteInputHelpMessage() {
        String titre = titreField.getText();
        String description = descriptionArea.getText();

        if (titre == null || titre.isBlank()) {
            return "Aide: saisissez un titre avant de continuer.";
        }
        if (description == null || description.isBlank()) {
            return "Aide: saisissez une description avant de continuer.";
        }

        int titreLength = titre.trim().length();
        if (titreLength < PosteService.TITRE_MIN_LENGTH) {
            return "Aide: le titre doit contenir au moins "
                    + PosteService.TITRE_MIN_LENGTH + " caracteres.";
        }
        if (titreLength > PosteService.TITRE_MAX_LENGTH) {
            return "Aide: le titre ne doit pas depasser "
                    + PosteService.TITRE_MAX_LENGTH + " caracteres.";
        }

        int descriptionLength = description.trim().length();
        if (descriptionLength < PosteService.DESCRIPTION_MIN_LENGTH) {
            return "Aide: la description doit contenir au moins "
                    + PosteService.DESCRIPTION_MIN_LENGTH + " caracteres.";
        }
        if (descriptionLength > PosteService.DESCRIPTION_MAX_LENGTH) {
            return "Aide: la description ne doit pas depasser "
                    + PosteService.DESCRIPTION_MAX_LENGTH + " caracteres.";
        }

        return null;
    }

}

