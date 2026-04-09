package db;

import model.User;
import utils.FileStore;

import java.sql.*;
import java.util.List;

/** CRUD for the users table with CSV fallback. */
public class UserStore {

    private static final String[] CSV_HEADERS = {"username", "password_hash", "salt", "created_at"};

    private final DatabaseManager db;
    private final FileStore       fileStore;

    public UserStore(DatabaseManager db, FileStore fileStore) {
        this.db        = db;
        this.fileStore = fileStore;
    }

    /** Persists a new user to DB (primary) and CSV (fallback/backup). */
    public void save(User user) {
        if (db.isAvailable()) {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "INSERT INTO users(username, password_hash, salt, created_at) VALUES(?,?,?,?)")) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getSalt());
                ps.setString(4, user.getCreatedAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[UserStore] DB save failed: " + e.getMessage());
            }
        } else {
            System.err.println("[UserStore] DB unavailable — writing to CSV only.");
        }
        // Always write to CSV as backup
        fileStore.appendCsv(FileStore.CREDENTIALS_PATH, CSV_HEADERS,
            new String[]{user.getUsername(), user.getPasswordHash(), user.getSalt(), user.getCreatedAt()});
    }

    /** Finds a user by username. DB first; falls back to CSV if DB unavailable. */
    public User findByUsername(String username) {
        if (db.isAvailable()) {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT username, password_hash, salt, created_at FROM users WHERE username = ?")) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                }
            } catch (SQLException e) {
                System.err.println("[UserStore] DB query failed: " + e.getMessage());
            }
        } else {
            System.err.println("[UserStore] DB unavailable — reading from CSV.");
            return findInCsv(username);
        }
        return null;
    }

    /** Returns true if the username already exists. */
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    // ── CSV fallback ───────────────────────────────────────────────

    private User findInCsv(String username) {
        List<String[]> rows = fileStore.readCsv(FileStore.CREDENTIALS_PATH);
        for (String[] row : rows) {
            if (row.length >= 4 && row[0].equals(username)) {
                return new User(row[0], row[1], row[2], row[3]);
            }
        }
        return null;
    }
}
