package org.example.services;

import org.example.entity.User;
import org.example.utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceUser {
    private Connection connection;
    // In-memory storage for reset codes: email -> {code, expiration timestamp}
    private static final Map<String, ResetCodeData> resetCodes = new HashMap<>();

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // Data class to hold reset code and expiration
    private static class ResetCodeData {
        String code;
        long expiresAt;

        ResetCodeData(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    public User authenticate(String email, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ? AND etat = 'actif'";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("password");

                // Conversion du format Symfony ($2y$) vers format BCrypt Java ($2a$)
                if (storedHash.startsWith("$2y$")) {
                    storedHash = "$2a$" + storedHash.substring(4);
                }

                if (BCrypt.checkpw(password, storedHash)) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setNom(resultSet.getString("nom"));
                    user.setPrenom(resultSet.getString("prenom"));
                    user.setTelephone(resultSet.getInt("telephone"));
                    user.setDateNaissance(resultSet.getString("dateNaissance"));
                    user.setPhotoProfil(resultSet.getString("photo_profil"));

                    String rolesJson = resultSet.getString("roles");
                    List<String> roles = convertJsonToRoles(rolesJson);
                    user.setRoles(roles);

                    return user;
                }
            }
        }
        return null;
    }

    private List<String> convertJsonToRoles(String rolesJson) {
        List<String> roles = new ArrayList<>();
        if (rolesJson != null && !rolesJson.isEmpty()) {
            // Supposons que le JSON est sous forme ["ROLE1","ROLE2"]
            String cleaned = rolesJson.replaceAll("[\\[\\]\"]", "");
            String[] roleArray = cleaned.split(",");
            for (String role : roleArray) {
                if (!role.trim().isEmpty()) {
                    roles.add(role.trim());
                }
            }
        }
        return roles;
    }

    public boolean toggleUserStatus(int userId) throws SQLException {
        // D'abord récupérer l'état actuel de l'utilisateur
        String currentStateQuery = "SELECT etat FROM user WHERE id = ?";
        String currentState = null;

        try (PreparedStatement getStateStmt = connection.prepareStatement(currentStateQuery)) {
            getStateStmt.setInt(1, userId);
            ResultSet rs = getStateStmt.executeQuery();

            if (rs.next()) {
                currentState = rs.getString("etat");
            } else {
                throw new SQLException("User not found with ID: " + userId);
            }
        }

        // Déterminer le nouvel état
        String newState = "actif".equalsIgnoreCase(currentState) ? "inactif" : "actif";

        // Mettre à jour l'état
        String updateQuery = "UPDATE user SET etat = ? WHERE id = ?";

        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setString(1, newState);
            updateStmt.setInt(2, userId);

            int rowsAffected = updateStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public User findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        User user = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setEmail(resultSet.getString("email"));
                user.setNom(resultSet.getString("nom"));
                user.setPrenom(resultSet.getString("prenom"));
                user.setEtat(resultSet.getString("etat"));
                user.setPhotoProfil(resultSet.getString("photo_profil"));
                user.setPassword(resultSet.getString("password"));
                user.setTelephone(resultSet.getInt("telephone"));

                String rolesJson = resultSet.getString("roles");
                List<String> roles = convertJsonToRoles(rolesJson);
                user.setRoles(roles);
            }
        }
        return user;
    }

    public void updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Hash the new password using BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No user found with email: " + email);
            }
        }
    }

    public void storePasswordResetCode(String email, String resetCode) {
        // Store the reset code with a 30-minute expiration
        long expiresAt = System.currentTimeMillis() + 30 * 60 * 1000; // 30 minutes
        resetCodes.put(email, new ResetCodeData(resetCode, expiresAt));
    }

    public boolean verifyResetCode(String email, String resetCode) {
        ResetCodeData data = resetCodes.get(email);
        if (data == null) {
            return false;
        }
        // Check if code matches and is not expired
        boolean isValid = data.code.equals(resetCode) && data.expiresAt > System.currentTimeMillis();
        if (isValid) {
            // Clear the code after successful verification
            resetCodes.remove(email);
        }
        return isValid;
    }
}