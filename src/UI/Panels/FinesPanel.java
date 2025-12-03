package UI.Panels;

import API.FinesAPI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/// Fines management panel
public class FinesPanel extends JPanel {

    public FinesPanel() {
        super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton calculateBtn = new JButton("Calculate Overdue Fines (75+ days)");
        JButton viewUnpaidBtn = new JButton("View Unpaid Fines");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(calculateBtn);
        topPanel.add(viewUnpaidBtn);
        topPanel.add(refreshBtn);

        String[] columns = {"Member Name", "Amount", "Applied Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        calculateBtn.addActionListener(e -> {
            try {
                FinesAPI.calculateOverdueFines();
                JOptionPane.showMessageDialog(this, "Fines calculated for books overdue 75+ days.");
                refreshFineTable(model);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error calculating fines: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewUnpaidBtn.addActionListener(e -> refreshFineTable(model));
        refreshBtn.addActionListener(e -> refreshFineTable(model));

        refreshFineTable(model);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private static void refreshFineTable(DefaultTableModel model) {
        try {
            List<Map<String, Object>> fines = FinesAPI.getMembersWithUnpaidFines();
            model.setRowCount(0);
            for (Map<String, Object> fine : fines) {
                model.addRow(new Object[]{
                        fine.get("MemberName"),
                        fine.get("amount"),
                        fine.get("applied_date"),
                        fine.get("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading fines: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
