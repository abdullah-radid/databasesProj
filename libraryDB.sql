-- ---------------------------------------------------------
-- Database Creation
-- ---------------------------------------------------------
DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db;
USE library_db;

-- ---------------------------------------------------------
-- 1. Core Entities (Members, Staff, Rooms, Books)
-- ---------------------------------------------------------

-- Table: Member
-- Based on ERD  and text [cite: 82]
CREATE TABLE Member (
    member_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    last_name VARCHAR(50) NOT NULL,
    member_type VARCHAR(20) NOT NULL, -- e.g., Student, Teacher
    contact_info VARCHAR(100) NOT NULL
);

-- Table: Staff
-- Based on ERD [cite: 141] and text [cite: 84]
CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    contact_info VARCHAR(100) NOT NULL
);

-- Table: Room
-- Based on ERD [cite: 173] and text [cite: 90]
CREATE TABLE Room (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(50), -- e.g., "Study Room A"
    capacity INT
);

-- Table: Book (General Info)
-- Based on ERD [cite: 160] and text 
-- Tracks ISBN, Title, Author, etc.
CREATE TABLE Book (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    category VARCHAR(50),
    edition VARCHAR(20)
);

-- ---------------------------------------------------------
-- 2. Sub-Entities (Copies)
-- ---------------------------------------------------------

-- Table: Physical_Copy
-- Based on ERD [cite: 167] and text 
-- Links specific physical items to the general Book ISBN
CREATE TABLE Physical_Copy (
    copy_id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20),
    shelf_location VARCHAR(50),
    FOREIGN KEY (isbn) REFERENCES Book(isbn) ON DELETE CASCADE
);

-- Table: Online_Copy
-- Based on ERD [cite: 169]
CREATE TABLE Online_Copy (
    online_id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20),
    url_link VARCHAR(255),
    FOREIGN KEY (isbn) REFERENCES Book(isbn) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- 3. Transaction Entities (Loans, Reservations, Fines)
-- ---------------------------------------------------------

-- Table: Loan
-- Based on ERD [cite: 179] and text 
CREATE TABLE Loan (
    loan_id INT PRIMARY KEY AUTO_INCREMENT,
    member_id INT,
    copy_id INT,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE, -- Can be NULL if not returned yet
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (copy_id) REFERENCES Physical_Copy(copy_id)
);

-- Table: Fine
-- Based on ERD  and text [cite: 88]
CREATE TABLE Fine (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    loan_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Unpaid', -- Paid or Unpaid
    applied_date DATE,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id)
);

-- ---------------------------------------------------------
-- 4. Data Input Statements (Sample Data)
-- ---------------------------------------------------------

-- Insert Members (Using group names from report [cite: 1-6])
INSERT INTO Member (first_name, middle_name, last_name, member_type, contact_info) VALUES 
('Jonathan', 'D.', 'Edwards', 'Student', 'jedwards@towson.edu'),
('Saif', 'A.', 'Al-Name', 'Student', 'saif@towson.edu'),
('Landon', 'B.', 'Lastname', 'Student', 'landon@towson.edu'),
('Dr.', 'Jane', 'Professor', 'Teacher', 'jane@towson.edu');

-- Insert Staff
INSERT INTO Staff (first_name, last_name, contact_info) VALUES 
('Alice', 'Librarian', 'desk@library.edu'),
('Bob', 'Manager', 'manager@library.edu');

-- Insert Rooms
INSERT INTO Room (room_name, capacity) VALUES 
('Study Room 101', 4),
('Study Room 102', 6),
('Computer Lab A', 30);

-- Insert Books
INSERT INTO Book (isbn, title, author, publisher, category, edition) VALUES 
('978-013376', 'Java: A Beginner Guide', 'Herbert Schildt', 'Oracle Press', 'Technology', '8th'),
('978-032112', 'Database Systems', 'C.J. Date', 'Pearson', 'Education', '6th'),
('978-054400', 'The Hobbit', 'J.R.R. Tolkien', 'Mariner Books', 'Fiction', '1st');

-- Insert Physical Copies (Mapping to Books)
INSERT INTO Physical_Copy (isbn, shelf_location) VALUES 
('978-013376', 'Row 5, Shelf A'), -- Copy of Java
('978-013376', 'Row 5, Shelf A'), -- Second copy of Java
('978-032112', 'Row 3, Shelf B'),
('978-054400', 'Row 1, Shelf C');

-- Insert Loans (Transactions)
-- Jonathan borrows Java book
INSERT INTO Loan (member_id, copy_id, issue_date, due_date, return_date) VALUES 
(1, 1, '2025-10-01', '2025-10-15', NULL); 

-- Saif borrows Database book (Returned late)
INSERT INTO Loan (member_id, copy_id, issue_date, due_date, return_date) VALUES 
(2, 3, '2025-09-01', '2025-09-15', '2025-12-01');

-- Insert Fine (for the late book above)
-- Text says fines apply if 75 days late [cite: 34]
INSERT INTO Fine (loan_id, amount, status, applied_date) VALUES 
(2, 15.00, 'Unpaid', '2025-12-01');


-- Verify Data
SELECT * FROM Member;
SELECT * FROM Book;
SELECT * FROM Loan;