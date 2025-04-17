package org.example.dao;

import org.example.entity.Categorie;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.*;

public class CategorieDAO {

    public List<Categorie> getAllCategories() {
        List<Categorie> list = new ArrayList<>();
        String query = "SELECT * FROM category";

        Connection con = MyDatabase.getInstance().getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

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
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return list;
    }

    public void ajouterCategorie(Categorie cat) {
        String query = "INSERT INTO category (nom, description, image) VALUES (?, ?, ?)";

        Connection con = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, cat.getNom());
            ps.setString(2, cat.getDescription());
            ps.setString(3, cat.getImage());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout catégorie : " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void modifierCategorie(Categorie cat) {
        String query = "UPDATE category SET nom=?, description=?, image=? WHERE id=?";

        Connection con = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, cat.getNom());
            ps.setString(2, cat.getDescription());
            ps.setString(3, cat.getImage());
            ps.setInt(4, cat.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification catégorie : " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void supprimerCategorie(int id) {
        String query = "DELETE FROM category WHERE id = ?";

        Connection con = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Catégorie supprimée avec succès de la base de données.");

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression catégorie : " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean nomCategorieExiste(String nom) {
        String sql = "SELECT COUNT(*) FROM category WHERE LOWER(nom) = LOWER(?)";
        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nom);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public Categorie getCategorieByNom(String nom) {
        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM category WHERE nom = ?");
            ps.setString(1, nom);
            rs = ps.executeQuery();

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
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public int getNombreTotalCategories() {
        String query = "SELECT COUNT(*) FROM category";

        Connection connection = MyDatabase.getInstance().getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = connection.prepareStatement(query);
            rs = statement.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public String getCategorieLaPlusUtilisee() {
        String sql = "SELECT c.nom, COUNT(e.id) as total " +
                "FROM category c JOIN event e ON c.id = e.category_id " +
                "GROUP BY c.nom ORDER BY total DESC LIMIT 1";

        Connection conn = MyDatabase.getInstance().getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getString("nom");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public Map<String, Integer> getRepartitionCategories() {
        Map<String, Integer> repartition = new HashMap<>();
        String sql = "SELECT c.nom, COUNT(e.id) as total " +
                "FROM category c JOIN event e ON c.id = e.category_id " +
                "GROUP BY c.nom";

        Connection conn = MyDatabase.getInstance().getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                repartition.put(rs.getString("nom"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return repartition;
    }

    public String getNomCategorieById(int id) {
        String query = "SELECT nom FROM category WHERE id = ?";

        Connection conn = MyDatabase.getInstance().getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return "Inconnue";
    }
}
