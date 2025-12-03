-- Database Creation
DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db;
USE library_db;


-- 1. Core Entities

CREATE TABLE Member (
    member_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    last_name VARCHAR(50) NOT NULL,
    member_type VARCHAR(20) NOT NULL,
    contact_info VARCHAR(100) NOT NULL
);

CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    contact_info VARCHAR(100) NOT NULL
);

CREATE TABLE Room (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(50),
    capacity INT
);

CREATE TABLE Book (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    category VARCHAR(50),
    edition VARCHAR(20)
);

-- 2. Transactions 
CREATE TABLE Loan (
    loan_id INT PRIMARY KEY AUTO_INCREMENT,
    member_id INT,
    isbn VARCHAR(20), -- CHANGED: Now uses ISBN instead of copy_id
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (isbn) REFERENCES Book(isbn) -- Direct link to Book
);

CREATE TABLE Fine (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    loan_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Unpaid',
    applied_date DATE,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id) -- Links to the specific Loan transaction
);

-- 3. Data Input Statements
INSERT INTO Member (first_name, middle_name, last_name, member_type, contact_info) VALUES 
('Jonathan', 'D.', 'Edwards', 'Student', 'jedwards@towson.edu'),
('Saif', 'A.', 'Zagloul', 'Student', 'saif@towson.edu'),
('Landon', 'B.', 'Pasana', 'Student', 'landon@towson.edu'),
('Abdullah', 'NA', 'Radid', 'Student', 'abdullah@towson.edu'),
('Christian', 'idk', 'idk', 'Student', 'christian@towson.edu'),
('Dr.Chhaya', 'NA', 'Kulkarni', 'Professor', 'ckulkarni@towson.edu');

INSERT INTO Staff (first_name, last_name, contact_info) VALUES 
('Alice', 'Librarian', 'desk@library.edu'),
('Bob', 'Manager', 'manager@library.edu');

INSERT INTO Room (room_name, capacity) VALUES 
('Study Room 101', 4),
('Study Room 102', 6),
('Computer Lab A', 30);

INSERT INTO Book (isbn, title, author, publisher, category, edition) VALUES 
('1001', 'Java: A Beginner Guide', 'Herbert Schildt', 'Oracle Press', 'Technology', '8th'),
('1002', 'Database Systems', 'C.J. Date', 'Pearson', 'Education', '6th'),
('1003', 'The Hobbit', 'J.R.R. Tolkien', 'Mariner Books', 'Fiction', '1st');

-- Insert Loans (Now using ISBNs)
-- Jonathan borrows Java book
INSERT INTO Loan (member_id, isbn, issue_date, due_date, return_date) VALUES 
(1, '1001', '2025-10-01', '2025-10-15', NULL); 

-- Saif borrows Database book (Returned late)
INSERT INTO Loan (member_id, isbn, issue_date, due_date, return_date) VALUES 
(2, '1002', '2025-09-01', '2025-09-15', '2025-10-28');

-- Insert Fine (Linked to Saif's loan #2)
INSERT INTO Fine (loan_id, amount, status, applied_date) VALUES 
(2, 15.00, 'Unpaid', '2025-10-29');

-- 4. Verification
SELECT * FROM Member;
SELECT * FROM Book;
SELECT * FROM Loan;
SELECT * FROM Fine;