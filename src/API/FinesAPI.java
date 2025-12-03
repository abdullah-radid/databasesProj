package API;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fines and penalty utilities.
 */
public final class FinesAPI {
    public static void calculateOverdueFines() throws SQLException {
        String sql = """
                INSERT INTO Fine (loan_id, amount, status, applied_date)
                SELECT l.loan_id,
                       10.00,
                       'Unpaid',
                       CURDATE()
                FROM Loan l
                LEFT JOIN Fine f ON l.loan_id = f.loan_id
                WHERE l.return_date IS NULL
                  AND DATEDIFF(CURDATE(), l.due_date) > 75
                  AND f.loan_id IS NULL
                """;
        try (Connection conn = Database.getConnection()) {
            Database.executeUpdate(conn, sql);
        }
    }

    public static List<Map<String, Object>> getMembersWithUnpaidFines() throws SQLException {
        String sql = "SELECT * FROM v_MemberFines WHERE status = 'Unpaid'";
        return fetchFines(sql);
    }

    public static List<Map<String, Object>> getFinesLastQuarter() throws SQLException {
        String sql = "SELECT * FROM v_MemberFines WHERE applied_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)";
        return fetchFines(sql);
    }

    private static List<Map<String, Object>> fetchFines(String sql) throws SQLException {
        try (Connection conn = Database.getConnection();
             ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MemberName", rs.getString("member_name"));
                row.put("amount", rs.getDouble("amount"));
                row.put("applied_date", rs.getDate("applied_date"));
                row.put("status", rs.getString("status"));
                rows.add(row);
            }
            return rows;
        }
    }
}
