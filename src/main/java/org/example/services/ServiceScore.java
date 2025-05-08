package org.example.services;

import org.example.entity.Score;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceScore {
    private Connection conn;
    public ServiceScore() {
        conn = MyDatabase.getInstance().getConnection();
    }
    public List<Score> getScoresByUserId(int userId) throws SQLException {
        String query = "SELECT s.id, s.high_score, s.user_id, s.jeu_id, u.nom, j.nom AS jeu_nom " +
                "FROM score s " +
                "JOIN user u ON s.user_id = u.id " +
                "JOIN jeux j ON s.jeu_id = j.id " +
                "WHERE s.user_id = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        List<Score> scores = new ArrayList<>();
        while (rs.next()) {
            Score score = new Score(
                    rs.getInt("id"),
                    rs.getInt("high_score"),
                    new org.example.entity.User(rs.getInt("user_id"), rs.getString("nom")),
                    new org.example.entity.Jeux(rs.getInt("id"), rs.getString("jeu_nom"))
            );
            scores.add(score);
        }
        return scores;
    }

    public List<Score> getScoresByGameId(int gameId) throws SQLException {
        String query = "SELECT s.id, s.high_score, s.user_id, s.jeu_id, u.nom, j.nom AS jeu_nom " +
                "FROM score s " +
                "JOIN user u ON s.user_id = u.id " +
                "JOIN jeux j ON s.jeu_id = j.id " +
                "WHERE s.jeu_id = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, gameId);
        ResultSet rs = ps.executeQuery();
        List<Score> scores = new ArrayList<>();
        while (rs.next()) {
            Score score = new Score(
                    rs.getInt("id"),
                    rs.getInt("high_score"),
                    new org.example.entity.User(rs.getInt("user_id"), rs.getString("nom")),
                    new org.example.entity.Jeux(rs.getInt("id"), rs.getString("jeu_nom"))
            );
            scores.add(score);
        }
        return scores;
    }
    public void ajouter(Score score) throws SQLException {

        String sql = "INSERT INTO score (high_score, user_id, jeu_id) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, score.getHighScore());
        stmt.setInt(2, score.getUser().getId());
        stmt.setInt(3, score.getJeux().getId());
        stmt.executeUpdate();
    }
    public Score findByUserAndGame(int userId, int jeuId) throws SQLException {
        String query = "SELECT * FROM score WHERE user_id = ? AND jeu_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, userId);
        pst.setInt(2, jeuId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            Score s = new Score();
            s.setId(rs.getInt("id"));
            s.setHighScore(rs.getInt("high_score"));
            // You can also set User and Jeux objects if needed
            return s;
        }
        return null;
    }

    public void modifier(Score s) throws SQLException {
        String query = "UPDATE score SET high_score = ? WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, s.getHighScore());
        pst.setInt(2, s.getId());
        pst.executeUpdate();
    }

}
