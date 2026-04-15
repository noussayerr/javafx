package javafx.forum1.service;

import javafx.forum1.dao.PosteDao;
import javafx.forum1.model.Poste;

import java.sql.SQLException;
import java.util.List;

public class PosteService {
    private static final int TITRE_MIN_LENGTH = 3;

    private final PosteDao posteDao;

    public PosteService() {
        this(new PosteDao());
    }

    public PosteService(PosteDao posteDao) {
        this.posteDao = posteDao;
    }

    public List<Poste> listPostes() throws SQLException {
        return posteDao.findAll();
    }

    public Poste createPoste(String titre, String description) throws SQLException, ValidationException {
        validate(titre, description);
        return posteDao.insert(titre.trim(), description.trim());
    }

    public void updatePoste(long id, String titre, String description) throws SQLException, ValidationException {
        validate(titre, description);
        boolean updated = posteDao.update(id, titre.trim(), description.trim());
        if (!updated) {
            throw new SQLException("Le poste a modifier est introuvable.");
        }
    }

    public void deletePoste(long id) throws SQLException {
        boolean deleted = posteDao.deleteById(id);
        if (!deleted) {
            throw new SQLException("Le poste a supprimer est introuvable.");
        }
    }

    private void validate(String titre, String description) throws ValidationException {
        if (titre == null || titre.isBlank()) {
            throw new ValidationException("Le titre est obligatoire.");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("La description est obligatoire.");
        }

        String normalizedTitle = titre.trim();
        if (normalizedTitle.length() < TITRE_MIN_LENGTH) {
            throw new ValidationException("Le titre doit contenir au moins " + TITRE_MIN_LENGTH + " caracteres.");
        }
    }
}

