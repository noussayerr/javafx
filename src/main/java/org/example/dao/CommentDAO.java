package org.example.dao;

import org.example.entity.Comment;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private final Connection connection;

    public CommentDAO() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Comment comment) {
        String query = "INSERT INTO comment (event_id, user_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, comment.getEventId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Comment> getCommentairesByEvent(int eventId) {
        List<Comment> list = new ArrayList<>();
        String query = """
    SELECT c.event_id, c.user_id, c.content, u.nom
    FROM comment c
    JOIN user u ON c.user_id = u.id
    WHERE c.event_id = ?
""";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment c = new Comment(
                        rs.getInt("event_id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getString("nom")
                );

                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void supprimer(Comment comment) {
        String sql = "DELETE FROM comment WHERE event_id = ? AND user_id = ? AND content = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
