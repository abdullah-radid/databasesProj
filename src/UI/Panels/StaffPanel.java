package UI.Panels;

import API.DTO.StaffRecord;
import API.StaffAPI;
import UI.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StaffPanel extends JPanel {

    public StaffPanel() {
        super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Staff");
        JButton updateBtn = new JButton("Update Staff");
        JButton removeBtn = new JButton("Remove Staff");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(addBtn);
        topPanel.add(updateBtn);
        topPanel.add(removeBtn);
        topPanel.add(refreshBtn);

        String[] columns = { "Staff ID", "First Name", "Last Name", "Email" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        refreshBtn.addActionListener(e -> refreshTable(model));
        addBtn.addActionListener(e -> showAddDialog(this, model));
        updateBtn.addActionListener(e -> showUpdateDialog(this, table, model));
        removeBtn.addActionListener(e -> removeStaff(this, table, model));

        refreshTable(model); // Initial load

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshTable(DefaultTableModel model) {
        try {
            List<StaffRecord> staffList = StaffAPI.getAllStaff();
            model.setRowCount(0);
            for (StaffRecord staff : staffList) {
                model.addRow(new Object[] { staff.staffId(), staff.fname(), staff.lname(), staff.email() });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading staff: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Add New Staff", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField fnameField = new JTextField(15);
        JTextField lnameField = new JTextField(15);
        JTextField emailField = new JTextField(15);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "First Name:", fnameField, row++);
        Utility.addLabelAndField(dialog, gbc, "Last Name:", lnameField, row++);
        Utility.addLabelAndField(dialog, gbc, "Email:", emailField, row++);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 1;
        gbc.gridy = row;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                int id = StaffAPI.addStaff(fnameField.getText().trim(), lnameField.getText().trim(),
                        emailField.getText().trim());
                if (id > 0) {
                    JOptionPane.showMessageDialog(dialog, "Staff added! ID: " + id);
                    dialog.dispose();
                    refreshTable(model);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void showUpdateDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Select a staff member to update.");
            return;
        }
        int staffId = (int) model.getValueAt(selectedRow, 0);
        String currentEmail = (String) model.getValueAt(selectedRow, 3);

        String newEmail = JOptionPane.showInputDialog(parent, "Enter new email:", currentEmail);
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            try {
                if (StaffAPI.updateStaff(staffId, newEmail.trim())) {
                    JOptionPane.showMessageDialog(parent, "Staff updated.");
                    refreshTable(model);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage());
            }
        }
    }

    private void removeStaff(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Select a staff member to remove.");
            return;
        }
        int staffId = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(parent, "Are you sure you want to remove this staff member?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (StaffAPI.deleteStaff(staffId)) {
                    JOptionPane.showMessageDialog(parent, "Staff removed.");
                    refreshTable(model);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage());
            }
        }
    }
}