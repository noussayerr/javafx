package org.example.services;

import org.example.entity.Cours;
import org.example.entity.Fichier;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFichier implements IService<Fichier> {

    private Connection connection;

    public ServiceFichier() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Fichier fichier) throws SQLException {
        String sql = "INSERT INTO fichier (nomF, urlF, type, cours_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fichier.getNomF());
            ps.setString(2, fichier.getUrlF());
            ps.setString(3, fichier.getType());

            if (fichier.getCours() != null) {
                ps.setInt(4, fichier.getCours().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fichier.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Fichier fichier) throws SQLException {
        String sql = "UPDATE fichier SET nomF = ?, urlF = ?, type = ?, cours_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fichier.getNomF());
            ps.setString(2, fichier.getUrlF());
            ps.setString(3, fichier.getType());

            if (fichier.getCours() != null) {
                ps.setInt(4, fichier.getCours().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setInt(5, fichier.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM fichier WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Fichier> afficher() throws SQLException {
        List<Fichier> fichiers = new ArrayList<>();
        String sql = "SELECT * FROM fichier";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                fichiers.add(createFichierFromResultSet(rs));
            }
        }
        return fichiers;
    }

    public List<Fichier> getFichiersByCours(int coursId) throws SQLException {
        List<Fichier> fichiers = new ArrayList<>();
        String sql = "SELECT * FROM fichier WHERE cours_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fichiers.add(createFichierFromResultSet(rs));
                }
            }
        }
        return fichiers;
    }

    public Fichier getById(int id) throws SQLException {
        String sql = "SELECT * FROM fichier WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createFichierFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Fichier createFichierFromResultSet(ResultSet rs) throws SQLException {
        Fichier fichier = new Fichier();
        fichier.setId(rs.getInt("id"));
        fichier.setNomF(rs.getString("nomF"));
        fichier.setUrlF(rs.getString("urlF"));
        fichier.setType(rs.getString("type"));

        // Note: Le cours n'est pas entièrement chargé ici, seulement l'ID
        // Vous pourriez utiliser ServiceCours pour charger l'objet complet si nécessaire
        int coursId = rs.getInt("cours_id");
        if (!rs.wasNull()) {
            Cours cours = new Cours();
            cours.setId(coursId);
            fichier.setCours(cours);
        }

        return fichier;
    }

    public int countFichiersByCours(int coursId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fichier WHERE cours_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}