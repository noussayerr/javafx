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
        // 1. Démarrer une transaction
        connection.setAutoCommit(false);

        try {
            // 2. Récupérer tous les cours de cette matière
            List<Integer> coursIds = new ArrayList<>();
            String selectCoursSql = "SELECT id FROM cours WHERE matiere_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(selectCoursSql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    coursIds.add(rs.getInt("id"));
                }
            }

            // 3. Pour chaque cours, supprimer d'abord les fichiers associés
            String deleteFichiersSql = "DELETE FROM fichier WHERE cours_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteFichiersSql)) {
                for (int coursId : coursIds) {
                    ps.setInt(1, coursId);
                    ps.executeUpdate();
                }
            }

            // 4. Maintenant supprimer les dépendances de la matière
            // a. Supprimer les cours
            String deleteCoursSql = "DELETE FROM cours WHERE matiere_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteCoursSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // b. Supprimer les commentaires
            String deleteCommentaireSql = "DELETE FROM commentaire WHERE matiere_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteCommentaireSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // c. Supprimer les evaluations
            String deleteEvaluSql = "DELETE FROM evalu WHERE matiere_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteEvaluSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // 5. Enfin supprimer la matière elle-même
            String deleteMatiereSql = "DELETE FROM matiere WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteMatiereSql)) {
                ps.setInt(1, id);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Aucune matière supprimée, ID introuvable : " + id);
                }
            }

            // 6. Valider la transaction
            connection.commit();
            System.out.println("Matière et toutes ses dépendances supprimées avec succès, ID : " + id);
        } catch (SQLException e) {
            // 7. En cas d'erreur, annuler la transaction
            connection.rollback();
            throw e;
        } finally {
            // 8. Rétablir le mode auto-commit
            connection.setAutoCommit(true);
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
