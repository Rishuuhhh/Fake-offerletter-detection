package ui;

import auth.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Registration screen. Same card layout as login, just with three fields
 * and slightly different validation messages.
 */
public class RegisterScreen extends JPanel {

    private final AppFrame    frame;
    private final AuthService auth;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel         errorLabel;

    public RegisterScreen(AppFrame frame, AuthService auth) {
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
        card.setBorder(BorderFactory.createEmptyBorder(38, 48, 38, 48));
        card.setPreferredSize(new Dimension(420, 530));

        JLabel icon = new JLabel(Theme.shieldIcon(52));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Create Account");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Start detecting fake offers today");
        sub.setFont(Theme.BODY.deriveFont(12f));
        sub.setForeground(Theme.TEXT_DIM);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.BODY.deriveFont(12f));
        errorLabel.setForeground(Theme.RED);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        usernameField = Theme.inputField("3–30 chars, letters / numbers / underscore");
        passwordField = Theme.passwordField();
        confirmField  = Theme.passwordField();
        confirmField.addActionListener(e -> doRegister());

        JButton createBtn = Theme.primaryButton("Create Account");
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        createBtn.setAlignmentX(CENTER_ALIGNMENT);
        createBtn.addActionListener(e -> doRegister());

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel hasAcc = new JLabel("Already have an account?");
        hasAcc.setFont(Theme.BODY.deriveFont(12f));
        hasAcc.setForeground(Theme.TEXT_DIM);
        JButton signIn = Theme.linkButton("Sign In");
        signIn.addActionListener(e -> frame.showCard(AppFrame.CARD_LOGIN));
        linkRow.add(hasAcc);
        linkRow.add(signIn);

        card.add(icon);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(Theme.fieldLabel("Username"));
        card.add(Box.createVerticalStrut(5));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(Theme.fieldLabel("Password  (min. 8 characters)"));
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(14));
        card.add(Theme.fieldLabel("Confirm Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(10));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(createBtn);
        card.add(Box.createVerticalStrut(18));
        card.add(linkRow);

        bg.add(card);
    }

    private void doRegister() {
        String user    = usernameField.getText().trim();
        String pass    = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        errorLabel.setText(" ");

        switch (auth.register(user, pass, confirm)) {
            case SUCCESS:
                frame.showCard(AppFrame.CARD_LOGIN);
                break;
            case INVALID_USERNAME:
                showError("Username must be 3–30 characters (letters, numbers, underscore).");
                break;
            case INVALID_PASSWORD:
                showError("Password must be at least 8 characters.");
                break;
            case PASSWORD_MISMATCH:
                showError("Passwords don't match.");
                break;
            case USERNAME_TAKEN:
                showError("That username is already taken.");
                break;
        }
    }

    public void clearFields() {
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (confirmField  != null) confirmField.setText("");
        if (errorLabel    != null) errorLabel.setText(" ");
    }

    public void showError(String msg) {
        errorLabel.setText(msg);
    }
}
