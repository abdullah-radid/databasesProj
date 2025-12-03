package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import API.*;
import API.DTO.*;
import UI.Panels.*;


public class MainPage extends JFrame {
    private static int currentStaffId = 1; // Default staff ID for issuing books


    // Main Application Window
    public MainPage() {
        super("Library Management System");
        this.setSize(1200, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add all panels
        tabbedPane.addTab("Books", new BookPanel());
        tabbedPane.addTab("Members",new MemberPanel());
        tabbedPane.addTab("Loans", createLoanManagementPanel());
        tabbedPane.addTab("Fines", createFineManagementPanel());
        tabbedPane.addTab("Reservations", createReservationManagementPanel());
        tabbedPane.addTab("Study Rooms", createStudyRoomPanel());
        tabbedPane.addTab("Reports", createReportsPanel());

        this.add(tabbedPane);
        this.setVisible(true);
    }








    // ==================== LOAN MANAGEMENT PANEL ====================
    private static JPanel createLoanManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

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
        issueBtn.addActionListener(e -> showIssueBookDialog(panel, model));
        returnBtn.addActionListener(e -> showReturnBookDialog(panel, table, model));
        renewBtn.addActionListener(e -> JOptionPane.showMessageDialog(panel,
                "Renewal functionality requires additional API methods.", "Not Implemented",
                JOptionPane.INFORMATION_MESSAGE));
        checkAvailabilityBtn.addActionListener(e -> showCheckAvailabilityDialog(panel));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void refreshLoanTable(DefaultTableModel model) {
        try {
            List<LoanDetailsRecord> loans = LibraryAPI.getAllLoanDetails();
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

    // ==================== FINE MANAGEMENT PANEL ====================
    private static JPanel createFineManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

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
                LibraryAPI.calculateOverdueFines();
                JOptionPane.showMessageDialog(panel, "Fines calculated for books overdue 75+ days.");
                refreshFineTable(model);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error calculating fines: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewUnpaidBtn.addActionListener(e -> refreshUnpaidFinesTable(model));
        refreshBtn.addActionListener(e -> refreshFineTable(model));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void refreshFineTable(DefaultTableModel model) {
        try {
            List<Map<String, Object>> fines = LibraryAPI.getMembersWithUnpaidFines();
            model.setRowCount(0);
            for (Map<String, Object> fine : fines) {
                model.addRow(new Object[]{
                        fine.get("MemberName"),
                        fine.get("amount"),
                        fine.get("applied_date"),
                        "Unpaid"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading fines: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void refreshUnpaidFinesTable(DefaultTableModel model) {
        refreshFineTable(model);
    }

    // ==================== RESERVATION MANAGEMENT PANEL ====================
    private static JPanel createReservationManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Reservation");
        JButton updateBtn = new JButton("Update Status");
        JButton removeBtn = new JButton("Cancel Reservation");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(addBtn);
        topPanel.add(updateBtn);
        topPanel.add(removeBtn);
        topPanel.add(refreshBtn);

        String[] columns = {"Reservation ID", "Member ID", "Book ISBN", "Reservation Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        addBtn.addActionListener(e -> showAddReservationDialog(panel, model));
        updateBtn.addActionListener(e -> showUpdateReservationDialog(panel, table, model));
        removeBtn.addActionListener(e -> showCancelReservationDialog(panel, table, model));
        refreshBtn.addActionListener(e -> refreshReservationTable(model));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void showAddReservationDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Add Reservation", true);
        dialog.setSize(350, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField memberIdField = new JTextField(20);
        JTextField isbnField = new JTextField(20);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "Member ID:", memberIdField, row++);
        Utility.addLabelAndField(dialog, gbc, "Book ISBN:", isbnField, row++);

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
                int memberId = Integer.parseInt(memberIdField.getText());
                String isbn = isbnField.getText();
                if (LibraryAPI.reserveBook(memberId, isbn)) {
                    JOptionPane.showMessageDialog(dialog, "Reservation added successfully!");
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid member ID.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding reservation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void refreshReservationTable(DefaultTableModel model) {
        try {
            List<Map<String, Object>> reservations = LibraryAPI.getAllBookReservations();
            model.setRowCount(0);
            for (Map<String, Object> res : reservations) {
                model.addRow(new Object[]{
                        res.get("reservation_id"),
                        res.get("member_id"),
                        res.get("book_isbn"),
                        res.get("reservation_date"),
                        res.get("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading reservations: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showUpdateReservationDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a reservation to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reservationId = (Integer) model.getValueAt(selectedRow, 0);
        String currentStatus = (String) model.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Update Reservation Status", true);
        dialog.setSize(300, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel idLabel = new JLabel("Reservation ID: " + reservationId);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "Fulfilled", "Cancelled"});
        statusCombo.setSelectedItem(currentStatus);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(idLabel, gbc);
        Utility.addLabelAndField(dialog, gbc, "Status:", statusCombo, 1);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                if (LibraryAPI.updateReservationStatus(reservationId, (String) statusCombo.getSelectedItem())) {
                    JOptionPane.showMessageDialog(dialog, "Reservation updated successfully!");
                    dialog.dispose();
                    refreshReservationTable(model);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating reservation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showCancelReservationDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a reservation to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reservationId = (Integer) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(parent,
                "Cancel reservation #" + reservationId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (LibraryAPI.updateReservationStatus(reservationId, "Cancelled")) {
                    JOptionPane.showMessageDialog(parent, "Reservation cancelled successfully.");
                    refreshReservationTable(model);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error cancelling reservation: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== STUDY ROOM PANEL ====================
    private static JPanel createStudyRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewReservationsBtn = new JButton("View Room Reservations");
        JButton searchByDateBtn = new JButton("Search by Date");

        topPanel.add(viewReservationsBtn);
        topPanel.add(searchByDateBtn);

        String[] columns = {"Reservation ID", "Room Type", "Time Slot", "Reservation Date", "Member Name", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        viewReservationsBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> reservations = LibraryAPI.searchReservations(null, null);
                model.setRowCount(0);
                for (Map<String, Object> res : reservations) {
                    model.addRow(new Object[]{
                            res.get("reservation_id"),
                            res.get("room_type"),
                            res.get("time_slot"),
                            res.get("reservation_date"),
                            res.get("Member_Name"),
                            res.get("status")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error loading reservations: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchByDateBtn.addActionListener(e -> showSearchRoomByDateDialog(panel, model));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void showSearchRoomByDateDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Search Room Reservations by Date", true);
        dialog.setSize(300, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField dateField = new JTextField(20);
        dateField.setToolTipText("Format: YYYY-MM-DD");
        Utility.addLabelAndField(dialog, gbc, "Date (YYYY-MM-DD):", dateField, 0);

        JButton searchBtn = new JButton("Search");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(searchBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        searchBtn.addActionListener(e -> {
            try {
                java.sql.Date searchDate = java.sql.Date.valueOf(dateField.getText());
                List<Map<String, Object>> reservations = LibraryAPI.searchReservations(searchDate, null);
                model.setRowCount(0);
                for (Map<String, Object> res : reservations) {
                    model.addRow(new Object[]{
                            res.get("reservation_id"),
                            res.get("room_type"),
                            res.get("time_slot"),
                            res.get("reservation_date"),
                            res.get("Member_Name"),
                            res.get("status")
                    });
                }
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error searching: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // ==================== REPORTS PANEL ====================
    private static JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

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

        String[] columns = {"Column 1", "Column 2", "Column 3"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        mostBorrowedBtn.addActionListener(e -> {
            try {
                Optional<Map.Entry<String, Integer>> result = LibraryAPI.getMostBorrowedBookLastQuarter();
                model.setColumnCount(2);
                model.setColumnIdentifiers(new String[]{"Book Title", "Borrow Count"});
                model.setRowCount(0);
                if (result.isPresent()) {
                    model.addRow(new Object[]{result.get().getKey(), result.get().getValue()});
                } else {
                    model.addRow(new Object[]{"No data available", ""});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        overdueBtn.addActionListener(e -> {
            try {
                List<LoanDetailsRecord> overdue = LibraryAPI.getOverdueLoans();
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
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        finesReportBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> fines = LibraryAPI.getFinesLastQuarter();
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
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        neverBorrowedBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> books = LibraryAPI.getBooksNeverBorrowed();
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
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        totalLoansBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> books = LibraryAPI.getTotalLoansPerBook();
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
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


}
