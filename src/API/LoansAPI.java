package API;

import API.DTO.LoanDetailsRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loan related operations.
 */
public final class LoansAPI {
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    public static List<LoanDetailsRecord> getAllLoanDetails() throws SQLException {
        return queryLoanDetails("SELECT * FROM v_LoanDetails", null);
    }

    public static List<LoanDetailsRecord> getLoanDetailsForMember(int memberId) throws SQLException {
        return queryLoanDetails("SELECT * FROM v_LoanDetails WHERE member_id = ?", ps -> ps.setInt(1, memberId));
    }

    public static List<LoanDetailsRecord> getOverdueLoans() throws SQLException {
        return queryLoanDetails("SELECT * FROM v_LoanDetails WHERE Loan_Status = 'Overdue'", null);
    }

    public static Map<String, Integer> getActiveLoanCounts() throws SQLException {
        String sql = "SELECT isbn, COUNT(*) AS active_count FROM Loan WHERE return_date IS NULL GROUP BY isbn";
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            Map<String, Integer> counts = new HashMap<>();
            while (rs.next()) {
                counts.put(rs.getString("isbn"), rs.getInt("active_count"));
            }
            return counts;
        }
    }

    public static int getActiveLoanCount(String isbn) throws SQLException {
        String sql = "SELECT COUNT(*) AS active_count FROM Loan WHERE isbn = ? AND return_date IS NULL";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("active_count");
                }
                return 0;
            }
        }
    }

    public static String issueBook(int memberId, String isbn) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            if (!memberExists(conn, memberId)) {
                return "Error: Member ID not found.";
            }
            if (!BooksAPI.bookExists(conn, isbn)) {
                return "Error: ISBN not found.";
            }
            if (countActiveLoans(conn, isbn) > 0) {
                return "Error: Book has been loaned out already.";
            }

            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(DEFAULT_LOAN_PERIOD_DAYS);
            String insertSql = "INSERT INTO Loan (member_id, isbn, issue_date, due_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, memberId);
                ps.setString(2, isbn);
                ps.setDate(3, Date.valueOf(issueDate));
                ps.setDate(4, Date.valueOf(dueDate));
                ps.executeUpdate();
            }
            return "Success: Book issued. Due on " + dueDate;
        }
    }

    public static String returnBook(int loanId) throws SQLException {
        String sql = "UPDATE Loan SET return_date = CURDATE() WHERE loan_id = ? AND return_date IS NULL";
        try (Connection conn = Database.getConnection()) {
            int updated = Database.executeUpdate(conn, sql, loanId);
            if (updated == 0) {
                return "Error: Loan not found or already returned.";
            }
            return "Success: Book returned.";
        }
    }

    private static List<LoanDetailsRecord> queryLoanDetails(String sql, StatementConfigurer configurer) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (configurer != null) {
                configurer.accept(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<LoanDetailsRecord> records = new ArrayList<>();
                while (rs.next()) {
                    records.add(new LoanDetailsRecord(
                            rs.getInt("loan_id"),
                            rs.getInt("member_id"),
                            rs.getString("Member_Name"),
                            rs.getString("Member_Email"),
                            rs.getString("Book_Title"),
                            rs.getString("ISBN"),
                            toLocalDate(rs.getDate("issue_date")),
                            toLocalDate(rs.getDate("due_date")),
                            toLocalDate(rs.getDate("return_date")),
                            rs.getString("Loan_Status")
                    ));
                }
                return records;
            }
        }
    }

    private static int countActiveLoans(Connection conn, String isbn) throws SQLException {
        String sql = "SELECT COUNT(*) AS active_count FROM Loan WHERE isbn = ? AND return_date IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("active_count");
                }
            }
        }
        return 0;
    }

    private static boolean memberExists(Connection conn, int memberId) throws SQLException {
        String sql = "SELECT 1 FROM Member WHERE member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    @FunctionalInterface
    private interface StatementConfigurer {
        void accept(PreparedStatement statement) throws SQLException;
    }
}
