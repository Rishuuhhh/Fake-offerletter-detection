package service;

import db.VocabularyStore;
import model.LearnedVocabEntry;

import java.util.*;
import java.util.regex.*;

/**
 * Enhanced NLP signal analyzer with:
 * - 25+ high-risk fraud phrases (diminishing returns on repeats)
 * - 15+ medium-risk fraud phrases
 * - 15+ genuine-signal phrases
 * - Domain-spoofing regex detection
 * - Salary-promise regex detection
 * - Adaptive learned vocabulary from user feedback
 */
public class NlpSignalAnalyzer {

    // ── Static vocabulary ──────────────────────────────────────────

    private static final String[] FRAUD_HIGH_PHRASES = {
        "registration fee", "security deposit", "processing fee", "pay now", "instant joining",
        "guaranteed placement", "confirm seat now", "otp", "bank details", "aadhaar",
        "advance payment", "refundable deposit", "joining fee", "training fee", "kit charges",
        "id proof required immediately", "send money", "wire transfer", "western union", "money gram",
        "otp verification", "aadhaar number", "guaranteed placement", "pay registration fee",
        "pay security deposit", "pay processing fee", "pay joining fee", "pay training fee"
    };

    private static final String[] FRAUD_MEDIUM_PHRASES = {
        "limited seats", "immediate response", "selected candidate", "act now",
        "work from home", "no interview", "no experience required",
        "100% job guarantee", "earn from home", "part time income", "easy money",
        "no qualification needed", "apply immediately", "urgent requirement",
        "guaranteed job", "earn rs"
    };

    private static final String[] GENUINE_PHRASES = {
        "structured interview process", "background verification",
        "offer letter on company letterhead", "hr department", "official company website",
        "competitive salary package", "employee benefits", "probation period",
        "joining date confirmation", "reference check", "panel interview",
        "technical assessment", "onboarding process", "nda agreement", "performance review",
        "selection process", "interview rounds", "human resources", "internship duration"
    };

    private static final String[] RUSH_WORDS = {
        "urgent", "hurry", "today", "immediately", "deadline", "last chance", "asap", "right now"
    };

    private static final String[] PAYMENT_CUES = {
        "payment", "deposit", "fee", "charges", "transfer", "upi", "neft", "rtgs"
    };

    // Domain-spoofing: known company names followed by a hyphen/dot and extra domain parts
    private static final Pattern DOMAIN_SPOOF = Pattern.compile(
        "\\b(?:amazon|google|microsoft|infosys|tcs|wipro|flipkart|hdfc|icici|accenture|cognizant)" +
        "[\\-\\.][a-z0-9\\-\\.]+\\.(com|net|org|in)\\b"
    );

    // Salary-promise patterns
    private static final Pattern SALARY_PROMISE = Pattern.compile(
        "earn\\s+rs|guaranteed salary|salary of \\d+|stipend of \\d+|" +
        "get paid \\d+|income of \\d+|earn \\d+|rs\\.?\\s*\\d+\\s*per month"
    );

    // ── Learned vocabulary (loaded at startup, reloaded after feedback) ──

    private List<LearnedVocabEntry> learnedEntries  = Collections.emptyList();
    private int                     feedbackEntryCount = 0;

    // ── Inner result class ─────────────────────────────────────────

    public static final class NlpAnalysis {
        private final int          risk;
        private final List<String> notes;

        public NlpAnalysis(int risk, List<String> notes) {
            this.risk  = Math.max(0, Math.min(risk, 100));
            this.notes = notes;
        }

        public int          getRisk()  { return risk; }
        public List<String> getNotes() { return notes; }
    }

    // ── Public API ─────────────────────────────────────────────────

    /** Loads (or reloads) learned vocabulary from the VocabularyStore. */
    public void reloadLearnedVocabulary(VocabularyStore vs) {
        this.learnedEntries     = Collections.unmodifiableList(vs.loadAll());
        this.feedbackEntryCount = vs.countFeedbackEntries();
    }

