package javafx.forum1.dao;

import javafx.forum1.db.DatabaseConnection;
import javafx.forum1.model.Poste;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PosteDao {
    private static final String SQL_COLUMNS = "SELECT COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION, COLUMN_KEY, EXTRA "
            + "FROM INFORMATION_SCHEMA.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poste'";

    private static final Set<String> TEXT_TYPES = Set.of("varchar", "char", "text", "tinytext", "mediumtext", "longtext");
    private static final Set<String> DATE_TYPES = Set.of("timestamp", "datetime", "date");
    private static final Set<String> NUMERIC_TYPES = Set.of("bigint", "int", "integer", "smallint", "mediumint", "tinyint", "decimal", "numeric");

    private static final class ColumnInfo {
        private final String name;
        private final String dataType;
        private final int ordinal;
        private final String key;
        private final String extra;

        private ColumnInfo(String name, String dataType, int ordinal, String key, String extra) {
            this.name = name;
            this.dataType = dataType;
            this.ordinal = ordinal;
            this.key = key;
            this.extra = extra;
        }
    }

    private static final class ColumnMapping {
        private final String id;
        private final String titre;
        private final String description;
        private final String dateCreation;

        private ColumnMapping(String id, String titre, String description, String dateCreation) {
            this.id = id;
            this.titre = titre;
            this.description = description;
            this.dateCreation = dateCreation;
        }
    }

    public List<Poste> findAll() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildFindAllSql(connection));
             ResultSet rs = statement.executeQuery()) {
            List<Poste> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapPoste(rs));
            }
            return result;
        }
    }

    public Optional<Poste> findById(long id) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildFindByIdSql(connection))) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPoste(rs));
                }
                return Optional.empty();
            }
        }
    }

    public Poste insert(String titre, String description) throws SQLException {
        try (Connection connection = DatabaseConnection.open()) {
            ColumnMapping mapping = resolveColumns(connection);
            String sql = "INSERT INTO poste (" + q(mapping.titre) + ", " + q(mapping.description) + ") VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, titre);
                statement.setString(2, description);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        Optional<Poste> inserted = findById(id);
                        if (inserted.isPresent()) {
                            return inserted.get();
                        }
                    }
                }
            }

            throw new SQLException("Impossible de recuperer le poste insere.");
        }
    }

    public boolean update(long id, String titre, String description) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildUpdateSql(connection))) {
            statement.setString(1, titre);
            statement.setString(2, description);
            statement.setLong(3, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(long id) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildDeleteSql(connection))) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private String buildFindAllSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        String dateExpr = mapping.dateCreation == null ? "NULL" : q(mapping.dateCreation);
        String orderExpr = mapping.dateCreation == null ? q(mapping.id) : q(mapping.dateCreation);
        return "SELECT " + q(mapping.id) + " AS id, "
                + q(mapping.titre) + " AS titre, "
                + q(mapping.description) + " AS description, "
                + dateExpr + " AS date_creation "
                + "FROM poste ORDER BY " + orderExpr + " DESC, " + q(mapping.id) + " DESC";
    }

    private String buildFindByIdSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        String dateExpr = mapping.dateCreation == null ? "NULL" : q(mapping.dateCreation);
        return "SELECT " + q(mapping.id) + " AS id, "
                + q(mapping.titre) + " AS titre, "
                + q(mapping.description) + " AS description, "
                + dateExpr + " AS date_creation "
                + "FROM poste WHERE " + q(mapping.id) + " = ?";
    }

    private String buildUpdateSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        return "UPDATE poste SET " + q(mapping.titre) + " = ?, " + q(mapping.description) + " = ? WHERE " + q(mapping.id) + " = ?";
    }

    private String buildDeleteSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        return "DELETE FROM poste WHERE " + q(mapping.id) + " = ?";
    }

    private ColumnMapping resolveColumns(Connection connection) throws SQLException {
        Set<String> cols = new HashSet<>();
        List<ColumnInfo> details = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_COLUMNS);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                int ordinal = rs.getInt("ORDINAL_POSITION");
                String key = rs.getString("COLUMN_KEY");
                String extra = rs.getString("EXTRA");

                cols.add(name.toLowerCase());
                details.add(new ColumnInfo(
                        name,
                        dataType == null ? "" : dataType.toLowerCase(),
                        ordinal,
                        key == null ? "" : key.toLowerCase(),
                        extra == null ? "" : extra.toLowerCase()
                ));
            }
        }

        String id = pickIdColumn(cols, details);
        String titre = pickTitreColumn(cols, details, id);
        String description = pickDescriptionColumn(cols, details, id, titre);
        String dateCreation = pickDateColumn(cols, details, id, titre, description);

        if (id == null) {
            throw new SQLException("Impossible de determiner la colonne identifiant de `poste`.");
        }
        if (titre == null) {
            throw new SQLException("Impossible de determiner la colonne titre de `poste`.");
        }
        if (description == null) {
            throw new SQLException("Impossible de determiner la colonne description de `poste`.");
        }

        return new ColumnMapping(id, titre, description, dateCreation);
    }

    private String pickIdColumn(Set<String> cols, List<ColumnInfo> details) {
        String byName = pickOptional(cols, "id", "poste_id", "post_id", "id_poste");
        if (byName != null) {
            return byName;
        }

        Optional<ColumnInfo> autoIncrement = details.stream()
                .filter(c -> NUMERIC_TYPES.contains(c.dataType))
                .filter(c -> c.extra.contains("auto_increment") || "pri".equals(c.key))
                .min(Comparator.comparingInt(c -> c.ordinal));

        if (autoIncrement.isPresent()) {
            return autoIncrement.get().name;
        }

        return details.stream()
                .filter(c -> NUMERIC_TYPES.contains(c.dataType))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .map(c -> c.name)
                .orElse(null);
    }

    private String pickTitreColumn(Set<String> cols, List<ColumnInfo> details, String id) {
        String byName = pickOptional(cols, "titre", "title", "nom", "sujet", "libelle", "label", "name");
        if (byName != null) {
            return byName;
        }

        return details.stream()
                .filter(c -> TEXT_TYPES.contains(c.dataType))
                .filter(c -> !c.name.equalsIgnoreCase(id))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .map(c -> c.name)
                .orElse(null);
    }

    private String pickDescriptionColumn(Set<String> cols, List<ColumnInfo> details, String id, String titre) {
        String byName = pickOptional(cols, "description", "contenu", "content", "texte", "body", "message", "details");
        if (byName != null && !byName.equalsIgnoreCase(titre)) {
            return byName;
        }

        List<ColumnInfo> textCols = details.stream()
                .filter(c -> TEXT_TYPES.contains(c.dataType))
                .filter(c -> !c.name.equalsIgnoreCase(id))
                .filter(c -> !c.name.equalsIgnoreCase(titre))
                .sorted(Comparator.comparingInt(c -> c.ordinal))
                .toList();

        if (!textCols.isEmpty()) {
            return textCols.get(0).name;
        }

        return null;
    }

    private String pickDateColumn(Set<String> cols, List<ColumnInfo> details, String id, String titre, String description) {
        String byName = pickOptional(cols, "date_creation", "created_at", "date_post", "createdat", "date", "posted_at");
        if (byName != null) {
            return byName;
        }

        return details.stream()
                .filter(c -> DATE_TYPES.contains(c.dataType))
                .filter(c -> !c.name.equalsIgnoreCase(id))
                .filter(c -> !c.name.equalsIgnoreCase(titre))
                .filter(c -> !c.name.equalsIgnoreCase(description))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .map(c -> c.name)
                .orElse(null);
    }

    private String pickRequired(Set<String> cols, String... candidates) throws SQLException {
        String value = pickOptional(cols, candidates);
        if (value == null) {
            throw new SQLException("Colonne requise introuvable dans `poste`. Candidats: " + String.join(", ", candidates));
        }
        return value;
    }

    private String pickOptional(Set<String> cols, String... candidates) {
        for (String candidate : candidates) {
            if (cols.contains(candidate.toLowerCase())) {
                return candidate;
            }
        }
        return null;
    }

    private String q(String column) {
        return "`" + column + "`";
    }

    private Poste mapPoste(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String titre = rs.getString("titre");
        String description = rs.getString("description");
        Timestamp timestamp = rs.getTimestamp("date_creation");
        LocalDateTime dateCreation = timestamp == null ? null : timestamp.toLocalDateTime();

        return new Poste(id, titre == null ? "" : titre, description == null ? "" : description, dateCreation);
    }
}

