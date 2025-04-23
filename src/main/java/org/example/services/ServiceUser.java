package org.example.services;

import org.example.entity.User;
import org.example.utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {
    private Connection connection;

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
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
}