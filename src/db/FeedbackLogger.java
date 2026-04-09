package db;

import model.FeedbackEvent;

import java.sql.*;

/** Appends feedback events to the feedback_log table. */
public class FeedbackLogger {

    private final DatabaseManager db;

    public FeedbackLogger(DatabaseManager db) {
        this.db = db;
    }

    /** Logs a feedback event. Silently skips if DB is unavailable. */
    public void log(FeedbackEvent event) {
        if (!db.isAvailable()) {
            System.err.println("[FeedbackLogger] DB unavailable — feedback event not logged.");
            return;
        }
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO feedback_log(username,timestamp,offer_description_hash," +
                "system_verdict,user_verdict,phrases_extracted) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, event.getUsername());
            ps.setString(2, event.getTimestamp());
            ps.setString(3, event.getOfferDescriptionHash());
            ps.setString(4, event.getSystemVerdict());
            ps.setString(5, event.getUserVerdict());
            ps.setString(6, event.getPhrasesExtracted());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FeedbackLogger] log failed: " + e.getMessage());
        }
    }
}
