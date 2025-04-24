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
}
