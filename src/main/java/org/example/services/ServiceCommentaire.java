package org.example.services;

import org.example.entity.Commentaire;
import org.example.entity.Matiere;
import org.example.entity.User;
import org.example.utils.MyDatabase;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire {
    private final Connection conn = MyDatabase.getInstance().getConnection();

    public List<Commentaire> getCommentairesByMatiere(int matiereId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String query = "SELECT c.*, u.nom, u.prenom FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "WHERE c.matiere_id = ? ORDER BY c.date DESC";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, matiereId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Commentaire commentaire = new Commentaire();
                commentaire.setId(rs.getInt("id"));
                commentaire.setSujet(rs.getString("sujet"));
                commentaire.setContenu(rs.getString("contenu"));
                commentaire.setDate(rs.getTimestamp("date").toLocalDateTime());

                // Set matiere (juste l'ID pour éviter les requêtes circulaires)
                Matiere matiere = new Matiere();
                matiere.setId(matiereId);
                commentaire.setMatiere(matiere);

                // Set user
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                commentaire.setUser(user);

                commentaires.add(commentaire);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commentaires: " + e.getMessage());
        }
        return commentaires;
    }

    public void ajouter(Commentaire commentaire) {
        String query = "INSERT INTO commentaire (sujet, contenu, date, matiere_id, user_id) VALUES (?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, commentaire.getSujet());
                pst.setString(2, commentaire.getContenu());
                pst.setTimestamp(3, Timestamp.valueOf(commentaire.getDate()));
                pst.setInt(4, commentaire.getMatiere().getId());
                pst.setInt(5, commentaire.getUser().getId());

                pst.executeUpdate();

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        commentaire.setId(generatedKeys.getInt(1));
                    }
                }

                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du commentaire: " + e.getMessage());
            throw new RuntimeException("Échec de l'ajout du commentaire", e);
        } finally {
            try {
                conn.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de auto-commit: " + e.getMessage());
            }
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM commentaire WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du commentaire: " + e.getMessage());
            throw new RuntimeException("Échec de la suppression du commentaire", e);
        }
    }

    public void modifier(Commentaire commentaire) {
        String query = "UPDATE commentaire SET sujet = ?, contenu = ? WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, commentaire.getSujet());
            pst.setString(2, commentaire.getContenu());
            pst.setInt(3, commentaire.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du commentaire: " + e.getMessage());
            throw new RuntimeException("Échec de la modification du commentaire", e);
        }
    }
}