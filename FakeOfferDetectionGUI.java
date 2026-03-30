import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

import model.InternshipOffer;
import model.JobOffer;
import model.Offer;
import model.VerificationResult;
import service.VerificationEngine;

/**
 * Fake Internship / Job Offer Detection System
 * Team: JVM Juggernauts (JAVA-IV-T062)
 * Frontend: Java Swing GUI
 */
public class FakeOfferDetectionGUI extends JFrame {

    // ── Color Palette ──────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(247, 238, 224);
    private static final Color BG_CARD       = new Color(255, 252, 246);
    private static final Color BG_INPUT      = new Color(253, 247, 237);
    private static final Color ACCENT_BLUE   = new Color(15, 112, 99);
    private static final Color ACCENT_CYAN   = new Color(28, 151, 131);
    private static final Color ACCENT_GOLD   = new Color(222, 142, 62);
    private static final Color TEXT_PRIMARY  = new Color(47, 49, 46);
    private static final Color TEXT_MUTED    = new Color(108, 103, 96);
    private static final Color BORDER_COLOR  = new Color(214, 198, 176);
    private static final Color GENUINE_COLOR = new Color(35, 197, 94);
    private static final Color SUSPICIOUS_COLOR = new Color(255, 179, 0);
    private static final Color FAKE_COLOR    = new Color(248, 81, 73);

