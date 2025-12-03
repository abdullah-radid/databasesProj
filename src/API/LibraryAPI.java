package API;

import API.DTO.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * LibraryAPI: High-level DB access for the library application.
 */
public class LibraryAPI {

    /**
     * Tests database connection.
     *
     * @return true if all good
     */
    public boolean testConnection() {
        try (Connection conn = Database.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Book / Catalog methods
    /**
     * Returns all books with their copy records using view v_BookInventory.
     */
    public List<BookInventoryRecord> getAllBookInventory() throws SQLException {
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
    private List<BookInventoryRecord> getBookInventoryRecords(ResultSet rs) throws SQLException {
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
     * @param authorLike - the author to search for
     * @return a list of BookInventoryRecord objects
     * @throws SQLException if error
     */
    public List<BookInventoryRecord> findBooksByAuthor(String authorLike) throws SQLException {
        String sql = "SELECT * FROM v_BookInventory WHERE Author LIKE ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authorLike);
            try (ResultSet rs = ps.executeQuery()) {
                return mapBookInventory(rs);
            }
        }
    }

    public List<BookInventoryRecord> findBooksByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM v_BookInventory WHERE Category = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                return mapBookInventory(rs);
            }
        }
    }

    private List<BookInventoryRecord> mapBookInventory(ResultSet rs) throws SQLException {
        return getBookInventoryRecords(rs);
    }

