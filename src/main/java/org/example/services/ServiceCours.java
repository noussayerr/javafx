package org.example.services;

import org.example.entity.Cours;
import org.example.entity.Matiere;
import org.example.entity.User;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCours implements IService<Cours> {

    private Connection connection;
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();

    public ServiceCours() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Cours cours) throws SQLException {
        String sql = "INSERT INTO cours (nomC, objC, dateC, nivC, type, matiere_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getNomC());
            ps.setString(2, cours.getObjC());
            ps.setTimestamp(3, Timestamp.valueOf(cours.getDateC()));
            ps.setString(4, cours.getNivC());
            ps.setString(5, cours.getType());
            ps.setInt(6, cours.getMatiere().getId());
            ps.setInt(7, cours.getUser().getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Cours cours) throws SQLException {
        String sql = "UPDATE cours SET nomC=?, objC=?, dateC=?, nivC=?, type=?, matiere_id=?, user_id=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getNomC());
            ps.setString(2, cours.getObjC());
            ps.setTimestamp(3, Timestamp.valueOf(cours.getDateC()));
            ps.setString(4, cours.getNivC());
            ps.setString(5, cours.getType());
            ps.setInt(6, cours.getMatiere().getId());
            ps.setInt(7, cours.getUser().getId());
            ps.setInt(8, cours.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String deleteFichiersSql = "DELETE FROM fichier WHERE cours_id=?";
        try (PreparedStatement ps = connection.prepareStatement(deleteFichiersSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        String deleteCoursSql = "DELETE FROM cours WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(deleteCoursSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Cours> afficher() throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS user_nom FROM cours c JOIN user u ON c.user_id = u.id";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setNomC(rs.getString("nomC"));
                c.setObjC(rs.getString("objC"));
                c.setDateC(rs.getTimestamp("dateC").toLocalDateTime());
                c.setNivC(rs.getString("nivC"));
                c.setType(rs.getString("type"));

                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setNom(rs.getString("user_nom"));
                c.setUser(user);

                Matiere m = new Matiere();
                m.setId(rs.getInt("matiere_id"));
                c.setMatiere(m);

                coursList.add(c);
            }
        }
        return coursList;
    }

    public List<Cours> getCoursParMatiere(int matiereId) throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE matiere_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, matiereId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Cours cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setNomC(rs.getString("nomC"));
                cours.setObjC(rs.getString("objC"));
                cours.setNivC(rs.getString("nivC"));
                cours.setType(rs.getString("type"));
                coursList.add(cours);
            }
        }
        return coursList;
    }

    public Cours getCoursById(int id) throws SQLException {
        String query = "SELECT * FROM cours WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Cours cours = new Cours();
            cours.setId(rs.getInt("id"));
            cours.setNomC(rs.getString("nom"));
            cours.setObjC(rs.getString("objectif"));
            cours.setDateC(rs.getTimestamp("date").toLocalDateTime());
            cours.setNivC(rs.getString("niveau"));
            cours.setType(rs.getString("type"));

            // 👉 Ne PAS oublier de récupérer la matière associée
            int matiereId = rs.getInt("matiere_id");
            cours.setMatiere(serviceMatiere.getById(matiereId));  // ⚠️ Nécessite une méthode getById dans ServiceMatiere

            return cours;
        }

        return null;
    }

    public List<Cours> afficherParMatiere(int matiereId) throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE matiere_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, matiereId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Cours c = new Cours();
            c.setId(rs.getInt("id"));
            c.setNomC(rs.getString("nomC"));
            c.setObjC(rs.getString("objC"));
            // Ajoute les autres champs si besoin
            coursList.add(c);
        }
        return coursList;
    }


}
