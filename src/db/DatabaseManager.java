package db;

import java.io.File;
import java.sql.*;

/**
 * Manages the SQLite connection and auto-creates the schema on first run.
 * All other DB classes obtain their Connection through this class.
 */
public class DatabaseManager {

    private static final String DB_PATH = "data/fakeofferdetector.db";

    private Connection connection;
    private boolean    available = false;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            new File("data").mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            connection.setAutoCommit(true);
            available = true;
            initSchema();
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] sqlite-jdbc driver not found on classpath. Place sqlite-jdbc-3.x.x.jar in lib/. Falling back to CSV-only mode.");
        } catch (SQLException e) {
            System.err.println("[DB] Cannot open database: " + e.getMessage() + ". Falling back to CSV-only mode.");
        }
    }

    public Connection getConnection() { return connection; }
    public boolean    isAvailable()   { return available; }

    public void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }

    // ── Schema ─────────────────────────────────────────────────────

    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {

            // users
            st.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username      TEXT    NOT NULL UNIQUE," +
                "  password_hash TEXT    NOT NULL," +
                "  salt          TEXT    NOT NULL," +
                "  created_at    TEXT    NOT NULL" +
                ")");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username)");

            // analysis_history
            st.execute(
                "CREATE TABLE IF NOT EXISTS analysis_history (" +
                "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username     TEXT    NOT NULL," +
                "  timestamp    TEXT    NOT NULL," +
                "  company_name TEXT    NOT NULL," +
                "  offer_type   TEXT    NOT NULL," +
                "  risk_score   INTEGER NOT NULL," +
                "  nlp_score    INTEGER NOT NULL," +
                "  verdict      TEXT    NOT NULL" +
                ")");
            st.execute("CREATE INDEX IF NOT EXISTS idx_history_username ON analysis_history(username)");

            // seed_offers
            st.execute(
                "CREATE TABLE IF NOT EXISTS seed_offers (" +
                "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  company_name TEXT    NOT NULL," +
                "  description  TEXT    NOT NULL," +
                "  verdict      TEXT    NOT NULL DEFAULT 'FAKE'," +
                "  seeded_at    TEXT    NOT NULL" +
                ")");

            // learned_vocabulary
            st.execute(
                "CREATE TABLE IF NOT EXISTS learned_vocabulary (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  phrase     TEXT    NOT NULL UNIQUE," +
                "  category   TEXT    NOT NULL CHECK(category IN ('fraud','genuine'))," +
                "  weight     INTEGER NOT NULL," +
                "  source     TEXT    NOT NULL DEFAULT 'feedback'," +
                "  updated_at TEXT    NOT NULL" +
                ")");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_vocab_phrase ON learned_vocabulary(phrase)");

            // feedback_log
            st.execute(
                "CREATE TABLE IF NOT EXISTS feedback_log (" +
                "  id                     INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username               TEXT    NOT NULL," +
                "  timestamp              TEXT    NOT NULL," +
                "  offer_description_hash TEXT    NOT NULL," +
                "  system_verdict         TEXT    NOT NULL," +
                "  user_verdict           TEXT    NOT NULL," +
                "  phrases_extracted      TEXT    NOT NULL" +
                ")");
        }
    }
}
