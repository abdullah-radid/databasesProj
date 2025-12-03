package UI.Panels;

import API.FinesAPI;
import UI.Utility;

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
        JButton addFineBtn = new JButton("Add Fine Manually");
        JButton viewUnpaidBtn = new JButton("View Unpaid Fines");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(calculateBtn);
        topPanel.add(addFineBtn);
        topPanel.add(viewUnpaidBtn);
        topPanel.add(refreshBtn);

        String[] columns = { "Member Name", "Amount", "Applied Date", "Status" };
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

        addFineBtn.addActionListener(e -> showAddFineDialog(this, model));
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
                model.addRow(new Object[] {
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

    private void showAddFineDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Add Fine", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField loanIdField = new JTextField(10);
        JTextField amountField = new JTextField(10);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "Loan ID:", loanIdField, row++);
        Utility.addLabelAndField(dialog, gbc, "Amount:", amountField, row++);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 1;
        gbc.gridy = row;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                int loanId = Integer.parseInt(loanIdField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                FinesAPI.addManualFine(loanId, amount);
                JOptionPane.showMessageDialog(dialog, "Fine added successfully.");
                dialog.dispose();
                refreshFineTable(model);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding fine (Check Loan ID): " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
