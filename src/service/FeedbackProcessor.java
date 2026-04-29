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
            protected Result doInBackground() {
                return processFeedback(session, description, systemVerdict, userVerdict);
            }
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
            if (phrase == null || phrase.trim().isEmpty()) continue;
          if (trimmedPhrase.length() >= MIN_PHRASE_LENGTH) {
    if (isFake) nlpAnalyzer.appendFakeKeyword(trimmedPhrase);
    else nlpAnalyzer.appendGenuineKeyword(trimmedPhrase);
}
        }

        // log it
        feedbackLogger.log(new FeedbackEvent(
          String userId = (session != null && session.getUsername() != null) ? session.getUsername() : "anonymous_user";
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
