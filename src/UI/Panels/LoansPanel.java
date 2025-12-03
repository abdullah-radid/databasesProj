package UI.Panels;

import API.BooksAPI;
import API.DTO.BookInventoryRecord;
import API.DTO.LoanDetailsRecord;
import API.LibraryAPI;
import UI.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LoansPanel extends JPanel {
    private static int currentStaffId = 1; // Default staff ID for issuing books
    // ==================== LOAN MANAGEMENT PANEL ====================
    public LoansPanel() {
      super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton issueBtn = new JButton("Issue Book");
        JButton returnBtn = new JButton("Return Book");
        JButton renewBtn = new JButton("Renew Loan");
        JButton checkAvailabilityBtn = new JButton("Check Availability");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(issueBtn);
        topPanel.add(returnBtn);
        topPanel.add(renewBtn);
        topPanel.add(checkAvailabilityBtn);
        topPanel.add(refreshBtn);

        String[] columns = {"Loan ID", "Member Name", "Book Title", "Issue Date", "Due Date", "Return Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        refreshBtn.addActionListener(e -> refreshLoanTable(model));
        issueBtn.addActionListener(e -> showIssueBookDialog(this, model));
        returnBtn.addActionListener(e -> showReturnBookDialog(this, table, model));
        renewBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Renewal functionality requires additional API methods.", "Not Implemented",
                JOptionPane.INFORMATION_MESSAGE));
        checkAvailabilityBtn.addActionListener(e -> showCheckAvailabilityDialog(this));

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);


    }

    private static void refreshLoanTable(DefaultTableModel model) {
        try {
            java.util.List<LoanDetailsRecord> loans = LibraryAPI.getAllLoanDetails();
            model.setRowCount(0);
            for (LoanDetailsRecord loan : loans) {
                model.addRow(new Object[]{
                        loan.loanId(),
                        loan.memberName(),
                        loan.bookTitle(),
                        loan.issueDate(),
                        loan.dueDate(),
                        loan.returnDate() != null ? loan.returnDate() : "Not Returned",
                        loan.loanStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading loans: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showIssueBookDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Issue Book", true);
        dialog.setSize(350, 200);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField memberIdField = new JTextField(20);
        JTextField copyIdField = new JTextField(20);
        JTextField staffIdField = new JTextField(String.valueOf(currentStaffId), 20);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "Member ID:", memberIdField, row++);
        Utility.addLabelAndField(dialog, gbc, "Copy ID:", copyIdField, row++);
        Utility.addLabelAndField(dialog, gbc, "Staff ID:", staffIdField, row++);

        JButton issueBtn = new JButton("Issue");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(issueBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        issueBtn.addActionListener(e -> {
            try {
                int memberId = Integer.parseInt(memberIdField.getText());
                int copyId = Integer.parseInt(copyIdField.getText());
                int staffId = Integer.parseInt(staffIdField.getText());

                String result = LibraryAPI.issueBook(memberId, copyId, staffId);
                JOptionPane.showMessageDialog(dialog, result);
                if (result.startsWith("Success")) {
                    dialog.dispose();
                    refreshLoanTable(model);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error issuing book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showReturnBookDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a loan to return.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loanId = (Integer) model.getValueAt(selectedRow, 0);
        String bookTitle = (String) model.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(parent,
                "Return loan #" + loanId + " for '" + bookTitle + "'?",
                "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String result = LibraryAPI.returnBook(loanId);
                JOptionPane.showMessageDialog(parent, result);
                if (result.startsWith("Success")) {
                    refreshLoanTable(model);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error returning book: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void showCheckAvailabilityDialog(JPanel parent) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Check Book Availability", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search by Title/ISBN");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        String[] columns = {"ISBN", "Title", "Author", "Copy ID", "Location", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        searchBtn.addActionListener(e -> {
            try {
                String searchText = searchField.getText();
                List<BookInventoryRecord> books = BooksAPI.getAllBookInventory();
                model.setRowCount(0);
                for (BookInventoryRecord book : books) {
                    if (book.title().toLowerCase().contains(searchText.toLowerCase()) ||
                            book.isbn().contains(searchText)) {
                        model.addRow(new Object[]{
                                book.isbn(), book.title(), book.author(),
                                book.copyId(), book.location(), book.status()
                        });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
