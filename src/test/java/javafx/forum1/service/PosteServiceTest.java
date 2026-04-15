package javafx.forum1.service;

import javafx.forum1.dao.PosteDao;
import javafx.forum1.model.Poste;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PosteServiceTest {

    @Test
    void createPoste_shouldTrimAndInsert() throws Exception {
        FakePosteDao dao = new FakePosteDao();
        Poste expected = new Poste(1L, "Titre", "Description valide", LocalDateTime.now());
        dao.insertResult = expected;
        PosteService service = new PosteService(dao);

        Poste created = service.createPoste("  Titre  ", "  Description valide  ");

        assertSame(expected, created);
        assertEquals(1, dao.insertCalls);
        assertEquals("Titre", dao.lastInsertedTitre);
        assertEquals("Description valide", dao.lastInsertedDescription);
    }

    @Test
    void createPoste_shouldRejectBlankTitre() {
        FakePosteDao dao = new FakePosteDao();
        PosteService service = new PosteService(dao);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createPoste("   ", "Description valide")
        );

        assertEquals("Le titre est obligatoire.", ex.getMessage());
        assertEquals(0, dao.insertCalls);
    }

    @Test
    void createPoste_shouldRejectDescriptionTooShort() {
        FakePosteDao dao = new FakePosteDao();
        PosteService service = new PosteService(dao);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createPoste("Titre", "court")
        );

        assertEquals("La description doit contenir au moins " + PosteService.DESCRIPTION_MIN_LENGTH + " caracteres.", ex.getMessage());
        assertEquals(0, dao.insertCalls);
    }

    @Test
    void createPoste_shouldRejectTitreTooLong() {
        FakePosteDao dao = new FakePosteDao();
        PosteService service = new PosteService(dao);
        String tooLongTitre = "A".repeat(PosteService.TITRE_MAX_LENGTH + 1);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createPoste(tooLongTitre, "Description valide assez longue")
        );

        assertEquals("Le titre ne doit pas depasser " + PosteService.TITRE_MAX_LENGTH + " caracteres.", ex.getMessage());
        assertEquals(0, dao.insertCalls);
    }

    @Test
    void updatePoste_shouldRejectInvalidId() {
        FakePosteDao dao = new FakePosteDao();
        PosteService service = new PosteService(dao);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.updatePoste(0L, "Titre", "Description valide")
        );

        assertEquals("Selectionnez un poste valide.", ex.getMessage());
        assertEquals(0, dao.updateCalls);
    }

    @Test
    void updatePoste_shouldThrowWhenPosteNotFound() {
        FakePosteDao dao = new FakePosteDao();
        dao.updateResult = false;
        PosteService service = new PosteService(dao);

        SQLException ex = assertThrows(
                SQLException.class,
                () -> service.updatePoste(12L, "Titre", "Description valide")
        );

        assertEquals("Le poste a modifier est introuvable.", ex.getMessage());
        assertEquals(1, dao.updateCalls);
    }

    @Test
    void deletePoste_shouldRejectInvalidId() {
        FakePosteDao dao = new FakePosteDao();
        PosteService service = new PosteService(dao);

        SQLException ex = assertThrows(
                SQLException.class,
                () -> service.deletePoste(-1L)
        );

        assertEquals("Selectionnez un poste valide.", ex.getMessage());
        assertEquals(0, dao.deleteCalls);
    }

    private static final class FakePosteDao extends PosteDao {
        private final List<Poste> posteList = new ArrayList<>();

        private Poste insertResult = new Poste(1L, "", "", LocalDateTime.now());
        private boolean updateResult = true;
        private boolean deleteResult = true;

        private int insertCalls;
        private int updateCalls;
        private int deleteCalls;

        private String lastInsertedTitre;
        private String lastInsertedDescription;

        @Override
        public List<Poste> findAll() {
            return posteList;
        }

        @Override
        public Poste insert(String titre, String description) {
            insertCalls++;
            lastInsertedTitre = titre;
            lastInsertedDescription = description;
            return insertResult;
        }

        @Override
        public boolean update(long id, String titre, String description) {
            updateCalls++;
            return updateResult;
        }

        @Override
        public boolean deleteById(long id) {
            deleteCalls++;
            return deleteResult;
        }
    }
}

