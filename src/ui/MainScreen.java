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
import java.awt.*;
import java.util.Locale;

public class MainScreen extends JPanel {

    private final AppFrame           frame;
    private final AuthService        auth;
    private final HistoryStore       historyStore;
    private final FeedbackProcessor  feedbackProcessor;
    private final VerificationEngine engine;

    private Session currentSession;
    private String  lastDescription = "";
    private String  lastVerdict     = "";

    private JLabel  usernameLabel;

    // Form inputs
    private JTextField    companyField, emailField, salaryField, positionField;
    private JTextArea     descriptionArea;
    private JCheckBox     feeCheck, urgencyCheck, personalInfoCheck;
    private JComboBox<String> offerTypeCombo;

    // Result panel
    private JPanel resultPanel;

    public MainScreen(AppFrame frame, AuthService auth, HistoryStore historyStore,
                      FeedbackProcessor feedbackProcessor, VerificationEngine engine) {
        this.frame             = frame;
        this.auth              = auth;
        this.historyStore      = historyStore;
        this.feedbackProcessor = feedbackProcessor;
        this.engine            = engine;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    public void setSession(Session session) {
        this.currentSession = session;
        if (usernameLabel != null && session != null)
            usernameLabel.setText(session.getUsername());
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

    // ── Header ────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        JLabel appName = new JLabel("Fake Offer Detector");
        appName.setFont(Theme.HEADING);
        appName.setForeground(Theme.TEAL);
        header.add(appName, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        usernameLabel = new JLabel("—");
        usernameLabel.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
        usernameLabel.setForeground(Theme.TEXT);

        JButton historyBtn = Theme.linkButton("History");
        historyBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            String u = currentSession != null ? currentSession.getUsername() : "";
            new HistoryPanel(w, historyStore, u);
        });

        JButton logoutBtn = Theme.linkButton("Sign Out");
        logoutBtn.setForeground(Theme.RED);
        logoutBtn.addActionListener(e -> {
            auth.logout();
            clearFormFields();
            frame.showCard(AppFrame.CARD_LOGIN);
        });

        right.add(usernameLabel);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(historyBtn);
        right.add(logoutBtn);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── Body ──────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.BG_MAIN);
        body.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets = new Insets(0, 5, 0, 5);

        g.gridx = 0; g.weightx = 0.54;
        body.add(buildFormCard(), g);

        g.gridx = 1; g.weightx = 0.46;
        body.add(buildResultCard(), g);

