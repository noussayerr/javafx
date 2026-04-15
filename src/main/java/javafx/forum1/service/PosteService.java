package javafx.forum1.service;

import javafx.forum1.dao.PosteDao;
import javafx.forum1.model.Poste;

import java.sql.SQLException;
import java.util.List;

public class PosteService {
    public static final int TITRE_MIN_LENGTH = 3;
    public static final int TITRE_MAX_LENGTH = 150;
    public static final int DESCRIPTION_MIN_LENGTH = 10;
    public static final int DESCRIPTION_MAX_LENGTH = 5000;

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
        String[] normalized = validateAndNormalize(titre, description);
        return posteDao.insert(normalized[0], normalized[1]);
    }

    public void updatePoste(long id, String titre, String description) throws SQLException, ValidationException {
        validateId(id);
        String[] normalized = validateAndNormalize(titre, description);
        boolean updated = posteDao.update(id, normalized[0], normalized[1]);
        if (!updated) {
            throw new SQLException("Le poste a modifier est introuvable.");
        }
    }

    public void deletePoste(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("Selectionnez un poste valide.");
        }
        boolean deleted = posteDao.deleteById(id);
        if (!deleted) {
            throw new SQLException("Le poste a supprimer est introuvable.");
        }
    }

    private String[] validateAndNormalize(String titre, String description) throws ValidationException {
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
        if (normalizedTitle.length() > TITRE_MAX_LENGTH) {
            throw new ValidationException("Le titre ne doit pas depasser " + TITRE_MAX_LENGTH + " caracteres.");
        }

        String normalizedDescription = description.trim();
        if (normalizedDescription.length() < DESCRIPTION_MIN_LENGTH) {
            throw new ValidationException("La description doit contenir au moins " + DESCRIPTION_MIN_LENGTH + " caracteres.");
        }
        if (normalizedDescription.length() > DESCRIPTION_MAX_LENGTH) {
            throw new ValidationException("La description ne doit pas depasser " + DESCRIPTION_MAX_LENGTH + " caracteres.");
        }

        return new String[]{normalizedTitle, normalizedDescription};
    }

    private void validateId(long id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("Selectionnez un poste valide.");
        }
    }
}
