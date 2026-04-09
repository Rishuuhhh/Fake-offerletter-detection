package model;

// user feedback event
public class FeedbackEvent {

    private final String username;
    private final String offerDescriptionHash;
    private final String systemVerdict;
    private final String userVerdict;
    private final String phrasesExtracted;

    public FeedbackEvent(String username, String offerDescriptionHash, String systemVerdict, String userVerdict, String phrasesExtracted) {
        this.username = username;
        this.offerDescriptionHash = offerDescriptionHash;
        this.systemVerdict = systemVerdict;
        this.userVerdict = userVerdict;
        this.phrasesExtracted = phrasesExtracted;
    }

    public String getUsername() { 
        return username; 
    }
    
    public String getOfferDescriptionHash() { 
        return offerDescriptionHash; 
    }
    
    public String getSystemVerdict() { 
        return systemVerdict; 
    }
    
    public String getUserVerdict() { 
        return userVerdict; 
    }
    
    public String getPhrasesExtracted() { 
        return phrasesExtracted; 
    }
}
