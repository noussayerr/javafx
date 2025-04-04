package org.example.utils;

import javafx.stage.Stage;
import org.example.entity.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Stage currentStage;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
    }

    public void logout() {
        this.currentUser = null;
        this.currentStage = null;
    }
}