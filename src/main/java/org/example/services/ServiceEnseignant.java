package org.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Enseignant;
import org.example.utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEnseignant implements IService<Enseignant> {
    private Connection connection;

    public ServiceEnseignant() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Enseignant enseignant) throws SQLException {
        String rolesJson = convertRolesToJson(enseignant.getRoles());

        String userQuery = "INSERT INTO user (id, email, password, nom, prenom, roles, dateNaissance, etat, telephone, is_verified, verification_token, photo_profil, interactions_count, sessions_count, last_activity, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String enseignantQuery = "INSERT INTO enseignant (id, specialite, experience) VALUES (?, ?, ?)";

        try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
             PreparedStatement enseignantStatement = connection.prepareStatement(enseignantQuery)) {

            connection.setAutoCommit(false);

            // Insertion dans la table User
            userStatement.setInt(1, enseignant.getId());
            userStatement.setString(2, enseignant.getEmail());

            String hashedPassword = BCrypt.hashpw(enseignant.getPassword(), BCrypt.gensalt());
            hashedPassword = "$2y$" + hashedPassword.substring(4);


            userStatement.setString(3, hashedPassword);
            userStatement.setString(4, enseignant.getNom());
            userStatement.setString(5, enseignant.getPrenom());
            userStatement.setString(6, rolesJson);
            userStatement.setString(7, enseignant.getDateNaissance());
            userStatement.setString(8, "inactif");
            userStatement.setInt(9, enseignant.getTelephone());
            userStatement.setBoolean(10, true);
            userStatement.setString(11, enseignant.getVerificationToken());
            userStatement.setString(12, enseignant.getPhotoProfil());
            userStatement.setInt(13, enseignant.getInteractionsCount());
            userStatement.setString(16, "enseignant");

            if (enseignant.getSessionsCount() != null) {
                userStatement.setInt(14, enseignant.getSessionsCount());
            } else {
                userStatement.setNull(14, Types.INTEGER);
            }
            if (enseignant.getLastActivity() != null) {
                userStatement.setTimestamp(15, Timestamp.valueOf(enseignant.getLastActivity()));
            } else {
                userStatement.setNull(15, Types.TIMESTAMP);
            }
            userStatement.executeUpdate();

            // Insertion dans la table Enseignant
            enseignantStatement.setInt(1, enseignant.getId());
            enseignantStatement.setString(2, enseignant.getSpecialite());
            enseignantStatement.setString(3, enseignant.getExperience());
            enseignantStatement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
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
            return "[\"ROLE_USER\"]";
        }
    }

    @Override
    public void modifier(Enseignant enseignant) throws SQLException {
        String userQuery = "UPDATE user SET email = ?, password = ?, nom = ?, prenom = ?, dateNaissance = ?, etat = ?, telephone = ?, is_verified = ?, verification_token = ?, photo_profil = ?, interactions_count = ?, sessions_count = ?, last_activity = ? WHERE id = ?";
        String enseignantQuery = "UPDATE enseignant SET specialite = ?, experience = ? WHERE id = ?";

        try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
             PreparedStatement enseignantStatement = connection.prepareStatement(enseignantQuery)) {

            connection.setAutoCommit(false);

            // Mise à jour dans la table User
            userStatement.setString(1, enseignant.getEmail());
            userStatement.setString(2, enseignant.getPassword());
            userStatement.setString(3, enseignant.getNom());
            userStatement.setString(4, enseignant.getPrenom());
            userStatement.setString(5, enseignant.getDateNaissance());
            userStatement.setString(6, enseignant.getEtat());
            userStatement.setInt(7, enseignant.getTelephone());
            userStatement.setBoolean(8, enseignant.isVerified());
            userStatement.setString(9, enseignant.getVerificationToken());
            userStatement.setString(10, enseignant.getPhotoProfil());
            userStatement.setInt(11, enseignant.getInteractionsCount());

            if (enseignant.getSessionsCount() != null) {
                userStatement.setInt(12, enseignant.getSessionsCount());
            } else {
                userStatement.setNull(12, Types.INTEGER);
            }
            if (enseignant.getLastActivity() != null) {
                userStatement.setTimestamp(13, Timestamp.valueOf(enseignant.getLastActivity()));
            } else {
                userStatement.setNull(13, Types.TIMESTAMP);
            }
            userStatement.setInt(14, enseignant.getId());
            userStatement.executeUpdate();

            // Mise à jour dans la table Enseignant
            enseignantStatement.setString(1, enseignant.getSpecialite());
            enseignantStatement.setString(2, enseignant.getExperience());
            enseignantStatement.setInt(3, enseignant.getId());
            enseignantStatement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String deleteEnseignantQuery = "DELETE FROM enseignant WHERE id = ?";
        String deleteUserQuery = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement deleteEnseignantStatement = connection.prepareStatement(deleteEnseignantQuery);
             PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery)) {

            connection.setAutoCommit(false);

            deleteEnseignantStatement.setInt(1, id);
            deleteEnseignantStatement.executeUpdate();

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
    public List<Enseignant> afficher() throws SQLException {
        List<Enseignant> enseignants = new ArrayList<>();
        String query = "SELECT u.*, e.specialite, e.experience FROM user u JOIN enseignant e ON u.id = e.id";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                enseignants.add(mapResultSetToEnseignant(resultSet));
            }
        }

        return enseignants;
    }

    private Enseignant mapResultSetToEnseignant(ResultSet resultSet) throws SQLException {
        Enseignant enseignant = new Enseignant();
        enseignant.setId(resultSet.getInt("id"));
        enseignant.setEmail(resultSet.getString("email"));
        enseignant.setPassword(resultSet.getString("password"));
        enseignant.setNom(resultSet.getString("nom"));
        enseignant.setPrenom(resultSet.getString("prenom"));

        String[] rolesArray = resultSet.getString("roles").split(",");
        for (String role : rolesArray) {
            if (!role.isEmpty()) {
                enseignant.getRoles().add(role);
            }
        }

        enseignant.setDateNaissance(resultSet.getString("dateNaissance"));
        enseignant.setEtat(resultSet.getString("etat"));
        enseignant.setTelephone(resultSet.getInt("telephone"));
        enseignant.setVerified(resultSet.getBoolean("is_verified"));
        enseignant.setVerificationToken(resultSet.getString("verification_token"));
        enseignant.setPhotoProfil(resultSet.getString("photo_profil"));
        enseignant.setInteractionsCount(resultSet.getInt("interactions_count"));
        enseignant.setSessionsCount(resultSet.getInt("sessions_count"));

        Timestamp lastActivity = resultSet.getTimestamp("last_activity");
        if (lastActivity != null) {
            enseignant.setLastActivity(lastActivity.toLocalDateTime());
        }

        // Propriétés spécifiques à Enseignant
        enseignant.setSpecialite(resultSet.getString("specialite"));
        enseignant.setExperience(resultSet.getString("experience"));

        return enseignant;
    }

    // Méthodes supplémentaires spécifiques à Enseignant
    public List<Enseignant> rechercherParSpecialite(String specialite) throws SQLException {
        List<Enseignant> enseignants = new ArrayList<>();
        String query = "SELECT u.*, e.specialite, e.experience FROM user u JOIN enseignant e ON u.id = e.id WHERE e.specialite = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, specialite);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                enseignants.add(mapResultSetToEnseignant(resultSet));
            }
        }

        return enseignants;
    }
}