package model;

// saved analysis result
public class AnalysisRecord {

    private final String username;
    private final String companyName;
    private final String offerType;
    private final int riskScore;
    private final int nlpScore;
    private final String verdict;

    public AnalysisRecord(String username, String companyName, String offerType, int riskScore, int nlpScore, String verdict) {
        this.username = username;
        this.companyName = companyName;
        this.offerType = offerType;
        this.riskScore = riskScore;
        this.nlpScore = nlpScore;
        this.verdict = verdict;
    }

    public String getUsername() { 
        return username; 
    }
    
    public String getCompanyName() { 
        return companyName; 
    }
    
    public String getOfferType() { 
        return offerType; 
    }
    
    public int getRiskScore() { 
        return riskScore; 
    }
    
    public int getNlpScore() { 
        return nlpScore; 
    }
    
    public String getVerdict() { 
        return verdict; 
    }
}
