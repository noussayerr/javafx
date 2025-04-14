package org.example.services;

import org.example.entity.Matiere;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatiere implements IService<Matiere> {

    Connection connection;

    public ServiceMatiere() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Matiere matiere) throws SQLException {
        String sql = "INSERT INTO matiere (nomM, titreM, descM, objM, imgM) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, matiere.getNomM());
        preparedStatement.setString(2, matiere.getTitreM());
        preparedStatement.setString(3, matiere.getDescM());
        preparedStatement.setString(4, matiere.getObjM());
        preparedStatement.setString(5, matiere.getImgM());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    @Override
    public void modifier(Matiere matiere) throws SQLException {
        String sql = "UPDATE matiere SET nomM=?, titreM=?, descM=?, objM=?, imgM=? WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, matiere.getNomM());
        preparedStatement.setString(2, matiere.getTitreM());
        preparedStatement.setString(3, matiere.getDescM());
        preparedStatement.setString(4, matiere.getObjM());
        preparedStatement.setString(5, matiere.getImgM());
        preparedStatement.setInt(6, matiere.getId());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Commencer par supprimer les cours associés à la matière
        String deleteCoursSql = "DELETE FROM cours WHERE matiere_id = ?";
        try (PreparedStatement preparedStatementCours = connection.prepareStatement(deleteCoursSql)) {
            preparedStatementCours.setInt(1, id);
            preparedStatementCours.executeUpdate();
        }

        // Ensuite, supprimer les commentaires associés à la matière
        String deleteCommentaireSql = "DELETE FROM commentaire WHERE matiere_id = ?";
        try (PreparedStatement preparedStatementCommentaire = connection.prepareStatement(deleteCommentaireSql)) {
            preparedStatementCommentaire.setInt(1, id);
            preparedStatementCommentaire.executeUpdate();
        }

        // Supprimer les évaluations associées à la matière
        String deleteEvaluSql = "DELETE FROM evalu WHERE matiere_id = ?";
        try (PreparedStatement preparedStatementEvalu = connection.prepareStatement(deleteEvaluSql)) {
            preparedStatementEvalu.setInt(1, id);
            preparedStatementEvalu.executeUpdate();
        }

        // Enfin, supprimer la matière elle-même
        String sql = "DELETE FROM matiere WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();

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
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String nomM = resultSet.getString("nomM");
            String titreM = resultSet.getString("titreM");
            String descM = resultSet.getString("descM");
            String objM = resultSet.getString("objM");
            String imgM = resultSet.getString("imgM");

            // Utilisation du constructeur avec ID pour éviter id=0
            Matiere matiere = new Matiere(id, nomM, titreM, descM, objM, imgM);
            matieres.add(matiere);
        }

        resultSet.close();
        statement.close();

        return matieres;
    }
}
