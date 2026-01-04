package cl.myconstruction.app.config;

import java.util.Optional;

public record AppConfig(int serverPort, DatabaseConfig databaseConfig) {
    public static AppConfig fromSystem() {
        int port = Integer.parseInt(get("APP_PORT").orElse("8080"));
        DatabaseConfig db = new DatabaseConfig(
                get("DB_URL").orElse("jdbc:mariadb://localhost:3306/my_construction"),
                get("DB_USER").orElse("root"),
                get("DB_PASSWORD").orElse("")
        );
        return new AppConfig(port, db);
    }

    private static Optional<String> get(String key) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return Optional.of(env.trim());
        }
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return Optional.of(prop.trim());
        }
        return Optional.empty();
    }
}

