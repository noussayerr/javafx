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
}
