USE library_db;


-- Master Book Inventory
-- Helps with: Req 3, Query 1, Query 2
CREATE OR REPLACE VIEW v_BookInventory AS
SELECT
    b.isbn,
    b.title AS Title,
    b.author AS Author,
    b.category AS Category,
    b.edition AS Edition,
    bc.copy_id AS CopyID,
    bc.shelf_location AS Location,
    bc.status AS Status
FROM Book b
JOIN BookCopy bc ON b.isbn = bc.isbn;

-- Loan Details
-- Helps with: Req 4, Req 9b, Query 4, Query 6, Query 9
CREATE OR REPLACE VIEW v_LoanDetails AS
SELECT
    l.loan_id,
    l.member_id,
    l.staff_id,
    CONCAT(s.Fname, ' ', s.Lname) AS Staff_Name,
    CONCAT(m.Fname, ' ', m.Lname) AS Member_Name,
    m.email AS Member_Email,
    b.title AS Book_Title,
    l.issue_date,
    l.due_date,
    l.return_date,
    CASE
        WHEN l.return_date IS NULL AND CURDATE() > l.due_date THEN 'Overdue'
        WHEN l.return_date IS NULL THEN 'Active'
        ELSE 'Returned'
    END AS Loan_Status
FROM Loan l
JOIN Member m ON l.member_id = m.member_id
JOIN Staff s ON l.staff_id = s.staff_id
JOIN BookCopy bc ON l.copy_id = bc.copy_id
JOIN Book b ON bc.isbn = b.isbn;

-- Room Reservations
-- Helps with: Req 10, Query 8
CREATE OR REPLACE VIEW v_RoomReservations AS
SELECT
    res.reservation_id,
    res.reservation_date,
    res.time_slot,
    res.status,
    r.room_type,
    CONCAT(m.Fname, ' ', m.Lname) AS Member_Name
FROM Reservation res
JOIN Member m ON res.member_id = m.member_id
JOIN Room r ON res.room_id = r.room_id
WHERE res.room_id IS NOT NULL;

-- Book Popularity / Total Loans
-- Helps with: Req 12
CREATE OR REPLACE VIEW v_BookPopularity AS
SELECT
    b.isbn,
    b.title,
    COUNT(l.loan_id) AS total_loans
FROM Book b
JOIN BookCopy bc ON b.isbn = bc.isbn
LEFT JOIN Loan l ON bc.copy_id = l.copy_id
GROUP BY b.isbn, b.title;

-- Quarterly Borrowing Report
-- Helps with: Req 9a, Query 7
CREATE OR REPLACE VIEW v_QuarterlyReport AS
SELECT
    b.title,
    COUNT(l.loan_id) as borrow_count,
    l.issue_date
FROM Loan l
JOIN BookCopy bc ON l.copy_id = bc.copy_id
JOIN Book b ON bc.isbn = b.isbn
WHERE l.issue_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
GROUP BY b.isbn, b.title;

--  All Members
-- Helps with: Req 11, Query 3
CREATE OR REPLACE VIEW v_AllMembers AS
SELECT
    member_id,
    Fname,
    Lname,
    member_type,
    email
FROM Member;

-- All Reservations (Books & Rooms)
-- Helps with: Req 8, Req 15, Query 10
CREATE OR REPLACE VIEW v_AllReservations AS
SELECT
    r.reservation_id,
    r.member_id,
    CONCAT(m.Fname, ' ', m.Lname) AS member_name,
    r.book_isbn,
    b.title AS book_title,
    r.room_id,
    rm.room_type,
    r.reservation_date,
    r.time_slot,
    r.status
FROM Reservation r
LEFT JOIN Member m ON r.member_id = m.member_id
LEFT JOIN Book b ON r.book_isbn = b.isbn
LEFT JOIN Room rm ON r.room_id = rm.room_id;

-- Member Fines
-- Helps with: Req 9c, Query 5, Query 12
CREATE OR REPLACE VIEW v_MemberFines AS
SELECT
    f.fine_id,
    f.loan_id,
    f.member_id,
    CONCAT(m.Fname, ' ', m.Lname) AS member_name,
    f.amount,
    f.status,
    f.applied_date
FROM Fine f
JOIN Member m ON f.member_id = m.member_id;

-- Never Borrowed Books
-- Helps with: Query 11
CREATE OR REPLACE VIEW v_NeverBorrowedBooks AS
SELECT
    b.isbn,
    b.title,
    b.author
FROM Book b
WHERE NOT EXISTS (
    SELECT 1
    FROM Loan l
    JOIN BookCopy bc ON l.copy_id = bc.copy_id
    WHERE bc.isbn = b.isbn
);

