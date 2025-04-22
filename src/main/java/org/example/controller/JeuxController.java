package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Jeux;
import org.example.services.ServiceJeux;
import org.example.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class JeuxController {

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
    public void initialize() {
        if (jeuxTable != null) {
            idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
            nomColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
            descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
            typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
            docColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDoC()));
            loadJeux();
        }

        // Disable action buttons only if they are present
        if (deleteButton != null) deleteButton.setDisable(true);
        if (modifyButton != null) modifyButton.setDisable(true);
        if (launchButton != null) launchButton.setDisable(true);

        if (jeuxTable != null) {
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
    }

    public void setSelectedJeux(Jeux jeux) {
        this.selectedJeux = jeux;
        nomField.setText(jeux.getNom());
        descriptionField.setText(jeux.getDescription());
        typeField.setText(jeux.getType());
        docPicker.setValue(jeux.getDoC());
    }

    private final ServiceJeux serviceJeux = new ServiceJeux();

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
        String fxmlFileName = nom + ".fxml";
        String controllerFileName = capitalize(nom) + "Controller.java"; // Convention: Match game name

        if (selectedGameFile == null || selectedControllerFile == null) {
            fileError.setText("Please select both FXML and Controller files.");
            return;
        }

        // Copy FXML into org.example.view
        Path fxmlDestPath = Paths.get("src/main/resources/org/example/view/", fxmlFileName);
        // Copy Controller into org.example.controller
        Path controllerDestPath = Paths.get("src/main/java/org/example/controller/", controllerFileName);

        try {
            Files.copy(selectedGameFile.toPath(), fxmlDestPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(selectedControllerFile.toPath(), controllerDestPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fileError.setText("Failed to copy files.");
            e.printStackTrace();
            return;
        }

        // Persist the game info (we only store metadata, not files)
        Jeux newGame = new Jeux();
        newGame.setNom(nom);
        newGame.setDescription(descriptionField.getText().trim());
        newGame.setType(typeField.getText().trim());
        newGame.setDoC(docPicker.getValue());

        try {
            serviceJeux.ajouter(newGame);
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void modifierJeux(ActionEvent event) {
        if (selectedJeux != null && validateFields()) {
            selectedJeux.setNom(nomField.getText());
            selectedJeux.setDescription(descriptionField.getText());
            selectedJeux.setType(typeField.getText());
            selectedJeux.setDoC(docPicker.getValue());

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
            AnchorPane listPane = loader.load();

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
    // Navigation methods
    @FXML
    private void showHome(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/EnseignantDashboard.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    @FXML
    private void showGames() throws IOException {
        loadView("jeuxApprenant.fxml");
    }



    @FXML
    private void handleProfile(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/ProfileApprenant.fxml"));
            AnchorPane listPane = loader.load();

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(listPane));
            stage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private AnchorPane rootPane;
    private void loadView(String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/" + fxmlFile));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
