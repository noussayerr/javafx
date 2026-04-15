package javafx.forum1.dev;

import javafx.forum1.dao.CommentaireDao;
import javafx.forum1.dao.PosteDao;
import javafx.forum1.model.Commentaire;
import javafx.forum1.model.Poste;

import java.sql.SQLException;
import java.util.List;

public final class DbSmokeTest {
    private DbSmokeTest() {
    }

    public static void main(String[] args) {
        PosteDao posteDao = new PosteDao();
        CommentaireDao commentaireDao = new CommentaireDao();

        try {
            List<Poste> postes = posteDao.findAll();
            List<Commentaire> commentaires = commentaireDao.findAll();

            System.out.println("Postes trouves: " + postes.size());
            if (!postes.isEmpty()) {
                System.out.println("Premier poste id=" + postes.get(0).id() + ", titre=" + postes.get(0).titre());
            }

            System.out.println("Commentaires trouves: " + commentaires.size());
            if (!commentaires.isEmpty()) {
                Commentaire c = commentaires.get(0);
                System.out.println("Premier commentaire id=" + c.id() + ", posteId=" + c.posteId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

