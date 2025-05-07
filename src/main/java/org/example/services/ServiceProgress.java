package org.example.services;

import org.example.entity.Progress;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProgress {

    private Connection connection;

    public ServiceProgress() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Progress progress) throws SQLException {
        String query = "INSERT INTO Progress (user_id, cours_id, fichier_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, progress.getUserId());
            stmt.setInt(2, progress.getCoursId());
            stmt.setInt(3, progress.getFichierId());
            stmt.executeUpdate();
        }
    }

    public List<Progress> getProgressByUserAndCours(int userId, int coursId) throws SQLException {
        List<Progress> progressList = new ArrayList<>();
        String query = "SELECT * FROM Progress WHERE user_id = ? AND cours_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, coursId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Progress progress = new Progress();
                progress.setId(rs.getInt("id"));
                progress.setUserId(rs.getInt("user_id"));
                progress.setCoursId(rs.getInt("cours_id"));
                progress.setFichierId(rs.getInt("fichier_id"));
                progress.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                progressList.add(progress);
            }
        }
        return progressList;
    }

    public void deleteByFichierId(int fichierId) throws SQLException {
        String query = "DELETE FROM Progress WHERE fichier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, fichierId);
            stmt.executeUpdate();
        }
    }

    public void deleteByUser(int userId) throws SQLException {
        String query = "DELETE FROM Progress WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}