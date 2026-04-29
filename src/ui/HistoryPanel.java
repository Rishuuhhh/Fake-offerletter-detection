package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import model.AnalysisRecord;
import store.HistoryStore;

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
        setSize(820, 400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header
        JLabel heading = new JLabel("Analysis History");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEAL);
        heading.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        add(heading, BorderLayout.NORTH);

        // Table
        String[] cols = {"Company", "Type", "Risk", "NLP", "Verdict"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(Theme.BODY.deriveFont(12f));
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT);
        table.setGridColor(new Color(230, 218, 200));
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.LABEL);
        header.setBackground(Theme.BG_INPUT);
        header.setForeground(Theme.TEXT_DIM);
        header.setReorderingAllowed(false);

        int[] widths = {170, 110, 55, 55, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Color-code Verdict column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setHorizontalAlignment(CENTER);
                if (!sel) {
                    String v = val == null ? "" : val.toString();
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        scroll.getViewport().setBackground(Theme.BG_CARD);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JButton close = new JButton("Close");
        close.setFont(Theme.BODY);
        close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Theme.BG_MAIN);
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadRecords() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        List<AnalysisRecord> records = store.findByUsername(username);
        if (records.isEmpty()) {
            model.addRow(new Object[]{"No records yet", "", "", "", ""});
            return;
        }
        for (AnalysisRecord r : records)
            model.addRow(new Object[]{r.getCompanyName(), r.getOfferType(),
                r.getRiskScore(), r.getNlpScore(), r.getVerdict()});
    }
}
