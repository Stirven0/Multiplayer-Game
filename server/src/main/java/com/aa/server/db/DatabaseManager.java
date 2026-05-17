package com.aa.server.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    user_id TEXT PRIMARY KEY,
                    total_kills INTEGER NOT NULL DEFAULT 0,
                    total_deaths INTEGER NOT NULL DEFAULT 0,
                    total_wins INTEGER NOT NULL DEFAULT 0,
                    total_games INTEGER NOT NULL DEFAULT 0,
                    upgrade_points INTEGER NOT NULL DEFAULT 0
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database schema", e);
        }
    }

    public static void savePlayerStats(String userId, int kills, int deaths, boolean won) {
        String sql = """
            INSERT INTO player_stats (user_id, total_kills, total_deaths, total_wins, total_games, upgrade_points)
            VALUES (?, ?, ?, ?, 1, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                total_kills = total_kills + ?,
                total_deaths = total_deaths + ?,
                total_wins = total_wins + ?,
                total_games = total_games + 1,
                upgrade_points = upgrade_points + ?
        """;
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, kills);
            ps.setInt(3, deaths);
            ps.setInt(4, won ? 1 : 0);
            ps.setInt(5, kills); // upgrade points = kills for now
            ps.setInt(6, kills);
            ps.setInt(7, deaths);
            ps.setInt(8, won ? 1 : 0);
            ps.setInt(9, kills);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving player stats: " + e.getMessage());
        }
    }

    public static int[] loadPlayerStats(String userId) {
        String sql = "SELECT total_kills, total_deaths, total_wins, total_games, upgrade_points FROM player_stats WHERE user_id = ?";
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new int[] {
                    rs.getInt("total_kills"),
                    rs.getInt("total_deaths"),
                    rs.getInt("total_wins"),
                    rs.getInt("total_games"),
                    rs.getInt("upgrade_points")
                };
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading player stats: " + e.getMessage());
        }
        return new int[]{0, 0, 0, 0, 0};
    }

}
