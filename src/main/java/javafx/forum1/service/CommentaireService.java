package javafx.forum1.service;

import javafx.forum1.dao.CommentaireDao;
import javafx.forum1.model.Commentaire;

import java.sql.SQLException;
import java.util.List;

public class CommentaireService {
    private static final int CONTENU_MIN_LENGTH = 2;

    private final CommentaireDao commentaireDao;

    public CommentaireService() {
        this(new CommentaireDao());
    }

    public CommentaireService(CommentaireDao commentaireDao) {
        this.commentaireDao = commentaireDao;
    }

    public List<Commentaire> listByPosteId(long posteId) throws SQLException {
        return commentaireDao.findByPosteId(posteId);
    }

    public Commentaire createCommentaire(long posteId, String contenu) throws SQLException, ValidationException {
        validatePosteId(posteId);
        validateContenu(contenu);
        return commentaireDao.insert(posteId, contenu.trim());
    }

    public void updateCommentaire(long commentaireId, String contenu) throws SQLException, ValidationException {
        validateContenu(contenu);
        boolean updated = commentaireDao.update(commentaireId, contenu.trim());
        if (!updated) {
            throw new SQLException("Le commentaire a modifier est introuvable.");
        }
    }

    public void deleteCommentaire(long commentaireId) throws SQLException {
        boolean deleted = commentaireDao.deleteById(commentaireId);
        if (!deleted) {
            throw new SQLException("Le commentaire a supprimer est introuvable.");
        }
    }

    private void validatePosteId(long posteId) throws ValidationException {
        if (posteId <= 0) {
            throw new ValidationException("Selectionnez un poste valide.");
        }
    }

    private void validateContenu(String contenu) throws ValidationException {
        if (contenu == null || contenu.isBlank()) {
            throw new ValidationException("Le contenu du commentaire est obligatoire.");
        }
        if (contenu.trim().length() < CONTENU_MIN_LENGTH) {
            throw new ValidationException("Le commentaire doit contenir au moins " + CONTENU_MIN_LENGTH + " caracteres.");
        }
    }
}

