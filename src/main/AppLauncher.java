package main;

import auth.AuthService;
import service.*;
import store.FeedbackLogger;
import store.HistoryStore;
import store.UserStore;
import ui.AppFrame;
import utils.FileStore;

import javax.swing.*;

// starts the app
public class AppLauncher {

    public static void main(String[] args) {
        FileStore fileStore = new FileStore();

        // storage
        UserStore userStore = new UserStore(fileStore);
        HistoryStore historyStore = new HistoryStore(fileStore);
        FeedbackLogger feedbackLogger = new FeedbackLogger(fileStore);

        // analysis engine
        NlpSignalAnalyzer nlpAnalyzer = new NlpSignalAnalyzer();
        VerificationEngine verificationEngine = new VerificationEngine(nlpAnalyzer);
        PhraseExtractor phraseExtractor = new PhraseExtractor();
        FeedbackProcessor feedbackProcessor = new FeedbackProcessor(phraseExtractor, feedbackLogger, nlpAnalyzer);

        // auth
        AuthService authService = new AuthService(userStore);

        // launch UI
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new AppFrame(authService, historyStore, feedbackProcessor, verificationEngine).setVisible(true);
        });
    }
}
