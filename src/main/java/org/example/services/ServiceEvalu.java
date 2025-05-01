package org.example.services;

import org.example.entity.Evalu;
import org.example.entity.Matiere;
import org.example.entity.User;
import org.example.utils.MyDatabase;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceEvalu {
    private Connection connection = MyDatabase.getInstance().getConnection();

    public List<Evalu> getEvaluationsByMatiere(int matiereId) {
        List<Evalu> evaluations = new ArrayList<>();
        String query = "SELECT e.*, u.nom, u.prenom FROM evalu e " +
                "LEFT JOIN user u ON e.user_id = u.id " +
                "WHERE e.matiere_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, matiereId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Evalu evalu = new Evalu();
                evalu.setId(resultSet.getInt("id"));
                evalu.setNote(resultSet.getInt("note"));

                // Création de l'utilisateur associé
                User user = new User();
                user.setId(resultSet.getInt("user_id"));
                user.setNom(resultSet.getString("nom"));
                user.setPrenom(resultSet.getString("prenom"));
                evalu.setUser(user);

                // Création de la matière associée
                Matiere matiere = new Matiere();
                matiere.setId(matiereId);
                evalu.setMatiere(matiere);

                evaluations.add(evalu);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des évaluations: " + e.getMessage());
        }
        return evaluations;
    }

    public Optional<Evalu> getEvaluationByUserAndMatiere(int userId, int matiereId) {
        String query = "SELECT * FROM evalu WHERE user_id = ? AND matiere_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, matiereId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Evalu evalu = new Evalu();
                evalu.setId(resultSet.getInt("id"));
                evalu.setNote(resultSet.getInt("note"));

                // Création de l'utilisateur associé
                User user = new User();
                user.setId(userId);
                evalu.setUser(user);

                // Création de la matière associée
                Matiere matiere = new Matiere();
                matiere.setId(matiereId);
                evalu.setMatiere(matiere);

                return Optional.of(evalu);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'évaluation: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void ajouter(Evalu evalu) throws SQLException {
        String query = "INSERT INTO evalu (note, matiere_id, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, evalu.getNote());
            statement.setInt(2, evalu.getMatiere().getId());
            statement.setInt(3, evalu.getUser().getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("L'ajout de l'évaluation a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evalu.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("L'ajout de l'évaluation a échoué, aucun ID obtenu.");
                }
            }
        }
    }

    public void modifier(Evalu evalu) throws SQLException {
        String query = "UPDATE evalu SET note = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, evalu.getNote());
            statement.setInt(2, evalu.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La modification de l'évaluation a échoué, aucune ligne affectée.");
            }
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM evalu WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La suppression de l'évaluation a échoué, aucune ligne affectée.");
            }
        }
    }

    public double getAverageRatingForMatiere(int matiereId) {
        String query = "SELECT AVG(note) as average FROM evalu WHERE matiere_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, matiereId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("average");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de la moyenne: " + e.getMessage());
        }
        return 0.0;
    }
}