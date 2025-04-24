package org.example.services;

import org.example.entity.Abonnement;
import org.example.entity.Duration;
import org.example.entity.Promotion;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAbonnement implements IService<Abonnement> {
    private Connection connection;
    public ServiceAbonnement() {
        connection= MyDatabase.getInstance().getConnection();
    }
    @Override
    public void ajouter(Abonnement abonnement) throws SQLException {
        String query = "INSERT INTO abonnement (titre_abonnement, prix, description, duration) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, abonnement.getTitreAbonnement());
        ps.setInt(2, abonnement.getPrix());
        ps.setString(3, abonnement.getDescription());
        ps.setString(4, abonnement.getDuration().toString());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Abonnement abonnement) throws SQLException {
        String query = "UPDATE abonnement SET titre_abonnement = ?, prix = ?, description = ?,  duration = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);

        ps.setString(1, abonnement.getTitreAbonnement());
        ps.setInt(2, abonnement.getPrix());
        ps.setString(3, abonnement.getDescription());
        ps.setString(4, abonnement.getDuration().toString());
        ps.setInt(5, abonnement.getId());

        ps.executeUpdate();
    }



    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM abonnement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            // On relance une exception spécifique pour que le contrôleur la capture
            throw new SQLException("L'abonnement ne peut pas être supprimé car il est lié à un ou plusieurs apprenants.", e);
        }
    }


    @Override
    public List<Abonnement> afficher() throws SQLException {
        List<Abonnement> list = new ArrayList<>();
        String query = "SELECT a.*, p.id as promo_id, p.titre as promo_titre, p.description as promo_description, " +
                "p.reduction, p.date_debut, p.date_fin " +
                "FROM abonnement a LEFT JOIN promotion p ON a.promotion_id = p.id";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Abonnement a = new Abonnement();
            a.setId(rs.getInt("id"));
            a.setTitreAbonnement(rs.getString("titre_abonnement"));
            a.setPrix(rs.getInt("prix"));
            a.setDescription(rs.getString("description"));
            a.setDuration(Duration.valueOf(rs.getString("duration").toUpperCase()));

            // Vérifie si une promotion est liée
            int promoId = rs.getInt("promo_id");

            list.add(a);
        }
        return list;
    }

}
