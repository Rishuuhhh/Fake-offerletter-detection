package ui;

import auth.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Login screen. Kept intentionally simple — just a centered card
 * with username/password fields and a sign-in button.
 */
public class LoginScreen extends JPanel {

    private final AppFrame    frame;
    private final AuthService auth;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JLabel         lockoutLabel;

    public LoginScreen(AppFrame frame, AuthService auth) {
        this.frame = frame;
        this.auth  = auth;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        JPanel bg = Theme.gradientBg();
        bg.setLayout(new GridBagLayout());
        add(bg, BorderLayout.CENTER);

        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(420, 480));

        // Header
        JLabel icon = new JLabel(Theme.shieldIcon(52));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue");
        sub.setFont(Theme.BODY.deriveFont(12f));
        sub.setForeground(Theme.TEXT_DIM);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        // Feedback labels
        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.BODY.deriveFont(12f));
        errorLabel.setForeground(Theme.RED);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        lockoutLabel = new JLabel(" ");
        lockoutLabel.setFont(Theme.BODY.deriveFont(12f));
        lockoutLabel.setForeground(Theme.AMBER);
        lockoutLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Fields
        usernameField = Theme.inputField("Username");
        passwordField = Theme.passwordField();
        passwordField.addActionListener(e -> doLogin());

        // Buttons
        JButton signIn = Theme.primaryButton("Sign In");
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signIn.setAlignmentX(CENTER_ALIGNMENT);
        signIn.addActionListener(e -> doLogin());

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setFont(Theme.BODY.deriveFont(12f));
        noAcc.setForeground(Theme.TEXT_DIM);
        JButton reg = Theme.linkButton("Register");
        reg.addActionListener(e -> frame.showCard(AppFrame.CARD_REGISTER));
        linkRow.add(noAcc);
        linkRow.add(reg);

        // Assemble
        card.add(icon);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(Theme.fieldLabel("Username"));
        card.add(Box.createVerticalStrut(5));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(Theme.fieldLabel("Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));
        card.add(errorLabel);
        card.add(lockoutLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(signIn);
        card.add(Box.createVerticalStrut(18));
        card.add(linkRow);

        bg.add(card);
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        errorLabel.setText(" ");
        lockoutLabel.setText(" ");

        switch (auth.login(user, pass)) {
            case SUCCESS:
                frame.showCard(AppFrame.CARD_MAIN);
                break;
            case INVALID_CREDENTIALS:
                showError("Invalid username or password.");
                break;
            case LOCKED_OUT:
                showLockout(auth.getLockoutSecondsRemaining(user));
                break;
        }
    }

    public void clearFields() {
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (errorLabel    != null) errorLabel.setText(" ");
        if (lockoutLabel  != null) lockoutLabel.setText(" ");
    }

    public void showError(String msg) {
        errorLabel.setText(msg);
        lockoutLabel.setText(" ");
    }

    public void showLockout(int secs) {
        errorLabel.setText(" ");
        lockoutLabel.setText("Too many attempts. Try again in " + secs + "s.");
    }
}
