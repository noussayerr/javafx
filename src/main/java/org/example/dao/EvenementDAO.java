package org.example.dao;
import org.example.entity.Evenement;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class EvenementDAO {
    private final Connection connection;

    public EvenementDAO() {
        connection = MyDatabase.getInstance().getConnection();

    }

    // ➕ Ajouter un événement
    public void ajouterEvenement(Evenement event) {
        String sql = "INSERT INTO event (category_id, nom, description, date, heure_debut, heure_fin, lieu, image,prix) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, event.getCategoryId());
            stmt.setString(2, event.getNom());
            stmt.setString(3, event.getDescription());
            stmt.setDate(4, java.sql.Date.valueOf(event.getDate()));

            stmt.setTime(5, Time.valueOf(event.getHeureDebut()));
            stmt.setTime(6, Time.valueOf(event.getHeureFin()));
            stmt.setString(7, event.getLieu());
            stmt.setString(8, event.getImage());
            stmt.setFloat(9, event.getPrix());

            stmt.executeUpdate();
            System.out.println("✅ Événement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur d'ajout d'événement : " + e.getMessage());
        }
    }

    public List<Evenement> getAllEvenements() {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT e.*, c.nom AS nomCategorie " +
                "FROM event e JOIN category c ON e.category_id = c.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Evenement e = new Evenement(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDate("date").toLocalDate(),
                        rs.getTime("heure_debut").toLocalTime(),
                        rs.getTime("heure_fin").toLocalTime(),
                        rs.getString("lieu"),
                        rs.getString("image"),
                        rs.getFloat("prix")
                );
                e.setNomCategorie(rs.getString("nomCategorie")); // ✅ ICI
                list.add(e);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur de lecture des événements : " + e.getMessage());
        }

        return list;
    }

    // ❌ Supprimer un événement
    public void supprimerEvenement(int id) {
        String sql = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✅ Événement supprimé !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
        }
    }

    // ✏️ Modifier un événement
    public void modifierEvenement(Evenement event) {
        String sql = "UPDATE event SET category_id = ?, nom = ?, description = ?, date = ?, heure_debut = ?, heure_fin = ?, lieu = ?, image = ?, prix = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, event.getCategoryId());
            stmt.setString(2, event.getNom());
            stmt.setString(3, event.getDescription());
            stmt.setDate(4, java.sql.Date.valueOf(event.getDateEvent()));
            stmt.setTime(5, Time.valueOf(event.getHeureDebut()));
            stmt.setTime(6, Time.valueOf(event.getHeureFin()));
            stmt.setString(7, event.getLieu());
            stmt.setString(8, event.getImage());
            stmt.setFloat(9, event.getPrix());
            stmt.setInt(10, event.getId());
            stmt.executeUpdate();
            System.out.println("✅ Événement modifié !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de modification : " + e.getMessage());
        }
    }

    // 📊 Statistiques mensuelles (par date de l’événement)
    public Map<String, Integer> getNombreEvenementsParMois() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = "SELECT MONTH(date) AS mois, COUNT(*) AS total FROM event GROUP BY mois ORDER BY mois";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int mois = rs.getInt("mois");
                int total = rs.getInt("total");
                String nomMois = getNomMois(mois);
                data.put(nomMois, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private String getNomMois(int mois) {
        return switch (mois) {
            case 1 -> "Janvier";
            case 2 -> "Février";
            case 3 -> "Mars";
            case 4 -> "Avril";
            case 5 -> "Mai";
            case 6 -> "Juin";
            case 7 -> "Juillet";
            case 8 -> "Août";
            case 9 -> "Septembre";
            case 10 -> "Octobre";
            case 11 -> "Novembre";
            case 12 -> "Décembre";
            default -> "Inconnu";
        };
    }

    public List<LocalDate> getDatesEvenements() {
        List<LocalDate> dates = new ArrayList<>();
        try (Connection conn = MyDatabase.getInstance().getConnection();

             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT date FROM event")) {

            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                dates.add(date);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }
    public List<Evenement> getAll() {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM event";

        try (Connection conn = MyDatabase.getInstance().getConnection();



             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setCategoryId(rs.getInt("category_id"));
                e.setNom(rs.getString("nom"));
                e.setDescription(rs.getString("description"));
                e.setDate(rs.getDate("date").toLocalDate());
                e.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                e.setHeureFin(rs.getTime("heure_fin").toLocalTime());
                e.setLieu(rs.getString("lieu"));
                e.setImage(rs.getString("image"));
                e.setPrix(rs.getFloat("prix"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
