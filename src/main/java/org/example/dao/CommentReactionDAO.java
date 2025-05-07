package org.example.dao;

import org.example.utils.MyDatabase;

import java.sql.*;

public class CommentReactionDAO {

    private final Connection conn = MyDatabase.getInstance().getConnection(); // ✔️ méthode réelle


    // 🔍 Vérifie si l'utilisateur a déjà réagi à ce commentaire
    public boolean aDéjàRéagi(int userId, int commentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_reaction WHERE user_id=? AND comment_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // 🔍 Récupère la réaction de l'utilisateur (like/dislike/null)
    public String getReactionType(int userId, int commentId) throws SQLException {
        String sql = "SELECT reaction_type FROM comment_reaction WHERE user_id=? AND comment_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("reaction_type") : null;
        }
    }

    // 💾 Enregistre ou remplace la réaction
    public void enregistrerReaction(int userId, int commentId, String type) throws SQLException {
        String sql = "REPLACE INTO comment_reaction (user_id, comment_id, reaction_type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            ps.setString(3, type);
            ps.executeUpdate();
        }
    }
    public void supprimerReaction(int userId, int commentId) throws SQLException {
        String sql = "DELETE FROM comment_reaction WHERE user_id = ? AND comment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            ps.executeUpdate();
        }
    }


    // 🔢 Nombre total de likes ou dislikes pour un commentaire
    public int countReactions(int commentId, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_reaction WHERE comment_id=? AND reaction_type=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
