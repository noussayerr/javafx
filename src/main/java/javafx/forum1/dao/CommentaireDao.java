package javafx.forum1.dao;

import javafx.forum1.db.DatabaseConnection;
import javafx.forum1.model.Commentaire;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommentaireDao {
    private static final String SQL_FIND_ALL = "SELECT * FROM commentaires ORDER BY id DESC";
    private static final String SQL_FIND_BY_POSTE_ID = "SELECT * FROM commentaires WHERE poste_id = ? ORDER BY id DESC";

    public List<Commentaire> findAll() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = statement.executeQuery()) {
            return readCommentaires(rs);
        }
    }

    public List<Commentaire> findByPosteId(long posteId) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_POSTE_ID)) {
            statement.setLong(1, posteId);
            try (ResultSet rs = statement.executeQuery()) {
                return readCommentaires(rs);
            }
        }
    }

    private List<Commentaire> readCommentaires(ResultSet rs) throws SQLException {
        Set<String> columns = extractColumns(rs.getMetaData());
        String idCol = requireColumn(columns, "id", "commentaire_id");
        String posteIdCol = requireColumn(columns, "poste_id", "post_id", "id_poste");
        String contenuCol = optionalColumn(columns, "contenu", "content", "texte", "description");
        String createdAtCol = optionalColumn(columns, "created_at", "date_creation", "createdat", "date_commentaire");

        List<Commentaire> result = new ArrayList<>();
        while (rs.next()) {
            long id = rs.getLong(idCol);
            long rowPosteId = rs.getLong(posteIdCol);
            String contenu = contenuCol == null ? "" : rs.getString(contenuCol);
            LocalDateTime createdAt = readDateTime(rs, createdAtCol);
            result.add(new Commentaire(id, rowPosteId, nullToEmpty(contenu), createdAt));
        }
        return result;
    }

    private static Set<String> extractColumns(ResultSetMetaData metaData) throws SQLException {
        Set<String> columns = new HashSet<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columns.add(metaData.getColumnLabel(i).toLowerCase(Locale.ROOT));
        }
        return columns;
    }

    private static String requireColumn(Set<String> columns, String... candidates) throws SQLException {
        String col = optionalColumn(columns, candidates);
        if (col == null) {
            throw new SQLException("Colonne requise introuvable. Candidats: " + String.join(", ", candidates));
        }
        return col;
    }

    private static String optionalColumn(Set<String> columns, String... candidates) {
        for (String candidate : candidates) {
            if (columns.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return null;
    }

    private static LocalDateTime readDateTime(ResultSet rs, String column) throws SQLException {
        if (column == null) {
            return null;
        }
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

