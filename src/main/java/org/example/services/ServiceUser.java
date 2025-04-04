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

}