package API;

import API.DTO.BookInventoryRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access helpers for book catalog operations.
 */
public final class BooksAPI {
    public static List<BookInventoryRecord> getAllBookInventory() throws SQLException {
        String sql = "SELECT * FROM v_BookInventory";
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            return mapBookInventory(rs);
        }
    }

    public static List<BookInventoryRecord> findBooksByAuthor(String author) throws SQLException {
        String sql = "SELECT * FROM v_BookInventory WHERE Author LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + author + "%");
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

    public static boolean addNewBook(String isbn,
                                     String title,
                                     String author,
                                     String publisher,
                                     String category,
                                     String edition) throws SQLException {
        String sql = "INSERT IGNORE INTO Book (isbn, title, author, publisher, category, edition) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, isbn, title, author, publisher, category, edition) > 0;
        }
    }

    public static boolean updateBook(String isbn,
                                     String title,
                                     String author,
                                     String publisher,
                                     String category,
                                     String edition) throws SQLException {
        String sql = "UPDATE Book SET title = ?, author = ?, publisher = ?, category = ?, edition = ? WHERE isbn = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, title, author, publisher, category, edition, isbn) > 0;
        }
    }

    public static boolean deleteBook(String isbn) throws SQLException {
        String sql = "DELETE FROM Book WHERE isbn = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, isbn) > 0;
        }
    }

    public static boolean bookExists(Connection conn, String isbn) throws SQLException {
        String sql = "SELECT 1 FROM Book WHERE isbn = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static List<BookInventoryRecord> mapBookInventory(ResultSet rs) throws SQLException {
        List<BookInventoryRecord> out = new ArrayList<>();
        while (rs.next()) {
            out.add(new BookInventoryRecord(
                    rs.getString("isbn"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getString("Category"),
                    rs.getString("Edition"),
                    rs.getString("Publisher")));
        }
        return out;
    }
}
