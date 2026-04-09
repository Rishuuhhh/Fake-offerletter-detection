package db;

import model.LearnedVocabEntry;
import utils.FileStore;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** CRUD for the learned_vocabulary table with CSV fallback. */
public class VocabularyStore {

    private static final String[] CSV_HEADERS = {"phrase", "category", "weight", "source", "updated_at"};

    private final DatabaseManager db;
    private final FileStore       fileStore;

    public VocabularyStore(DatabaseManager db, FileStore fileStore) {
        this.db        = db;
        this.fileStore = fileStore;
    }

    /**
     * Inserts a new phrase or increments the weight of an existing one.
     * Also syncs learned_vocab.csv.
     */
    public void upsert(LearnedVocabEntry entry) {
        String now = LocalDateTime.now().toString();
        entry.setUpdatedAt(now);

        if (db.isAvailable()) {
            try {
                // Insert if phrase doesn't exist yet (weight = 0 placeholder, updated below)
                try (PreparedStatement ins = db.getConnection().prepareStatement(
                        "INSERT OR IGNORE INTO learned_vocabulary(phrase,category,weight,source,updated_at)" +
                        " VALUES(?,?,0,?,?)")) {
                    ins.setString(1, entry.getPhrase());
                    ins.setString(2, entry.getCategory());
                    ins.setString(3, entry.getSource());
                    ins.setString(4, now);
                    ins.executeUpdate();
                }
                // Increment weight for the phrase (works for both new and existing rows)
                try (PreparedStatement upd = db.getConnection().prepareStatement(
                        "UPDATE learned_vocabulary SET weight = weight + ?, updated_at = ? WHERE phrase = ?")) {
                    upd.setInt   (1, entry.getWeight());
                    upd.setString(2, now);
                    upd.setString(3, entry.getPhrase());
                    upd.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("[VocabularyStore] upsert failed: " + e.getMessage());
            }
        } else {
            System.err.println("[VocabularyStore] DB unavailable — writing to CSV only.");
        }

        // Sync CSV: rewrite entire file
        syncCsv();
    }

    /** Loads all learned vocabulary entries. DB first; CSV fallback. */
    public List<LearnedVocabEntry> loadAll() {
        if (db.isAvailable()) {
            try (Statement st = db.getConnection().createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT id, phrase, category, weight, source, updated_at FROM learned_vocabulary")) {
                List<LearnedVocabEntry> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new LearnedVocabEntry(
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getString(5), rs.getString(6)));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[VocabularyStore] loadAll DB failed: " + e.getMessage());
            }
        } else {
            System.err.println("[VocabularyStore] DB unavailable — reading from CSV.");
            return loadFromCsv();
        }
        return Collections.emptyList();
    }

    /** Counts entries with source='feedback'. */
    public int countFeedbackEntries() {
        if (db.isAvailable()) {
            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM learned_vocabulary WHERE source='feedback'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
                System.err.println("[VocabularyStore] countFeedbackEntries failed: " + e.getMessage());
            }
        }
        return loadFromCsv().size(); // approximate fallback
    }

    // ── CSV helpers ────────────────────────────────────────────────

    private void syncCsv() {
        List<LearnedVocabEntry> all = loadAll();
        List<String[]> rows = new ArrayList<>();
        for (LearnedVocabEntry e : all) {
            rows.add(new String[]{e.getPhrase(), e.getCategory(),
                String.valueOf(e.getWeight()), e.getSource(), e.getUpdatedAt()});
        }
        fileStore.rewriteCsv(FileStore.VOCAB_PATH, CSV_HEADERS, rows);
    }

    private List<LearnedVocabEntry> loadFromCsv() {
        List<LearnedVocabEntry> list = new ArrayList<>();
        List<String[]> rows = fileStore.readCsv(FileStore.VOCAB_PATH);
        for (String[] row : rows) {
            if (row.length >= 5) {
                try {
                    list.add(new LearnedVocabEntry(0, row[0], row[1],
                        Integer.parseInt(row[2]), row[3], row[4]));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }
}
