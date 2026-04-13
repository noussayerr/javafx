package org.example.services;

import org.example.entity.User;
import org.example.utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection connection;

    public UserService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String query = "INSERT INTO user (email, roles, password, name, role_type, created_at, is_verified, verification_code, verification_code_expires_at, water_intake) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getRoles());
            // Hash password before saving
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            pst.setString(3, hashedPassword);
            pst.setString(4, user.getName());
            pst.setString(5, user.getRoleType());
            pst.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            pst.setBoolean(7, user.isVerified());
            pst.setString(8, user.getVerificationCode());
            pst.setTimestamp(9, user.getVerificationCodeExpiresAt() != null ? Timestamp.valueOf(user.getVerificationCodeExpiresAt()) : null);
            pst.setString(10, user.getWaterIntake());

            pst.executeUpdate();
            
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String query = "UPDATE user SET email = ?, roles = ?, name = ?, role_type = ?, is_verified = ?, " +
                "verification_code = ?, verification_code_expires_at = ?, water_intake = ?, password_reset_code = ?, password_reset_code_expires_at = ? " +
                "WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getRoles());
            pst.setString(3, user.getName());
            pst.setString(4, user.getRoleType());
            pst.setBoolean(5, user.isVerified());
            pst.setString(6, user.getVerificationCode());
            pst.setTimestamp(7, user.getVerificationCodeExpiresAt() != null ? Timestamp.valueOf(user.getVerificationCodeExpiresAt()) : null);
            pst.setString(8, user.getWaterIntake());
            pst.setString(9, user.getPasswordResetCode());
            pst.setTimestamp(10, user.getPasswordResetCodeExpiresAt() != null ? Timestamp.valueOf(user.getPasswordResetCodeExpiresAt()) : null);
            pst.setInt(11, user.getId());

            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }

    @Override
    public User getById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public User getByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean authenticate(String email, String password) throws SQLException {
        User user = getByEmail(email);
        if (user != null && user.getPassword() != null) {
            String hashedPassword = user.getPassword();
            
            // Correction pour la compatibilité avec Symfony/PHP ($2y$ -> $2a$)
            if (hashedPassword.startsWith("$2y$")) {
                hashedPassword = "$2a$" + hashedPassword.substring(4);
            }
            
            try {
                return BCrypt.checkpw(password, hashedPassword);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid password format in database: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setRoles(rs.getString("roles"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setRoleType(rs.getString("role_type"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setVerified(rs.getBoolean("is_verified"));
        user.setVerificationCode(rs.getString("verification_code"));
        if (rs.getTimestamp("verification_code_expires_at") != null) {
            user.setVerificationCodeExpiresAt(rs.getTimestamp("verification_code_expires_at").toLocalDateTime());
        }
        user.setPasswordResetCode(rs.getString("password_reset_code"));
        if (rs.getTimestamp("password_reset_code_expires_at") != null) {
            user.setPasswordResetCodeExpiresAt(rs.getTimestamp("password_reset_code_expires_at").toLocalDateTime());
        }
        user.setWaterIntake(rs.getString("water_intake"));
        return user;
    }
}
