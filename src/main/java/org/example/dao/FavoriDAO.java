package org.example.dao;

import org.example.entity.Favori;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FavoriDAO {
    private Connection connection;
    private static FavoriDAO instance;

    private FavoriDAO() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public static FavoriDAO getInstance() {
        if (instance == null) {
            instance = new FavoriDAO();
        } else {
            try {
                if (instance.connection == null || instance.connection.isClosed()) {
                    instance.connection = MyDatabase.getInstance().getConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }


    public boolean ajouterFavori(int userId, int eventId) {
        String sql = "INSERT INTO favoris (user_id, event_id, date_ajout) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout favori : " + e.getMessage());
            return false;
        }
    }

    // ✅ Supprimer un favori
    public boolean supprimerFavori(int userId, int eventId) {
        String sql = "DELETE FROM favoris WHERE user_id = ? AND event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression favori : " + e.getMessage());
            return false;
        }
    }

    // ✅ Vérifier si un événement est déjà en favori
    public boolean estFavori(int userId, int eventId) {
        String sql = "SELECT id FROM favoris WHERE user_id = ? AND event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // s’il y a une ligne => déjà en favori
        } catch (SQLException e) {
            System.out.println("❌ Erreur vérification favori : " + e.getMessage());
            return false;
        }
    }

    // ✅ Obtenir tous les favoris d’un utilisateur (objets Favori)
    public List<Favori> getFavorisByUser(int userId) {
        List<Favori> favoris = new ArrayList<>();
        String sql = "SELECT * FROM favoris WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Favori f = new Favori();
                f.setId(rs.getInt("id"));
                f.setUserId(rs.getInt("user_id"));
                f.setEventId(rs.getInt("event_id"));
                f.setDateAjout(rs.getTimestamp("date_ajout").toLocalDateTime());
                favoris.add(f);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération favoris : " + e.getMessage());
        }

        return favoris;
    }

    public List<Integer> getEventIdsFavorisByUser(int userId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT event_id FROM favoris WHERE user_id = ?";

        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("event_id"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération IDs favoris : " + e.getMessage());
        }

        return ids;
    }

}
