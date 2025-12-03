package UI.Panels;

import API.DTO.LoanDetailsRecord;
import API.DTO.MemberRecord;
import API.LibraryAPI;
import UI.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/// Panel for Member Management
public class MemberPanel extends JPanel {

    public MemberPanel() {
        super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Member");
        JButton updateBtn = new JButton("Update Member");
        JButton refreshBtn = new JButton("Refresh");
        JButton historyBtn = new JButton("View Borrowing History");

        topPanel.add(addBtn);
        topPanel.add(updateBtn);
        topPanel.add(refreshBtn);
        topPanel.add(historyBtn);

        // Filter by member type
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel filterLabel = new JLabel("Filter by Type:");
        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All", "Student", "Faculty", "Staff", "Public"});
        filterPanel.add(filterLabel);
        filterPanel.add(typeFilter);

        String[] columns = {"Member ID", "First Name", "Last Name", "Type", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        refreshBtn.addActionListener(e -> refreshMemberTable(model, (String) typeFilter.getSelectedItem()));
        typeFilter.addActionListener(e -> refreshMemberTable(model, (String) typeFilter.getSelectedItem()));
        refreshBtn.doClick();

        addBtn.addActionListener(e -> showAddMemberDialog(this, model));
        updateBtn.addActionListener(e -> showUpdateMemberDialog(this, table, model));
        historyBtn.addActionListener(e -> showBorrowingHistoryDialog(this, table));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.SOUTH);

        this.add(northPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

    }

    private static void refreshMemberTable(DefaultTableModel model, String memberType) {
        try {
            java.util.List<MemberRecord> members;
            if ("All".equals(memberType)) {
                members = LibraryAPI.getMembersByType("Student");
                members.addAll(LibraryAPI.getMembersByType("Faculty"));
                members.addAll(LibraryAPI.getMembersByType("Staff"));
                members.addAll(LibraryAPI.getMembersByType("Public"));
            } else {
                members = LibraryAPI.getMembersByType(memberType);
            }
            model.setRowCount(0);
            for (MemberRecord member : members) {
                model.addRow(new Object[]{
                        member.memberId(), member.fname(), member.lname(),
                        member.memberType().toString(), member.email()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading members: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddMemberDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Add New Member", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField fnameField = new JTextField(20);
        JTextField mnameField = new JTextField(20);
        JTextField lnameField = new JTextField(20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Student", "Faculty", "Staff", "Public"});
        JTextField emailField = new JTextField(20);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "First Name:", fnameField, row++);
        Utility.addLabelAndField(dialog, gbc, "Middle Name:", mnameField, row++);
        Utility.addLabelAndField(dialog, gbc, "Last Name:", lnameField, row++);
        Utility.addLabelAndField(dialog, gbc, "Member Type:", typeCombo, row++);
        Utility.addLabelAndField(dialog, gbc, "Email:", emailField, row++);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                int memberId = LibraryAPI.createMember(
                        fnameField.getText(),
                        mnameField.getText(),
                        lnameField.getText(),
                        (String) typeCombo.getSelectedItem(),
                        emailField.getText()
                );
                if (memberId > 0) {
                    JOptionPane.showMessageDialog(dialog, "Member added successfully! ID: " + memberId);
                    dialog.dispose();
                    refreshMemberTable(model, "All");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding member: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showUpdateMemberDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a member to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int memberId = (Integer) model.getValueAt(selectedRow, 0);
        String email = (String) model.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Update Member", true);
        dialog.setSize(350, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel idLabel = new JLabel("Member ID: " + memberId);
        JTextField emailField = new JTextField(email, 20);
        JTextField phoneField = new JTextField(20);

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row++;
        dialog.add(idLabel, gbc);
        Utility.addLabelAndField(dialog, gbc, "Email:", emailField, row++);
        Utility.addLabelAndField(dialog, gbc, "Phone:", phoneField, row++);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                if (LibraryAPI.updateMemberContact(memberId, emailField.getText(), phoneField.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Member updated successfully!");
                    dialog.dispose();
                    refreshMemberTable(model, "All");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating member: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showBorrowingHistoryDialog(JPanel parent, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a member to view history.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int memberId = (Integer) ((DefaultTableModel) table.getModel()).getValueAt(selectedRow, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Borrowing History", true);
        dialog.setSize(800, 400);

        String[] columns = {"Loan ID", "Book Title", "Issue Date", "Due Date", "Return Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable historyTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        try {
            List<LoanDetailsRecord> loans = LibraryAPI.getLoanDetailsForMember(memberId);
            for (LoanDetailsRecord loan : loans) {
                model.addRow(new Object[]{
                        loan.loanId(),
                        loan.bookTitle(),
                        loan.issueDate(),
                        loan.dueDate(),
                        loan.returnDate() != null ? loan.returnDate() : "Not Returned",
                        loan.loanStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading history: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        dialog.add(scrollPane);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
