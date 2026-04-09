package service;

import auth.Session;
import db.FeedbackLogger;
import db.VocabularyStore;
import model.FeedbackEvent;
import model.LearnedVocabEntry;
import utils.PasswordHasher;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Processes user feedback on offer analysis results.
 * Extracts phrases, updates the learned vocabulary, logs the event,
 * and reloads the NLP analyzer — all on a background thread.
 */
public class FeedbackProcessor {

    public enum FeedbackResult { ALREADY_CORRECT, VOCABULARY_UPDATED }

    private static final int    FAKE_WEIGHT_INCREMENT    =  5;
    private static final int    GENUINE_WEIGHT_INCREMENT = -3;
    private static final int    TOP_N_PHRASES            =  5;

    private final PhraseExtractor    phraseExtractor;
    private final VocabularyStore    vocabularyStore;
    private final FeedbackLogger     feedbackLogger;
    private final NlpSignalAnalyzer  nlpAnalyzer;

    public FeedbackProcessor(PhraseExtractor phraseExtractor,
                             VocabularyStore vocabularyStore,
                             FeedbackLogger feedbackLogger,
                             NlpSignalAnalyzer nlpAnalyzer) {
        this.phraseExtractor = phraseExtractor;
        this.vocabularyStore = vocabularyStore;
        this.feedbackLogger  = feedbackLogger;
        this.nlpAnalyzer     = nlpAnalyzer;
    }

    /**
     * Processes feedback asynchronously on a SwingWorker background thread.
     * Calls {@code onComplete} on the EDT when done.
     *
     * @param session       current user session
     * @param description   the offer description text that was analysed
     * @param systemVerdict the verdict the system produced (FAKE/SUSPICIOUS/GENUINE)
     * @param userVerdict   the user's correction (FAKE or GENUINE)
     * @param onComplete    callback invoked on EDT with the FeedbackResult
     */
    public void processFeedbackAsync(Session session, String description,
                                     String systemVerdict, String userVerdict,
                                     Consumer<FeedbackResult> onComplete) {
        new SwingWorker<FeedbackResult, Void>() {
            @Override
            protected FeedbackResult doInBackground() {
                return doProcess(session, description, systemVerdict, userVerdict);
            }
            @Override
            protected void done() {
                try { onComplete.accept(get()); }
                catch (Exception e) { onComplete.accept(FeedbackResult.ALREADY_CORRECT); }
            }
        }.execute();
    }

    /** Synchronous version (for testing). */
    public FeedbackResult process(Session session, String description,
                                  String systemVerdict, String userVerdict) {
        return doProcess(session, description, systemVerdict, userVerdict);
    }

    private FeedbackResult doProcess(Session session, String description,
                                     String systemVerdict, String userVerdict) {
        // Normalise verdicts for comparison
        String sysNorm  = normaliseVerdict(systemVerdict);
        String userNorm = normaliseVerdict(userVerdict);

        if (sysNorm.equals(userNorm)) return FeedbackResult.ALREADY_CORRECT;

        // Extract top phrases
        List<String> phrases = phraseExtractor.extractTopPhrases(description, TOP_N_PHRASES);

        // Determine category and weight delta
        boolean isFakeFeedback = "FAKE".equals(userNorm);
        String  category       = isFakeFeedback ? "fraud" : "genuine";
        int     weightDelta    = isFakeFeedback ? FAKE_WEIGHT_INCREMENT : GENUINE_WEIGHT_INCREMENT;

        // Upsert each phrase into the vocabulary
        for (String phrase : phrases) {
            if (phrase == null || phrase.trim().isEmpty()) continue;
            LearnedVocabEntry entry = new LearnedVocabEntry(phrase.trim(), category, weightDelta);
            vocabularyStore.upsert(entry);
        }

        // Log the feedback event
        String descHash = PasswordHasher.hash(description, "feedback-hash-salt");
        FeedbackEvent event = new FeedbackEvent(
            session != null ? session.getUsername() : "anonymous",
            LocalDateTime.now().toString(),
            descHash,
            systemVerdict,
            userVerdict,
            String.join(", ", phrases)
        );
        feedbackLogger.log(event);

        // Reload NLP analyzer with updated vocabulary
        nlpAnalyzer.reloadLearnedVocabulary(vocabularyStore);

        return FeedbackResult.VOCABULARY_UPDATED;
    }

    private String normaliseVerdict(String verdict) {
        if (verdict == null) return "";
        String v = verdict.trim().toUpperCase();
        // Treat SUSPICIOUS same as FAKE for feedback purposes
        return "SUSPICIOUS".equals(v) ? "FAKE" : v;
    }
}
