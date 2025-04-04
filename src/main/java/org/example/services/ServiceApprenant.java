package org.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Apprenant;
import org.example.entity.Abonnement;
import org.example.entity.Event;
import org.example.entity.Message;
import org.example.entity.Reclamation;
import org.example.utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceApprenant implements IService<Apprenant> {
    private Connection connection;

    public ServiceApprenant() {
        connection= MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Apprenant apprenant) throws SQLException {

        String rolesJson = convertRolesToJson(apprenant.getRoles());

        String userQuery = "INSERT INTO user (id, email, password, nom, prenom, roles, dateNaissance, etat, telephone, is_verified, verification_token, photo_profil, interactions_count, sessions_count, last_activity,type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
        String apprenantQuery = "INSERT INTO apprenant (id, niveau) VALUES (?, ?)";

        try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
             PreparedStatement apprenantStatement = connection.prepareStatement(apprenantQuery)) {

            // Commencer une transaction
            connection.setAutoCommit(false);

            // Insertion dans la table User
            userStatement.setInt(1, apprenant.getId());
            userStatement.setString(2, apprenant.getEmail());
            String hashedPassword = BCrypt.hashpw(apprenant.getPassword(), BCrypt.gensalt());
            hashedPassword = "$2y$" + hashedPassword.substring(4);

            userStatement.setString(3, hashedPassword);
            userStatement.setString(4, apprenant.getNom());
            userStatement.setString(5, apprenant.getPrenom());
            userStatement.setString(6, rolesJson); // Utilisation du JSON
            userStatement.setString(7, apprenant.getDateNaissance());
            userStatement.setString(8, "actif");
            userStatement.setInt(9, apprenant.getTelephone());
            userStatement.setBoolean(10, true);
            userStatement.setString(11, apprenant.getVerificationToken());
            userStatement.setString(12, apprenant.getPhotoProfil());
            userStatement.setInt(13, apprenant.getInteractionsCount());
            userStatement.setString(16, "apprenant");

            if (apprenant.getSessionsCount() != null) {
                userStatement.setInt(14, apprenant.getSessionsCount());
            } else {
                userStatement.setNull(14, Types.INTEGER);
            }
            if (apprenant.getLastActivity() != null) {
                userStatement.setTimestamp(15, Timestamp.valueOf(apprenant.getLastActivity()));
            } else {
                userStatement.setNull(15, Types.TIMESTAMP);
            }
            userStatement.executeUpdate();

            // Insertion dans la table Apprenant
            apprenantStatement.setInt(1, apprenant.getId());
            apprenantStatement.setString(2, apprenant.getNiveau());
            apprenantStatement.executeUpdate();

            // Valider la transaction
            connection.commit();
        } catch (SQLException e) {
            // Annuler la transaction en cas d'erreur
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    private String convertRolesToJson(List<String> roles) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(roles);
        } catch (JsonProcessingException e) {
            return "[\"ROLE_USER\"]"; // Valeur par défaut si erreur
        }
    }

    @Override
    public void modifier(Apprenant apprenant) throws SQLException {
        String userQuery = "UPDATE user SET email = ?, password = ?, nom = ?, prenom = ?, dateNaissance = ?, etat = ?, telephone = ?, is_verified = ?, verification_token = ?, photo_profil = ?, interactions_count = ?, sessions_count = ?, last_activity = ? WHERE id = ?";
        String apprenantQuery = "UPDATE apprenant SET niveau = ? WHERE id = ?";

        try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
             PreparedStatement apprenantStatement = connection.prepareStatement(apprenantQuery)) {

            // Commencer une transaction
            connection.setAutoCommit(false);

            // Mise à jour dans la table User (sans toucher aux roles)
            userStatement.setString(1, apprenant.getEmail());
            String hashedPassword = BCrypt.hashpw(apprenant.getPassword(), BCrypt.gensalt());
            hashedPassword = "$2y$" + hashedPassword.substring(4);

            userStatement.setString(2, hashedPassword);
            userStatement.setString(3, apprenant.getNom());
            userStatement.setString(4, apprenant.getPrenom());
            userStatement.setString(5, apprenant.getDateNaissance());
            userStatement.setString(6, apprenant.getEtat());
            userStatement.setInt(7, apprenant.getTelephone());
            userStatement.setBoolean(8, apprenant.isVerified());
            userStatement.setString(9, apprenant.getVerificationToken());
            userStatement.setString(10, apprenant.getPhotoProfil());
            userStatement.setInt(11, apprenant.getInteractionsCount());

            if (apprenant.getSessionsCount() != null) {
                userStatement.setInt(12, apprenant.getSessionsCount());
            } else {
                userStatement.setNull(12, Types.INTEGER);
            }
            if (apprenant.getLastActivity() != null) {
                userStatement.setTimestamp(13, Timestamp.valueOf(apprenant.getLastActivity()));
            } else {
                userStatement.setNull(13, Types.TIMESTAMP);
            }
            userStatement.setInt(14, apprenant.getId());
            userStatement.executeUpdate();

            // Mise à jour dans la table Apprenant
            apprenantStatement.setString(1, apprenant.getNiveau());
            apprenantStatement.setInt(2, apprenant.getId());
            apprenantStatement.executeUpdate();

            // Valider la transaction
            connection.commit();
        } catch (SQLException e) {
            // Annuler la transaction en cas d'erreur
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String deleteApprenantQuery = "DELETE FROM apprenant WHERE id = ?";
        String deleteUserQuery = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement deleteApprenantStatement = connection.prepareStatement(deleteApprenantQuery);
             PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery)) {

            connection.setAutoCommit(false);

            // D'abord supprimer de la table Apprenant
            deleteApprenantStatement.setInt(1, id);
            deleteApprenantStatement.executeUpdate();

            // Puis supprimer de la table User
            deleteUserStatement.setInt(1, id);
            deleteUserStatement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public List<Apprenant> afficher() throws SQLException {
        List<Apprenant> apprenants = new ArrayList<>();
        String query = "SELECT u.*, a.niveau FROM user u JOIN apprenant a ON u.id = a.id";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Apprenant apprenant = new Apprenant();
                apprenant.setId(resultSet.getInt("id"));
                apprenant.setEmail(resultSet.getString("email"));
                apprenant.setPassword(resultSet.getString("password"));
                apprenant.setNom(resultSet.getString("nom"));
                apprenant.setPrenom(resultSet.getString("prenom"));

                // Conversion des rôles depuis une chaîne séparée par des virgules
                String[] rolesArray = resultSet.getString("roles").split(",");
                for (String role : rolesArray) {
                    if (!role.isEmpty()) {
                        apprenant.getRoles().add(role);
                    }
                }

                apprenant.setDateNaissance(resultSet.getString("dateNaissance"));
                apprenant.setEtat(resultSet.getString("etat"));
                apprenant.setTelephone(resultSet.getInt("telephone"));
                apprenant.setVerified(resultSet.getBoolean("is_verified"));
                apprenant.setVerificationToken(resultSet.getString("verification_token"));
                apprenant.setPhotoProfil(resultSet.getString("photo_profil"));
                apprenant.setInteractionsCount(resultSet.getInt("interactions_count"));
                apprenant.setSessionsCount(resultSet.getInt("sessions_count"));

                Timestamp lastActivity = resultSet.getTimestamp("last_activity");
                if (lastActivity != null) {
                    apprenant.setLastActivity(lastActivity.toLocalDateTime());
                }

                // Propriétés spécifiques à Apprenant
                apprenant.setNiveau(resultSet.getString("niveau"));

                apprenants.add(apprenant);
            }
        }

        return apprenants;
    }

    // Méthodes supplémentaires spécifiques à Apprenant

    public List<Apprenant> rechercherParNiveau(String niveau) throws SQLException {
        List<Apprenant> apprenants = new ArrayList<>();
        String query = "SELECT u.*, a.niveau FROM user u JOIN apprenant a ON u.id = a.id WHERE a.niveau = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, niveau);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Apprenant apprenant = mapResultSetToApprenant(resultSet);
                apprenants.add(apprenant);
            }
        }

        return apprenants;
    }

    private Apprenant mapResultSetToApprenant(ResultSet resultSet) throws SQLException {
        Apprenant apprenant = new Apprenant();
        apprenant.setId(resultSet.getInt("id"));
        apprenant.setEmail(resultSet.getString("email"));
        apprenant.setPassword(resultSet.getString("password"));
        apprenant.setNom(resultSet.getString("nom"));
        apprenant.setPrenom(resultSet.getString("prenom"));

        String[] rolesArray = resultSet.getString("roles").split(",");
        for (String role : rolesArray) {
            if (!role.isEmpty()) {
                apprenant.getRoles().add(role);
            }
        }

        apprenant.setDateNaissance(resultSet.getString("dateNaissance"));
        apprenant.setEtat(resultSet.getString("etat"));
        apprenant.setTelephone(resultSet.getInt("telephone"));
        apprenant.setVerified(resultSet.getBoolean("is_verified"));
        apprenant.setVerificationToken(resultSet.getString("verification_token"));
        apprenant.setPhotoProfil(resultSet.getString("photo_profil"));
        apprenant.setInteractionsCount(resultSet.getInt("interactions_count"));
        apprenant.setSessionsCount(resultSet.getInt("sessions_count"));

        Timestamp lastActivity = resultSet.getTimestamp("last_activity");
        if (lastActivity != null) {
            apprenant.setLastActivity(lastActivity.toLocalDateTime());
        }

        apprenant.setNiveau(resultSet.getString("niveau"));

        return apprenant;
    }
}