        return body;
    }

    // ── Form card ─────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel cardTitle = new JLabel("Offer Details");
        cardTitle.setFont(Theme.HEADING);
        cardTitle.setForeground(Theme.TEAL);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
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
        row = addRow(form, gc, row, "Offer Type",        offerTypeCombo);

        companyField  = Theme.inputField("e.g. Amazon, XYZ Pvt. Ltd.");
        row = addRow(form, gc, row, "Company Name",      companyField);

        emailField    = Theme.inputField("e.g. hr@company.com");
        row = addRow(form, gc, row, "Sender Email",      emailField);

        positionField = Theme.inputField("e.g. Software Developer Intern");
        row = addRow(form, gc, row, "Position / Role",   positionField);

        salaryField   = Theme.inputField("Monthly ₹, e.g. 50000");
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

        JPanel checks = new JPanel(new GridLayout(3, 1, 0, 4));
        checks.setOpaque(false);
        feeCheck          = check("Requires a registration or security fee");
        urgencyCheck      = check("Uses urgent / pressure language");
        personalInfoCheck = check("Asks for Aadhaar, bank details, or OTP");
        checks.add(feeCheck);
        checks.add(urgencyCheck);
        checks.add(personalInfoCheck);
        addRow(form, gc, row, "Red Flags", checks);

        card.add(form, BorderLayout.CENTER);

        JButton analyzeBtn = Theme.primaryButton("Analyze Offer");
        analyzeBtn.setPreferredSize(new Dimension(0, 40));
        analyzeBtn.addActionListener(e -> runAnalysis());

        JButton clearBtn = Theme.linkButton("Clear");
        clearBtn.setForeground(Theme.TEXT_DIM);
        clearBtn.addActionListener(e -> clearFormFields());

        JPanel btnRow = new JPanel(new BorderLayout(8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        btnRow.add(analyzeBtn, BorderLayout.CENTER);
        btnRow.add(clearBtn,   BorderLayout.EAST);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private int addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row * 2;
        gc.insets = new Insets(7, 0, 2, 0);
        form.add(Theme.fieldLabel(label), gc);
        gc.gridy = row * 2 + 1;
        gc.insets = new Insets(0, 0, 0, 0);
        form.add(comp, gc);
        return row + 1;
    }

    // ── Result card ───────────────────────────────────────────────

    private JPanel buildResultCard() {
        resultPanel = Theme.card();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel cardTitle = new JLabel("Analysis Result");
        cardTitle.setFont(Theme.HEADING);
        cardTitle.setForeground(Theme.TEAL);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        resultPanel.add(cardTitle, BorderLayout.NORTH);
        resultPanel.add(buildIdleState(), BorderLayout.CENTER);
        return resultPanel;
    }

    private JPanel buildIdleState() {
        JPanel idle = new JPanel(new GridBagLayout());
        idle.setOpaque(false);
        JLabel hint = new JLabel("Fill in the offer details and click Analyze.");
        hint.setFont(Theme.BODY.deriveFont(13f));
        hint.setForeground(Theme.TEXT_DIM);
        idle.add(hint);
        return idle;
    }

    // ── Footer ────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JLabel lbl = new JLabel("TCS-408  ·  JVM Juggernauts");
        lbl.setFont(Theme.SMALL);
        lbl.setForeground(new Color(140, 126, 110));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        footer.setBackground(Theme.BG_MAIN);
        footer.add(lbl);
        return footer;
    }

    // ── Analysis logic ────────────────────────────────────────────

    private void runAnalysis() {
        String company = companyField.getText().trim();
        String email   = emailField.getText().trim();
        String salary  = salaryField.getText().trim();
        String role    = positionField.getText().trim();
        String desc    = descriptionArea.getText().trim();
        boolean hasFee = feeCheck.isSelected();
        boolean urgent = urgencyCheck.isSelected();
        boolean piFlag = personalInfoCheck.isSelected();
        String type    = (String) offerTypeCombo.getSelectedItem();

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

        if (currentSession != null)
            historyStore.save(new AnalysisRecord(currentSession.getUsername(),
                company, type != null ? type : "",
                result.getRiskScore(), result.getNlpRisk(), result.getVerdict()));

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
        resultPanel.remove(1);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Verdict row
        JLabel verdictLbl = new JLabel(glyph + "  " + result.getVerdict());
        verdictLbl.setFont(Theme.HEADING.deriveFont(Font.BOLD, 20f));
        verdictLbl.setForeground(tone);
        verdictLbl.setAlignmentX(LEFT_ALIGNMENT);

        // Score bars
        JLabel riskLbl = new JLabel("Overall Risk: " + result.getRiskScore() + " / 100");
        riskLbl.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
        riskLbl.setForeground(Theme.TEXT_DIM);
        riskLbl.setAlignmentX(LEFT_ALIGNMENT);
        riskLbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 3, 0));

        JProgressBar riskBar = scoreBar(result.getRiskScore(), tone);

        JLabel nlpLbl = new JLabel("NLP Signal: " + result.getNlpRisk() + " / 100");
        nlpLbl.setFont(Theme.BODY.deriveFont(12f));
        nlpLbl.setForeground(Theme.TEXT_DIM);
        nlpLbl.setAlignmentX(LEFT_ALIGNMENT);
        nlpLbl.setBorder(BorderFactory.createEmptyBorder(8, 0, 3, 0));

        JProgressBar nlpBar = scoreBar(result.getNlpRisk(), new Color(110, 90, 70));

        JLabel context = new JLabel(type + "  ·  " + company);
        context.setFont(Theme.BODY.deriveFont(Font.ITALIC, 12f));
        context.setForeground(Theme.TEXT_DIM);
        context.setAlignmentX(LEFT_ALIGNMENT);
        context.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));

        // Findings
        JLabel findingsLbl = new JLabel("Findings");
        findingsLbl.setFont(Theme.LABEL);
        findingsLbl.setForeground(Theme.TEXT_DIM);
        findingsLbl.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea findings = new JTextArea();
        findings.setEditable(false);
        findings.setFont(Theme.BODY.deriveFont(12f));
        findings.setBackground(Theme.BG_INPUT);
        findings.setForeground(Theme.TEXT);
        findings.setLineWrap(true);
        findings.setWrapStyleWord(true);
        findings.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        StringBuilder sb = new StringBuilder();
        for (String line : result.getFindings()) sb.append("• ").append(line).append("\n\n");
        findings.setText(sb.toString().trim());

        JScrollPane findingsScroll = new JScrollPane(findings);
        findingsScroll.setBorder(Theme.roundedBorder(Theme.BORDER, 8));
        findingsScroll.setAlignmentX(LEFT_ALIGNMENT);
        findingsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        // Feedback
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
        feedbackRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        feedbackRow.add(markFake);
        feedbackRow.add(markGenuine);

        JButton again = Theme.linkButton("+ Analyze another offer");
        again.setAlignmentX(LEFT_ALIGNMENT);
        again.addActionListener(e -> { clearFormFields(); resetResult(); });

        center.add(verdictLbl);
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

    // ── Helpers ───────────────────────────────────────────────────

    private JProgressBar scoreBar(int value, Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setStringPainted(false);
        bar.setBackground(Theme.BG_INPUT);
        bar.setForeground(color);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setAlignmentX(LEFT_ALIGNMENT);
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
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
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

    public void showFeedbackButtons(VerificationResult result, String description) {
        // feedback buttons are embedded in showResult
    }
}
