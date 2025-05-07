package org.example.services;
import org.example.entity.Abonnement;
import org.example.entity.Apprenant;
import org.example.entity.Transaction;
import org.example.utils.MyDatabase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceTransaction implements IService<Transaction> {

    private Connection cnx;

    public ServiceTransaction() {cnx = MyDatabase.getInstance().getConnection();}
    @Override
    public void ajouter(Transaction transaction) throws SQLException {
        String req = "INSERT INTO transaction (amount, transaction_id, transaction_date, apprenant_id, status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setFloat(1, transaction.getAmount());
        pst.setString(2, transaction.getTransactionId());
        pst.setTimestamp(3, Timestamp.valueOf(transaction.getTransactionDate()));
        pst.setInt(4, transaction.getApprenant().getId());
        pst.setString(5, transaction.getStatus());
        pst.executeUpdate();
    }

    @Override
    public void modifier(Transaction transaction) throws SQLException {
        String req = "UPDATE transaction SET amount = ?, transaction_id = ?, transaction_date = ?, apprenant_id = ?, status = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setFloat(1, transaction.getAmount());
        pst.setString(2, transaction.getTransactionId());
        pst.setTimestamp(3, Timestamp.valueOf(transaction.getTransactionDate()));
        pst.setInt(4, transaction.getApprenant().getId());
        pst.setString(5, transaction.getStatus());
        pst.setInt(6, transaction.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM transaction WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    @Override
    public List<Transaction> afficher() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String req = "SELECT * FROM transaction";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            int id = rs.getInt("id");
            float amount = rs.getFloat("amount");
            String transactionId = rs.getString("transaction_id");
            LocalDateTime transactionDate = rs.getTimestamp("transaction_date").toLocalDateTime();
            int apprenantId = rs.getInt("apprenant_id");
            String status = rs.getString("status");

            // Suppose que vous avez une méthode pour récupérer un Apprenant par ID
            Apprenant apprenant = getApprenantById(apprenantId);

            Transaction t = new Transaction(id, amount, transactionId, transactionDate, apprenant, status);
            list.add(t);
        }

        return list;
    }
    public Apprenant getApprenantById(int id) throws SQLException {
        String req = "SELECT * FROM apprenant a JOIN user u ON a.id = u.id WHERE a.id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String email = rs.getString("email");
            String password = rs.getString("password");
            String nom = rs.getString("nom");
            String prenom = rs.getString("prenom");
            String niveau = rs.getString("niveau");

            return new Apprenant(id, email, password, nom, prenom, niveau);
        }
        return null;
    }
    public Apprenant getApprenantById2(int id) throws SQLException {
        String req = "SELECT * FROM apprenant a JOIN user u ON a.id = u.id WHERE a.id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String email = rs.getString("email");
            String password = rs.getString("password");
            String nom = rs.getString("nom");
            String prenom = rs.getString("prenom");
            String dateNaissance = rs.getString("dateNaissance");
            String etat = rs.getString("etat");
            int telephone = rs.getInt("telephone");
            boolean isVerified = rs.getBoolean("is_verified");
            String verificationToken = rs.getString("verification_token");
            String photoProfil = rs.getString("photo_profil");
            int interactionsCount = rs.getInt("interactions_count");

            Integer sessionsCount = rs.getObject("sessions_count") != null ? rs.getInt("sessions_count") : null;

            Timestamp lastActivityTs = rs.getTimestamp("last_activity");
            LocalDateTime lastActivity = lastActivityTs != null ? lastActivityTs.toLocalDateTime() : null;

            String niveau = rs.getString("niveau");

            // Build the Apprenant object
            Apprenant apprenant = new Apprenant(id, email, password, nom, prenom, niveau);
            apprenant.setDateNaissance(dateNaissance);
            apprenant.setEtat(etat);
            apprenant.setTelephone(telephone);
            apprenant.setVerified(isVerified);
            apprenant.setVerificationToken(verificationToken);
            apprenant.setPhotoProfil(photoProfil);
            apprenant.setInteractionsCount(interactionsCount);
            apprenant.setSessionsCount(sessionsCount);
            apprenant.setLastActivity(lastActivity);

            return apprenant;
        }
        return null;
    }

    public Map<String, Integer> getSubscribersPerDay() throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        String query = "SELECT DATE(transaction_date) AS date, COUNT(*) AS total FROM transaction GROUP BY DATE(transaction_date) ORDER BY date";

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String date = rs.getString("date"); // Or use rs.getDate("date").toString()
                int count = rs.getInt("total");
                result.put(date, count);
            }
        }

        return result;
    }
    public Abonnement getAbonnementLePlusVendu() throws SQLException {
        String req = """
        SELECT a.abonement_id, COUNT(*) AS total
        FROM transaction t
        JOIN apprenant a ON t.apprenant_id = a.id
        GROUP BY a.abonement_id
        ORDER BY total DESC
        LIMIT 1
    """;

        PreparedStatement pst = cnx.prepareStatement(req);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            int abonnementId = rs.getInt("abonement_id");

            // Charger l’abonnement depuis son ID (utilise le service d’abonnement)
            ServiceAbonnement serviceAbonnement = new ServiceAbonnement();
            return serviceAbonnement.getById(abonnementId);
        }

        return null; // Aucun résultat
    }

}