    public NlpAnalysis analyze(String text) {
        String normalized = normalize(text);
        List<String> notes = new ArrayList<>();
        int score = 0;

        if (normalized.isEmpty()) {
            notes.add("NLP: Description missing — NLP checks were limited.");
            return new NlpAnalysis(5, notes);
        }

        // Short-text penalty
        int wordCount = normalized.split("\\s+").length;
        if (wordCount < 20) {
            score += 10;
            notes.add("NLP: Offer description is unusually short; limited analysis performed.");
        }

        // High-risk phrases with diminishing returns
        int highHits = 0;
        for (String phrase : FRAUD_HIGH_PHRASES) {
            int occurrences = countOccurrences(normalized, phrase);
            if (occurrences >= 1) { score += 15; highHits++; }
            if (occurrences >= 2) { score += 7; }  // 50% of 15, rounded
            // 3+ occurrences: no additional points
        }
        if (highHits > 0) notes.add("NLP: High-risk scam phrases detected: " + highHits + ".");

        // Medium-risk phrases
        int medHits = 0;
        for (String phrase : FRAUD_MEDIUM_PHRASES) {
            if (normalized.contains(phrase)) { score += 7; medHits++; }
        }
        if (medHits > 0) notes.add("NLP: Medium-risk persuasion phrases detected: " + medHits + ".");

        // Genuine signals
        int genuineHits = 0;
        for (String phrase : GENUINE_PHRASES) {
            if (normalized.contains(phrase)) { score -= 6; genuineHits++; }
        }
        if (genuineHits >= 2) notes.add("NLP: Some legitimate recruitment context signals were found.");

        // Rush words (capped at 5 hits × +5)
        int rushHits = 0;
        for (String word : RUSH_WORDS) {
            if (normalized.contains(word)) rushHits++;
        }
        score += Math.min(rushHits, 5) * 5;
        if (rushHits >= 2) notes.add("NLP: Pressure language intensity appears elevated.");

        // Payment cues (capped at 5 hits × +4)
        int payHits = 0;
        for (String cue : PAYMENT_CUES) {
            if (normalized.contains(cue)) payHits++;
        }
        score += Math.min(payHits, 5) * 4;
        if (payHits >= 2) notes.add("NLP: Financial request language appears repeatedly.");

        // Domain-spoofing regex (capped at 1 hit, +20)
        if (DOMAIN_SPOOF.matcher(normalized).find()) {
            score += 20;
            notes.add("NLP: Domain-spoofing pattern detected.");
        }

        // Salary-promise regex (capped at 2 hits, +10 each)
        Matcher sm = SALARY_PROMISE.matcher(normalized);
        int salaryHits = 0;
        while (sm.find() && salaryHits < 2) { score += 10; salaryHits++; }
        if (salaryHits > 0) notes.add("NLP: Unrealistic salary promise pattern detected.");

        // Learned vocabulary (with 1.2× multiplier when ≥10 feedback entries)
        boolean applyMultiplier = feedbackEntryCount >= 10;
        for (LearnedVocabEntry entry : learnedEntries) {
            if (normalized.contains(entry.getPhrase())) {
                double multiplier = applyMultiplier ? 1.2 : 1.0;
                int contribution = (int) Math.round(entry.getWeight() * multiplier);
                score += contribution;
                notes.add("NLP: Learned signal matched: \"" + entry.getPhrase() + "\".");
            }
        }

        if (score <= 0 && notes.isEmpty()) {
            notes.add("NLP: Text tone appears mostly neutral/professional.");
        }

        return new NlpAnalysis(Math.max(0, Math.min(score, 100)), notes);
    }

    // ── Helpers ────────────────────────────────────────────────────

    public static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9 ]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
    }

    private static int countOccurrences(String text, String phrase) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(phrase, idx)) != -1) { count++; idx += phrase.length(); }
        return count;
    }
}
