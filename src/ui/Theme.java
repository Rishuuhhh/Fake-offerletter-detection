package ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Shared colors, fonts, and small UI helpers used across all screens.
 * Keeping everything in one place makes it easy to tweak the look later.
 */
public final class Theme {

    private Theme() {}

    // Colors
    public static final Color BG_MAIN    = new Color(245, 236, 220);
    public static final Color BG_CARD    = new Color(255, 252, 246);
    public static final Color BG_INPUT   = new Color(252, 246, 235);
    public static final Color TEAL       = new Color(15, 112, 99);
    public static final Color TEAL_LIGHT = new Color(28, 151, 131);
    public static final Color AMBER      = new Color(210, 130, 50);
    public static final Color TEXT       = new Color(45, 47, 44);
    public static final Color TEXT_DIM   = new Color(105, 100, 93);
    public static final Color BORDER     = new Color(210, 194, 172);
    public static final Color RED        = new Color(215, 50, 65);
    public static final Color GREEN      = new Color(30, 185, 85);
    public static final Color YELLOW     = new Color(240, 170, 0);

    // Fonts
    public static final Font TITLE   = new Font("Georgia",      Font.BOLD,  26);
    public static final Font HEADING = new Font("Trebuchet MS", Font.BOLD,  15);
    public static final Font BODY    = new Font("Trebuchet MS", Font.PLAIN, 13);
    public static final Font SMALL   = new Font("Trebuchet MS", Font.PLAIN, 11);
    public static final Font LABEL   = new Font("Segoe UI",     Font.BOLD,  12);

    /** A simple rounded border. */
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

    /** Draws a gradient-filled rounded rectangle as a card background. */
    public static JPanel card() {
        return new JPanel() {
            { setOpaque(false); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            }
        };
    }

    /** Full-screen gradient background panel. */
    public static JPanel gradientBg() {
        return new JPanel() {
            { setOpaque(false); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, BG_MAIN, 0, getHeight(), new Color(238, 222, 198)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle decorative blobs
                g2.setColor(new Color(20, 120, 105, 22));
                g2.fillOval(-120, -90, 320, 320);
                g2.setColor(new Color(210, 130, 50, 28));
                g2.fillOval(getWidth() - 260, getHeight() - 210, 300, 300);
            }
        };
    }

    /** A gradient-filled primary action button. */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            { setContentAreaFilled(false); setOpaque(false); setFocusPainted(false); setBorderPainted(false); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, TEAL, getWidth(), 0, TEAL_LIGHT));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BODY.deriveFont(Font.BOLD, 14f));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    /** A plain text-style link button. */
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

    /** Styled text field with placeholder text. */
    public static JTextField inputField(String placeholder) {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(145, 130, 110));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleInput(f);
        return f;
    }

    /** Styled password field. */
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
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
            roundedBorder(BORDER, 8),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    /** Small field label. */
    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(LABEL);
        lbl.setForeground(TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Draws a simple shield icon with a checkmark. */
    public static ImageIcon shieldIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] xs = {size/2, size-4, size-4, size/2, 4, 4};
        int[] ys = {2, size/5, size*3/5, size-4, size*3/5, size/5};
        g.setPaint(new GradientPaint(0, 0, TEAL, size, size, TEAL_LIGHT));
        g.fillPolygon(xs, ys, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Georgia", Font.BOLD, size / 2));
        FontMetrics fm = g.getFontMetrics();
        String mark = "✓";
        g.drawString(mark, (size - fm.stringWidth(mark)) / 2, size * 3 / 5);
        g.dispose();
        return new ImageIcon(img);
    }
}
