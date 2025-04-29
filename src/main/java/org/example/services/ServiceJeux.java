package org.example.services;

import org.example.entity.Jeux;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceJeux implements IService<Jeux> {
    private Connection connection;

    public ServiceJeux() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Jeux jeux) throws SQLException {
        String query = "INSERT INTO jeux (nom, description, type, do_c) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, jeux.getNom());
            ps.setString(2, jeux.getDescription());
            ps.setString(3, jeux.getType());
            ps.setDate(4, Date.valueOf(jeux.getDoC()));
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Jeux jeux) throws SQLException {
        String query = "UPDATE jeux SET nom = ?, description = ?, type = ?, do_c = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, jeux.getNom());
            ps.setString(2, jeux.getDescription());
            ps.setString(3, jeux.getType());
            ps.setDate(4, Date.valueOf(jeux.getDoC()));
            ps.setInt(5, jeux.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM jeux WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Jeux> afficher() throws SQLException {
        List<Jeux> list = new ArrayList<>();
        String query = "SELECT * FROM jeux";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Jeux jeux = new Jeux();
                jeux.setId(rs.getInt("id"));
                jeux.setNom(rs.getString("nom"));
                jeux.setDescription(rs.getString("description"));
                jeux.setType(rs.getString("type"));
                jeux.setDoC(rs.getDate("do_c").toLocalDate());

                list.add(jeux);
            }
        }
        return list;
    }
    public boolean jeuExiste(String nom) {
        String sql = "SELECT COUNT(*) FROM jeux WHERE nom = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public Jeux getByNom(String nom) throws SQLException {
        String sql = "SELECT * FROM jeux WHERE nom = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, nom);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Jeux jeu = new Jeux();
            jeu.setId(rs.getInt("id"));
            jeu.setNom(rs.getString("nom"));
            jeu.setDescription(rs.getString("description"));
            jeu.setType(rs.getString("type"));
            jeu.setDoC(rs.getDate("do_c").toLocalDate()); // If you use LocalDate
            return jeu;
        }
        return null; // No game found
    }
    public List<String> getDistinctGameTypes() throws SQLException {
        List<String> types = new ArrayList<>();
        String query = "SELECT DISTINCT type FROM jeux";

        try (
             PreparedStatement pst = connection.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        }
        return types;
    }
}
