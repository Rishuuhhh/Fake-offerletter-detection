package ui;

import db.HistoryStore;
import model.AnalysisRecord;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Shows the logged-in user's past analyses in a clean table dialog.
 * Verdict cells are color-coded so you can spot risky ones at a glance.
 */
public class HistoryPanel extends JDialog {

    private final HistoryStore store;
    private final String       username;
    private JTable             table;

    public HistoryPanel(Window owner, HistoryStore store, String username) {
        super(owner, "History — " + username, ModalityType.APPLICATION_MODAL);
        this.store    = store;
        this.username = username;
        buildUI();
        loadRecords();
        setSize(860, 440);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout(0, 0));

        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_MAIN);
        top.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        JLabel heading = new JLabel("Analysis History");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEAL_LIGHT);
        top.add(heading, BorderLayout.WEST);

        JLabel sub = new JLabel("Most recent first");
        sub.setFont(Theme.SMALL);
        sub.setForeground(Theme.TEXT_DIM);
        top.add(sub, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        String[] cols = {"Timestamp", "Company", "Type", "Risk", "NLP", "Verdict"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(Theme.BODY.deriveFont(12f));
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT);
        table.setGridColor(new Color(230, 218, 200));
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(200, 235, 225));
        table.setSelectionForeground(Theme.TEXT);

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.LABEL);
        header.setBackground(Theme.BG_INPUT);
        header.setForeground(Theme.TEXT_DIM);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {170, 170, 115, 55, 55, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Color-code the Verdict column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setHorizontalAlignment(CENTER);
                String v = val == null ? "" : val.toString();
                if (!sel) {
                    switch (v) {
                        case "GENUINE":    lbl.setForeground(Theme.GREEN);  break;
                        case "SUSPICIOUS": lbl.setForeground(Theme.YELLOW); break;
                        case "FAKE":       lbl.setForeground(Theme.RED);    break;
                        default:           lbl.setForeground(Theme.TEXT);
                    }
                }
                lbl.setFont(Theme.BODY.deriveFont(Font.BOLD, 12f));
                return lbl;
            }
        });

        // Center-align Risk and NLP columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setBackground(Theme.BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Theme.BG_MAIN);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 20, 12, 20));

        JButton close = new JButton("Close");
        close.setFont(Theme.BODY);
        close.setForeground(Theme.TEXT_DIM);
        close.setFocusPainted(false);
        close.addActionListener(e -> dispose());
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadRecords() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        List<AnalysisRecord> records = store.findByUsername(username);
        for (AnalysisRecord r : records) {
            model.addRow(new Object[]{
                r.getTimestamp(), r.getCompanyName(), r.getOfferType(),
                r.getRiskScore(), r.getNlpScore(), r.getVerdict()
            });
        }
        if (records.isEmpty()) {
            model.addRow(new Object[]{"No records yet", "", "", "", "", ""});
        }
    }
}