    /**
     * Adds a book to the catalog AND inserts a physical copy.
     *
     * @apiNote uses a transaction to make sure both happen or none happens.
     */
    public void addNewBook(String isbn, String title, String author, String publisher, String category, String edition,
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
    public boolean markCopyLost(int copyId) throws SQLException {
        String sql = "UPDATE BookCopy SET status = 'Lost' WHERE copy_id = ?";
        try (Connection conn = Database.getConnection()) {
            int updated = Database.executeUpdate(conn, sql, copyId);
            return updated > 0;
        }
    }

    // ----------------------
    // Member / Staff methods
    // ----------------------

    public Optional<MemberRecord> findMemberById(int memberId) throws SQLException {
        // Uses v_AllMembers per queries.sql
        String sql = "SELECT * FROM v_AllMembers WHERE member_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new MemberRecord(
                            rs.getInt("member_id"),
                            rs.getString("Fname"),
                            "", // View doesn't strictly have Mname, or you can add it to view
                            rs.getString("Lname"),
                            MemberRecord.MemberType.valueOf(rs.getString("member_type")),
                            rs.getString("email")));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public List<MemberRecord> getMembersByType(String memberType) throws SQLException {
        String sql = "SELECT * FROM v_AllMembers WHERE member_type = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memberType);
            try (ResultSet rs = ps.executeQuery()) {
                List<MemberRecord> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new MemberRecord(
                            rs.getInt("member_id"),
                            rs.getString("Fname"),
                            "",
                            rs.getString("Lname"),
                            MemberRecord.MemberType.valueOf(rs.getString("member_type")),
                            rs.getString("email")));
                }
                return out;
            }
        }
    }

    public int createMember(String fname, String mname, String lname, String memberType, String email)
            throws SQLException {
        String sql = "INSERT INTO Member (Fname, Mname, Lname, member_type, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fname);
            ps.setString(2, mname);
            ps.setString(3, lname);
            ps.setString(4, memberType);
            ps.setString(5, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
                return -1;
            }
        }
    }

    public boolean updateMemberContact(int memberId, String email, String phone) throws SQLException {
        // Simple Update - no view needed
        String sql = "UPDATE Member SET email = ? WHERE member_id = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, email, memberId) > 0;
        }
    }

    // ----------------------
    // Loan / Return / Fines
    // ----------------------

    /**
     * Issue Book Transaction
     * Checks availability -> Inserts Loan -> Updates Copy Status
     */
    public String issueBook(int memberId, int copyId, int staffId) throws SQLException {
        String checkStatusSql = "SELECT status FROM BookCopy WHERE copy_id = ?";
        String insertLoanSql = "INSERT INTO Loan (member_id, copy_id, staff_id, issue_date, due_date) VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY))";
        String updateCopySql = "UPDATE BookCopy SET status = 'Loaned' WHERE copy_id = ?";

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Check Availability
            try (PreparedStatement psCheck = conn.prepareStatement(checkStatusSql)) {
                psCheck.setInt(1, copyId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        if (!"Available".equalsIgnoreCase(rs.getString("status"))) {
                            return "Error: Book is " + rs.getString("status");
                        }
                    } else {
                        return "Error: Copy ID not found.";
                    }
                }
            }

            // 2. Insert Loan
            try (PreparedStatement psLoan = conn.prepareStatement(insertLoanSql)) {
                psLoan.setInt(1, memberId);
                psLoan.setInt(2, copyId);
                psLoan.setInt(3, staffId);
                psLoan.executeUpdate();
            }

            // 3. Update Status
            try (PreparedStatement psUpdate = conn.prepareStatement(updateCopySql)) {
                psUpdate.setInt(1, copyId);
                psUpdate.executeUpdate();
            }

            conn.commit();
            return "Success: Book Issued.";
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

    /**
     * Return Book Transaction
     * Finds Copy -> Updates Loan Return Date -> Updates Copy Status
     */
    public String returnBook(int loanId) throws SQLException {
        String findCopySql = "SELECT copy_id FROM Loan WHERE loan_id = ?";
        String updateLoanSql = "UPDATE Loan SET return_date = CURDATE() WHERE loan_id = ?";
        String updateCopySql = "UPDATE BookCopy SET status = 'Available' WHERE copy_id = ?";

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            int copyId = -1;
            try (PreparedStatement psFind = conn.prepareStatement(findCopySql)) {
                psFind.setInt(1, loanId);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next())
                        copyId = rs.getInt("copy_id");
                    else
                        return "Error: Loan ID not found.";
                }
            }

            try (PreparedStatement psLoan = conn.prepareStatement(updateLoanSql)) {
                psLoan.setInt(1, loanId);
                psLoan.executeUpdate();
            }

            try (PreparedStatement psCopy = conn.prepareStatement(updateCopySql)) {
                psCopy.setInt(1, copyId);
                psCopy.executeUpdate();
            }

            conn.commit();
            return "Success: Book Returned.";
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

    /**
     * Calculates overdue fines using pure SQL batch logic.
     */
    public void calculateOverdueFines() throws SQLException {
        String sql = "INSERT INTO Fine (loan_id, member_id, amount, status, applied_date) " +
                "SELECT l.loan_id, l.member_id, 10.00, 'Unpaid', CURDATE() " +
                "FROM Loan l " +
                "LEFT JOIN Fine f ON l.loan_id = f.loan_id " +
                "WHERE l.return_date IS NULL " +
                "AND DATEDIFF(CURDATE(), l.due_date) > 75 " +
                "AND f.fine_id IS NULL";

        try (Connection conn = Database.getConnection()) {
            Database.executeUpdate(conn, sql);
        }
    }

    /**
     * Uses v_LoanDetails (View 2)
     */
    public List<LoanDetailsRecord> getLoanDetailsForMember(int memberId) throws SQLException {
        String sql = "SELECT * FROM v_LoanDetails WHERE member_id = ?";
        return getLoanDetailsList(sql, memberId);
    }

    public List<LoanDetailsRecord> getOverdueLoans() throws SQLException {
        String sql = "SELECT * FROM v_LoanDetails WHERE Loan_Status = 'Overdue'";
        return getLoanDetailsList(sql, null);
    }

    private List<LoanDetailsRecord> getLoanDetailsList(String sql, Integer param) throws SQLException {
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null)
                ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<LoanDetailsRecord> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new LoanDetailsRecord(
                            rs.getInt("loan_id"),
                            rs.getInt("member_id"),
                            rs.getString("Member_Name"),
                            rs.getString("Member_Email"),
                            rs.getString("Book_Title"),
                            toLocalDate(rs.getDate("issue_date")),
                            toLocalDate(rs.getDate("due_date")),
                            toLocalDate(rs.getDate("return_date")),
                            rs.getString("Loan_Status")));
                }
                return out;
            }
        }
    }

    /**
     * Uses v_MemberFines (View 8)
     */
    public List<Map<String, Object>> getMembersWithUnpaidFines() throws SQLException {
        String sql = "SELECT member_name, amount, applied_date FROM v_MemberFines WHERE status = 'Unpaid'";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MemberName", rs.getString("member_name"));
                row.put("amount", rs.getDouble("amount"));
                row.put("applied_date", toLocalDate(rs.getDate("applied_date")));
                out.add(row);
            }
            return out;
        }
    }

    // ----------------------
    // Reservations & Rooms
    // ----------------------

    /**
     * Uses v_RoomReservations (View 3) with dynamic SQL generation
     */
    public List<Map<String, Object>> searchReservations(java.sql.Date date, String roomType) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM v_RoomReservations WHERE status = 'Pending'");
        if (date != null)
            sql.append(" AND reservation_date = ?");
        if (roomType != null)
            sql.append(" AND room_type = ?");

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (date != null)
                ps.setDate(index++, date);
            if (roomType != null)
                ps.setString(index++, roomType);

            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("reservation_id", rs.getInt("reservation_id"));
                    row.put("room_type", rs.getString("room_type"));
                    row.put("time_slot", rs.getTime("time_slot"));
                    row.put("reservation_date", toLocalDate(rs.getDate("reservation_date")));
                    row.put("Member_Name", rs.getString("Member_Name"));
                    row.put("status", rs.getString("status"));
                    out.add(row);
                }
                return out;
            }
        }
    }

    public boolean reserveBook(int memberId, String isbn) throws SQLException {
        String sql = "INSERT INTO Reservation (member_id, book_isbn, reservation_date, status) VALUES (?, ?, CURDATE(), 'Pending')";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, memberId, isbn) > 0;
        }
    }

    // ----------------------
    // Reports
    // ----------------------

    /**
     * Uses v_QuarterlyReport (View 5)
     */
    public Optional<Map.Entry<String, Integer>> getMostBorrowedBookLastQuarter() throws SQLException {
        String sql = "SELECT * FROM v_QuarterlyReport ORDER BY borrow_count DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            if (rs.next()) {
                return Optional.of(new AbstractMap.SimpleEntry<>(rs.getString("title"), rs.getInt("borrow_count")));
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * Uses v_NeverBorrowedBooks (View 9)
     */
    public List<Map<String, Object>> getBooksNeverBorrowed() throws SQLException {
        String sql = "SELECT * FROM v_NeverBorrowedBooks";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("Title", rs.getString("title"));
                row.put("ISBN", rs.getString("isbn"));
                row.put("Author", rs.getString("author"));
                out.add(row);
            }
            return out;
        }
    }

    /**
     * Uses v_MemberFines (View 8)
     */
    public List<Map<String, Object>> getFinesLastQuarter() throws SQLException {
        String sql = "SELECT member_name, amount, applied_date FROM v_MemberFines WHERE applied_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MemberName", rs.getString("member_name"));
                row.put("amount", rs.getDouble("amount"));
                row.put("applied_date", toLocalDate(rs.getDate("applied_date")));
                out.add(row);
            }
            return out;
        }
    }

    /**
     * Gets all loan details (for loan management panel)
     */
    public List<LoanDetailsRecord> getAllLoanDetails() throws SQLException {
        String sql = "SELECT * FROM v_LoanDetails";
        return getLoanDetailsList(sql, null);
    }

    /**
     * Gets loans issued by a particular staff member
     */
    public List<LoanDetailsRecord> getLoansByStaff(int staffId) throws SQLException {
        String sql = "SELECT * FROM v_LoanDetails WHERE staff_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                List<LoanDetailsRecord> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new LoanDetailsRecord(
                            rs.getInt("loan_id"),
                            rs.getInt("member_id"),
                            rs.getString("Member_Name"),
                            rs.getString("Member_Email"),
                            rs.getString("Book_Title"),
                            toLocalDate(rs.getDate("issue_date")),
                            toLocalDate(rs.getDate("due_date")),
                            toLocalDate(rs.getDate("return_date")),
                            rs.getString("Loan_Status")));
                }
                return out;
            }
        }
    }

    /**
     * Uses v_BookPopularity (View 4) - Total loans per book
     */
    public List<Map<String, Object>> getTotalLoansPerBook() throws SQLException {
        String sql = "SELECT * FROM v_BookPopularity ORDER BY total_loans DESC";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ISBN", rs.getString("isbn"));
                row.put("Title", rs.getString("title"));
                row.put("TotalLoans", rs.getInt("total_loans"));
                out.add(row);
            }
            return out;
        }
    }

    /**
     * Gets all book reservations
     */
    public List<Map<String, Object>> getAllBookReservations() throws SQLException {
        String sql = "SELECT * FROM v_AllReservations WHERE book_isbn IS NOT NULL";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("reservation_id", rs.getInt("reservation_id"));
                row.put("member_id", rs.getInt("member_id"));
                row.put("book_isbn", rs.getString("book_isbn"));
                row.put("reservation_date", toLocalDate(rs.getDate("reservation_date")));
                row.put("status", rs.getString("status"));
                out.add(row);
            }
            return out;
        }
    }

    /**
     * Gets members who have reserved a book currently on loan
     */
    public List<Map<String, Object>> getMembersWithReservedBooksOnLoan() throws SQLException {
        String sql = "SELECT r.reservation_id, r.member_id, r.book_isbn, " +
                "CONCAT(m.Fname, ' ', m.Lname) AS member_name, r.status " +
                "FROM Reservation r " +
                "JOIN Member m ON r.member_id = m.member_id " +
                "WHERE r.book_isbn IS NOT NULL " +
                "AND r.status = 'Pending' " +
                "AND EXISTS (SELECT 1 FROM Loan l " +
                "            JOIN BookCopy bc ON l.copy_id = bc.copy_id " +
                "            WHERE bc.isbn = r.book_isbn AND l.return_date IS NULL)";
        try (Connection conn = Database.getConnection();
                ResultSet rs = Database.executeQuery(conn, sql)) {
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("reservation_id", rs.getInt("reservation_id"));
                row.put("member_id", rs.getInt("member_id"));
                row.put("member_name", rs.getString("member_name"));
                row.put("book_isbn", rs.getString("book_isbn"));
                row.put("status", rs.getString("status"));
                out.add(row);
            }
            return out;
        }
    }

    /**
     * Updates book information
     */
    public boolean updateBook(String isbn, String title, String author, String publisher,
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

    /**
     * Updates reservation status
     */
    public boolean updateReservationStatus(int reservationId, String status) throws SQLException {
        String sql = "UPDATE Reservation SET status = ? WHERE reservation_id = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, status, reservationId) > 0;
        }
    }

    // ----------------------
    // Misc helpers
    // ----------------------
    private static LocalDate toLocalDate(java.sql.Date d) {
        return d == null ? null : d.toLocalDate();
    }
}