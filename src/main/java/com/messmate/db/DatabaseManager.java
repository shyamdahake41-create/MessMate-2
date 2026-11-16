package com.messmate.db;

import com.messmate.util.PasswordHasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private static final String[] SCHEMA = {
            """
            CREATE TABLE IF NOT EXISTS admins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS members (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL,
                joining_date TEXT NOT NULL,
                monthly_fee REAL NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS memberships (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                member_id INTEGER NOT NULL,
                start_date TEXT NOT NULL,
                expiry_date TEXT NOT NULL,
                monthly_fee REAL NOT NULL,
                FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                receipt_number TEXT NOT NULL UNIQUE,
                member_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                payment_date TEXT NOT NULL,
                payment_month TEXT NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                UNIQUE (member_id, payment_month)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS receipt_counter (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                last_value INTEGER NOT NULL
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_members_name ON members(name)",
            "CREATE INDEX IF NOT EXISTS idx_members_phone ON members(phone)",
            "CREATE INDEX IF NOT EXISTS idx_memberships_member ON memberships(member_id)",
            "CREATE INDEX IF NOT EXISTS idx_payments_member ON payments(member_id)"
    };

    private static String jdbcUrl;

    private DatabaseManager() {
    }

    public static Path defaultDatabasePath() {
        return Path.of(System.getProperty("user.dir"), "data", "messmate.db");
    }

    public static synchronized void initialize(Path databaseFile) throws SQLException {
        try {
            Path parent = databaseFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException ex) {
            throw new SQLException("Could not create the database folder.", ex);
        }
        jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQLite JDBC driver is not on the classpath.", ex);
        }
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String sql : SCHEMA) {
                    statement.execute(sql);
                }
            }
            seed(connection);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (jdbcUrl == null) {
            throw new SQLException("Database is not initialized.");
        }
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    public static String like(String query) {
        String trimmed = query == null ? "" : query.trim();
        String escaped = trimmed
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private static void seed(Connection connection) throws SQLException {
        try (PreparedStatement admin = connection.prepareStatement("""
                INSERT INTO admins(username, password_hash)
                SELECT ?, ?
                WHERE NOT EXISTS (SELECT 1 FROM admins WHERE username = ?)
                """)) {
            admin.setString(1, "admin");
            admin.setString(2, PasswordHasher.hash("admin123"));
            admin.setString(3, "admin");
            admin.executeUpdate();
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO receipt_counter(id, last_value)
                    SELECT 1, 1000
                    WHERE NOT EXISTS (SELECT 1 FROM receipt_counter WHERE id = 1)
                    """);
        }
    }
}
