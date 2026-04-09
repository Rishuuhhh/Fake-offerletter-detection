package main;

import auth.AuthService;
import db.*;
import service.*;
import ui.AppFrame;
import utils.FileStore;

import javax.swing.*;

/**
 * Application entry point.
 * Wires all layers together and launches the AppFrame on the EDT.
 */
public class AppLauncher {

    public static void main(String[] args) {
        // 1. Database + seed
        DatabaseManager dbManager = new DatabaseManager();
        SeedLoader seedLoader = new SeedLoader(dbManager);
        seedLoader.seedIfEmpty();

        // 2. File store (CSV fallback)
        FileStore fileStore = new FileStore();

        // 3. DB stores
        UserStore       userStore       = new UserStore(dbManager, fileStore);
        HistoryStore    historyStore    = new HistoryStore(dbManager, fileStore);
        VocabularyStore vocabularyStore = new VocabularyStore(dbManager, fileStore);
        FeedbackLogger  feedbackLogger  = new FeedbackLogger(dbManager);

        // 4. NLP + service layer
        NlpSignalAnalyzer nlpAnalyzer = new NlpSignalAnalyzer();
        nlpAnalyzer.reloadLearnedVocabulary(vocabularyStore);

        VerificationEngine verificationEngine = new VerificationEngine(nlpAnalyzer);
        PhraseExtractor    phraseExtractor    = new PhraseExtractor();
        FeedbackProcessor  feedbackProcessor  = new FeedbackProcessor(
            phraseExtractor, vocabularyStore, feedbackLogger, nlpAnalyzer);

        // 5. Auth
        AuthService authService = new AuthService(userStore);

        // 6. Launch UI on EDT
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AppFrame appFrame = new AppFrame(authService, historyStore, feedbackProcessor, verificationEngine);
            appFrame.setVisible(true);
        });
    }
}
