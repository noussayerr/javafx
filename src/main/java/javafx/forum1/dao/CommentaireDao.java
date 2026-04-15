package javafx.forum1.dao;

import javafx.forum1.db.DatabaseConnection;
import javafx.forum1.model.Commentaire;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommentaireDao {
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
        private final String posteId;
        private final String contenu;
        private final String createdAt;

        private ColumnMapping(String id, String posteId, String contenu, String createdAt) {
            this.id = id;
            this.posteId = posteId;
            this.contenu = contenu;
            this.createdAt = createdAt;
        }
    }

    public List<Commentaire> findAll() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildFindAllSql(connection));
             ResultSet rs = statement.executeQuery()) {
            return readCommentaires(rs);
        }
    }

    public List<Commentaire> findByPosteId(long posteId) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildFindByPosteIdSql(connection))) {
            statement.setLong(1, posteId);
            try (ResultSet rs = statement.executeQuery()) {
                return readCommentaires(rs);
            }
        }
    }

    public Commentaire insert(long posteId, String contenu) throws SQLException {
        try (Connection connection = DatabaseConnection.open()) {
            ColumnMapping mapping = resolveColumns(connection);
            String sql = "INSERT INTO commentaires (" + q(mapping.posteId) + ", " + q(mapping.contenu) + ") VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, posteId);
                statement.setString(2, contenu);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        Optional<Commentaire> inserted = findById(id);
                        if (inserted.isPresent()) {
                            return inserted.get();
                        }
                    }
                }
            }

            throw new SQLException("Impossible de recuperer le commentaire insere.");
        }
    }

    public boolean update(long id, String contenu) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildUpdateSql(connection))) {
            statement.setString(1, contenu);
            statement.setLong(2, id);
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

    private Optional<Commentaire> findById(long id) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(buildFindByIdSql(connection))) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(readCommentaire(rs));
                }
                return Optional.empty();
            }
        }
    }

    private String buildFindAllSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        String base = buildSelectBase(mapping);
        String orderExpr = mapping.createdAt == null ? q(mapping.id) : q(mapping.createdAt);
        return base + " ORDER BY " + orderExpr + " DESC, " + q(mapping.id) + " DESC";
    }

    private String buildFindByPosteIdSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        String base = buildSelectBase(mapping);
        String orderExpr = mapping.createdAt == null ? q(mapping.id) : q(mapping.createdAt);
        return base + " WHERE " + q(mapping.posteId) + " = ? ORDER BY " + orderExpr + " DESC, " + q(mapping.id) + " DESC";
    }

    private String buildFindByIdSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        return buildSelectBase(mapping) + " WHERE " + q(mapping.id) + " = ?";
    }

    private String buildUpdateSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        return "UPDATE commentaires SET " + q(mapping.contenu) + " = ? WHERE " + q(mapping.id) + " = ?";
    }

    private String buildDeleteSql(Connection connection) throws SQLException {
        ColumnMapping mapping = resolveColumns(connection);
        return "DELETE FROM commentaires WHERE " + q(mapping.id) + " = ?";
    }

    private String buildSelectBase(ColumnMapping mapping) {
        String dateExpr = mapping.createdAt == null ? "NULL" : q(mapping.createdAt);
        return "SELECT " + q(mapping.id) + " AS id, "
                + q(mapping.posteId) + " AS poste_id, "
                + q(mapping.contenu) + " AS contenu, "
                + dateExpr + " AS created_at FROM commentaires";
    }

    private ColumnMapping resolveColumns(Connection connection) throws SQLException {
        List<ColumnInfo> details = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        boolean found = readColumns(metaData, "commentaires", details, 1);
        if (!found) {
            readColumns(metaData, "COMMENTAIRES", details, 1);
        }

        String id = pickIdColumn(details);
        String posteId = pickPosteIdColumn(details, id);
        String contenu = pickContenuColumn(details, id, posteId);
        String createdAt = pickDateColumn(details, id, posteId, contenu);

        if (id == null) {
            throw new SQLException("Impossible de determiner la colonne identifiant de `commentaires`.");
        }
        if (posteId == null) {
            throw new SQLException("Impossible de determiner la colonne de liaison poste dans `commentaires`.");
        }
        if (contenu == null) {
            throw new SQLException("Impossible de determiner la colonne contenu de `commentaires`.");
        }

        return new ColumnMapping(id, posteId, contenu, createdAt);
    }

    private boolean readColumns(DatabaseMetaData metaData,
                                String tableName,
                                List<ColumnInfo> details,
                                int startOrdinal) throws SQLException {
        boolean foundAny = false;
        int ordinal = startOrdinal;

        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                foundAny = true;
                String name = rs.getString("COLUMN_NAME");
                String type = normalizeType(rs.getString("TYPE_NAME"));
                String auto = safeGet(rs, "IS_AUTOINCREMENT");

                details.add(new ColumnInfo(
                        name,
                        type.toLowerCase(),
                        ordinal,
                        "",
                        auto.equalsIgnoreCase("YES") ? "auto_increment" : ""
                ));
                ordinal++;
            }
        }

        try (ResultSet pk = metaData.getPrimaryKeys(null, null, tableName)) {
            while (pk.next()) {
                String pkName = pk.getString("COLUMN_NAME");
                for (int i = 0; i < details.size(); i++) {
                    ColumnInfo c = details.get(i);
                    if (c.name.equalsIgnoreCase(pkName)) {
                        details.set(i, new ColumnInfo(c.name, c.dataType, c.ordinal, "pri", c.extra));
                        break;
                    }
                }
            }
        }

        return foundAny;
    }

    private String pickIdColumn(List<ColumnInfo> details) {
        Optional<ColumnInfo> byName = details.stream()
                .filter(c -> isOneOf(c.name, "id", "commentaire_id", "id_commentaire"))
                .findFirst();
        if (byName.isPresent()) {
            return byName.get().name;
        }

        return details.stream()
                .filter(c -> NUMERIC_TYPES.contains(c.dataType))
                .filter(c -> c.extra.contains("auto_increment") || "pri".equals(c.key))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .or(() -> details.stream()
                        .filter(c -> NUMERIC_TYPES.contains(c.dataType))
                        .min(Comparator.comparingInt(c -> c.ordinal)))
                .map(c -> c.name)
                .orElse(null);
    }

    private String pickPosteIdColumn(List<ColumnInfo> details, String id) {
        Optional<ColumnInfo> byName = details.stream()
                .filter(c -> isOneOf(c.name, "poste_id", "post_id", "id_poste"))
                .findFirst();
        if (byName.isPresent()) {
            return byName.get().name;
        }

        return details.stream()
                .filter(c -> NUMERIC_TYPES.contains(c.dataType))
                .filter(c -> !c.name.equalsIgnoreCase(id))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .map(c -> c.name)
                .orElse(null);
    }

    private String pickContenuColumn(List<ColumnInfo> details, String id, String posteId) {
        Optional<ColumnInfo> byName = details.stream()
                .filter(c -> isOneOf(c.name, "contenu", "content", "texte", "description", "message", "body"))
                .findFirst();
        if (byName.isPresent()) {
            return byName.get().name;
        }

        return details.stream()
                .filter(c -> TEXT_TYPES.contains(c.dataType))
                .filter(c -> !c.name.equalsIgnoreCase(id))
                .filter(c -> !c.name.equalsIgnoreCase(posteId))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .map(c -> c.name)
                .orElse(null);
    }

    private String pickDateColumn(List<ColumnInfo> details, String id, String posteId, String contenu) {
        Optional<ColumnInfo> byName = details.stream()
                .filter(c -> isOneOf(c.name, "created_at", "date_creation", "date_commentaire", "createdat"))
                .findFirst();
        if (byName.isPresent()) {
            return byName.get().name;
        }

        return details.stream()
                .filter(c -> DATE_TYPES.contains(c.dataType))
                .filter(c -> !c.name.equalsIgnoreCase(id))
                .filter(c -> !c.name.equalsIgnoreCase(posteId))
                .filter(c -> !c.name.equalsIgnoreCase(contenu))
                .min(Comparator.comparingInt(c -> c.ordinal))
                .map(c -> c.name)
                .orElse(null);
    }

    private List<Commentaire> readCommentaires(ResultSet rs) throws SQLException {
        List<Commentaire> result = new ArrayList<>();
        while (rs.next()) {
            result.add(readCommentaire(rs));
        }
        return result;
    }

    private Commentaire readCommentaire(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long posteId = rs.getLong("poste_id");
        String contenu = rs.getString("contenu");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts == null ? null : ts.toLocalDateTime();
        return new Commentaire(id, posteId, contenu == null ? "" : contenu, createdAt);
    }

    private boolean isOneOf(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String q(String column) {
        return "`" + column + "`";
    }

    private String normalizeType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return "";
        }
        String lower = rawType.toLowerCase();
        int paren = lower.indexOf('(');
        return paren >= 0 ? lower.substring(0, paren) : lower;
    }

    private String safeGet(ResultSet rs, String column) {
        try {
            String value = rs.getString(column);
            return value == null ? "" : value;
        } catch (SQLException ignored) {
            return "";
        }
    }
}
