package ui;

import auth.AuthService;
import auth.Session;
import store.HistoryStore;
import service.FeedbackProcessor;
import service.VerificationEngine;

import javax.swing.*;
import java.awt.*;

/**
 * Single JFrame that hosts all screens via CardLayout.
 * All screen transitions go through showCard().
 */
public class AppFrame extends JFrame {

    public static final String CARD_LOGIN    = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";
    public static final String CARD_MAIN     = "MAIN";

    private final CardLayout cardLayout;
    private final JPanel     cardPanel;

    private final LoginScreen    loginScreen;
    private final RegisterScreen registerScreen;
    private final MainScreen     mainScreen;

    private final AuthService authService;

    public AppFrame(AuthService authService,
                    HistoryStore historyStore,
                    FeedbackProcessor feedbackProcessor,
                    VerificationEngine verificationEngine) {
        this.authService = authService;

        setTitle("Fake Offer Detector — JVM Juggernauts");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 780);
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        loginScreen    = new LoginScreen(this, authService);
        registerScreen = new RegisterScreen(this, authService);
        mainScreen     = new MainScreen(this, authService, historyStore, feedbackProcessor, verificationEngine);

        cardPanel.add(loginScreen,    CARD_LOGIN);
        cardPanel.add(registerScreen, CARD_REGISTER);
        cardPanel.add(mainScreen,     CARD_MAIN);

        setContentPane(cardPanel);
        showCard(CARD_LOGIN);
    }

    /**
     * Switches to the named card.
     * Always clears password fields before switching.
     * Updates the MainScreen session when switching to CARD_MAIN.
     */
    public void showCard(String cardName) {
        // Always clear sensitive fields
        loginScreen.clearFields();
        registerScreen.clearFields();

        if (CARD_MAIN.equals(cardName)) {
            Session session = authService.getSession();
            if (session == null) return; // guard: no session, don't show main
            mainScreen.setSession(session);
        }

        cardLayout.show(cardPanel, cardName);
    }

    public MainScreen getMainScreen() { return mainScreen; }
}
