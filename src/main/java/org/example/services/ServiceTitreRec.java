package org.example.services;

import org.example.entity.TitreRec;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTitreRec implements IService<TitreRec> {
    private Connection cnx;

    public ServiceTitreRec() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(TitreRec titreRec) throws SQLException {
        String sql = "INSERT INTO rectitres (titreRec) VALUES (?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, titreRec.getTitrerec());
        ps.executeUpdate();
        System.out.println("Titre de réclamation ajouté avec succès !");
    }

    @Override
    public void modifier(TitreRec titreRec) throws SQLException {

    }

    @Override
    public List<TitreRec> afficher() throws SQLException {
        List<TitreRec> titres = new ArrayList<>();
        String sql = "SELECT * FROM rectitres";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            TitreRec tr = new TitreRec();
            tr.setIdtitre(rs.getInt("idtitre"));
            tr.setTitrerec(rs.getString("titrerec"));
            titres.add(tr);
        }
        return titres;
    }

    @Override
    public void supprimer(int id) throws SQLException {

    }
    public void promouvoirTitresFrequents() throws SQLException {
        String[] motsCles = {"problème", "bug", "erreur", "beug", "défaut"};

        StringBuilder condition = new StringBuilder();
        for (int i = 0; i < motsCles.length; i++) {
            if (i > 0) condition.append(" OR ");
            condition.append("LOWER(r.title) COLLATE utf8mb4_unicode_ci LIKE ?");
        }

        String sql = "SELECT r.title, COUNT(*) as freq " +
                "FROM reclamation r " +
                "WHERE r.title COLLATE utf8mb4_unicode_ci NOT IN (SELECT titrerec COLLATE utf8mb4_unicode_ci FROM rectitres) " +
                "AND (" + condition + ") " +
                "GROUP BY r.title " +
                "HAVING freq >= 5";

        PreparedStatement stmt = cnx.prepareStatement(sql);

        // Préparation des mots-clés avec la bonne collation
        for (int i = 0; i < motsCles.length; i++) {
            stmt.setString(i + 1, "%" + motsCles[i].toLowerCase() + "%");
        }

        // Exécution de la requête
        ResultSet rs = stmt.executeQuery();
        List<TitreRec> titresFrequents = new ArrayList<>();
        while (rs.next()) {
            String titre = rs.getString("title");
            titresFrequents.add(new TitreRec(0, titre));
        }

        // Ajouter les titres fréquents dans le service
        for (TitreRec titre : titresFrequents) {
            ajouter(titre);
        }

        // Fermer les ressources manuellement
        rs.close();
        stmt.close();
    }

}
