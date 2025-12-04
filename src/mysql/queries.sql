/*
 * FILENAME: queries.sql
 * PROJECT: Cook Library Database System
 * DESCRIPTION: Contains Views and standard Queries mapping to project requirements.
 * UPDATED: Aligned with simplified schema (No BookCopy, No Reservation, No Staff on Loans).
 */

USE library_db2;

-- ==========================================================
-- SECTION 1: VIEWS (For populating Java JTables)
-- ==========================================================

-- VIEW 1: Master Book Inventory
-- Helps with: Req 3, Query 1, Query 2
CREATE OR REPLACE VIEW v_BookInventory AS
SELECT
    b.isbn,
    b.title AS Title,
    b.author AS Author,
    b.category AS Category,
    b.edition AS Edition,
    b.publisher AS Publisher
FROM Book b;

-- VIEW 2: Loan Details Enhanced
-- Helps with: Req 4, Req 9b, Query 4, Query 6
-- Staff info removed as cus its no longer in the Loan table.
CREATE OR REPLACE VIEW v_LoanDetails AS
SELECT
    l.loan_id,
    l.member_id,
    CONCAT(m.Fname, ' ', m.Lname) AS Member_Name,
    m.email AS Member_Email,
    b.title AS Book_Title,
    b.isbn AS ISBN,
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
JOIN Book b ON l.isbn = b.isbn;

-- VIEW 3: Book Popularity / Total Loans
-- Helps with: Req 12
CREATE OR REPLACE VIEW v_BookPopularity AS
SELECT
    b.isbn,
    b.title,
    COUNT(l.loan_id) AS total_loans
FROM Book b
LEFT JOIN Loan l ON b.isbn = l.isbn
GROUP BY b.isbn, b.title;

-- VIEW 4: Quarterly Borrowing Report
-- Helps with: Req 9a, Query 7
CREATE OR REPLACE VIEW v_QuarterlyReport AS
SELECT
    b.title,
    COUNT(l.loan_id) as borrow_count,
    l.issue_date
FROM Loan l
JOIN Book b ON l.isbn = b.isbn
WHERE l.issue_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
GROUP BY b.isbn, b.title, l.issue_date;

-- VIEW 5: All Members
-- Helps with: Req 11, Query 3
CREATE OR REPLACE VIEW v_AllMembers AS
SELECT
    member_id,
    Fname,
    Lname,
    member_type,
    email
FROM Member;

-- VIEW 6: Member Fines
-- Helps with: Req 9c, Query 5, Query 12
CREATE OR REPLACE VIEW v_MemberFines AS
SELECT
    f.fine_id,
    f.loan_id,
    l.member_id,
    CONCAT(m.Fname, ' ', m.Lname) AS member_name,
    f.amount,
    f.status,
    f.applied_date
FROM Fine f
JOIN Loan l ON f.loan_id = l.loan_id
JOIN Member m ON l.member_id = m.member_id;

-- VIEW 7: Never Borrowed Books
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
    WHERE l.isbn = b.isbn
);


/* copy and paste into java when needed

-- java 1: Allow users to manually update all the above

-- java 2: Allow users to manually input all above information


-- Req 3: Allow users to generate a list of all books by title, author, category
SELECT * FROM v_BookInventory;

-- Req 4: Allow users to generate list of members and their borrowing history
SELECT * FROM v_LoanDetails WHERE member_id = ?;

-- java 5: Allow users to issue and return books with due dates

-- java 6: Allow users to calculate fines for overdue books

-- java 7: Allow users to add, update, or remove books

-- java 8: Allow users to manage reservations

-- Req 9: Generate quarterly reports...
-- 9a. Most borrowed books -> Uses View 4: v_QuarterlyReport
SELECT * FROM v_QuarterlyReport ORDER BY borrow_count DESC;
-- 9b. Overdue items -> Uses View 2: v_LoanDetails
SELECT * FROM v_LoanDetails WHERE Loan_Status = 'Overdue';
-- 9c. Members with fines -> Uses View 6: v_MemberFines
SELECT * FROM v_MemberFines;

-- Req 10: Allow users to track which members have access to study rooms
-- idk how to do this one with the current schema

-- Req 11: Allow users to generate list of members by type
SELECT * FROM v_AllMembers WHERE member_type = ?;

-- Req 12: Allow users to track total number of loans for each book
SELECT * FROM v_BookPopularity ORDER BY total_loans DESC;

-- java 13: Allow users to record when books are physically returned

-- Req 14: Allow users to check availability of books before issuing them
-- since there is no BookCopy we can only check if there are any active loans.
-- We cant get total quantity vs checked out quantity rn
SELECT count(*) AS ActiveLoans FROM Loan WHERE isbn = ? AND return_date IS NULL;

-- Req 15: Track reservation fulfillment status for each book
-- no


-- expected queries

-- Query 1: Find all books by specific author
SELECT * FROM v_BookInventory WHERE Author LIKE ?;

-- Query 2: Find all books in specific category
SELECT * FROM v_BookInventory WHERE Category = ?;

-- Query 3: Find all members that are students
SELECT * FROM v_AllMembers WHERE member_type = 'Student';

-- Query 4: List borrowing history of members
SELECT * FROM v_LoanDetails WHERE member_id = ?;

-- Query 5: Find members with unpaid fines
SELECT * FROM v_MemberFines WHERE status = 'Unpaid';

-- Query 6: List overdue books and the member who borrow them
SELECT Book_Title, Member_Name, due_date FROM v_LoanDetails WHERE Loan_Status = 'Overdue';

-- Query 7: Find the most borrowed book last Quarter
SELECT * FROM v_QuarterlyReport ORDER BY borrow_count DESC LIMIT 1;

-- Query 8: Find which study room are reserved for a specific day
-- resevation is gone

-- Query 9: Find all loans issued by a particular librarian
-- staff id isnt in loans anymore

-- Query 10: Find members who have reserved a book currently on loan
-- reservation is gone

-- Query 11: Find all books that have never been borrowed
SELECT * FROM v_NeverBorrowedBooks;

-- Query 12: Generate a report of fines applied for last quarter
SELECT * FROM v_MemberFines WHERE applied_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH);