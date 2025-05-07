package org.example.services;

import org.example.entity.Apprenant;
import org.example.entity.Reclamation;
import org.example.entity.Reponse;
import org.example.utils.MyDatabase;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation>
{




        private Connection cnx;

        public ServiceReclamation() {
            cnx = MyDatabase.getInstance().getConnection();
        }

        @Override
        public void ajouter(Reclamation r) throws SQLException {
            String sql = "INSERT INTO reclamation (title, description, etat, apprenant_id, reponse_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getEtat());
            ps.setInt(4, r.getApprenant().getId());
            if (r.getReponse() != null) {
                ps.setInt(5, r.getReponse().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        }

        @Override
        public void modifier(Reclamation r) throws SQLException {
            String sql = "UPDATE reclamation SET title=?, description=?, etat=?, apprenant_id=?, reponse_id=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getEtat());
            ps.setInt(4, r.getApprenant().getId());
            if (r.getReponse() != null) {
                ps.setInt(5, r.getReponse().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, r.getId());
            ps.executeUpdate();
        }

        @Override
        public void supprimer(int id) throws SQLException {
            String sql = "DELETE FROM reclamation WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        @Override
        public List<Reclamation> afficher() throws SQLException {
            List<Reclamation> list = new ArrayList<>();
            String sql = "SELECT * FROM reclamation";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setTitle(rs.getString("title"));
                r.setDescription(rs.getString("description"));
                r.setEtat(rs.getString("etat"));

                Apprenant apprenant = new Apprenant();
                apprenant.setId(rs.getInt("apprenant_id"));
                r.setApprenant(apprenant);

                int reponseId = rs.getInt("reponse_id");
                if (!rs.wasNull()) {
                    Reponse rep = new Reponse();
                    rep.setId(reponseId);
                    r.setReponse(rep);
                }

                list.add(r);
            }

            return list;
        }
    }

