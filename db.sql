DROP Database IF EXISTS library_db;
CREATE DATABASE library_db;
USE library_db;


-- exists to track who processes loans
CREATE TABLE Staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    Fname VARCHAR(50) NOT NULL,
    Lname VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE
);

-- Tracks students, faculty, and staff members
CREATE TABLE Member (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    Fname VARCHAR(50) NOT NULL,
    Mname VARCHAR(50),
    Lname VARCHAR(50) NOT NULL,
    member_type ENUM('Student', 'Faculty', 'Staff', 'Public') DEFAULT 'Public' ,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Tracks study rooms and labs
CREATE TABLE Room (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    capacity INT NOT NULL,
    room_type VARCHAR(50) NOT NULL -- was multivalue in ERD
);
CREATE TABLE Room (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(50),
    capacity INT
);

-- Metadata for the books
CREATE TABLE Book (
    isbn VARCHAR(20) PRIMARY KEY, -- Unique ID for the book info
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    category VARCHAR(50),   -- maybe would be better as a enum that gets updated as new categories are added
    edition VARCHAR(20)
);



-- Tracks who borrowed what and when
CREATE TABLE Loan (
    loan_id INT PRIMARY KEY AUTO_INCREMENT,
    member_id INT,
    isbn VARCHAR(20), -- CHANGED: Now uses ISBN instead of copy_id
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE DEFAULT NULL, -- NULL means the book has not been returned yet
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (isbn) REFERENCES Book(isbn) -- Direct link to Book
);



-- Tracks penalties for overdue items
CREATE TABLE Fine (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT  -- should probably be unique since single loan generates one fine record
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('Paid', 'Unpaid') DEFAULT 'Unpaid',
    applied_date DATE,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id), -- Links to the specific Loan transaction
);


-- Handles reservations for both Books and Rooms
CREATE TABLE Reservation (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT,
    room_id INT DEFAULT NULL,           -- Populated if reserving a room
    book_isbn VARCHAR(20) DEFAULT NULL, -- Populated if reserving a book
    reservation_date DATE NOT NULL,
    time_slot TIME,                     -- Only needed for rooms
    status ENUM('Pending', 'Fulfilled', 'Cancelled') DEFAULT 'Pending',
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (room_id) REFERENCES Room(room_id),
    FOREIGN KEY (book_isbn) REFERENCES Book(isbn)
);

-- for faster "Find all books by specific author"
CREATE INDEX idx_book_author ON Book(author);

-- for faster "Find all books in specific category"
CREATE INDEX idx_book_category ON Book(category);

-- for faster General search by title
CREATE INDEX idx_book_title ON Book(title);

-- for faster "Find most borrowed book last Quarter" & Quarterly Reports
CREATE INDEX idx_loan_issue_date ON Loan(issue_date);

-- for faster "Find overdue books" (Calculating fines)
CREATE INDEX idx_loan_due_date ON Loan(due_date);

-- for faster Staff looking up a student to issue a book
CREATE INDEX idx_member_lname ON Member(Lname);


-- Data insertion
INSERT INTO Member (first_name, middle_name, last_name, member_type, contact_info) VALUES
('Jonathan', 'D.', 'Edwards', 'Student', 'jedwards@towson.edu'),
('Saif', 'A.', 'Al-Name', 'Student', 'saif@towson.edu'),
('Landon', 'B.', 'Lastname', 'Student', 'landon@towson.edu'),
('Dr.', 'Jane', 'Professor', 'Teacher', 'jane@towson.edu'),
('Blessing','O.','Abumere','Student','babumer@towson.edu')

INSERT INTO Staff (first_name, last_name, contact_info) VALUES
('Alice', 'Librarian', 'desk@library.edu'),
('Bob', 'Manager', 'manager@library.edu');

INSERT INTO Room (room_name, capacity) VALUES
('Study Room 101', 4),
('Study Room 102', 6),
('Computer Lab A', 30);

INSERT INTO Book (isbn, title, author, publisher, category, edition) VALUES
('978-013376', 'Java: A Beginner Guide', 'Herbert Schildt', 'Oracle Press', 'Technology', '8th'),
('978-032112', 'Database Systems', 'C.J. Date', 'Pearson', 'Education', '6th'),
('978-054400', 'The Hobbit', 'J.R.R. Tolkien', 'Mariner Books', 'Fiction', '1st');

-- Insert Loans (Now using ISBNs)
-- Jonathan borrows Java book
INSERT INTO Loan (member_id, isbn, issue_date, due_date, return_date) VALUES
(1, '978-013376', '2025-10-01', '2025-10-15', NULL);

-- Saif borrows Database book (Returned late)
INSERT INTO Loan (member_id, isbn, issue_date, due_date, return_date) VALUES
(2, '978-032112', '2025-09-01', '2025-09-15', '2025-12-01');

-- Insert Fine (Linked to Saif's loan #2)
INSERT INTO Fine (loan_id, amount, status, applied_date) VALUES
(2, 15.00, 'Unpaid', '2025-12-01');

-- ---------------------------------------------------------
-- 4. Verification
-- ---------------------------------------------------------
SELECT * FROM Member;
SELECT * FROM Book;
SELECT * FROM Loan;
SELECT * FROM Fine;