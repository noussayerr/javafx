package org.example.utils;

import javafx.stage.Stage;
import org.example.entity.User;
import java.util.prefs.Preferences;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);

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

    public void logout() {
        this.currentUser = null;
        // Clear remembered credentials
        prefs.remove("remembered_email");
        prefs.remove("remembered_token");
    }
}