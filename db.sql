DROP Database IF EXISTS library_db;
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;


-- exists to track who processes loans
CREATE TABLE IF NOT EXISTS Staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    Fname VARCHAR(50) NOT NULL,
    Lname VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE
);

-- Tracks students, faculty, and staff members
CREATE TABLE IF NOT EXISTS Member (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    Fname VARCHAR(50) NOT NULL,
    Mname VARCHAR(50),
    Lname VARCHAR(50) NOT NULL,
    member_type ENUM('Student', 'Faculty', 'Staff', 'Public') DEFAULT 'Public' ,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Tracks study rooms and labs
CREATE TABLE IF NOT EXISTS Room (
    room_id INT AUTO_INCREMENT PRIMARY KEY,

    room_type VARCHAR(50) NOT NULL -- was multivalue in ERD
);

-- Metadata for the books
CREATE TABLE IF NOT EXISTS Book (
    ISBN VARCHAR(20) PRIMARY KEY, -- Unique ID for the book info
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    category VARCHAR(50),   -- maybe would be better as a enum that gets updated as new categories are added
    edition VARCHAR(20)
);

-- Tracks actual physical/digital copies on the shelf
CREATE TABLE IF NOT EXISTS BookCopy (
    copy_id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20),
    shelf_location VARCHAR(50),

    copy_type ENUM('Physical', 'Online') DEFAULT 'Physical',
    /* exists to quickly check if a book is available instead of calculating from Loan table;
    if online then it probably shouldnt be able to be loaned lost or reserved
    */
    status ENUM('Available', 'Loaned', 'Lost', 'Reserved') DEFAULT 'Available',
    FOREIGN KEY (isbn) REFERENCES Book(isbn) ON DELETE CASCADE
);

-- Tracks who borrowed what and when
CREATE TABLE IF NOT EXISTS Loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT,
    copy_id INT,
    staff_id INT, -- Staff must process the loan
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE, -- NULL means the book has not been returned yet
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (copy_id) REFERENCES BookCopy(copy_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);

-- Tracks penalties for overdue items
CREATE TABLE IF NOT EXISTS Fine (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT UNIQUE, -- A single loan generates one fine record
    member_id INT,      -- Requirements ask to track Member ID in Fines
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('Paid', 'Unpaid') DEFAULT 'Unpaid',
    applied_date DATE,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id),
    FOREIGN KEY (member_id) REFERENCES Member(member_id)
);

-- Handles reservations for both Books and Rooms
CREATE TABLE IF NOT EXISTS Reservation (
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