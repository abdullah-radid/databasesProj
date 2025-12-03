package UI.Panels;

import API.DTO.LoanDetailsRecord;
import API.FinesAPI;
import API.LoansAPI;
import API.ReportsAPI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton mostBorrowedBtn = new JButton("Most Borrowed Book (Last Quarter)");
        JButton overdueBtn = new JButton("Overdue Items");
        JButton finesReportBtn = new JButton("Fines Report (Last Quarter)");
        JButton neverBorrowedBtn = new JButton("Never Borrowed Books");
        JButton totalLoansBtn = new JButton("Total Loans per Book");

        topPanel.add(mostBorrowedBtn);
        topPanel.add(overdueBtn);
        topPanel.add(finesReportBtn);
        topPanel.add(neverBorrowedBtn);
        topPanel.add(totalLoansBtn);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Column 1", "Column 2"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        mostBorrowedBtn.addActionListener(e -> {
            try {
                Optional<Map.Entry<String, Integer>> result = ReportsAPI.getMostBorrowedBookLastQuarter();
                model.setColumnCount(2);
                model.setColumnIdentifiers(new String[]{"Book Title", "Borrow Count"});
                model.setRowCount(0);
                if (result.isPresent()) {
                    model.addRow(new Object[]{result.get().getKey(), result.get().getValue()});
                } else {
                    model.addRow(new Object[]{"No data available", ""});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        overdueBtn.addActionListener(e -> {
            try {
                List<LoanDetailsRecord> overdue = LoansAPI.getOverdueLoans();
                model.setColumnCount(6);
                model.setColumnIdentifiers(new String[]{"Loan ID", "Member Name", "Book Title",
                        "Issue Date", "Due Date", "Days Overdue"});
                model.setRowCount(0);
                for (LoanDetailsRecord loan : overdue) {
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                            loan.dueDate(), LocalDate.now());
                    model.addRow(new Object[]{
                            loan.loanId(),
                            loan.memberName(),
                            loan.bookTitle(),
                            loan.issueDate(),
                            loan.dueDate(),
                            daysOverdue
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        finesReportBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> fines = FinesAPI.getFinesLastQuarter();
                model.setColumnCount(3);
                model.setColumnIdentifiers(new String[]{"Member Name", "Amount", "Applied Date"});
                model.setRowCount(0);
                for (Map<String, Object> fine : fines) {
                    model.addRow(new Object[]{
                            fine.get("MemberName"),
                            fine.get("amount"),
                            fine.get("applied_date")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        neverBorrowedBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> books = ReportsAPI.getBooksNeverBorrowed();
                model.setColumnCount(3);
                model.setColumnIdentifiers(new String[]{"Title", "ISBN", "Author"});
                model.setRowCount(0);
                for (Map<String, Object> book : books) {
                    model.addRow(new Object[]{
                            book.get("Title"),
                            book.get("ISBN"),
                            book.get("Author")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        totalLoansBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> books = ReportsAPI.getTotalLoansPerBook();
                model.setColumnCount(3);
                model.setColumnIdentifiers(new String[]{"ISBN", "Title", "Total Loans"});
                model.setRowCount(0);
                for (Map<String, Object> book : books) {
                    model.addRow(new Object[]{
                            book.get("ISBN"),
                            book.get("Title"),
                            book.get("TotalLoans")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }
}
