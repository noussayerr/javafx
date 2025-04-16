package org.example.dao;

import org.example.entity.Categorie;
import org.example.utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.utils.MyDatabase;


public class CategorieDAO {

    public List<Categorie> getAllCategories() {
        List<Categorie> list = new ArrayList<>();
        String query = "SELECT * FROM category";

        try (Connection con = MyDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {


            while (rs.next()) {
                Categorie cat = new Categorie(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("image")
                );
                list.add(cat);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllCategories : " + e.getMessage());
        }
        return list;
    }

    public void ajouterCategorie(Categorie cat) {
        String query = "INSERT INTO category (nom, description, image) VALUES (?, ?, ?)";

        try (Connection con = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, cat.getNom());
            ps.setString(2, cat.getDescription());
            ps.setString(3, cat.getImage());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout catégorie : " + e.getMessage());
        }
    }

    public void modifierCategorie(Categorie cat) {
        String query = "UPDATE category SET nom=?, description=?, image=? WHERE id=?";

        try (Connection con = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, cat.getNom());
            ps.setString(2, cat.getDescription());
            ps.setString(3, cat.getImage());
            ps.setInt(4, cat.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification catégorie : " + e.getMessage());
        }
    }

    public void supprimerCategorie(int id) {
        String query = "DELETE FROM category WHERE id = ?";

        try (Connection con = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Catégorie supprimée avec succès de la base de données.");

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression catégorie : " + e.getMessage());
        }
    }

    public boolean nomCategorieExiste(String nom) {
        String sql = "SELECT COUNT(*) FROM category WHERE LOWER(nom) = LOWER(?)";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Categorie getCategorieByNom(String nom) {
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM category WHERE nom = ?")) {

            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Categorie(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getNombreTotalCategories() {
        String query = "SELECT COUNT(*) FROM category";
        try (Connection connection = MyDatabase.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getCategorieLaPlusUtilisee() {
        String sql = "SELECT c.nom, COUNT(e.id) as total " +
                "FROM category c JOIN event e ON c.id = e.category_id " +
                "GROUP BY c.nom ORDER BY total DESC LIMIT 1";

        try (Connection conn = MyDatabase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString("nom");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Map<String, Integer> getRepartitionCategories() {
        Map<String, Integer> repartition = new HashMap<>();
        String sql = "SELECT c.nom, COUNT(e.id) as total " +
                "FROM category c JOIN event e ON c.id = e.category_id " +
                "GROUP BY c.nom";

        try (Connection conn = MyDatabase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                repartition.put(rs.getString("nom"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return repartition;
    }

    public String getNomCategorieById(int id) {
        String query = "SELECT nom FROM category WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnue";
    }}
