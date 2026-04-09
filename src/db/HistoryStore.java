package db;

import model.AnalysisRecord;
import utils.FileStore;

import java.sql.*;
import java.util.*;

/** CRUD for the analysis_history table with CSV fallback. */
public class HistoryStore {

    private static final String[] CSV_HEADERS =
        {"username", "timestamp", "company_name", "offer_type", "risk_score", "nlp_score", "verdict"};

    private final DatabaseManager db;
    private final FileStore       fileStore;

    public HistoryStore(DatabaseManager db, FileStore fileStore) {
        this.db        = db;
        this.fileStore = fileStore;
    }

    /** Saves an analysis record to DB and CSV. */
    public void save(AnalysisRecord record) {
        if (db.isAvailable()) {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "INSERT INTO analysis_history(username,timestamp,company_name,offer_type,risk_score,nlp_score,verdict)" +
                    " VALUES(?,?,?,?,?,?,?)")) {
                ps.setString(1, record.getUsername());
                ps.setString(2, record.getTimestamp());
                ps.setString(3, record.getCompanyName());
                ps.setString(4, record.getOfferType());
                ps.setInt   (5, record.getRiskScore());
                ps.setInt   (6, record.getNlpScore());
                ps.setString(7, record.getVerdict());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[HistoryStore] DB save failed: " + e.getMessage());
            }
        } else {
            System.err.println("[HistoryStore] DB unavailable — writing to CSV only.");
        }
        fileStore.appendCsv(FileStore.HISTORY_PATH, CSV_HEADERS, new String[]{
            record.getUsername(), record.getTimestamp(), record.getCompanyName(),
            record.getOfferType(), String.valueOf(record.getRiskScore()),
            String.valueOf(record.getNlpScore()), record.getVerdict()
        });
    }

    /** Returns all records for a user, ordered by timestamp descending. */
    public List<AnalysisRecord> findByUsername(String username) {
        if (db.isAvailable()) {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT id,username,timestamp,company_name,offer_type,risk_score,nlp_score,verdict" +
                    " FROM analysis_history WHERE username=? ORDER BY timestamp DESC")) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                List<AnalysisRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new AnalysisRecord(
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5),
                        rs.getInt(6), rs.getInt(7), rs.getString(8)));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[HistoryStore] DB query failed: " + e.getMessage());
            }
        } else {
            System.err.println("[HistoryStore] DB unavailable — reading from CSV.");
            return findInCsv(username);
        }
        return Collections.emptyList();
    }

    // ── CSV fallback ───────────────────────────────────────────────

    private List<AnalysisRecord> findInCsv(String username) {
        List<AnalysisRecord> list = new ArrayList<>();
        List<String[]> rows = fileStore.readCsv(FileStore.HISTORY_PATH);
        for (String[] row : rows) {
            if (row.length >= 7 && row[0].equals(username)) {
                try {
                    list.add(new AnalysisRecord(
                        row[0], row[1], row[2], row[3],
                        Integer.parseInt(row[4]), Integer.parseInt(row[5]), row[6]));
                } catch (NumberFormatException ignored) {}
            }
        }
        // Sort descending by timestamp string (ISO-8601 sorts lexicographically)
        list.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return list;
    }
}
