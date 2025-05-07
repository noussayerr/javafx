package org.example.dao;

import org.example.entity.Comment;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public int ajouter(Comment comment) {
        String query = "INSERT INTO comment (event_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getEventId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());

            System.out.println("📤 Insertion en base : event_id=" + comment.getEventId() +
                    ", user_id=" + comment.getUserId() +
                    ", content=" + comment.getContent());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // retourne l'ID généré
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL dans CommentDAO.ajouter() : " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public List<Comment> getCommentairesByEvent(int eventId) {
        List<Comment> commentaires = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.content, u.nom " +
                "FROM comment c JOIN user u ON c.user_id = u.id " +
                "WHERE c.event_id = ?";

        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Comment c = new Comment(
                        eventId,
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getString("nom")
                );
                c.setId(rs.getInt("id")); // 🔥 obligatoire pour les likes/dislikes

                commentaires.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaires;
    }

    public void supprimer(Comment comment) {
        String sql = "DELETE FROM comment WHERE event_id = ? AND user_id = ? AND content = ?";
        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, comment.getEventId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());
            stmt.executeUpdate();

            System.out.println("✅ Commentaire supprimé !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression commentaire : " + e.getMessage());
        }
    }
}
