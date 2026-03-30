package service;

import java.util.ArrayList;
import java.util.List;

public class NlpSignalAnalyzer {

    public static final class NlpAnalysis {
        private final int risk;
        private final List<String> notes;

        public NlpAnalysis(int risk, List<String> notes) {
            this.risk = Math.max(0, Math.min(risk, 100));
            this.notes = notes;
        }

        public int getRisk() {
            return risk;
        }

        public List<String> getNotes() {
            return notes;
        }
    }

    private static final String[] FRAUD_HEAVY_PHRASES = {
            "registration fee", "security deposit", "processing fee", "pay now", "instant joining",
            "guaranteed placement", "confirm seat now", "otp", "bank details", "aadhaar"
    };

    private static final String[] FRAUD_LIGHT_PHRASES = {
            "limited seats", "immediate response", "selected candidate", "act now",
            "work from home", "no interview", "no experience required"
    };

    private static final String[] LEGIT_CONTEXT_PHRASES = {
            "official", "selection process", "interview rounds", "company website",
            "offer letter", "human resources", "internship duration", "job description"
    };

    private static final String[] RUSH_WORDS = {
            "urgent", "hurry", "today", "immediately", "deadline", "last chance"
    };

    private static final String[] PAYMENT_CUES = {
            "payment", "deposit", "fee", "charges", "transfer", "upi"
    };

    public static NlpAnalysis analyze(String text) {
        String cleanedText = normalize(text);
        List<String> analysisNotes = new ArrayList<>();
        int nlpScore = 0;

        if (cleanedText.isEmpty()) {
            analysisNotes.add("Description missing: NLP checks were limited.");
            return new NlpAnalysis(5, analysisNotes);
        }

        int heavyPhraseHits = countPhraseHits(cleanedText, FRAUD_HEAVY_PHRASES);
        int lightPhraseHits = countPhraseHits(cleanedText, FRAUD_LIGHT_PHRASES);
        int legitPhraseHits = countPhraseHits(cleanedText, LEGIT_CONTEXT_PHRASES);
        int rushWordHits = countPhraseHits(cleanedText, RUSH_WORDS);
        int paymentCueHits = countPhraseHits(cleanedText, PAYMENT_CUES);

        nlpScore += heavyPhraseHits * 12;
        nlpScore += lightPhraseHits * 6;
        nlpScore += Math.min(rushWordHits, 4) * 4;
        nlpScore += Math.min(paymentCueHits, 4) * 3;
        nlpScore -= Math.min(legitPhraseHits, 3) * 4;

        if (heavyPhraseHits > 0) {
            analysisNotes.add("NLP: High-risk scam phrases detected: " + heavyPhraseHits + ".");
        }
        if (lightPhraseHits > 0) {
            analysisNotes.add("NLP: Medium-risk persuasion phrases detected: " + lightPhraseHits + ".");
        }
        if (rushWordHits >= 2) {
            analysisNotes.add("NLP: Pressure language intensity appears elevated.");
        }
        if (paymentCueHits >= 2) {
            analysisNotes.add("NLP: Financial request language appears repeatedly.");
        }
        if (legitPhraseHits >= 2) {
            analysisNotes.add("NLP: Some legitimate recruitment context signals were found.");
        }

        if (nlpScore < 10 && analysisNotes.isEmpty()) {
            analysisNotes.add("NLP: Text tone appears mostly neutral/professional.");
        }

        return new NlpAnalysis(Math.max(0, Math.min(nlpScore, 100)), analysisNotes);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static int countPhraseHits(String text, String[] phrases) {
        int hits = 0;
        for (String phrase : phrases) {
            if (text.contains(phrase)) {
                hits++;
            }
        }
        return hits;
    }
}