    private static final Font FONT_TITLE = new Font("Georgia", Font.BOLD, 31);
    private static final Font FONT_HEADING = new Font("Trebuchet MS", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Trebuchet MS", Font.PLAIN, 13);

    // ── Form fields ────────────────────────────────────────────────
    private JTextField companyField, emailField, salaryField, positionField;
    private JTextArea  descriptionArea;
    private JCheckBox  feeCheckBox, urgencyCheckBox, personalInfoCheckBox;
    private JComboBox<String> offerTypeCombo;

    // ── Result panel ───────────────────────────────────────────────
    private JPanel    resultPanel;
    private JLabel    resultLabel, resultIcon, riskScoreLabel;
    private JProgressBar riskBar;
    private JTextArea detailsArea;

    public FakeOfferDetectionGUI() {
        setTitle("Fake Internship / Job Offer Detection System  ·  JVM Juggernauts");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 780);
        setLocationRelativeTo(null);
        setResizable(true);

        // root panel with gradient background
        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildBody(),    BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Header ─────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 32, 12, 32));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);

        // shield icon
        JLabel shieldIcon = new JLabel(createShieldIcon(40));
        titleRow.add(shieldIcon);

        JPanel titleText = new JPanel();
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.setOpaque(false);

        JLabel title = new JLabel("Fake Offer Detector");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("A quick authenticity check for internship and job offers");
        subtitle.setFont(FONT_BODY.deriveFont(12f));
        subtitle.setForeground(TEXT_MUTED);

        titleText.add(title);
        titleText.add(Box.createVerticalStrut(2));
        titleText.add(subtitle);
        titleRow.add(titleText);

        header.add(titleRow, BorderLayout.WEST);

        // team badge
        JLabel badge = new RoundedLabel("  JAVA-IV-T062  ", ACCENT_GOLD, 10);
        badge.setFont(FONT_BODY.deriveFont(Font.BOLD, 11f));
        badge.setForeground(Color.WHITE);
        header.add(badge, BorderLayout.EAST);

        // separator
        JPanel sep = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,ACCENT_BLUE,getWidth(),0,ACCENT_CYAN));
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

    // ── Body (form + result side by side) ─────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.55;
        body.add(buildFormPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.45;
        body.add(buildResultPanel(), gbc);

        return body;
    }

    // ── Form Panel ─────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel cardTitle = new JLabel("  Enter Offer Details");
        cardTitle.setFont(FONT_HEADING);
        cardTitle.setForeground(ACCENT_CYAN);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        g.weightx = 1.0;

        int row = 0;

        // Offer Type
        offerTypeCombo = styledCombo(new String[]{"Job Offer", "Internship Offer"});
        addFormRow(form, g, row++, "Offer Type", offerTypeCombo);

        // Company Name
        companyField = styledField("e.g.  Amazon, Google, XYZ Pvt. Ltd.");
        addFormRow(form, g, row++, "Company Name", companyField);

        // Email ID
        emailField = styledField("e.g.  hr@company.com");
        addFormRow(form, g, row++, "Sender Email", emailField);

        // Position
        positionField = styledField("e.g.  Software Developer Intern");
        addFormRow(form, g, row++, "Position / Role", positionField);

        // Salary / Stipend
        salaryField = styledField("e.g.  50000 (monthly, in INR)");
        addFormRow(form, g, row++, "Salary / Stipend (₹)", salaryField);

        // Job Description
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(FONT_BODY);
        descriptionArea.setBackground(BG_INPUT);
        descriptionArea.setForeground(TEXT_PRIMARY);
        descriptionArea.setCaretColor(ACCENT_BLUE);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(new RoundedBorder(BORDER_COLOR, 8));
        descScroll.setBackground(BG_INPUT);
        descScroll.getViewport().setBackground(BG_INPUT);
        addFormRow(form, g, row++, "Job Description", descScroll);

        // Checkboxes
        JPanel checkPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        checkPanel.setOpaque(false);
        feeCheckBox        = styledCheck("Requires registration / security fee");
        urgencyCheckBox    = styledCheck("Uses urgent / pressure language");
        personalInfoCheckBox = styledCheck("Asks for Aadhaar / bank / OTP upfront");
        checkPanel.add(feeCheckBox);
        checkPanel.add(urgencyCheckBox);
        checkPanel.add(personalInfoCheckBox);
        addFormRow(form, g, row++, "Red Flags", checkPanel);

        card.add(form, BorderLayout.CENTER);

        // Analyze Button
        JButton analyzeBtn = new JButton("Analyze Offer") {
            { setContentAreaFilled(false); setOpaque(true); setFocusPainted(false); }
            protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,ACCENT_BLUE,getWidth(),0,ACCENT_CYAN);
                g.setPaint(gp);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
            }
        };
        analyzeBtn.setFont(FONT_BODY.deriveFont(Font.BOLD, 14f));
        analyzeBtn.setForeground(Color.WHITE);
        analyzeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        analyzeBtn.setPreferredSize(new Dimension(0, 44));
        analyzeBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        analyzeBtn.addActionListener(e -> analyzeOffer());

        JButton clearBtn = new JButton("Clear") {
            { setContentAreaFilled(false); setOpaque(false); setFocusPainted(false); }
        };
        clearBtn.setFont(FONT_BODY);
        clearBtn.setForeground(TEXT_MUTED);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnRow = new JPanel(new BorderLayout(10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        btnRow.add(analyzeBtn, BorderLayout.CENTER);
        btnRow.add(clearBtn, BorderLayout.EAST);

        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx = 0; g.gridy = row * 2; g.insets = new Insets(8, 0, 2, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        form.add(lbl, g);

        g.gridy = row * 2 + 1; g.insets = new Insets(0, 0, 0, 0);
        form.add(comp, g);
    }

    // ── Result Panel ───────────────────────────────────────────────
    private JPanel buildResultPanel() {
        resultPanel = new CardPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel cardTitle = new JLabel("  Analysis Result");
        cardTitle.setFont(FONT_HEADING);
        cardTitle.setForeground(ACCENT_CYAN);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        resultPanel.add(cardTitle, BorderLayout.NORTH);

        // Placeholder / idle state
        resultPanel.add(buildIdleState(), BorderLayout.CENTER);
        return resultPanel;
    }

    private JPanel buildIdleState() {
        JPanel idle = new JPanel(new GridBagLayout());
        idle.setOpaque(false);
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel icon = new JLabel(createShieldIcon(72));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Fill in the offer details and");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(TEXT_MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg2 = new JLabel("click Analyze to get results.");
        msg2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg2.setForeground(TEXT_MUTED);
        msg2.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createVerticalStrut(16));
        inner.add(msg);
        inner.add(Box.createVerticalStrut(4));
        inner.add(msg2);
        idle.add(inner);
        return idle;
    }

    // ── Footer ─────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        footer.setOpaque(false);
        JLabel lbl = new JLabel("TCS-408 · JVM Juggernauts · Shanu Khatana · Disha Jha · Rakshit Sharma · Tanuja Kanswal");
        lbl.setFont(FONT_BODY.deriveFont(11f));
        lbl.setForeground(new Color(118, 106, 91));
        footer.add(lbl);
        return footer;
    }

    // ── Verification Engine ────────────────────────────────────────
    private void analyzeOffer() {
        String companyNameInput = companyField.getText().trim();
        String senderEmailInput = emailField.getText().trim();
        String salaryInput = salaryField.getText().trim();
        String roleInput = positionField.getText().trim();
        String descriptionInput = descriptionArea.getText().trim();
        boolean feeRequested = feeCheckBox.isSelected();
        boolean urgencyFlag = urgencyCheckBox.isSelected();
        boolean personalInfoFlag = personalInfoCheckBox.isSelected();
        String selectedOfferType = (String) offerTypeCombo.getSelectedItem();

        if (companyNameInput.isEmpty() || senderEmailInput.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in at least Company Name and Sender Email.",
                "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double monthlyPay = parseSalaryValue(salaryInput);
        Offer offerDraft;
        if (selectedOfferType != null && selectedOfferType.toLowerCase(Locale.ROOT).contains("intern")) {
            offerDraft = new InternshipOffer(companyNameInput, senderEmailInput, monthlyPay, descriptionInput, feeRequested);
        } else {
            offerDraft = new JobOffer(companyNameInput, senderEmailInput, monthlyPay, descriptionInput, feeRequested);
        }

        VerificationResult auditResult = VerificationEngine.evaluateDetailed(
                offerDraft,
                urgencyFlag,
                personalInfoFlag,
                roleInput,
                selectedOfferType
        );

        Color verdictTone;
        String verdictGlyph;
        if ("GENUINE".equals(auditResult.getVerdict())) {
            verdictTone = GENUINE_COLOR;
            verdictGlyph = "OK";
        } else if ("SUSPICIOUS".equals(auditResult.getVerdict())) {
            verdictTone = SUSPICIOUS_COLOR;
            verdictGlyph = "!";
        } else {
            verdictTone = FAKE_COLOR;
            verdictGlyph = "X";
        }

        showResult(auditResult, verdictTone, verdictGlyph, selectedOfferType, companyNameInput);
    }

    private void showResult(VerificationResult auditResult, Color toneColor, String verdictGlyph,
                            String offerTypeText, String companyText) {
        resultPanel.remove(1);  // remove center component

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Verdict badge
        JPanel verdictCard = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color badgeBg = new Color(toneColor.getRed(), toneColor.getGreen(), toneColor.getBlue(), 28);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(toneColor.getRed(), toneColor.getGreen(), toneColor.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            }
        };
        verdictCard.setOpaque(false);
        verdictCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel vInner = new JPanel();
        vInner.setLayout(new BoxLayout(vInner, BoxLayout.Y_AXIS));
        vInner.setOpaque(false);

        JLabel vIcon = new JLabel(verdictGlyph);
        vIcon.setFont(new Font("Trebuchet MS", Font.BOLD, 34));
        vIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel vLabel = new JLabel(auditResult.getVerdict());
        vLabel.setFont(FONT_HEADING.deriveFont(Font.BOLD, 24f));
        vLabel.setForeground(toneColor);
        vLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        vInner.add(vIcon);
        vInner.add(Box.createVerticalStrut(4));
        vInner.add(vLabel);
        verdictCard.add(vInner);

        // Risk score bar
        JLabel riskLbl = new JLabel("Overall Risk Score: " + auditResult.getRiskScore() + " / 100");
        riskLbl.setFont(FONT_BODY.deriveFont(Font.BOLD, 13f));
        riskLbl.setForeground(toneColor);
        riskLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        riskLbl.setBorder(BorderFactory.createEmptyBorder(14, 2, 4, 0));

        JProgressBar riskMeter = new JProgressBar(0, 100);
        riskMeter.setValue(auditResult.getRiskScore());
        riskMeter.setStringPainted(false);
        riskMeter.setBackground(BG_INPUT);
        riskMeter.setForeground(toneColor);
        riskMeter.setBorderPainted(false);
        riskMeter.setPreferredSize(new Dimension(0, 10));
        riskMeter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        riskMeter.setAlignmentX(Component.LEFT_ALIGNMENT);
        riskMeter.setBorder(new RoundedBorder(BG_INPUT, 6));

        JLabel nlpLabel = new JLabel("NLP Signal Score: " + auditResult.getNlpRisk() + " / 100");
        nlpLabel.setFont(FONT_BODY.deriveFont(Font.BOLD, 12f));
        nlpLabel.setForeground(TEXT_MUTED);
        nlpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nlpLabel.setBorder(BorderFactory.createEmptyBorder(8, 2, 2, 0));

        JProgressBar nlpMeter = new JProgressBar(0, 100);
        nlpMeter.setValue(auditResult.getNlpRisk());
        nlpMeter.setStringPainted(false);
        nlpMeter.setBackground(BG_INPUT);
        nlpMeter.setForeground(new Color(116, 95, 74));
        nlpMeter.setBorderPainted(false);
        nlpMeter.setPreferredSize(new Dimension(0, 8));
        nlpMeter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        nlpMeter.setAlignmentX(Component.LEFT_ALIGNMENT);
        nlpMeter.setBorder(new RoundedBorder(BG_INPUT, 6));

        // Context label
        JLabel contextLbl = new JLabel(offerTypeText + "  ·  " + companyText);
        contextLbl.setFont(FONT_BODY.deriveFont(Font.ITALIC, 12f));
        contextLbl.setForeground(TEXT_MUTED);
        contextLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        contextLbl.setBorder(BorderFactory.createEmptyBorder(10, 2, 6, 0));

        // Findings list
        JLabel findingsTitle = new JLabel("Detailed Findings");
        findingsTitle.setFont(FONT_BODY.deriveFont(Font.BOLD, 12f));
        findingsTitle.setForeground(TEXT_MUTED);
        findingsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        findingsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JTextArea notesArea = new JTextArea();
        notesArea.setEditable(false);
        notesArea.setFont(FONT_BODY.deriveFont(12f));
        notesArea.setBackground(BG_INPUT);
        notesArea.setForeground(TEXT_PRIMARY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        StringBuilder detailsBuilder = new StringBuilder();
        for (String line : auditResult.getFindings()) detailsBuilder.append("- ").append(line).append("\n\n");
        notesArea.setText(detailsBuilder.toString().trim());

        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesScrollPane.setBorder(new RoundedBorder(BORDER_COLOR, 8));
        notesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        center.add(verdictCard);
        center.add(riskLbl);
        center.add(riskMeter);
        center.add(nlpLabel);
        center.add(nlpMeter);
        center.add(contextLbl);
        center.add(findingsTitle);
        center.add(notesScrollPane);

        // New analysis button
        JButton newBtn = new JButton("+ Analyze Another Offer") {
            { setContentAreaFilled(false); setOpaque(false); setFocusPainted(false); }
        };
        newBtn.setFont(FONT_BODY.deriveFont(Font.BOLD, 12f));
        newBtn.setForeground(ACCENT_BLUE);
        newBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newBtn.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        newBtn.addActionListener(e -> { clearForm(); resetResult(); });
        center.add(newBtn);

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

    private void clearForm() {
        companyField.setText("");
        emailField.setText("");
        salaryField.setText("");
        positionField.setText("");
        descriptionArea.setText("");
        feeCheckBox.setSelected(false);
        urgencyCheckBox.setSelected(false);
        personalInfoCheckBox.setSelected(false);
        offerTypeCombo.setSelectedIndex(0);
    }

    private double parseSalaryValue(String salaryText) {
        if (salaryText == null || salaryText.trim().isEmpty()) {
            return 0;
        }
        String normalized = salaryText.replaceAll("[^0-9.]", "");
        if (normalized.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // ── Styled component helpers ───────────────────────────────────
    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(148, 133, 113));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(FONT_BODY);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 8),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(FONT_BODY);
        c.setBackground(BG_INPUT);
        c.setForeground(TEXT_PRIMARY);
        c.setBorder(new RoundedBorder(BORDER_COLOR, 8));
        ((JComponent) c.getRenderer()).setOpaque(true);
        return c;
    }

    private JCheckBox styledCheck(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(FONT_BODY.deriveFont(12f));
        cb.setForeground(TEXT_PRIMARY);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        return cb;
    }

    // ── Shield icon ───────────────────────────────────────────────
    private ImageIcon createShieldIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] xp = {size/2, size-4, size-4, size/2, 4, 4};
        int[] yp = {2, size/5, size*3/5, size-4, size*3/5, size/5};
        GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, size, size, ACCENT_CYAN);
        g.setPaint(gp);
        g.fillPolygon(xp, yp, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Georgia", Font.BOLD, size/2));
        FontMetrics fm = g.getFontMetrics();
        String ch = "✓";
        g.drawString(ch, (size - fm.stringWidth(ch))/2, size*3/5);
        g.dispose();
        return new ImageIcon(img);
    }

    // ── Inner helper classes ───────────────────────────────────────
    static class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, getHeight(), new Color(241, 226, 202)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(24, 125, 108, 30));
            g2.fillOval(-150, -110, 360, 360);
            g2.setColor(new Color(215, 141, 60, 35));
            g2.fillOval(getWidth() - 300, getHeight() - 240, 340, 340);
        }
    }

    static class CardPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
        }
    }

    static class RoundedLabel extends JLabel {
        private Color bg;
        private int arc;
        RoundedLabel(String text, Color bg, int arc) {
            super(text);
            this.bg = bg; this.arc = arc;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc*2, arc*2);
            super.paintComponent(g);
        }
    }

    static class RoundedBorder implements Border {
        private Color color;
        private int radius;
        RoundedBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, w-1, h-1, radius*2, radius*2);
        }
    }

    // ── Entry point ───────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FakeOfferDetectionGUI().setVisible(true));
    }
}
