package com.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA busy_timeout = 5000");
                stmt.execute("PRAGMA journal_mode = WAL");
            }
        }
        return connection;
    }

    public static void initialize() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  global_id   TEXT    NOT NULL UNIQUE," +
                "  name        TEXT    NOT NULL," +
                "  email       TEXT    UNIQUE," +
                "  phone       TEXT," +
                "  role        TEXT    NOT NULL DEFAULT 'student'," +
                "  password    TEXT    NOT NULL," +
                "  joined_date TEXT    DEFAULT (date('now'))," +
                "  status      TEXT    DEFAULT 'active'" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS books (" +
                "  id               INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title            TEXT    NOT NULL," +
                "  author           TEXT    NOT NULL," +
                "  isbn             TEXT    UNIQUE," +
                "  genre            TEXT," +
                "  publisher        TEXT," +
                "  year             INTEGER," +
                "  total_copies     INTEGER DEFAULT 1," +
                "  available_copies INTEGER DEFAULT 1" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS borrowings (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  book_id     INTEGER NOT NULL," +
                "  user_id     INTEGER NOT NULL," +
                "  borrow_date TEXT    NOT NULL DEFAULT (date('now'))," +
                "  due_date    TEXT    NOT NULL," +
                "  return_date TEXT," +
                "  fine        REAL    DEFAULT 0.0," +
                "  status      TEXT    DEFAULT 'active'," +
                "  FOREIGN KEY (book_id) REFERENCES books(id)," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_borrowings_active_book_user " +
                "ON borrowings(book_id, user_id) WHERE status='active'"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS fines (" +
                "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  borrowing_id INTEGER NOT NULL," +
                "  user_id      INTEGER NOT NULL," +
                "  amount       REAL    DEFAULT 0.0," +
                "  paid         INTEGER DEFAULT 0," +
                "  due_date     TEXT," +
                "  paid_date    TEXT," +
                "  FOREIGN KEY (borrowing_id) REFERENCES borrowings(id)," +
                "  FOREIGN KEY (user_id)      REFERENCES users(id)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reports (" +
                "  id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  month          TEXT    NOT NULL," +
                "  generated_at   TEXT    DEFAULT (datetime('now'))," +
                "  total_issued   INTEGER DEFAULT 0," +
                "  total_returned INTEGER DEFAULT 0," +
                "  total_fines    REAL    DEFAULT 0.0," +
                "  content        TEXT" +
                ")"
            );

            seedAdminUser(stmt);
            System.out.println("[DB] Initialized successfully.");

        } catch (SQLException e) {
            System.err.println("[DB] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedAdminUser(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role='admin'");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.executeUpdate(
                "INSERT INTO users (global_id, name, email, role, password, status) " +
                "VALUES ('ADMIN001','Administrator','admin@library.com','admin','admin123','active')"
            );
            System.out.println("[DB] Default admin created - ID: ADMIN001  Password: admin123");
        }
    }

    public static boolean resetDatabase() {
        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM fines");
                stmt.executeUpdate("DELETE FROM borrowings");
                stmt.executeUpdate("DELETE FROM reports");
                stmt.executeUpdate("DELETE FROM books");
                stmt.executeUpdate("DELETE FROM users WHERE role!='admin'");
                stmt.executeUpdate(
                    "DELETE FROM sqlite_sequence WHERE name IN ('fines','borrowings','reports','books','users')");
                connection.commit();
                connection.setAutoCommit(originalAutoCommit);
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[DB] Reset: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
