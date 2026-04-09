package model;

/** Immutable record of a single user feedback submission. */
public class FeedbackEvent {
    private final int    id;
    private final String username;
    private final String timestamp;             // ISO-8601
    private final String offerDescriptionHash;  // SHA-256 of description text
    private final String systemVerdict;         // FAKE / SUSPICIOUS / GENUINE
    private final String userVerdict;           // FAKE / GENUINE (user's correction)
    private final String phrasesExtracted;      // comma-separated extracted phrases

    public FeedbackEvent(String username, String timestamp, String offerDescriptionHash,
                         String systemVerdict, String userVerdict, String phrasesExtracted) {
        this(0, username, timestamp, offerDescriptionHash, systemVerdict, userVerdict, phrasesExtracted);
    }

    public FeedbackEvent(int id, String username, String timestamp, String offerDescriptionHash,
                         String systemVerdict, String userVerdict, String phrasesExtracted) {
        this.id                   = id;
        this.username             = username;
        this.timestamp            = timestamp;
        this.offerDescriptionHash = offerDescriptionHash;
        this.systemVerdict        = systemVerdict;
        this.userVerdict          = userVerdict;
        this.phrasesExtracted     = phrasesExtracted;
    }

    public int    getId()                   { return id; }
    public String getUsername()             { return username; }
    public String getTimestamp()            { return timestamp; }
    public String getOfferDescriptionHash() { return offerDescriptionHash; }
    public String getSystemVerdict()        { return systemVerdict; }
    public String getUserVerdict()          { return userVerdict; }
    public String getPhrasesExtracted()     { return phrasesExtracted; }
}
