package com.aa.server.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static HikariDataSource dataSource;

    private DatabaseManager() {}

    public static synchronized void init() {
        if (dataSource != null) return;
        String url = System.getProperty("DB_URL", "jdbc:sqlite:shooter.db");
        String user = System.getProperty("DB_USER", "");
        String password = System.getProperty("DB_PASSWORD", "");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(30_000);
        config.setMaxLifetime(600_000);

        if (url.startsWith("jdbc:sqlite:")) {
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("foreign_keys", "ON");
            // SQLite no usa usuario/contraseña
            config.setUsername(null);
            config.setPassword(null);
        } else {
            config.setMaximumPoolSize(10);
            config.setConnectionTestQuery("SELECT 1");
        }

        dataSource = new HikariDataSource(config);
        initSchema();
    }

    public static synchronized void initForTest() {
        System.setProperty("DB_URL", "jdbc:sqlite::memory:");
        dataSource = null;
        init();
    }

    public static HikariDataSource getDataSource() {
        if (dataSource == null) init();
        return dataSource;
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static void initSchema() {
        try (Connection conn = getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL,
                    user_id TEXT NOT NULL UNIQUE,
                    created_at TEXT NOT NULL DEFAULT (datetime('now'))
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database schema", e);
        }
    }

}
