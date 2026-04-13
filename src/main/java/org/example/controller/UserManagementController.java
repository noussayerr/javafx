package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.entity.User;
import org.example.services.UserService;
import org.example.utils.SceneController;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Boolean> verifiedColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private Label adminNameLabel;

    private UserService userService = new UserService();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            adminNameLabel.setText(user.getName());
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleType"));
        verifiedColumn.setCellValueFactory(new PropertyValueFactory<>("isVerified"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btnDelete = new Button("🗑️ Delete");
                    private final Button btnEdit = new Button("✏️ Edit");
                    private final HBox container = new HBox(10, btnEdit, btnDelete);

                    {
                        btnDelete.getStyleClass().add("btn-delete");
                        btnEdit.getStyleClass().add("btn-edit");
                        
                        btnDelete.setOnAction((ActionEvent event) -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDeleteUser(user);
                        });
                        
                        btnEdit.setOnAction((ActionEvent event) -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleEditUser(user);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAll();
            userList.setAll(users);
            userTable.setItems(userList);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load users: " + e.getMessage());
        }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete User: " + user.getName());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.delete(user.getId());
                loadUsers();
            } catch (SQLException e) {
                showAlert("Error", "Could not delete user: " + e.getMessage());
            }
        }
    }

    private void handleEditUser(User user) {
        Stage stage = (Stage) userTable.getScene().getWindow();
        TextInputDialog dialog = new TextInputDialog(user.getName());
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Update name for " + user.getEmail());
        dialog.setContentText("Please enter new name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            try {
                user.setName(newName);
                userService.update(user);
                loadUsers();
            } catch (SQLException e) {
                showAlert("Error", "Could not update user: " + e.getMessage());
            }
        });
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadUsers();
    }

    @FXML
    void handleBackToDashboard(MouseEvent event) {
        Stage stage = (Stage) userTable.getScene().getWindow();
        javafx.fxml.FXMLLoader loader = SceneController.switchTo("dashboardAdmin.fxml", stage, "Admin Dashboard");
        AdminDashboardController controller = loader.getController();
        controller.setUser(currentUser);
    }

    @FXML
    void handleLogout(MouseEvent event) {
        Stage stage = (Stage) userTable.getScene().getWindow();
        SceneController.switchTo("login.fxml", stage, "Login");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
