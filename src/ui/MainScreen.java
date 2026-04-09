package ui;

import auth.AuthService;
import auth.Session;
import store.HistoryStore;
import model.AnalysisRecord;
import model.InternshipOffer;
import model.JobOffer;
import model.Offer;
import model.VerificationResult;
import service.FeedbackProcessor;
import service.VerificationEngine;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * The main analysis screen. Users fill in offer details on the left,
 * and the verdict + breakdown appears on the right.
 *
 * Team: JVM Juggernauts (JAVA-IV-T062)
 */
public class MainScreen extends JPanel {

    private final AppFrame           frame;
    private final AuthService        auth;
    private final HistoryStore       historyStore;
    private final FeedbackProcessor  feedbackProcessor;
    private final VerificationEngine engine;

    private Session currentSession;
    private String  lastDescription = "";
    private String  lastVerdict     = "";

    // Header
    private JLabel  usernameLabel;

    // Form inputs
    private JTextField    companyField, emailField, salaryField, positionField;
    private JTextArea     descriptionArea;
    private JCheckBox     feeCheck, urgencyCheck, personalInfoCheck;
    private JComboBox<String> offerTypeCombo;

    // Result area
    private JPanel resultPanel;

    public MainScreen(AppFrame frame, AuthService auth, HistoryStore historyStore,
                      FeedbackProcessor feedbackProcessor, VerificationEngine engine) {
        this.frame            = frame;
        this.auth             = auth;
        this.historyStore     = historyStore;
        this.feedbackProcessor = feedbackProcessor;
        this.engine           = engine;

        setLayout(new BorderLayout());

        JPanel bg = Theme.gradientBg();
        bg.setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);

