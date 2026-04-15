package javafx.forum1.service;

import javafx.forum1.dao.CommentaireDao;
import javafx.forum1.model.Commentaire;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentaireServiceTest {

    @Test
    void createCommentaire_shouldTrimAndInsert() throws Exception {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        Commentaire expected = new Commentaire(5L, 3L, "Contenu", LocalDateTime.now());
        dao.insertResult = expected;
        CommentaireService service = new CommentaireService(dao);

        Commentaire created = service.createCommentaire(3L, "  Contenu  ");

        assertSame(expected, created);
        assertEquals(1, dao.insertCalls);
        assertEquals(3L, dao.lastInsertedPosteId);
        assertEquals("Contenu", dao.lastInsertedContenu);
    }

    @Test
    void createCommentaire_shouldRejectInvalidPosteId() {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        CommentaireService service = new CommentaireService(dao);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createCommentaire(0L, "contenu ok")
        );

        assertEquals("Selectionnez un poste valide.", ex.getMessage());
        assertEquals(0, dao.insertCalls);
    }

    @Test
    void createCommentaire_shouldRejectBlankContenu() {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        CommentaireService service = new CommentaireService(dao);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createCommentaire(1L, "   ")
        );

        assertEquals("Le contenu du commentaire est obligatoire.", ex.getMessage());
        assertEquals(0, dao.insertCalls);
    }

    @Test
    void createCommentaire_shouldRejectContenuTooLong() {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        CommentaireService service = new CommentaireService(dao);
        String tooLong = "x".repeat(CommentaireService.CONTENU_MAX_LENGTH + 1);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createCommentaire(1L, tooLong)
        );

        assertEquals("Le commentaire ne doit pas depasser " + CommentaireService.CONTENU_MAX_LENGTH + " caracteres.", ex.getMessage());
        assertEquals(0, dao.insertCalls);
    }

    @Test
    void updateCommentaire_shouldRejectInvalidCommentaireId() {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        CommentaireService service = new CommentaireService(dao);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.updateCommentaire(0L, "contenu ok")
        );

        assertEquals("Selectionnez un commentaire valide.", ex.getMessage());
        assertEquals(0, dao.updateCalls);
    }

    @Test
    void listByPosteId_shouldRejectInvalidPosteId() {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        CommentaireService service = new CommentaireService(dao);

        SQLException ex = assertThrows(
                SQLException.class,
                () -> service.listByPosteId(0L)
        );

        assertEquals("Selectionnez un poste valide.", ex.getMessage());
        assertEquals(0, dao.findByPosteIdCalls);
    }

    @Test
    void deleteCommentaire_shouldThrowWhenNotFound() {
        FakeCommentaireDao dao = new FakeCommentaireDao();
        dao.deleteResult = false;
        CommentaireService service = new CommentaireService(dao);

        SQLException ex = assertThrows(
                SQLException.class,
                () -> service.deleteCommentaire(9L)
        );

        assertEquals("Le commentaire a supprimer est introuvable.", ex.getMessage());
        assertEquals(1, dao.deleteCalls);
    }

    private static final class FakeCommentaireDao extends CommentaireDao {
        private final List<Commentaire> commentaires = new ArrayList<>();

        private Commentaire insertResult = new Commentaire(1L, 1L, "", LocalDateTime.now());
        private boolean updateResult = true;
        private boolean deleteResult = true;

        private int findByPosteIdCalls;
        private int insertCalls;
        private int updateCalls;
        private int deleteCalls;

        private long lastInsertedPosteId;
        private String lastInsertedContenu;

        @Override
        public List<Commentaire> findByPosteId(long posteId) {
            findByPosteIdCalls++;
            return commentaires;
        }

        @Override
        public Commentaire insert(long posteId, String contenu) {
            insertCalls++;
            lastInsertedPosteId = posteId;
            lastInsertedContenu = contenu;
            return insertResult;
        }

        @Override
        public boolean update(long id, String contenu) {
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

