package UI.Panels;

import API.BooksAPI;
import API.DTO.BookInventoryRecord;
import API.LibraryAPI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.Book;
import java.sql.SQLException;
import java.util.List;

import static UI.Utility.addLabelAndField;

/// Book Management Panel
public class BookPanel extends JPanel {

    public BookPanel() {
        super(new BorderLayout());
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
        refreshBtn.addActionListener(_ -> refreshBookTable(model));
        refreshBtn.doClick(); // Initial load

        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText();
            String type = (String) searchType.getSelectedItem();
            try {
                List<BookInventoryRecord> results;
                if ("All".equals(type) || searchText.isEmpty()) {
                    results = BooksAPI.getAllBookInventory();
                } else if ("Author".equals(type)) {
                    results = BooksAPI.findBooksByAuthor(searchText);
                } else if ("Category".equals(type)) {
                    results = BooksAPI.findBooksByCategory(searchText);
                } else {
                    results = BooksAPI.getAllBookInventory();
                }
                populateBookTable(model, results, type, searchText);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error searching books: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addBtn.addActionListener(e -> showAddBookDialog(this, model));
        updateBtn.addActionListener(e -> showUpdateBookDialog(this, table, model));
        removeBtn.addActionListener(e -> showRemoveBookCopyDialog(this, table, model));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.SOUTH);

        this.add(northPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

    }

    private static void refreshBookTable(DefaultTableModel model) {
        try {
            List<BookInventoryRecord> books = BooksAPI.getAllBookInventory();
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
                BooksAPI.addNewBook(
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
                if (BooksAPI.updateBook(isbn, titleField.getText(), authorField.getText(),
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
                if (BooksAPI.markCopyLost(copyId)) {
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

}
