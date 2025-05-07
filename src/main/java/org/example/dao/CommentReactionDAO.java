package org.example.dao;

import org.example.utils.MyDatabase;

import java.sql.*;

public class CommentReactionDAO {

    public boolean aDéjàRéagi(int userId, int commentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_reaction WHERE user_id=? AND comment_id=?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public String getReactionType(int userId, int commentId) throws SQLException {
        String sql = "SELECT reaction_type FROM comment_reaction WHERE user_id=? AND comment_id=?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("reaction_type") : null;
            }
        }
    }

    public void enregistrerReaction(int userId, int commentId, String type) throws SQLException {
        String sql = """
            INSERT INTO comment_reaction (user_id, comment_id, reaction_type)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE reaction_type = VALUES(reaction_type)
        """;

        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            ps.setString(3, type);
            ps.executeUpdate();
        }
    }

    public void supprimerReaction(int userId, int commentId) throws SQLException {
        String sql = "DELETE FROM comment_reaction WHERE user_id = ? AND comment_id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            ps.executeUpdate();
        }
    }

    public int countReactions(int commentId, String reactionType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_reaction WHERE comment_id = ? AND reaction_type = ?";
        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            stmt.setString(2, reactionType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

}
