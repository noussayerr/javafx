package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Jeux;
import org.example.services.ServiceJeux;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class JeuxController {

    // Search and Filter components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private FlowPane gamesContainer;

    // Game data
    private ObservableList<Jeux> allGames;
    private FilteredList<Jeux> filteredGames;
    private final ServiceJeux serviceJeux = new ServiceJeux();
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField typeField;
    @FXML private DatePicker docPicker;

    @FXML private Label nomError;
    @FXML private Label descError;
    @FXML private Label typeError;
    @FXML private Label docError;

    @FXML private TableView<Jeux> jeuxTable;
    @FXML private TableColumn<Jeux, Integer> idColumn;
    @FXML private TableColumn<Jeux, String> nomColumn;
    @FXML private TableColumn<Jeux, String> descriptionColumn;
    @FXML private TableColumn<Jeux, String> typeColumn;
    @FXML private TableColumn<Jeux, LocalDate> docColumn;

    @FXML private TextField  filePathField,controllerPathField;
    @FXML private Label fileError,controllerError;

    private File selectedGameFile,selectedControllerFile;

    private Jeux selectedJeux;

    @FXML private Button deleteButton;
    @FXML private Button modifyButton;
    @FXML private Button launchButton;

    @FXML
    private void initialize() {
        // Common initialization for both views
        if (jeuxTable != null) {
            // Initialize table columns (common for both views)
            idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
            nomColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
            descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
            typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
            docColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDoC()));

            loadJeux(); // Load data for admin view

            // Setup selection listener (common for both views)
            jeuxTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedJeux = newSelection;
                    if (deleteButton != null) deleteButton.setDisable(false);
                    if (modifyButton != null) modifyButton.setDisable(false);
                    if (launchButton != null) launchButton.setDisable(false);
                } else {
                    selectedJeux = null;
                    if (deleteButton != null) deleteButton.setDisable(true);
                    if (modifyButton != null) modifyButton.setDisable(true);
                    if (launchButton != null) launchButton.setDisable(true);
                }
            });
        }

        // Only initialize apprenant-specific components if they exist
        if (searchField != null && typeFilter != null && gamesContainer != null) {
            setupSearchAndFilter();
            loadGames();
        }
    }
    public void setSelectedJeux(Jeux jeux) {
        this.selectedJeux = jeux;
        nomField.setText(jeux.getNom());
        descriptionField.setText(jeux.getDescription());
        typeField.setText(jeux.getType());
        docPicker.setValue(jeux.getDoC());
    }


    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Game FXML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FXML Files", "*.fxml"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
            selectedGameFile = file;
            fileError.setText("");
        }
    }
    @FXML
    private void handleBrowseController(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Controller Java File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            controllerPathField.setText(file.getAbsolutePath());
            selectedControllerFile = file;
            controllerError.setText(""); // Clear errors if both are selected
        }
    }


    private void loadJeux() {
        try {
            List<Jeux> jeuxList = serviceJeux.afficher();
            ObservableList<Jeux> data = FXCollections.observableArrayList(jeuxList);
            jeuxTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    @FXML
    private void ajouterJeux(ActionEvent event) {
        String nom = nomField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = typeField.getText().trim();
        LocalDate date = docPicker.getValue();

        // Vérification champs vides
        if (nom.isEmpty() || description.isEmpty() || type.isEmpty() || date == null) {
            fileError.setText("Tous les champs sont obligatoires.");
            return;
        }

        // Vérification fichiers sélectionnés
        if (selectedGameFile == null || selectedControllerFile == null) {
            fileError.setText("Veuillez sélectionner les fichiers FXML et Controller.");
            return;
        }

        // Vérification doublon
        if (serviceJeux.jeuExiste(nom)) {
            fileError.setText("Erreur : Ce jeu existe déjà.");
            return;
        }

        // Préparation noms de fichiers
        String fxmlFileName = nom + ".fxml";
        String controllerClassName = capitalize(nom) + "Controller.java";
        Path fxmlDestPath = Paths.get("src/main/resources/org/example/view/", fxmlFileName);
        Path controllerDestPath = Paths.get("src/main/java/org/example/controller/", controllerClassName);

        try {
            // Copie du fichier FXML
            Files.copy(selectedGameFile.toPath(), fxmlDestPath, StandardCopyOption.REPLACE_EXISTING);

            // Modification du nom de classe dans le controller
            String content = new String(Files.readAllBytes(selectedControllerFile.toPath()), StandardCharsets.UTF_8);
            content = content.replaceFirst("public class .*?\\s", "public class " + capitalize(nom) + "Controller ");
            Files.write(controllerDestPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            fileError.setText("Erreur lors de la copie des fichiers.");
            e.printStackTrace();
            return;
        }

        // Création du jeu
        Jeux newGame = new Jeux();
        newGame.setNom(nom);
        newGame.setDescription(description);
        newGame.setType(type);
        newGame.setDoC(date);

        try {
            serviceJeux.ajouter(newGame);
            clearFields(); // Méthode à toi pour réinitialiser les champs
            fileError.setText("Jeu ajouté avec succès !");
        } catch (SQLException e) {
            fileError.setText("Erreur SQL lors de l'ajout du jeu.");
            e.printStackTrace();
        }
    }


    @FXML
    void modifierJeux(ActionEvent event) {
        if (selectedJeux != null && validateFields()) {
            String oldName = selectedJeux.getNom();
            String newName = nomField.getText().trim();

            selectedJeux.setNom(newName);
            selectedJeux.setDescription(descriptionField.getText());
            selectedJeux.setType(typeField.getText());
            selectedJeux.setDoC(docPicker.getValue());

            if (!oldName.equals(newName)) {
                String oldFxmlFile = oldName + ".fxml";
                String newFxmlFile = newName + ".fxml";

                String oldControllerFile = capitalize(oldName) + "Controller.java";
                String newControllerFile = capitalize(newName) + "Controller.java";

                Path oldFxmlPath = Paths.get("src/main/resources/org/example/view/", oldFxmlFile);
                Path newFxmlPath = Paths.get("src/main/resources/org/example/view/", newFxmlFile);

                Path oldControllerPath = Paths.get("src/main/java/org/example/controller/", oldControllerFile);
                Path newControllerPath = Paths.get("src/main/java/org/example/controller/", newControllerFile);

                try {
                    // Rename FXML file
                    if (Files.exists(oldFxmlPath)) {
                        Files.move(oldFxmlPath, newFxmlPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Rename Controller file
                    if (Files.exists(oldControllerPath)) {
                        String content = new String(Files.readAllBytes(oldControllerPath), StandardCharsets.UTF_8);
                        content = content.replaceFirst("public class .*?\\s", "public class " + capitalize(newName) + "Controller ");
                        Files.write(newControllerPath, content.getBytes(StandardCharsets.UTF_8));
                        Files.delete(oldControllerPath);
                    }
                } catch (IOException e) {
                    fileError.setText("Failed to rename files.");
                    e.printStackTrace();
                    return;
                }
            }

            try {
                serviceJeux.modifier(selectedJeux);
                clearFields();
                goToList(event); // Return to the list page
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void supprimerJeux(ActionEvent event) {
        Jeux selected = jeuxTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Delete from database
                serviceJeux.supprimer(selected.getId());

                // Delete FXML file
                String fxmlFileName = selected.getNom() + ".fxml";
                Path fxmlPath = Paths.get("src/main/resources/org/example/view/", fxmlFileName);

                // Delete Controller file
                String controllerFileName = selected.getNom() + "Controller.java";
                Path controllerPath = Paths.get("src/main/java/org/example/controller/", controllerFileName);

                try {
                    Files.deleteIfExists(fxmlPath);
                    Files.deleteIfExists(controllerPath);
                } catch (IOException e) {
                    System.err.println("Failed to delete one or more game files:");
                    System.err.println("FXML: " + fxmlPath);
                    System.err.println("Controller: " + controllerPath);
                    e.printStackTrace();
                }

                // Refresh table
                loadJeux();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean validateFields() {
        boolean valid = true;

        // Reset styles
        nomField.setStyle("");
        typeField.setStyle("");
        descriptionField.setStyle("");
        docPicker.setStyle("");

        nomError.setText("");
        typeError.setText("");
        descError.setText("");
        docError.setText("");

        if (nomField.getText().trim().isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            nomError.setText("Nom requis");
            valid = false;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            descriptionField.setStyle("-fx-border-color: red;");
            descError.setText("Description requise");
            valid = false;
        }
        if (typeField.getText().trim().isEmpty()) {
            typeField.setStyle("-fx-border-color: red;");
            typeError.setText("Type requis");
            valid = false;
        }
        if (docPicker.getValue() == null) {
            docPicker.setStyle("-fx-border-color: red;");
            docError.setText("Date requise");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        descriptionField.clear();
        typeField.clear();
        docPicker.setValue(null);
        filePathField.clear();

        nomField.setStyle("");
        descriptionField.setStyle("");
        typeField.setStyle("");
        docPicker.setStyle("");
        filePathField.setStyle("");

        nomError.setText("");
        descError.setText("");
        typeError.setText("");
        docError.setText("");
        fileError.setText("");
    }

    @FXML
    public void goToAjout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxForm.fxml"));
            Parent root = loader.load();

            // Optionally get the controller of the new page if needed
            // AjoutJeuxController controller = loader.getController();

            // Get current stage from event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void goToModifierForm(ActionEvent event) {
        selectedJeux = jeuxTable.getSelectionModel().getSelectedItem();

        if (selectedJeux != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxForm.fxml"));
                Parent root = loader.load();

                JeuxController formController = loader.getController();
                formController.setSelectedJeux(selectedJeux);  // Pass the selected item to the form

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    public void goToList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/jeuxIndex.fxml"));
            Parent listPane = loader.load(); // Use Parent instead of AnchorPane

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void launchGame() {
        if (selectedJeux == null) {
            System.out.println("No game selected.");
            return;
        }

        try {
            String fxmlFile = "/org/example/view/" + selectedJeux.getNom().replaceAll("\\s+", "") + ".fxml";
            System.out.println("Trying to load FXML: " + fxmlFile);

            URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                System.err.println("FXML file not found at: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            AnchorPane gamePane = loader.load();  // or VBox if your root is VBox, etc.

            Stage stage = new Stage();
            stage.setScene(new Scene(gamePane));
            stage.setTitle("Playing: " + selectedJeux.getNom());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load game file for: " + selectedJeux.getNom());
        }
    }
    @FXML
    private Button logoutButton;

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
            showAlertAdmin(Alert.AlertType.INFORMATION, "Déconnexion réussie", "Vous avez été déconnecté avec succès.", "");
        } catch (IOException e) {
            showAlertAdmin(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion", e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupSearchAndFilter() {
        try {
            // Get distinct game types from database
            List<String> gameTypes = serviceJeux.getDistinctGameTypes();

            // Add "Tous" option first
            gameTypes.add(0, "Tous");

            // Initialize type filter with actual categories from database
            typeFilter.getItems().addAll(gameTypes);
            typeFilter.getSelectionModel().selectFirst();

            // Set up listeners for search and filter
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterGames());
            typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterGames());
        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback to default types if database fails
            typeFilter.getItems().addAll("Tous", "Mémoire", "Vocabulaire", "Orthographe", "Logique");
            typeFilter.getSelectionModel().selectFirst();
        }
    }
    private void loadGames() {
        try {
            List<Jeux> gamesList = serviceJeux.afficher();
            allGames = FXCollections.observableArrayList(gamesList);
            filteredGames = new FilteredList<>(allGames);

            filterGames(); // Initial display with all games
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de chargement", "Impossible de charger les jeux.");
        }
    }

    private void filterGames() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = typeFilter.getValue();

        filteredGames.setPredicate(game -> {
            // Check if game matches search text
            boolean matchesSearch = game.getNom().toLowerCase().contains(searchText) ||
                    game.getDescription().toLowerCase().contains(searchText);

            // Check if game matches selected type
            boolean matchesType = selectedType.equals("Tous") ||
                    game.getType().equalsIgnoreCase(selectedType);

            return matchesSearch && matchesType;
        });

        displayGames();
    }

    private void displayGames() {
        gamesContainer.getChildren().clear();

        for (Jeux game : filteredGames) {
            VBox card = createGameCard(game);
            gamesContainer.getChildren().add(card);
        }
    }
    private VBox createGameCard(Jeux game) {
        // Card container
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15;");
        card.setEffect(new javafx.scene.effect.DropShadow(10, Color.gray(0.5)));
        card.setPrefWidth(250);
        card.setMinHeight(300);

        // Game image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream(
                    "/org/example/images/games/" + game.getNom().replaceAll("\\s+", "") + ".png"));
            imageView.setImage(image);
        } catch (Exception e) {
            // Use placeholder if no image found
            imageView.setImage(new Image(getClass().getResourceAsStream(
                    "/org/example/data/img.png")));
        }
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // Game name - THIS WAS THE ISSUE - you had type here instead of name
        Label nameLabel = new Label(game.getNom());  // Changed from game.getType() to game.getNom()
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #4a6baf; -fx-font-weight: bold;");

        // Game type - THIS WAS SHOWING THE NAME INSTEAD OF TYPE
        Label typeLabel = new Label(game.getType());  // Make sure this is game.getType() not game.getNom()
        typeLabel.setStyle("-fx-text-fill: #4a6baf; -fx-font-weight: light;");

        // Game description (truncated)
        Label descLabel = new Label(game.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);
        descLabel.setStyle("-fx-font-size: 12px;");

        // Play button
        Button playButton = new Button("Jouer");
        playButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        playButton.setOnAction(e -> launchGameApp(game));
        playButton.setMaxWidth(Double.MAX_VALUE);

        // Leaderboard button
        Button leaderboardButton = new Button("Classement");
        leaderboardButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        leaderboardButton.setOnAction(e -> goToLeaderboard(game));
        leaderboardButton.setMaxWidth(Double.MAX_VALUE);

        VBox buttonBox = new VBox(5, playButton, leaderboardButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        card.getChildren().addAll(imageView, nameLabel, typeLabel, descLabel, buttonBox);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        return card;
    }
    private void launchGameApp(Jeux game) {
        try {
            String fxmlFile = "/org/example/view/" + game.getNom().replaceAll("\\s+", "") + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(game.getNom());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Jeu non disponible", "Impossible de charger ce jeu.");
        }
    }

    private void goToLeaderboard(Jeux game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/score.fxml"));
            Parent root = loader.load();

            // You can pass game information to the leaderboard controller if needed
            // ScoreController controller = loader.getController();
            // controller.setGame(game);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Classement - " + game.getNom());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Existing navigation methods (keep all your original methods)
    @FXML
    private void showHome(ActionEvent event) throws IOException {
        loadView("EnseignantDashboard.fxml", event);
    }

    @FXML
    private void showCourses(ActionEvent event) throws IOException {
        loadView("CoursApprenant.fxml", event);
    }

    @FXML
    private void ouvrirListeEvenements(ActionEvent event) throws IOException {
        loadView("Evenement-list.fxml", event);
    }

    @FXML
    private void handleProfile(ActionEvent event) throws IOException {
        loadView("ProfileApprenant.fxml", event);
    }



    private void loadView(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/" + fxmlFile));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlertAdmin(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void goToLeaderboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/score.fxml"));
            Parent listPane = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}