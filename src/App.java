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

public class App {
    private static LibraryAPI api;
    private static JFrame mainFrame;
    private static int currentStaffId = 1; // Default staff ID for issuing books

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            api = new LibraryAPI();
            if (!api.testConnection()) {
                JOptionPane.showMessageDialog(null,
                    "Failed to connect to database. Please check your connection settings.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            showLogin();
        });
    }

    // Login screen
    public static void showLogin() {
        JFrame frame = new JFrame("Library Management System - Login");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        frame.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(110, 20, 200, 25);
        frame.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        frame.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(110, 60, 200, 25);
        frame.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(110, 100, 100, 30);
        frame.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());

            if (username.equals("admin") && password.equals("1234")) {
                frame.dispose();
                showMainApplication();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Enter key to login
        passwordText.addActionListener(e -> loginButton.doClick());

        frame.setVisible(true);
    }

    // Main Application Window
    public static void showMainApplication() {
        mainFrame = new JFrame("Library Management System");
        mainFrame.setSize(1200, 800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add all panels
        tabbedPane.addTab("Books", createBookManagementPanel());
        tabbedPane.addTab("Members", createMemberManagementPanel());
        tabbedPane.addTab("Loans", createLoanManagementPanel());
        tabbedPane.addTab("Fines", createFineManagementPanel());
        tabbedPane.addTab("Reservations", createReservationManagementPanel());
        tabbedPane.addTab("Study Rooms", createStudyRoomPanel());
        tabbedPane.addTab("Reports", createReportsPanel());

        mainFrame.add(tabbedPane);
        mainFrame.setVisible(true);
    }

    // ==================== BOOK MANAGEMENT PANEL ====================
    private static JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Book");
        JButton updateBtn = new JButton("Update Book");
        JButton removeBtn = new JButton("Remove Copy");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(addBtn);
        topPanel.add(updateBtn);
        topPanel.add(removeBtn);
        topPanel.add(refreshBtn);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search by:");
        JComboBox<String> searchType = new JComboBox<>(new String[]{"All", "Title", "Author", "Category", "Shelf Location"});
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");

        searchPanel.add(searchLabel);
        searchPanel.add(searchType);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // Table
        String[] columns = {"ISBN", "Title", "Author", "Category", "Edition", "Copy ID", "Location", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Button actions
        refreshBtn.addActionListener(e -> refreshBookTable(model));
        refreshBtn.doClick(); // Initial load

        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText();
            String type = (String) searchType.getSelectedItem();
            try {
                List<BookInventoryRecord> results;
                if ("All".equals(type) || searchText.isEmpty()) {
                    results = api.getAllBookInventory();
                } else if ("Author".equals(type)) {
                    results = api.findBooksByAuthor("%" + searchText + "%");
                } else if ("Category".equals(type)) {
                    results = api.findBooksByCategory(searchText);
                } else {
                    results = api.getAllBookInventory();
                }
                populateBookTable(model, results, type, searchText);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error searching books: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addBtn.addActionListener(e -> showAddBookDialog(panel, model));
        updateBtn.addActionListener(e -> showUpdateBookDialog(panel, table, model));
        removeBtn.addActionListener(e -> showRemoveBookCopyDialog(panel, table, model));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void refreshBookTable(DefaultTableModel model) {
        try {
            List<BookInventoryRecord> books = api.getAllBookInventory();
            populateBookTable(model, books, "All", "");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading books: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void populateBookTable(DefaultTableModel model, List<BookInventoryRecord> books,
                                         String filterType, String filterText) {
        model.setRowCount(0);
        for (BookInventoryRecord book : books) {
            if (filterType.equals("All") || filterText.isEmpty()) {
                model.addRow(new Object[]{
                    book.isbn(), book.title(), book.author(), book.category(),
                    book.edition(), book.copyId(), book.location(), book.status()
                });
            } else if (filterType.equals("Title") && book.title().toLowerCase().contains(filterText.toLowerCase())) {
                model.addRow(new Object[]{
                    book.isbn(), book.title(), book.author(), book.category(),
                    book.edition(), book.copyId(), book.location(), book.status()
                });
            } else if (filterType.equals("Shelf Location") && book.location().toLowerCase().contains(filterText.toLowerCase())) {
                model.addRow(new Object[]{
                    book.isbn(), book.title(), book.author(), book.category(),
                    book.edition(), book.copyId(), book.location(), book.status()
                });
            } else if (!filterType.equals("Title") && !filterType.equals("Shelf Location")) {
                model.addRow(new Object[]{
                    book.isbn(), book.title(), book.author(), book.category(),
                    book.edition(), book.copyId(), book.location(), book.status()
                });
            }
        }
    }

    private static void showAddBookDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Add New Book", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField isbnField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField publisherField = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JTextField editionField = new JTextField(20);
        JTextField shelfLocationField = new JTextField(20);

        int row = 0;
        addLabelAndField(dialog, gbc, "ISBN:", isbnField, row++);
        addLabelAndField(dialog, gbc, "Title:", titleField, row++);
        addLabelAndField(dialog, gbc, "Author:", authorField, row++);
        addLabelAndField(dialog, gbc, "Publisher:", publisherField, row++);
        addLabelAndField(dialog, gbc, "Category:", categoryField, row++);
        addLabelAndField(dialog, gbc, "Edition:", editionField, row++);
        addLabelAndField(dialog, gbc, "Shelf Location:", shelfLocationField, row++);

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
                api.addNewBook(
                    isbnField.getText(),
                    titleField.getText(),
                    authorField.getText(),
                    publisherField.getText(),
                    categoryField.getText(),
                    editionField.getText(),
                    shelfLocationField.getText()
                );
                JOptionPane.showMessageDialog(dialog, "Book added successfully!");
                dialog.dispose();
                refreshBookTable(model);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showUpdateBookDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a book to update.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String isbn = (String) model.getValueAt(selectedRow, 0);
        String title = (String) model.getValueAt(selectedRow, 1);
        String author = (String) model.getValueAt(selectedRow, 2);
        String category = (String) model.getValueAt(selectedRow, 3);
        String edition = (String) model.getValueAt(selectedRow, 4);
        String location = (String) model.getValueAt(selectedRow, 6);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Update Book", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField isbnField = new JTextField(isbn, 20);
        isbnField.setEditable(false);
        JTextField titleField = new JTextField(title, 20);
        JTextField authorField = new JTextField(author, 20);
        JTextField categoryField = new JTextField(category, 20);
        JTextField editionField = new JTextField(edition, 20);
        JTextField locationField = new JTextField(location, 20);

        int row = 0;
        addLabelAndField(dialog, gbc, "ISBN:", isbnField, row++);
        addLabelAndField(dialog, gbc, "Title:", titleField, row++);
        addLabelAndField(dialog, gbc, "Author:", authorField, row++);
        addLabelAndField(dialog, gbc, "Category:", categoryField, row++);
        addLabelAndField(dialog, gbc, "Edition:", editionField, row++);
        addLabelAndField(dialog, gbc, "Shelf Location:", locationField, row++);

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
                String publisher = ""; // Would need to get from database or add field
                if (api.updateBook(isbn, titleField.getText(), authorField.getText(),
                    publisher, categoryField.getText(), editionField.getText())) {
                    // Update shelf location if needed
                    JOptionPane.showMessageDialog(dialog, "Book updated successfully!");
                    dialog.dispose();
                    refreshBookTable(model);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update book.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showRemoveBookCopyDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a book copy to remove.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int copyId = (Integer) model.getValueAt(selectedRow, 5);
        String title = (String) model.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(parent,
            "Mark copy ID " + copyId + " of '" + title + "' as Lost?",
            "Confirm Removal", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (api.markCopyLost(copyId)) {
                    JOptionPane.showMessageDialog(parent, "Book copy marked as lost.");
                    refreshBookTable(model);
                } else {
                    JOptionPane.showMessageDialog(parent, "Failed to mark copy as lost.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== MEMBER MANAGEMENT PANEL ====================
    private static JPanel createMemberManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

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

        addBtn.addActionListener(e -> showAddMemberDialog(panel, model));
        updateBtn.addActionListener(e -> showUpdateMemberDialog(panel, table, model));
        historyBtn.addActionListener(e -> showBorrowingHistoryDialog(panel, table));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void refreshMemberTable(DefaultTableModel model, String memberType) {
        try {
            List<MemberRecord> members;
            if ("All".equals(memberType)) {
                members = api.getMembersByType("Student");
                members.addAll(api.getMembersByType("Faculty"));
                members.addAll(api.getMembersByType("Staff"));
                members.addAll(api.getMembersByType("Public"));
            } else {
                members = api.getMembersByType(memberType);
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
        addLabelAndField(dialog, gbc, "First Name:", fnameField, row++);
        addLabelAndField(dialog, gbc, "Middle Name:", mnameField, row++);
        addLabelAndField(dialog, gbc, "Last Name:", lnameField, row++);
        addLabelAndField(dialog, gbc, "Member Type:", typeCombo, row++);
        addLabelAndField(dialog, gbc, "Email:", emailField, row++);

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
                int memberId = api.createMember(
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
        addLabelAndField(dialog, gbc, "Email:", emailField, row++);
        addLabelAndField(dialog, gbc, "Phone:", phoneField, row++);

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
                if (api.updateMemberContact(memberId, emailField.getText(), phoneField.getText())) {
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
            List<LoanDetailsRecord> loans = api.getLoanDetailsForMember(memberId);
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
            List<LoanDetailsRecord> loans = api.getAllLoanDetails();
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
        addLabelAndField(dialog, gbc, "Member ID:", memberIdField, row++);
        addLabelAndField(dialog, gbc, "Copy ID:", copyIdField, row++);
        addLabelAndField(dialog, gbc, "Staff ID:", staffIdField, row++);

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

                String result = api.issueBook(memberId, copyId, staffId);
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
                String result = api.returnBook(loanId);
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
                List<BookInventoryRecord> books = api.getAllBookInventory();
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
                api.calculateOverdueFines();
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
            List<Map<String, Object>> fines = api.getMembersWithUnpaidFines();
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
        addLabelAndField(dialog, gbc, "Member ID:", memberIdField, row++);
        addLabelAndField(dialog, gbc, "Book ISBN:", isbnField, row++);

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
                if (api.reserveBook(memberId, isbn)) {
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
            List<Map<String, Object>> reservations = api.getAllBookReservations();
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
        addLabelAndField(dialog, gbc, "Status:", statusCombo, 1);

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
                if (api.updateReservationStatus(reservationId, (String) statusCombo.getSelectedItem())) {
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
                if (api.updateReservationStatus(reservationId, "Cancelled")) {
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
                List<Map<String, Object>> reservations = api.searchReservations(null, null);
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
        addLabelAndField(dialog, gbc, "Date (YYYY-MM-DD):", dateField, 0);

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
                List<Map<String, Object>> reservations = api.searchReservations(searchDate, null);
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
                Optional<Map.Entry<String, Integer>> result = api.getMostBorrowedBookLastQuarter();
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
                List<LoanDetailsRecord> overdue = api.getOverdueLoans();
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
                List<Map<String, Object>> fines = api.getFinesLastQuarter();
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
                List<Map<String, Object>> books = api.getBooksNeverBorrowed();
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
                List<Map<String, Object>> books = api.getTotalLoansPerBook();
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

    // Helper method to add label and field to dialog
    private static void addLabelAndField(JDialog dialog, GridBagConstraints gbc,
                                        String labelText, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
    }
}
