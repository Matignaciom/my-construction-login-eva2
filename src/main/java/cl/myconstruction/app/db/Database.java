package cl.myconstruction.app.db;

import cl.myconstruction.app.config.DatabaseConfig;
import cl.myconstruction.app.security.PasswordHasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public final class Database {
    public static final String CTX_KEY = "myconstruction.db";

    private final DatabaseConfig config;

    private Database(DatabaseConfig config) {
        this.config = config;
    }

    public static Database create(DatabaseConfig config) {
        return new Database(config);
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    public void ensureSchema() throws SQLException {
        String ddl = """
                CREATE TABLE IF NOT EXISTS users (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  email VARCHAR(255) NOT NULL UNIQUE,
                  password_hash VARBINARY(64) NOT NULL,
                  password_salt VARBINARY(16) NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection c = openConnection(); Statement st = c.createStatement()) {
            st.execute(ddl);
        }
    }

    public void ensureDemoUser() throws SQLException {
        String demoEmail = Optional.ofNullable(System.getenv("DEMO_EMAIL")).filter(s -> !s.isBlank()).orElse("demo@myconstruction.cl");
        String demoPassword = Optional.ofNullable(System.getenv("DEMO_PASSWORD")).filter(s -> !s.isBlank()).orElse("Demo1234!");
        if (findUserByEmail(demoEmail).isPresent()) {
            return;
        }
        PasswordHasher.Hash hash = PasswordHasher.hash(demoPassword);
        String sql = "INSERT INTO users (email, password_hash, password_salt) VALUES (?, ?, ?)";
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, demoEmail);
            ps.setBytes(2, hash.hash());
            ps.setBytes(3, hash.salt());
            ps.executeUpdate();
        }
    }

    public Optional<User> findUserByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, password_hash, password_salt FROM users WHERE email = ?";
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getBytes("password_hash"),
                        rs.getBytes("password_salt")
                );
                return Optional.of(user);
            }
        }
    }
}