/** queries to copy below
-- ==========================================================
-- SECTION 2: DATA MODELING REQUIREMENTS (1-15)
-- ==========================================================

-- Req 1: Allow users to manually update all the above
-- Handled by Java: PreparedStatement (UPDATE Member SET ... / UPDATE Book SET ...)

-- Req 2: Allow users to manually input all above information
-- Handled by Java: PreparedStatement (INSERT INTO Member ... / INSERT INTO Book ...)

-- Req 3: Allow users to generate a list of all books by title, author, category, shelf location
-- Uses View 1: v_BookInventory
SELECT * FROM v_BookInventory;

-- Req 4: Allow users to generate list of members and their borrowing history
-- Uses View 2: v_LoanDetails
SELECT * FROM v_LoanDetails WHERE member_id = ?;

-- Req 5: Allow users to issue and return books with due dates and renewal options
-- Handled by Java: LibraryAPI.issueBook() and LibraryAPI.returnBook() (Transaction Logic)

-- Req 6: Allow users to calculate fines for overdue books (75 days)
-- Handled by Java: LibraryAPI.calculateOverdueFines() (Batch Logic)

-- Req 7: Allow users to add, update, or remove books from inventory
-- Handled by Java: LibraryAPI.addNewBook() (Transaction Logic) or UPDATE queries

-- Req 8: Allow users to manage reservations for books that are currently borrowed
-- Handled by Java: LibraryAPI.reserveBook()
-- To View: Uses View 7 (v_AllReservations)
SELECT * FROM v_AllReservations WHERE book_isbn IS NOT NULL;

-- Req 9: Generate quarterly reports...
-- 9a. Most borrowed books -> Uses View 5: v_QuarterlyReport
SELECT * FROM v_QuarterlyReport ORDER BY borrow_count DESC;
-- 9b. Overdue items -> Uses View 2: v_LoanDetails
SELECT * FROM v_LoanDetails WHERE Loan_Status = 'Overdue';
-- 9c. Members with fines -> Uses View 8: v_MemberFines
SELECT * FROM v_MemberFines;

-- Req 10: Allow users to track which members have access to study rooms
-- Uses View 3: v_RoomReservations
SELECT * FROM v_RoomReservations WHERE status = 'Pending';

-- Req 11: Allow users to generate list of members by type
-- Uses View 6: v_AllMembers
SELECT * FROM v_AllMembers WHERE member_type = ?;

-- Req 12: Allow users to track total number of loans for each book
-- Uses View 4: v_BookPopularity
SELECT * FROM v_BookPopularity ORDER BY total_loans DESC;

-- Req 13: Allow users to record when books are physically returned
-- Handled by Java: LibraryAPI.returnBook() (Transaction Logic)

-- Req 14: Allow users to check availability of books before issuing them
-- Handled by Java: Checked inside LibraryAPI.issueBook()
SELECT count(*) FROM BookCopy WHERE isbn = ? AND status = 'Available';

-- Req 15: Allow users to track reservation fulfillment status for each book
-- Uses View 7: v_AllReservations
SELECT * FROM v_AllReservations WHERE status = 'Fulfilled';


/** expected queries

-- Query 1: Find all books by specific author
-- Uses View 1: v_BookInventory
SELECT * FROM v_BookInventory WHERE Author LIKE ?;

-- Query 2: Find all books in specific category
-- Uses View 1: v_BookInventory
SELECT * FROM v_BookInventory WHERE Category = ?;

-- Query 3: Find all members that are students
-- Uses View 6: v_AllMembers
SELECT * FROM v_AllMembers WHERE member_type = 'Student';

-- Query 4: List borrowing history of members
-- Uses View 2: v_LoanDetails
SELECT * FROM v_LoanDetails WHERE member_id = ?;

-- Query 5: Find members with unpaid fines
-- Uses View 8: v_MemberFines
SELECT * FROM v_MemberFines WHERE status = 'Unpaid';

-- Query 6: List overdue books and the member who borrow them
-- Uses View 2: v_LoanDetails
SELECT Book_Title, Member_Name, due_date FROM v_LoanDetails WHERE Loan_Status = 'Overdue';

-- Query 7: Find the most borrowed book last Quarter
-- Uses View 5: v_QuarterlyReport
SELECT * FROM v_QuarterlyReport ORDER BY borrow_count DESC LIMIT 1;

-- Query 8: Find which study room are reserved for a specific day
-- Uses View 3: v_RoomReservations
SELECT * FROM v_RoomReservations WHERE reservation_date = ?;

-- Query 9: Find all loans issued by a particular librarian
-- Uses View 2: v_LoanDetails (Staff_Name added to view)
SELECT * FROM v_LoanDetails WHERE Staff_Name LIKE ?;

-- Query 10: Find members who have reserved a book currently on loan
-- Uses View 7: v_AllReservations
SELECT * FROM v_AllReservations WHERE book_isbn IS NOT NULL AND status = 'Pending';

-- Query 11: Find all books that have never been borrowed
-- Uses View 9: v_NeverBorrowedBooks
SELECT * FROM v_NeverBorrowedBooks;

-- Query 12: Generate a report of fines applied for last quarter
-- Uses View 8: v_MemberFines
SELECT * FROM v_MemberFines WHERE applied_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH);
*/