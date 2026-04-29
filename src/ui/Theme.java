package ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class Theme {

    private Theme() {}

    // Colors
    public static final Color BG_MAIN  = new Color(245, 236, 220);
    public static final Color BG_CARD  = new Color(255, 252, 246);
    public static final Color BG_INPUT = new Color(252, 246, 235);
    public static final Color TEAL     = new Color(15, 112, 99);
    public static final Color TEAL_LIGHT = new Color(28, 151, 131);
    public static final Color AMBER    = new Color(210, 130, 50);
    public static final Color TEXT     = new Color(45, 47, 44);
    public static final Color TEXT_DIM = new Color(105, 100, 93);
    public static final Color BORDER   = new Color(210, 194, 172);
    public static final Color RED      = new Color(215, 50, 65);
    public static final Color GREEN    = new Color(30, 185, 85);
    public static final Color YELLOW   = new Color(240, 170, 0);

    // Fonts
    public static final Font TITLE   = new Font("Georgia",      Font.BOLD,  24);
    public static final Font HEADING = new Font("Trebuchet MS", Font.BOLD,  14);
    public static final Font BODY    = new Font("Trebuchet MS", Font.PLAIN, 13);
    public static final Font SMALL   = new Font("Trebuchet MS", Font.PLAIN, 11);
    public static final Font LABEL   = new Font("Segoe UI",     Font.BOLD,  12);

    public static Border roundedBorder(Color color, int radius) {
        return new Border() {
            public Insets getBorderInsets(Component c) {
                return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
            }
            public boolean isBorderOpaque() { return false; }
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            }
        };
    }

    /** Flat rounded card panel. */
    public static JPanel card() {
        return new JPanel() {
            { setOpaque(false); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            }
        };
    }

    /** Solid background panel (no gradient, no blobs). */
    public static JPanel bg() {
        JPanel p = new JPanel();
        p.setBackground(BG_MAIN);
        p.setOpaque(true);
        return p;
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            { setContentAreaFilled(false); setOpaque(false); setFocusPainted(false); setBorderPainted(false); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BODY.deriveFont(Font.BOLD, 13f));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return btn;
    }

    public static JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BODY.deriveFont(Font.BOLD, 12f));
        btn.setForeground(TEAL);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField inputField(String placeholder) {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    ((Graphics2D) g).setColor(new Color(145, 130, 110));
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleInput(f);
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        styleInput(f);
        return f;
    }

    private static void styleInput(JTextField f) {
        f.setFont(BODY);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT);
        f.setCaretColor(TEAL);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
            roundedBorder(BORDER, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(LABEL);
        lbl.setForeground(TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
