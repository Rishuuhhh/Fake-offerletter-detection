package model;

/** Immutable record of a single offer analysis session. */
public class AnalysisRecord {
    private final int    id;          // 0 if not yet persisted
    private final String username;
    private final String timestamp;   // ISO-8601
    private final String companyName;
    private final String offerType;
    private final int    riskScore;
    private final int    nlpScore;
    private final String verdict;     // FAKE / SUSPICIOUS / GENUINE

    public AnalysisRecord(String username, String timestamp, String companyName,
                          String offerType, int riskScore, int nlpScore, String verdict) {
        this(0, username, timestamp, companyName, offerType, riskScore, nlpScore, verdict);
    }

    public AnalysisRecord(int id, String username, String timestamp, String companyName,
                          String offerType, int riskScore, int nlpScore, String verdict) {
        this.id          = id;
        this.username    = username;
        this.timestamp   = timestamp;
        this.companyName = companyName;
        this.offerType   = offerType;
        this.riskScore   = riskScore;
        this.nlpScore    = nlpScore;
        this.verdict     = verdict;
    }

    public int    getId()          { return id; }
    public String getUsername()    { return username; }
    public String getTimestamp()   { return timestamp; }
    public String getCompanyName() { return companyName; }
    public String getOfferType()   { return offerType; }
    public int    getRiskScore()   { return riskScore; }
    public int    getNlpScore()    { return nlpScore; }
    public String getVerdict()     { return verdict; }
}
