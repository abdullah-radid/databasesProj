package API;


import API.DTO.BookCopyRecord;
import API.DTO.BookInventoryRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/// books only related DB access for the library application.
///
public final class BooksAPI {
    /**
     * Returns all books with their copy records using view v_BookInventory.
     */
    public static List<BookInventoryRecord> getAllBookInventory() throws SQLException {
        String sql = "SELECT * FROM v_BookInventory";
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            return getBookInventoryRecords(rs);
        }
    }

    /**
     * Maps a ResultSet containing book inventory records into a List of
     * BookInventoryRecord objects.
     *
     * @param rs The ResultSet containing book inventory records.
     * @return A List of BookInventoryRecord objects.
     * @throws SQLException if error
     */
    private static List<BookInventoryRecord> getBookInventoryRecords(ResultSet rs) throws SQLException {
        List<BookInventoryRecord> out = new ArrayList<>();
        while (rs.next()) {
            out.add(new BookInventoryRecord(
                    rs.getString("isbn"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getString("Category"),
                    rs.getString("Edition"),
                    rs.getInt("CopyID"),
                    rs.getString("Location"),
                    BookCopyRecord.BookCopyStatus.valueOf(rs.getString("Status"))));
        }
        return out;
    }

    /**
     * Finds books by author using v_BookInventory view.
     *
     * @param author - the author to search for
     * @return a list of BookInventoryRecord objects
     * @throws SQLException if error
     */
    public static List<BookInventoryRecord> findBooksByAuthor(String author) throws SQLException {
        String sql = "SELECT * FROM v_BookInventory WHERE Author LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,"%" + author + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return mapBookInventory(rs);
            }
        }
    }

    public static List<BookInventoryRecord> findBooksByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM v_BookInventory WHERE Category = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                return mapBookInventory(rs);
            }
        }
    }

    private static List<BookInventoryRecord> mapBookInventory(ResultSet rs) throws SQLException {
        return getBookInventoryRecords(rs);
    }

    /**
     * Adds a book to the catalog AND inserts a physical copy.
     *
     * @Note uses a transaction to make sure both happen or none happens.
     */
    public static void addNewBook(String isbn, String title, String author, String publisher, String category, String edition,
                           String shelfLocation) throws SQLException {
        String insertBookSql = "INSERT IGNORE INTO Book (ISBN, title, author, publisher, category, edition) VALUES (?, ?, ?, ?, ?, ?)";
        String insertCopySql = "INSERT INTO BookCopy (isbn, shelf_location, copy_type, status) VALUES (?, ?, 'Physical', 'Available')";

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Book (Ignore if ISBN exists)
            try (PreparedStatement psBook = conn.prepareStatement(insertBookSql)) {
                psBook.setString(1, isbn);
                psBook.setString(2, title);
                psBook.setString(3, author);
                psBook.setString(4, publisher);
                psBook.setString(5, category);
                psBook.setString(6, edition);
                psBook.executeUpdate();
            }

            // 2. Insert Copy
            try (PreparedStatement psCopy = conn.prepareStatement(insertCopySql)) {
                psCopy.setString(1, isbn);
                psCopy.setString(2, shelfLocation);
                psCopy.executeUpdate();
            }

            conn.commit(); // Commiting the transaction
        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /// its in the name
    /// @returns true if updated successfully
    public static boolean markCopyLost(int copyId) throws SQLException {
        String sql = "UPDATE BookCopy SET status = 'Lost' WHERE copy_id = ?";
        try (Connection conn = Database.getConnection()) {
            int updated = Database.executeUpdate(conn, sql, copyId);
            return updated > 0;
        }
    }
    private static void refreshBookTable(DefaultTableModel model) {
        try {
            List<BookInventoryRecord> books = getAllBookInventory();
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

    /**
     * Updates book information
     */
    public static boolean updateBook(String isbn, String title, String author, String publisher,
                                     String category, String edition) throws SQLException {
        String sql = "UPDATE Book SET title = ?, author = ?, publisher = ?, category = ?, edition = ? WHERE isbn = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, title, author, publisher, category, edition, isbn) > 0;
        }
    }

    /**
     * Updates book copy shelf location
     */
    public boolean updateBookCopyLocation(int copyId, String shelfLocation) throws SQLException {
        String sql = "UPDATE BookCopy SET shelf_location = ? WHERE copy_id = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, shelfLocation, copyId) > 0;
        }
    }

}
