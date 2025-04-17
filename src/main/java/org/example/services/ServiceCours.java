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
        if (cours.getMatiere() == null || cours.getMatiere().getId() <= 0) {
            throw new IllegalArgumentException("ID de matière invalide");
        }
        if (cours.getUser() == null || cours.getUser().getId() <= 0) {
            throw new IllegalArgumentException("ID utilisateur invalide");
        }

        String sql = "INSERT INTO cours (nomC, objC, dateC, nivC, type, matiere_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cours.getNomC());
            ps.setString(2, cours.getObjC());
            ps.setTimestamp(3, Timestamp.valueOf(cours.getDateC()));
            ps.setString(4, cours.getNivC());
            ps.setString(5, cours.getType());
            ps.setInt(6, cours.getMatiere().getId());
            ps.setInt(7, cours.getUser().getId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cours.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Cours cours) throws SQLException {
        String sql = "UPDATE cours SET nomC=?, objC=?, dateC=?, nivC=?, type=?, matiere_id=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getNomC());
            ps.setString(2, cours.getObjC());
            ps.setTimestamp(3, Timestamp.valueOf(cours.getDateC()));
            ps.setString(4, cours.getNivC());
            ps.setString(5, cours.getType());
            ps.setInt(6, cours.getMatiere().getId());
            ps.setInt(7, cours.getId());
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
        String sql = "SELECT c.*, u.nom AS user_nom, u.prenom AS user_prenom FROM cours c JOIN user u ON c.user_id = u.id";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                coursList.add(createCoursFromResultSet(rs));
            }
        }
        return coursList;
    }

    public List<Cours> afficherParMatiere(int matiereId) throws SQLException {
        if (matiereId <= 0) {
            throw new IllegalArgumentException("ID de matière invalide");
        }

        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS user_nom, u.prenom AS user_prenom FROM cours c JOIN user u ON c.user_id = u.id WHERE c.matiere_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, matiereId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coursList.add(createCoursFromResultSet(rs));
                }
            }
        }
        return coursList;
    }

    public Cours getCoursById(int id) throws SQLException {
        String query = "SELECT c.*, u.nom AS user_nom, u.prenom AS user_prenom FROM cours c JOIN user u ON c.user_id = u.id WHERE c.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createCoursFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Cours createCoursFromResultSet(ResultSet rs) throws SQLException {
        Cours cours = new Cours();
        cours.setId(rs.getInt("id"));
        cours.setNomC(rs.getString("nomC"));
        cours.setObjC(rs.getString("objC"));
        cours.setDateC(rs.getTimestamp("dateC").toLocalDateTime());
        cours.setNivC(rs.getString("nivC"));
        cours.setType(rs.getString("type"));

        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setNom(rs.getString("user_nom"));
        user.setPrenom(rs.getString("user_prenom"));
        cours.setUser(user);

        Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
        cours.setMatiere(matiere);

        return cours;
    }

    public List<Cours> getCoursParMatiere(int matiereId) throws SQLException {
        if (matiereId <= 0) {
            throw new IllegalArgumentException("ID de matière invalide");
        }

        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS user_nom, u.prenom AS user_prenom FROM cours c JOIN user u ON c.user_id = u.id WHERE c.matiere_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, matiereId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                coursList.add(createCoursFromResultSet(rs));
            }
        }
        return coursList;
    }

}