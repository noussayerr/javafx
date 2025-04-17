package org.example.services;

import org.example.entity.Promotion;
import org.example.utils.MyDatabase;

import java.sql.SQLException;
import java.util.List;
import java.sql.*;

import java.util.ArrayList;

public class ServicePromotion implements IService<Promotion> {

    private Connection connection;
    public ServicePromotion() {
        connection= MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Promotion promotion) throws SQLException {
        String query = "INSERT INTO promotion (titre, description, reduction, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);

        ps.setString(1, promotion.getTitre());
        ps.setString(2, promotion.getDescription());
        ps.setInt(3, promotion.getReduction());
        ps.setDate(4, Date.valueOf(promotion.getDateDebut()));
        ps.setDate(5, Date.valueOf(promotion.getDateFin()));

        ps.executeUpdate();
    }

    public void modifier(Promotion promotion) throws SQLException {
        String query = "UPDATE promotion SET titre = ?, description = ?, reduction = ?, date_debut = ?, date_fin = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);

        ps.setString(1, promotion.getTitre());
        ps.setString(2, promotion.getDescription());
        ps.setInt(3, promotion.getReduction());
        ps.setDate(4, Date.valueOf(promotion.getDateDebut()));
        ps.setDate(5, Date.valueOf(promotion.getDateFin()));
        ps.setInt(6, promotion.getId());

        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM promotion WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public Promotion getById(int id) throws SQLException {
        String query = "SELECT * FROM promotion WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Promotion p = new Promotion();
            p.setId(rs.getInt("id"));
            p.setTitre(rs.getString("titre"));
            p.setDescription(rs.getString("description"));
            p.setReduction(rs.getInt("reduction"));
            p.setDateDebut(rs.getDate("date_debut").toLocalDate());
            p.setDateFin(rs.getDate("date_fin").toLocalDate());
            return p;
        }
        return null; // ou tu peux lancer une exception si non trouvé
    }

    public List<Promotion> afficher() throws SQLException {
        List<Promotion> list = new ArrayList<>();
        String query = "SELECT * FROM promotion";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Promotion p = new Promotion();
            p.setId(rs.getInt("id"));
            p.setTitre(rs.getString("titre"));
            p.setDescription(rs.getString("description"));
            p.setReduction(rs.getInt("reduction"));
            p.setDateDebut(rs.getDate("date_debut").toLocalDate());
            p.setDateFin(rs.getDate("date_fin").toLocalDate());
            list.add(p);
        }
        return list;
    }
    public void ajouterPromo(Promotion promotion,int idAbonnement) throws SQLException{
        try {

            String insertPromoQuery = "INSERT INTO promotion (titre, description, reduction, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(insertPromoQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, promotion.getTitre());
            ps.setString(2, promotion.getDescription());
            ps.setInt(3, promotion.getReduction());
            ps.setDate(4, Date.valueOf(promotion.getDateDebut()));
            ps.setDate(5, Date.valueOf(promotion.getDateFin()));
            ps.executeUpdate();


            ResultSet rs = ps.getGeneratedKeys();
            int promoId = -1;
            if (rs.next()) {
                promoId = rs.getInt(1);
            } else {
                throw new SQLException("Erreur lors de l'insertion de la promotion : aucun ID généré.");
            }


            String updateAbonnementQuery = "UPDATE abonnement SET promotion_id = ? WHERE id = ?";
            PreparedStatement ps1 = connection.prepareStatement(updateAbonnementQuery);
            ps1.setInt(1, promoId);
            ps1.setInt(2, idAbonnement);
            ps1.executeUpdate();



        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }


    }
}
