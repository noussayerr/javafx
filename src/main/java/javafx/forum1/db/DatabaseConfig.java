package javafx.forum1.db;

/**
 * Centralise la configuration DB (surchageable via variables d'environnement).
 */
public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/pi_projet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConfig() {
    }

    public static String url() {
        return envOrDefault("DB_URL", DEFAULT_URL);
    }

    public static String user() {
        return envOrDefault("DB_USER", DEFAULT_USER);
    }

    public static String password() {
        return envOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

