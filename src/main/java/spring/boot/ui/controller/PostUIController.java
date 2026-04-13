package spring.boot.ui.controller;

import spring.boot.entity.Post;
import spring.boot.service.PostService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PostUIController {
    
    @Autowired
    private PostService postService;
    
    @FXML
    private TableView<Post> postTable;
    
    @FXML
    private TableColumn<Post, Long> idColumn;
    
    @FXML
    private TableColumn<Post, String> titleColumn;
    
    @FXML
    private TableColumn<Post, String> authorColumn;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea contentArea;
    
    @FXML
    private TextField authorField;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button clearButton;
    
    private Post selectedPost;
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadPosts();
        setupButtonActions();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().getId())
        );
        titleColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getTitle())
        );
        authorColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getAuthorName())
        );
    }
    
    public void loadPosts() {
        try {
            List<Post> posts = postService.getAllPosts();
            ObservableList<Post> observablePosts = FXCollections.observableArrayList(posts);
            postTable.setItems(observablePosts);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des posts: " + e.getMessage());
        }
    }
    
    private void setupButtonActions() {
        addButton.setOnAction(event -> handleAddPost());
        updateButton.setOnAction(event -> handleUpdatePost());
        deleteButton.setOnAction(event -> handleDeletePost());
        clearButton.setOnAction(event -> clearFields());
        
        postTable.setOnMouseClicked(event -> handlePostSelection());
    }
    
    private void handlePostSelection() {
        selectedPost = postTable.getSelectionModel().getSelectedItem();
        if (selectedPost != null) {
            titleField.setText(selectedPost.getTitle());
            contentArea.setText(selectedPost.getContent());
            authorField.setText(selectedPost.getAuthorName());
        }
    }
    
    @FXML
    private void handleAddPost() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Post newPost = new Post();
            newPost.setTitle(titleField.getText());
            newPost.setContent(contentArea.getText());
            newPost.setAuthorName(authorField.getText());
            
            postService.createPost(newPost);
            showAlert("Succès", "Post créé avec succès!");
            clearFields();
            loadPosts();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création du post: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdatePost() {
        if (selectedPost == null) {
            showAlert("Avertissement", "Veuillez sélectionner un post à modifier!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        try {
            Post updatedPost = new Post();
            updatedPost.setTitle(titleField.getText());
            updatedPost.setContent(contentArea.getText());
            updatedPost.setAuthorName(authorField.getText());
            
            postService.updatePost(selectedPost.getId(), updatedPost);
            showAlert("Succès", "Post mis à jour avec succès!");
            clearFields();
            loadPosts();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour du post: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeletePost() {
        if (selectedPost == null) {
            showAlert("Avertissement", "Veuillez sélectionner un post à supprimer!");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce post?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postService.deletePost(selectedPost.getId());
                showAlert("Succès", "Post supprimé avec succès!");
                clearFields();
                loadPosts();
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression du post: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void clearFields() {
        titleField.clear();
        contentArea.clear();
        authorField.clear();
        searchField.clear();
        selectedPost = null;
        postTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadPosts();
            return;
        }
        
        try {
            List<Post> posts = postService.searchByTitle(searchTerm);
            ObservableList<Post> observablePosts = FXCollections.observableArrayList(posts);
            postTable.setItems(observablePosts);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }
    
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Validation", "Le titre est obligatoire!");
            return false;
        }
        if (contentArea.getText().trim().isEmpty()) {
            showAlert("Validation", "Le contenu est obligatoire!");
            return false;
        }
        if (authorField.getText().trim().isEmpty()) {
            showAlert("Validation", "L'auteur est obligatoire!");
            return false;
        }
        return true;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