        bg.add(buildHeader(), BorderLayout.NORTH);
        bg.add(buildBody(),   BorderLayout.CENTER);
        bg.add(buildFooter(), BorderLayout.SOUTH);
    }

    public void setSession(Session session) {
        this.currentSession = session;
        if (usernameLabel != null && session != null) {
            usernameLabel.setText(session.getUsername());
        }
    }

    public void clearFormFields() {
        companyField.setText("");
        emailField.setText("");
        salaryField.setText("");
        positionField.setText("");
        descriptionArea.setText("");
        feeCheck.setSelected(false);
        urgencyCheck.setSelected(false);
        personalInfoCheck.setSelected(false);
        offerTypeCombo.setSelectedIndex(0);
    }

    // ── Header ─────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));

        // Left: logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(new JLabel(Theme.shieldIcon(38)));

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel appName = new JLabel("Fake Offer Detector");
        appName.setFont(new Font("Georgia", Font.BOLD, 24));
        appName.setForeground(Theme.TEXT);

        JLabel tagline = new JLabel("Spot fake internship and job offers before it's too late");
        tagline.setFont(Theme.BODY.deriveFont(11f));
        tagline.setForeground(Theme.TEXT_DIM);

        titleStack.add(appName);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(tagline);
        left.add(titleStack);
        header.add(left, BorderLayout.WEST);

        // Right: user info + actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        usernameLabel = new JLabel("—");
        usernameLabel.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
        usernameLabel.setForeground(Theme.TEXT);

        JButton historyBtn = Theme.linkButton("History");
        historyBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            String u = currentSession != null ? currentSession.getUsername() : "";
            new HistoryPanel(w, historyStore, u);
        });

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
        logoutBtn.setForeground(Theme.RED);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            auth.logout();
            clearFormFields();
            frame.showCard(AppFrame.CARD_LOGIN);
        });

        // Team badge
        JLabel badge = new JLabel("  JAVA-IV-T062  ") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.AMBER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        badge.setFont(Theme.SMALL.deriveFont(Font.BOLD));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        right.add(userIcon);
        right.add(usernameLabel);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(historyBtn);
        right.add(logoutBtn);
        right.add(badge);
        header.add(right, BorderLayout.EAST);

        // Thin teal separator line under the header
        JPanel sep = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Theme.TEAL, getWidth(), 0, Theme.TEAL_LIGHT));
                g2.fillRect(0, 0, getWidth(), 2);
            }
        };
        sep.setPreferredSize(new Dimension(0, 2));
        sep.setOpaque(false);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(header, BorderLayout.CENTER);
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    // ── Body ───────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets  = new Insets(0, 6, 0, 6);

        g.gridx = 0; g.weightx = 0.54;
        body.add(buildFormCard(), g);

        g.gridx = 1; g.weightx = 0.46;
        body.add(buildResultCard(), g);

        return body;
    }

    // ── Form card ──────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel cardTitle = new JLabel("Enter Offer Details");
        cardTitle.setFont(Theme.HEADING);
        cardTitle.setForeground(Theme.TEAL_LIGHT);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int row = 0;

        offerTypeCombo = new JComboBox<>(new String[]{"Job Offer", "Internship Offer"});
        offerTypeCombo.setFont(Theme.BODY);
        offerTypeCombo.setBackground(Theme.BG_INPUT);
        row = addRow(form, gc, row, "Offer Type", offerTypeCombo);

        companyField  = Theme.inputField("e.g. Amazon, Google, XYZ Pvt. Ltd.");
        row = addRow(form, gc, row, "Company Name", companyField);

        emailField    = Theme.inputField("e.g. hr@company.com");
        row = addRow(form, gc, row, "Sender Email", emailField);

        positionField = Theme.inputField("e.g. Software Developer Intern");
        row = addRow(form, gc, row, "Position / Role", positionField);

        salaryField   = Theme.inputField("Monthly amount in ₹, e.g. 50000");
        row = addRow(form, gc, row, "Salary / Stipend (₹)", salaryField);

        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Theme.BODY);
        descriptionArea.setBackground(Theme.BG_INPUT);
        descriptionArea.setForeground(Theme.TEXT);
        descriptionArea.setCaretColor(Theme.TEAL);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(Theme.roundedBorder(Theme.BORDER, 8));
        descScroll.getViewport().setBackground(Theme.BG_INPUT);
        row = addRow(form, gc, row, "Job Description", descScroll);

        JPanel checks = new JPanel(new GridLayout(3, 1, 0, 5));
        checks.setOpaque(false);
        feeCheck          = check("Requires a registration or security fee");
        urgencyCheck      = check("Uses urgent / pressure language");
        personalInfoCheck = check("Asks for Aadhaar, bank details, or OTP upfront");
        checks.add(feeCheck);
        checks.add(urgencyCheck);
        checks.add(personalInfoCheck);
        addRow(form, gc, row, "Red Flags", checks);

        card.add(form, BorderLayout.CENTER);

        // Action buttons
        JButton analyzeBtn = Theme.primaryButton("Analyze Offer");
        analyzeBtn.setPreferredSize(new Dimension(0, 44));
        analyzeBtn.addActionListener(e -> runAnalysis());

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(Theme.BODY);
        clearBtn.setForeground(Theme.TEXT_DIM);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearFormFields());

        JPanel btnRow = new JPanel(new BorderLayout(10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        btnRow.add(analyzeBtn, BorderLayout.CENTER);
        btnRow.add(clearBtn,   BorderLayout.EAST);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private int addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row * 2;
        gc.insets = new Insets(8, 0, 2, 0);
        form.add(Theme.fieldLabel(label), gc);

        gc.gridy = row * 2 + 1;
        gc.insets = new Insets(0, 0, 0, 0);
        form.add(comp, gc);
        return row + 1;
    }

    // ── Result card ────────────────────────────────────────────────

    private JPanel buildResultCard() {
        resultPanel = Theme.card();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel cardTitle = new JLabel("Analysis Result");
        cardTitle.setFont(Theme.HEADING);
        cardTitle.setForeground(Theme.TEAL_LIGHT);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        resultPanel.add(cardTitle, BorderLayout.NORTH);
        resultPanel.add(buildIdleState(), BorderLayout.CENTER);
        return resultPanel;
    }

    private JPanel buildIdleState() {
        JPanel idle = new JPanel(new GridBagLayout());
        idle.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel icon = new JLabel(Theme.shieldIcon(68));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel line1 = new JLabel("Fill in the offer details");
        line1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        line1.setForeground(Theme.TEXT_DIM);
        line1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel line2 = new JLabel("and click Analyze.");
        line2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        line2.setForeground(Theme.TEXT_DIM);
        line2.setAlignmentX(CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createVerticalStrut(14));
        inner.add(line1);
        inner.add(Box.createVerticalStrut(4));
        inner.add(line2);
        idle.add(inner);
        return idle;
    }

    // ── Footer ─────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        footer.setOpaque(false);
        JLabel lbl = new JLabel(
            "TCS-408  ·  JVM Juggernauts  ·  Shanu Khatana  ·  Disha Jha  ·  Rakshit Sharma  ·  Tanuja Kanswal");
        lbl.setFont(Theme.SMALL);
        lbl.setForeground(new Color(120, 108, 93));
        footer.add(lbl);
        return footer;
    }

    // ── Analysis logic ─────────────────────────────────────────────

    private void runAnalysis() {
        String company  = companyField.getText().trim();
        String email    = emailField.getText().trim();
        String salary   = salaryField.getText().trim();
        String role     = positionField.getText().trim();
        String desc     = descriptionArea.getText().trim();
        boolean hasFee  = feeCheck.isSelected();
        boolean urgent  = urgencyCheck.isSelected();
        boolean piFlag  = personalInfoCheck.isSelected();
        String type     = (String) offerTypeCombo.getSelectedItem();

        if (company.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in at least Company Name and Sender Email.",
                "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double pay = parseSalary(salary);
        Offer offer = (type != null && type.toLowerCase(Locale.ROOT).contains("intern"))
            ? new InternshipOffer(company, email, pay, desc, hasFee)
            : new JobOffer(company, email, pay, desc, hasFee);

        VerificationResult result = engine.evaluate(offer, urgent, piFlag, role, type);

        if (currentSession != null) {
            historyStore.save(new AnalysisRecord(
                currentSession.getUsername(),
                company, type != null ? type : "",
                result.getRiskScore(), result.getNlpRisk(), result.getVerdict()
            ));
        }

        Color tone;
        String glyph;
        switch (result.getVerdict()) {
            case "GENUINE":    tone = Theme.GREEN;  glyph = "✓"; break;
            case "SUSPICIOUS": tone = Theme.YELLOW; glyph = "!"; break;
            default:           tone = Theme.RED;    glyph = "✕"; break;
        }

        showResult(result, tone, glyph, type, company, desc);
    }

    private void showResult(VerificationResult result, Color tone, String glyph,
                            String type, String company, String desc) {
        // Swap out the center component
        resultPanel.remove(1);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Verdict badge
        JPanel badge = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(tone.getRed(), tone.getGreen(), tone.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(tone.getRed(), tone.getGreen(), tone.getBlue(), 70));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            }
        };
        badge.setOpaque(false);
        badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel vInner = new JPanel();
        vInner.setLayout(new BoxLayout(vInner, BoxLayout.Y_AXIS));
        vInner.setOpaque(false);

        JLabel glyphLbl = new JLabel(glyph);
        glyphLbl.setFont(new Font("Trebuchet MS", Font.BOLD, 32));
        glyphLbl.setForeground(tone);
        glyphLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel verdictLbl = new JLabel(result.getVerdict());
        verdictLbl.setFont(Theme.HEADING.deriveFont(Font.BOLD, 22f));
        verdictLbl.setForeground(tone);
        verdictLbl.setAlignmentX(CENTER_ALIGNMENT);

        vInner.add(glyphLbl);
        vInner.add(Box.createVerticalStrut(3));
        vInner.add(verdictLbl);
        badge.add(vInner);

        // Risk score bar
        JLabel riskLbl = new JLabel("Overall Risk: " + result.getRiskScore() + " / 100");
        riskLbl.setFont(Theme.BODY.deriveFont(Font.BOLD, 13f));
        riskLbl.setForeground(tone);
        riskLbl.setAlignmentX(LEFT_ALIGNMENT);
        riskLbl.setBorder(BorderFactory.createEmptyBorder(14, 2, 4, 0));

        JProgressBar riskBar = scoreBar(result.getRiskScore(), tone);

        JLabel nlpLbl = new JLabel("NLP Signal: " + result.getNlpRisk() + " / 100");
        nlpLbl.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
        nlpLbl.setForeground(Theme.TEXT_DIM);
        nlpLbl.setAlignmentX(LEFT_ALIGNMENT);
        nlpLbl.setBorder(BorderFactory.createEmptyBorder(8, 2, 4, 0));

        JProgressBar nlpBar = scoreBar(result.getNlpRisk(), new Color(110, 90, 70));

        JLabel context = new JLabel(type + "  ·  " + company);
        context.setFont(Theme.BODY.deriveFont(Font.ITALIC, 12f));
        context.setForeground(Theme.TEXT_DIM);
        context.setAlignmentX(LEFT_ALIGNMENT);
        context.setBorder(BorderFactory.createEmptyBorder(10, 2, 6, 0));

        // Findings
        JLabel findingsLbl = new JLabel("Findings");
        findingsLbl.setFont(Theme.LABEL);
        findingsLbl.setForeground(Theme.TEXT_DIM);
        findingsLbl.setAlignmentX(LEFT_ALIGNMENT);
        findingsLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JTextArea findings = new JTextArea();
        findings.setEditable(false);
        findings.setFont(Theme.BODY.deriveFont(12f));
        findings.setBackground(Theme.BG_INPUT);
        findings.setForeground(Theme.TEXT);
        findings.setLineWrap(true);
        findings.setWrapStyleWord(true);
        findings.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        StringBuilder sb = new StringBuilder();
        for (String line : result.getFindings()) sb.append("• ").append(line).append("\n\n");
        findings.setText(sb.toString().trim());

        JScrollPane findingsScroll = new JScrollPane(findings);
        findingsScroll.setBorder(Theme.roundedBorder(Theme.BORDER, 8));
        findingsScroll.setAlignmentX(LEFT_ALIGNMENT);
        findingsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Feedback buttons
        lastDescription = desc;
        lastVerdict     = result.getVerdict();

        JButton markFake = colorButton("Mark as Fake", Theme.RED);
        markFake.addActionListener(e ->
            feedbackProcessor.processFeedbackAsync(currentSession, lastDescription, lastVerdict, "FAKE",
                r -> onFeedback(r)));

        JButton markGenuine = colorButton("Mark as Genuine", Theme.GREEN);
        markGenuine.addActionListener(e ->
            feedbackProcessor.processFeedbackAsync(currentSession, lastDescription, lastVerdict, "GENUINE",
                r -> onFeedback(r)));

        JPanel feedbackRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        feedbackRow.setOpaque(false);
        feedbackRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        feedbackRow.add(markFake);
        feedbackRow.add(markGenuine);

        // "Analyze another" link
        JButton again = Theme.linkButton("+ Analyze another offer");
        again.setAlignmentX(LEFT_ALIGNMENT);
        again.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        again.addActionListener(e -> { clearFormFields(); resetResult(); });

        center.add(badge);
        center.add(riskLbl);
        center.add(riskBar);
        center.add(nlpLbl);
        center.add(nlpBar);
        center.add(context);
        center.add(findingsLbl);
        center.add(findingsScroll);
        center.add(feedbackRow);
        center.add(again);

        resultPanel.add(center, BorderLayout.CENTER);
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void resetResult() {
        resultPanel.remove(1);
        resultPanel.add(buildIdleState(), BorderLayout.CENTER);
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void onFeedback(FeedbackProcessor.Result r) {
        String msg = (r == FeedbackProcessor.Result.ALREADY_CORRECT)
            ? "The result was already correct — no changes needed."
            : "Got it! Keywords updated. The next analysis will be more accurate.";
        JOptionPane.showMessageDialog(this, msg, "Feedback", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Helpers ────────────────────────────────────────────────────

    private JProgressBar scoreBar(int value, Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setStringPainted(false);
        bar.setBackground(Theme.BG_INPUT);
        bar.setForeground(color);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 9));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9));
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setBorder(Theme.roundedBorder(Theme.BG_INPUT, 5));
        return bar;
    }

    private JButton colorButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            { setContentAreaFilled(false); setOpaque(false); setFocusPainted(false); setBorderPainted(false); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JCheckBox check(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(Theme.BODY.deriveFont(12f));
        cb.setForeground(Theme.TEXT);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        return cb;
    }

    private double parseSalary(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        String clean = s.replaceAll("[^0-9.]", "");
        if (clean.isEmpty()) return 0;
        try { return Double.parseDouble(clean); } catch (NumberFormatException e) { return 0; }
    }

    // Needed by showFeedbackButtons (kept for API compatibility)
    public void showFeedbackButtons(VerificationResult result, String description) {
        // feedback buttons are now embedded directly in showResult
    }
}
