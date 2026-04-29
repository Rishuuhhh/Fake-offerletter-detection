package service;

import auth.Session;
import model.FeedbackEvent;
import store.FeedbackLogger;
import utils.PasswordHasher;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.function.Consumer;

// handles user feedback when they correct the system
public class FeedbackProcessor {

    public enum Result { ALREADY_CORRECT, UPDATED }

    private static final int TOP_PHRASES = 5;
    private static final int MIN_PHRASE_LENGTH = 4; // Added constant to filter out junk words

    private final PhraseExtractor phraseExtractor;
    private final FeedbackLogger feedbackLogger;
    private final NlpSignalAnalyzer nlpAnalyzer;

    public FeedbackProcessor(PhraseExtractor phraseExtractor, FeedbackLogger feedbackLogger, NlpSignalAnalyzer nlpAnalyzer) {
        this.phraseExtractor = phraseExtractor;
        this.feedbackLogger = feedbackLogger;
        this.nlpAnalyzer = nlpAnalyzer;
    }

    // runs on background thread
    public void processFeedbackAsync(Session session, String description, String systemVerdict, String userVerdict, Consumer<Result> onComplete) {
        new SwingWorker<Result, Void>() {
            @Override
            protected Result doInBackground() {
                return processFeedback(session, description, systemVerdict, userVerdict);
            }
            @Override
            protected void done() {
                try { 
                    onComplete.accept(get()); 
                } catch (Exception e) { 
                    onComplete.accept(Result.ALREADY_CORRECT); 
                }
            }
        }.execute();
    }

    public Result processFeedback(Session session, String description, String systemVerdict, String userVerdict) {
        String sys = normalise(systemVerdict);
        String user = normalise(userVerdict);

        if (sys.equals(user)) return Result.ALREADY_CORRECT;

        boolean isFake = "FAKE".equals(user);

        // extract phrases and update keyword files
        List<String> phrases = phraseExtractor.extractTopPhrases(description, TOP_PHRASES);
        for (String phrase : phrases) {
            if (phrase == null) continue;
            String trimmedPhrase = phrase.trim(); // Defined the variable here
            
            // Check if the phrase is long enough to be a meaningful keyword
            if (trimmedPhrase.length() >= MIN_PHRASE_LENGTH) {
                if (isFake) nlpAnalyzer.appendFakeKeyword(trimmedPhrase);
                else nlpAnalyzer.appendGenuineKeyword(trimmedPhrase);
            }
        }

        // Log it - defined userId correctly before using it in the logger
        String userId = (session != null && session.getUsername() != null) ? session.getUsername() : "anonymous_user";
        
        feedbackLogger.log(new FeedbackEvent(
            userId,
            PasswordHasher.hash(description, "feedback-salt"),
            systemVerdict, 
            userVerdict,
            String.join(", ", phrases)
        ));

        return Result.UPDATED;
    }

    private String normalise(String v) {
        if (v == null) return "";
        String s = v.trim().toUpperCase();
        return "SUSPICIOUS".equals(s) ? "FAKE" : s;
    }
}
