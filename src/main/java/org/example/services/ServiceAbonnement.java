package org.example.services;

import org.example.entity.Abonnement;
import org.example.entity.Duration;
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
        ps.setString(2, abonnement.getPrix());
        ps.setString(3, abonnement.getDescription());
        ps.setString(4, abonnement.getDuration().toString());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Abonnement abonnement) throws SQLException {
        String query = "UPDATE abonnement SET titre_abonnement = ?, prix = ?, description = ?,  duration = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);

        ps.setString(1, abonnement.getTitreAbonnement());
        ps.setString(2, abonnement.getPrix());
        ps.setString(3, abonnement.getDescription());
        ps.setString(4, abonnement.getDuration().toString());
        ps.setInt(5, abonnement.getId());

        ps.executeUpdate();
    }



    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM abonnement WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Abonnement> afficher() throws SQLException {
        List<Abonnement> list = new ArrayList<>();
        String query = "SELECT * FROM abonnement";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Abonnement a = new Abonnement();
            a.setId(rs.getInt("id"));
            a.setTitreAbonnement(rs.getString("titre_abonnement"));
            a.setPrix(rs.getString("prix"));
            a.setDescription(rs.getString("description"));

            a.setDuration(Duration.valueOf(rs.getString("duration")));


            list.add(a);
        }
        return list;
    }
}
