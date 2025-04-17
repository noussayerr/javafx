package org.example.services;

import org.example.entity.Matiere;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatiere implements IService<Matiere> {

    private Connection connection;

    public ServiceMatiere() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Matiere matiere) throws SQLException {
        String sql = "INSERT INTO matiere (nomM, titreM, descM, objM, imgM) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, matiere.getNomM());
            ps.setString(2, matiere.getTitreM());
            ps.setString(3, matiere.getDescM());
            ps.setString(4, matiere.getObjM());
            ps.setString(5, matiere.getImgM());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Matiere matiere) throws SQLException {
        String sql = "UPDATE matiere SET nomM=?, titreM=?, descM=?, objM=?, imgM=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, matiere.getNomM());
            ps.setString(2, matiere.getTitreM());
            ps.setString(3, matiere.getDescM());
            ps.setString(4, matiere.getObjM());
            ps.setString(5, matiere.getImgM());
            ps.setInt(6, matiere.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement psCours = connection.prepareStatement("DELETE FROM cours WHERE matiere_id = ?");
             PreparedStatement psCommentaire = connection.prepareStatement("DELETE FROM commentaire WHERE matiere_id = ?");
             PreparedStatement psEvalu = connection.prepareStatement("DELETE FROM evalu WHERE matiere_id = ?");
             PreparedStatement psMatiere = connection.prepareStatement("DELETE FROM matiere WHERE id = ?")) {

            psCours.setInt(1, id);
            psCours.executeUpdate();

            psCommentaire.setInt(1, id);
            psCommentaire.executeUpdate();

            psEvalu.setInt(1, id);
            psEvalu.executeUpdate();

            psMatiere.setInt(1, id);
            int rowsAffected = psMatiere.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Aucune matière supprimée, ID introuvable : " + id);
            } else {
                System.out.println("Matière supprimée avec succès, ID : " + id);
            }
        }
    }

    @Override
    public List<Matiere> afficher() throws SQLException {
        List<Matiere> matieres = new ArrayList<>();
        String sql = "SELECT * FROM matiere";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Matiere m = new Matiere(
                        rs.getInt("id"),
                        rs.getString("nomM"),
                        rs.getString("titreM"),
                        rs.getString("descM"),
                        rs.getString("objM"),
                        rs.getString("imgM")
                );
                matieres.add(m);
            }
        }
        return matieres;
    }

    public Matiere getById(int id) throws SQLException {
        String sql = "SELECT * FROM matiere WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Matiere(
                        rs.getInt("id"),
                        rs.getString("nomM"),
                        rs.getString("titreM"),
                        rs.getString("descM"),
                        rs.getString("objM"),
                        rs.getString("imgM")
                );
            }
        }
        return null;
    }

    public Matiere getMatiereById(int id) throws SQLException {
        String query = "SELECT * FROM matiere WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Matiere matiere = new Matiere();
                matiere.setId(rs.getInt("id"));
                matiere.setNomM(rs.getString("nomM"));
                matiere.setTitreM(rs.getString("titreM"));
                matiere.setDescM(rs.getString("descM"));
                matiere.setObjM(rs.getString("objM"));
                matiere.setImgM(rs.getString("imgM"));
                return matiere;
            }
        }
        return null;
    }

}
