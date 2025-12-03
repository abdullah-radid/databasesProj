package API;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reporting queries backed by views defined in queries.sql.
 */
public final class ReportsAPI {
    public static Optional<Map.Entry<String, Integer>> getMostBorrowedBookLastQuarter() throws SQLException {
        String sql = "SELECT * FROM v_QuarterlyReport ORDER BY borrow_count DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            if (rs.next()) {
                return Optional.of(new AbstractMap.SimpleEntry<>(rs.getString("title"), rs.getInt("borrow_count")));
            }
            return Optional.empty();
        }
    }

    public static List<Map<String, Object>> getBooksNeverBorrowed() throws SQLException {
        String sql = "SELECT * FROM v_NeverBorrowedBooks";
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(Map.of(
                        "Title", rs.getString("title"),
                        "ISBN", rs.getString("isbn"),
                        "Author", rs.getString("author")
                ));
            }
            return rows;
        }
    }

    public static List<Map<String, Object>> getTotalLoansPerBook() throws SQLException {
        String sql = "SELECT * FROM v_BookPopularity ORDER BY total_loans DESC";
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(Map.of(
                        "ISBN", rs.getString("isbn"),
                        "Title", rs.getString("title"),
                        "TotalLoans", rs.getInt("total_loans")
                ));
            }
            return rows;
        }
    }
}
