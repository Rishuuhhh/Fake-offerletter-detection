package ui;

import auth.AuthService;
import java.awt.*;
import javax.swing.*;

public class LoginScreen extends JPanel {

    private final AppFrame    frame;
    private final AuthService auth;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         msgLabel;

    public LoginScreen(AppFrame frame, AuthService auth) {
        this.frame = frame;
        this.auth  = auth;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(380, 340));

        JLabel title = new JLabel("Fake Offer Detector");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        msgLabel = new JLabel(" ");
        msgLabel.setFont(Theme.BODY.deriveFont(12f));
        msgLabel.setForeground(Theme.RED);
        msgLabel.setAlignmentX(CENTER_ALIGNMENT);

        usernameField = Theme.inputField("Username");
        passwordField = Theme.passwordField();
        passwordField.addActionListener(e -> doLogin());

        JButton signIn = Theme.primaryButton("Sign In");
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        signIn.setAlignmentX(CENTER_ALIGNMENT);
        signIn.addActionListener(e -> doLogin());

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel noAcc = new JLabel("No account?");
        noAcc.setFont(Theme.BODY.deriveFont(12f));
        noAcc.setForeground(Theme.TEXT_DIM);
        JButton reg = Theme.linkButton("Register");
        reg.addActionListener(e -> frame.showCard(AppFrame.CARD_REGISTER));
        linkRow.add(noAcc);
        linkRow.add(reg);

        card.add(title);
        card.add(Box.createVerticalStrut(24));
        card.add(Theme.fieldLabel("Username"));
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(Theme.fieldLabel("Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(msgLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(signIn);
        card.add(Box.createVerticalStrut(12));
        card.add(linkRow);

        add(card);
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        msgLabel.setText(" ");

        switch (auth.login(user, pass)) {
            case SUCCESS:
                frame.showCard(AppFrame.CARD_MAIN);
                break;
            case INVALID_CREDENTIALS:
                msgLabel.setText("Invalid username or password.");
                break;
            case LOCKED_OUT:
                msgLabel.setForeground(Theme.AMBER);
                msgLabel.setText("Too many attempts. Try again in " + auth.getLockoutSecondsRemaining(user) + "s.");
                break;
        }
    }

    public void clearFields() {
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (msgLabel      != null) { msgLabel.setForeground(Theme.RED); msgLabel.setText(" "); }
    }

    public void showError(String msg)    { msgLabel.setForeground(Theme.RED);   msgLabel.setText(msg); }
    public void showLockout(int secs)    { msgLabel.setForeground(Theme.AMBER); msgLabel.setText("Locked out. Try again in " + secs + "s."); }
}
