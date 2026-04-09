package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VerificationResult {
    private final int riskScore;
    private final int nlpRisk;
    private final String verdict;
    private final List<String> findings;

    public VerificationResult(int riskScore, int nlpRisk, String verdict, List<String> findings) {
        this.riskScore = Math.max(0, Math.min(riskScore, 100));
        this.nlpRisk   = Math.max(0, Math.min(nlpRisk, 100));
        this.verdict   = verdict;
        this.findings  = Collections.unmodifiableList(new ArrayList<>(findings));
    }

    public int         getRiskScore() { return riskScore; }
    public int         getNlpRisk()   { return nlpRisk; }
    public String      getVerdict()   { return verdict; }
    public List<String> getFindings() { return findings; }
}
