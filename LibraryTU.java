import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryTU extends JFrame {

    // ===== DB CONFIG (change user/password if needed) =====
    private String url = "jdbc:mysql://localhost:3306/library_db";
    private String user = "root";
    private String password = "Paniarup115!";

    // ===== MEMBER FIELDS =====
    private JTextField memberIDField, memberFirstNameField, memberMiddleNameField,
            memberLastNameField, memberTypeField, memberContactInfoField;
    private JTextArea memberOutput;

    // ===== STAFF FIELDS =====
    private JTextField staffIDField, staffFirstNameField, staffLastNameField, staffContactInfoField;
    private JTextArea staffOutput;

    // ===== ROOM FIELDS =====
    private JTextField roomIDField, roomNameField, roomCapacityField;
    private JTextArea roomOutput;

    // ===== BOOK FIELDS =====
    private JTextField bookIsbnField, bookTitleField, bookAuthorField,
            bookPublisherField, bookCategoryField, bookEditionField;
    private JTextArea bookOutput;

    // ===== LOAN FIELDS (uses isbn) =====
    private JTextField loanIDField, loanMemberIDField, loanIsbnField,
            loanIssueDateField, loanDueDateField, loanReturnDateField;
    private JTextArea loanOutput;

    // ===== FINE FIELDS =====
    private JTextField fineIDField, fineLoanIDField, fineAmountField,
            fineStatusField, fineAppliedDateField;
    private JTextArea fineOutput;

    // ===== CONSTRUCTOR =====
    public LibraryTU() {
        setTitle("Towson Library DB");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Member", createMemberTab());
        tabs.addTab("Staff", createStaffTab());
        tabs.addTab("Room", createRoomTab());
        tabs.addTab("Book", createBookTab());
        tabs.addTab("Loan", createLoanTab());
        tabs.addTab("Fine", createFineTab());

        add(tabs, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ====================== GENERIC VIEW HELPER ======================

    private void viewTable(String tableName, JTextArea targetArea) {
        targetArea.setText("");
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();

            List<String[]> rows = new ArrayList<>();
            String[] header = new String[colCount];
            int[] widths = new int[colCount];

            // headers
            for (int i = 0; i < colCount; i++) {
                header[i] = md.getColumnLabel(i + 1);
                widths[i] = header[i].length();
            }
            rows.add(header);

            // data rows + track max width
            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    String val = rs.getString(i + 1);
                    if (val == null) val = "null";
                    row[i] = val;
                    if (val.length() > widths[i]) {
                        widths[i] = val.length();
                    }
                }
                rows.add(row);
            }

            // build padded text
            StringBuilder sb = new StringBuilder();
            for (String[] row : rows) {
                for (int i = 0; i < colCount; i++) {
                    sb.append(padRight(row[i], widths[i] + 2)); // +2 spaces
                }
                sb.append("\n");
            }

            targetArea.setText(sb.toString());

        } catch (Exception ex) {
            targetArea.setText("Error: " + ex.getMessage());
        }
    }

    private String padRight(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    // ====================== MEMBER TAB ======================

    private JPanel createMemberTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 4));

        top.add(new JLabel("member_id:"));
        memberIDField = new JTextField();
        top.add(memberIDField);

        top.add(new JLabel("first_name:"));
        memberFirstNameField = new JTextField();
        top.add(memberFirstNameField);

        top.add(new JLabel("middle_name:"));
        memberMiddleNameField = new JTextField();
        top.add(memberMiddleNameField);

        top.add(new JLabel("last_name:"));
        memberLastNameField = new JTextField();
        top.add(memberLastNameField);

        top.add(new JLabel("member_type:"));
        memberTypeField = new JTextField();
        top.add(memberTypeField);

        top.add(new JLabel("contact_info:"));
        memberContactInfoField = new JTextField();
        top.add(memberContactInfoField);

        panel.add(top, BorderLayout.NORTH);

        memberOutput = new JTextArea();
        memberOutput.setEditable(false);
        memberOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(memberOutput), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 4));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update (by ID)");
        JButton removeBtn = new JButton("Remove (by ID)");
        JButton viewBtn = new JButton("View All");

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        removeBtn.addActionListener(e -> removeMember());
        viewBtn.addActionListener(e -> viewTable("Member", memberOutput));

        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(removeBtn);
        bottom.add(viewBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addMember() {
        String first = memberFirstNameField.getText().trim();
        String middle = memberMiddleNameField.getText().trim();
        String last = memberLastNameField.getText().trim();
        String type = memberTypeField.getText().trim();
        String contact = memberContactInfoField.getText().trim();

        String sql = "INSERT INTO Member (first_name, middle_name, last_name, member_type, contact_info) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, first);
            if (middle.isEmpty()) ps.setNull(2, Types.VARCHAR); else ps.setString(2, middle);
            ps.setString(3, last);
            ps.setString(4, type);
            ps.setString(5, contact);

            int rows = ps.executeUpdate();
            memberOutput.setText(rows + " Member(s) added.");

        } catch (Exception ex) {
            memberOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void updateMember() {
        String idText = memberIDField.getText().trim();
        if (idText.isEmpty()) {
            memberOutput.setText("Enter member_id to update.");
            return;
        }

        String first = memberFirstNameField.getText().trim();
        String middle = memberMiddleNameField.getText().trim();
        String last = memberLastNameField.getText().trim();
        String type = memberTypeField.getText().trim();
        String contact = memberContactInfoField.getText().trim();

        String sql = "UPDATE Member SET first_name = ?, middle_name = ?, last_name = ?, " +
                "member_type = ?, contact_info = ? WHERE member_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, first);
            if (middle.isEmpty()) ps.setNull(2, Types.VARCHAR); else ps.setString(2, middle);
            ps.setString(3, last);
            ps.setString(4, type);
            ps.setString(5, contact);
            ps.setInt(6, Integer.parseInt(idText));

            int rows = ps.executeUpdate();
            memberOutput.setText(rows + " Member(s) updated.");

        } catch (Exception ex) {
            memberOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void removeMember() {
        String idText = memberIDField.getText().trim();
        if (idText.isEmpty()) {
            memberOutput.setText("Enter member_id to remove.");
            return;
        }
        String sql = "DELETE FROM Member WHERE member_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idText));
            int rows = ps.executeUpdate();
            memberOutput.setText(rows + " Member(s) removed.");

        } catch (Exception ex) {
            memberOutput.setText("Error: " + ex.getMessage());
        }
    }

    // ====================== STAFF TAB ======================

    private JPanel createStaffTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 4));
        top.add(new JLabel("staff_id:"));
        staffIDField = new JTextField();
        top.add(staffIDField);

        top.add(new JLabel("first_name:"));
        staffFirstNameField = new JTextField();
        top.add(staffFirstNameField);

        top.add(new JLabel("last_name:"));
        staffLastNameField = new JTextField();
        top.add(staffLastNameField);

        top.add(new JLabel("contact_info:"));
        staffContactInfoField = new JTextField();
        top.add(staffContactInfoField);

        panel.add(top, BorderLayout.NORTH);

        staffOutput = new JTextArea();
        staffOutput.setEditable(false);
        staffOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(staffOutput), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 4));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update (by ID)");
        JButton removeBtn = new JButton("Remove (by ID)");
        JButton viewBtn = new JButton("View All");

        addBtn.addActionListener(e -> addStaff());
        updateBtn.addActionListener(e -> updateStaff());
        removeBtn.addActionListener(e -> removeStaff());
        viewBtn.addActionListener(e -> viewTable("Staff", staffOutput));

        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(removeBtn);
        bottom.add(viewBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addStaff() {
        String first = staffFirstNameField.getText().trim();
        String last = staffLastNameField.getText().trim();
        String contact = staffContactInfoField.getText().trim();

        String sql = "INSERT INTO Staff (first_name, last_name, contact_info) VALUES (?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, contact);

            int rows = ps.executeUpdate();
            staffOutput.setText(rows + " Staff(s) added.");

        } catch (Exception ex) {
            staffOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void updateStaff() {
        String idText = staffIDField.getText().trim();
        if (idText.isEmpty()) {
            staffOutput.setText("Enter staff_id to update.");
            return;
        }

        String first = staffFirstNameField.getText().trim();
        String last = staffLastNameField.getText().trim();
        String contact = staffContactInfoField.getText().trim();

        String sql = "UPDATE Staff SET first_name = ?, last_name = ?, contact_info = ? WHERE staff_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, contact);
            ps.setInt(4, Integer.parseInt(idText));

            int rows = ps.executeUpdate();
            staffOutput.setText(rows + " Staff(s) updated.");

        } catch (Exception ex) {
            staffOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void removeStaff() {
        String idText = staffIDField.getText().trim();
        if (idText.isEmpty()) {
            staffOutput.setText("Enter staff_id to remove.");
            return;
        }
        String sql = "DELETE FROM Staff WHERE staff_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idText));
            int rows = ps.executeUpdate();
            staffOutput.setText(rows + " Staff(s) removed.");

        } catch (Exception ex) {
            staffOutput.setText("Error: " + ex.getMessage());
        }
    }

    // ====================== ROOM TAB ======================

    private JPanel createRoomTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 4));
        top.add(new JLabel("room_id:"));
        roomIDField = new JTextField();
        top.add(roomIDField);

        top.add(new JLabel("room_name:"));
        roomNameField = new JTextField();
        top.add(roomNameField);

        top.add(new JLabel("capacity:"));
        roomCapacityField = new JTextField();
        top.add(roomCapacityField);

        panel.add(top, BorderLayout.NORTH);

        roomOutput = new JTextArea();
        roomOutput.setEditable(false);
        roomOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(roomOutput), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 4));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update (by ID)");
        JButton removeBtn = new JButton("Remove (by ID)");
        JButton viewBtn = new JButton("View All");

        addBtn.addActionListener(e -> addRoom());
        updateBtn.addActionListener(e -> updateRoom());
        removeBtn.addActionListener(e -> removeRoom());
        viewBtn.addActionListener(e -> viewTable("Room", roomOutput));

        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(removeBtn);
        bottom.add(viewBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addRoom() {
        String name = roomNameField.getText().trim();
        String capText = roomCapacityField.getText().trim();

        String sql = "INSERT INTO Room (room_name, capacity) VALUES (?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (name.isEmpty()) ps.setNull(1, Types.VARCHAR); else ps.setString(1, name);
            if (capText.isEmpty()) ps.setNull(2, Types.INTEGER); else ps.setInt(2, Integer.parseInt(capText));

            int rows = ps.executeUpdate();
            roomOutput.setText(rows + " Room(s) added.");

        } catch (Exception ex) {
            roomOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void updateRoom() {
        String idText = roomIDField.getText().trim();
        if (idText.isEmpty()) {
            roomOutput.setText("Enter room_id to update.");
            return;
        }

        String name = roomNameField.getText().trim();
        String capText = roomCapacityField.getText().trim();

        String sql = "UPDATE Room SET room_name = ?, capacity = ? WHERE room_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (name.isEmpty()) ps.setNull(1, Types.VARCHAR); else ps.setString(1, name);
            if (capText.isEmpty()) ps.setNull(2, Types.INTEGER); else ps.setInt(2, Integer.parseInt(capText));
            ps.setInt(3, Integer.parseInt(idText));

            int rows = ps.executeUpdate();
            roomOutput.setText(rows + " Room(s) updated.");

        } catch (Exception ex) {
            roomOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void removeRoom() {
        String idText = roomIDField.getText().trim();
        if (idText.isEmpty()) {
            roomOutput.setText("Enter room_id to remove.");
            return;
        }

        String sql = "DELETE FROM Room WHERE room_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idText));
            int rows = ps.executeUpdate();
            roomOutput.setText(rows + " Room(s) removed.");

        } catch (Exception ex) {
            roomOutput.setText("Error: " + ex.getMessage());
        }
    }

    // ====================== BOOK TAB ======================

    private JPanel createBookTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 4));
        top.add(new JLabel("isbn:"));
        bookIsbnField = new JTextField();
        top.add(bookIsbnField);

        top.add(new JLabel("title:"));
        bookTitleField = new JTextField();
        top.add(bookTitleField);

        top.add(new JLabel("author:"));
        bookAuthorField = new JTextField();
        top.add(bookAuthorField);

        top.add(new JLabel("publisher:"));
        bookPublisherField = new JTextField();
        top.add(bookPublisherField);

        top.add(new JLabel("category:"));
        bookCategoryField = new JTextField();
        top.add(bookCategoryField);

        top.add(new JLabel("edition:"));
        bookEditionField = new JTextField();
        top.add(bookEditionField);

        panel.add(top, BorderLayout.NORTH);

        bookOutput = new JTextArea();
        bookOutput.setEditable(false);
        bookOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(bookOutput), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 4));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update (by ISBN)");
        JButton removeBtn = new JButton("Remove (by ISBN)");
        JButton viewBtn = new JButton("View All");

        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        removeBtn.addActionListener(e -> removeBook());
        viewBtn.addActionListener(e -> viewTable("Book", bookOutput));

        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(removeBtn);
        bottom.add(viewBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addBook() {
        String isbn = bookIsbnField.getText().trim();
        String title = bookTitleField.getText().trim();
        String author = bookAuthorField.getText().trim();
        String publisher = bookPublisherField.getText().trim();
        String category = bookCategoryField.getText().trim();
        String edition = bookEditionField.getText().trim();

        String sql = "INSERT INTO Book (isbn, title, author, publisher, category, edition) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            ps.setString(2, title);
            ps.setString(3, author);
            if (publisher.isEmpty()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, publisher);
            if (category.isEmpty()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, category);
            if (edition.isEmpty()) ps.setNull(6, Types.VARCHAR); else ps.setString(6, edition);

            int rows = ps.executeUpdate();
            bookOutput.setText(rows + " Book(s) added.");

        } catch (Exception ex) {
            bookOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void updateBook() {
        String isbn = bookIsbnField.getText().trim();
        if (isbn.isEmpty()) {
            bookOutput.setText("Enter isbn to update.");
            return;
        }

        String title = bookTitleField.getText().trim();
        String author = bookAuthorField.getText().trim();
        String publisher = bookPublisherField.getText().trim();
        String category = bookCategoryField.getText().trim();
        String edition = bookEditionField.getText().trim();

        String sql = "UPDATE Book SET title = ?, author = ?, publisher = ?, " +
                "category = ?, edition = ? WHERE isbn = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            if (publisher.isEmpty()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, publisher);
            if (category.isEmpty()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, category);
            if (edition.isEmpty()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, edition);
            ps.setString(6, isbn);

            int rows = ps.executeUpdate();
            bookOutput.setText(rows + " Book(s) updated.");

        } catch (Exception ex) {
            bookOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void removeBook() {
        String isbn = bookIsbnField.getText().trim();
        if (isbn.isEmpty()) {
            bookOutput.setText("Enter isbn to remove.");
            return;
        }

        String sql = "DELETE FROM Book WHERE isbn = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            int rows = ps.executeUpdate();
            bookOutput.setText(rows + " Book(s) removed.");

        } catch (Exception ex) {
            bookOutput.setText("Error: " + ex.getMessage());
        }
    }

    // ====================== LOAN TAB (member_id + isbn) ======================

    private JPanel createLoanTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 4));
        top.add(new JLabel("loan_id:"));
        loanIDField = new JTextField();
        top.add(loanIDField);

        top.add(new JLabel("member_id:"));
        loanMemberIDField = new JTextField();
        top.add(loanMemberIDField);

        top.add(new JLabel("isbn:"));
        loanIsbnField = new JTextField();
        top.add(loanIsbnField);

        top.add(new JLabel("issue_date (YYYY-MM-DD):"));
        loanIssueDateField = new JTextField();
        top.add(loanIssueDateField);

        top.add(new JLabel("due_date (YYYY-MM-DD):"));
        loanDueDateField = new JTextField();
        top.add(loanDueDateField);

        top.add(new JLabel("return_date (YYYY-MM-DD):"));
        loanReturnDateField = new JTextField();
        top.add(loanReturnDateField);

        panel.add(top, BorderLayout.NORTH);

        loanOutput = new JTextArea();
        loanOutput.setEditable(false);
        loanOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(loanOutput), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 4));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update (by ID)");
        JButton removeBtn = new JButton("Remove (by ID)");
        JButton viewBtn = new JButton("View All");

        addBtn.addActionListener(e -> addLoan());
        updateBtn.addActionListener(e -> updateLoan());
        removeBtn.addActionListener(e -> removeLoan());
        viewBtn.addActionListener(e -> viewTable("Loan", loanOutput));

        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(removeBtn);
        bottom.add(viewBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addLoan() {
        String memberID = loanMemberIDField.getText().trim();
        String isbn     = loanIsbnField.getText().trim();
        String issue    = loanIssueDateField.getText().trim();
        String due      = loanDueDateField.getText().trim();
        String ret      = loanReturnDateField.getText().trim();

        if (isbn.isEmpty()) {
            loanOutput.setText("ISBN is required.");
            return;
        }
        if (issue.isEmpty()) {
            loanOutput.setText("Issue date is required (YYYY-MM-DD).");
            return;
        }
        if (due.isEmpty()) {
            loanOutput.setText("Due date is required (YYYY-MM-DD).");
            return;
        }

        LocalDate issueLocal;
        LocalDate dueLocal;
        LocalDate retLocal = null;

        try {
            issueLocal = LocalDate.parse(issue);
        } catch (Exception ex) {
            loanOutput.setText("Issue date must be in format YYYY-MM-DD.");
            return;
        }

        try {
            dueLocal = LocalDate.parse(due);
        } catch (Exception ex) {
            loanOutput.setText("Due date must be in format YYYY-MM-DD.");
            return;
        }

        if (!ret.isEmpty()) {
            try {
                retLocal = LocalDate.parse(ret);
            } catch (Exception ex) {
                loanOutput.setText("Return date must be in format YYYY-MM-DD.");
                return;
            }
        }

        LocalDate today = LocalDate.now();

        if (issueLocal.equals(today)) {
            loanOutput.setText("Issue date cannot be today.");
            return;
        }
        if (!issueLocal.isBefore(dueLocal)) {
            loanOutput.setText("Issue date must be before due date.");
            return;
        }
        if (retLocal != null && retLocal.isBefore(issueLocal)) {
            loanOutput.setText("Return date cannot be before issue date.");
            return;
        }

        String insertSql = "INSERT INTO Loan (member_id, isbn, issue_date, due_date, return_date) " +
                           "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConn()) {

            // FK check: member exists (if provided)
            if (!memberID.isEmpty()) {
                String memSql = "SELECT COUNT(*) FROM Member WHERE member_id = ?";
                try (PreparedStatement memPs = conn.prepareStatement(memSql)) {
                    memPs.setInt(1, Integer.parseInt(memberID));
                    try (ResultSet rs = memPs.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            loanOutput.setText("Member ID " + memberID + " does not exist.");
                            return;
                        }
                    }
                }
            }

            // FK check: book exists
            String bookSql = "SELECT COUNT(*) FROM Book WHERE isbn = ?";
            try (PreparedStatement bookPs = conn.prepareStatement(bookSql)) {
                bookPs.setString(1, isbn);
                try (ResultSet rs = bookPs.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        loanOutput.setText("Book with ISBN " + isbn + " does not exist.");
                        return;
                    }
                }
            }

            // validation: book not already on open loan
            String checkSql = "SELECT COUNT(*) FROM Loan WHERE isbn = ? AND return_date IS NULL";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, isbn);
                try (ResultSet rs = checkPs.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        loanOutput.setText("That book is already on loan and cannot be loaned out again.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                if (memberID.isEmpty()) ps.setNull(1, Types.INTEGER);
                else ps.setInt(1, Integer.parseInt(memberID));

                ps.setString(2, isbn);
                ps.setDate(3, java.sql.Date.valueOf(issueLocal));
                ps.setDate(4, java.sql.Date.valueOf(dueLocal));

                if (retLocal == null) ps.setNull(5, Types.DATE);
                else ps.setDate(5, java.sql.Date.valueOf(retLocal));

                int rows = ps.executeUpdate();
                loanOutput.setText(rows + " Loan(s) added.");
            }

        } catch (Exception ex) {
            loanOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void updateLoan() {
        String idText   = loanIDField.getText().trim();
        String memberID = loanMemberIDField.getText().trim();
        String isbn     = loanIsbnField.getText().trim();
        String issue    = loanIssueDateField.getText().trim();
        String due      = loanDueDateField.getText().trim();
        String ret      = loanReturnDateField.getText().trim();

        if (idText.isEmpty()) {
            loanOutput.setText("Enter loan_id to update.");
            return;
        }
        if (isbn.isEmpty()) {
            loanOutput.setText("ISBN is required.");
            return;
        }
        if (issue.isEmpty()) {
            loanOutput.setText("Issue date is required (YYYY-MM-DD).");
            return;
        }
        if (due.isEmpty()) {
            loanOutput.setText("Due date is required (YYYY-MM-DD).");
            return;
        }

        LocalDate issueLocal;
        LocalDate dueLocal;
        LocalDate retLocal = null;

        try {
            issueLocal = LocalDate.parse(issue);
        } catch (Exception ex) {
            loanOutput.setText("Issue date must be in format YYYY-MM-DD.");
            return;
        }

        try {
            dueLocal = LocalDate.parse(due);
        } catch (Exception ex) {
            loanOutput.setText("Due date must be in format YYYY-MM-DD.");
            return;
        }

        if (!ret.isEmpty()) {
            try {
                retLocal = LocalDate.parse(ret);
            } catch (Exception ex) {
                loanOutput.setText("Return date must be in format YYYY-MM-DD.");
                return;
            }
        }

        LocalDate today = LocalDate.now();

        if (issueLocal.equals(today)) {
            loanOutput.setText("Issue date cannot be today.");
            return;
        }
        if (!issueLocal.isBefore(dueLocal)) {
            loanOutput.setText("Issue date must be before due date.");
            return;
        }
        if (retLocal != null && retLocal.isBefore(issueLocal)) {
            loanOutput.setText("Return date cannot be before issue date.");
            return;
        }

        String updateSql = "UPDATE Loan SET member_id = ?, isbn = ?, issue_date = ?, " +
                           "due_date = ?, return_date = ? WHERE loan_id = ?";

        try (Connection conn = getConn()) {

            // FK check: member exists (if provided)
            if (!memberID.isEmpty()) {
                String memSql = "SELECT COUNT(*) FROM Member WHERE member_id = ?";
                try (PreparedStatement memPs = conn.prepareStatement(memSql)) {
                    memPs.setInt(1, Integer.parseInt(memberID));
                    try (ResultSet rs = memPs.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            loanOutput.setText("Member ID " + memberID + " does not exist.");
                            return;
                        }
                    }
                }
            }

            // FK check: book exists
            String bookSql = "SELECT COUNT(*) FROM Book WHERE isbn = ?";
            try (PreparedStatement bookPs = conn.prepareStatement(bookSql)) {
                bookPs.setString(1, isbn);
                try (ResultSet rs = bookPs.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        loanOutput.setText("Book with ISBN " + isbn + " does not exist.");
                        return;
                    }
                }
            }

            // validation: book not already on open loan in another row
            String checkSql = "SELECT COUNT(*) FROM Loan " +
                              "WHERE isbn = ? AND return_date IS NULL AND loan_id <> ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, isbn);
                checkPs.setInt(2, Integer.parseInt(idText));
                try (ResultSet rs = checkPs.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        loanOutput.setText("That book is already on loan in another record.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {

                if (memberID.isEmpty()) ps.setNull(1, Types.INTEGER);
                else ps.setInt(1, Integer.parseInt(memberID));

                ps.setString(2, isbn);
                ps.setDate(3, java.sql.Date.valueOf(issueLocal));
                ps.setDate(4, java.sql.Date.valueOf(dueLocal));

                if (retLocal == null) ps.setNull(5, Types.DATE);
                else ps.setDate(5, java.sql.Date.valueOf(retLocal));

                ps.setInt(6, Integer.parseInt(idText));

                int rows = ps.executeUpdate();
                loanOutput.setText(rows + " Loan(s) updated.");
            }

        } catch (Exception ex) {
            loanOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void removeLoan() {
        String idText = loanIDField.getText().trim();
        if (idText.isEmpty()) {
            loanOutput.setText("Enter loan_id to remove.");
            return;
        }

        String sql = "DELETE FROM Loan WHERE loan_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idText));
            int rows = ps.executeUpdate();
            loanOutput.setText(rows + " Loan(s) removed.");

        } catch (Exception ex) {
            loanOutput.setText("Error: " + ex.getMessage());
        }
    }

    // ====================== FINE TAB ======================

    private JPanel createFineTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 4));
        top.add(new JLabel("fine_id:"));
        fineIDField = new JTextField();
        top.add(fineIDField);

        top.add(new JLabel("loan_id:"));
        fineLoanIDField = new JTextField();
        top.add(fineLoanIDField);

        top.add(new JLabel("amount:"));
        fineAmountField = new JTextField();
        top.add(fineAmountField);

        top.add(new JLabel("status:"));
        fineStatusField = new JTextField();
        top.add(fineStatusField);

        top.add(new JLabel("applied_date (YYYY-MM-DD):"));
        fineAppliedDateField = new JTextField();
        top.add(fineAppliedDateField);

        panel.add(top, BorderLayout.NORTH);

        fineOutput = new JTextArea();
        fineOutput.setEditable(false);
        fineOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(fineOutput), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 4));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update (by ID)");
        JButton removeBtn = new JButton("Remove (by ID)");
        JButton viewBtn = new JButton("View All");

        addBtn.addActionListener(e -> addFine());
        updateBtn.addActionListener(e -> updateFine());
        removeBtn.addActionListener(e -> removeFine());
        viewBtn.addActionListener(e -> viewTable("Fine", fineOutput));

        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(removeBtn);
        bottom.add(viewBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addFine() {
        String loanID = fineLoanIDField.getText().trim();
        String amountText = fineAmountField.getText().trim();
        String status = fineStatusField.getText().trim();
        String applied = fineAppliedDateField.getText().trim();

        String sql = "INSERT INTO Fine (loan_id, amount, status, applied_date) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (loanID.isEmpty()) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, Integer.parseInt(loanID));

            if (amountText.isEmpty()) ps.setNull(2, Types.DECIMAL);
            else {
                BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(amountText));
                ps.setBigDecimal(2, bd);
            }

            if (status.isEmpty()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, status);

            if (applied.isEmpty()) ps.setNull(4, Types.DATE);
            else ps.setDate(4, java.sql.Date.valueOf(applied));

            int rows = ps.executeUpdate();
            fineOutput.setText(rows + " Fine(s) added.");

        } catch (Exception ex) {
            fineOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void updateFine() {
        String idText = fineIDField.getText().trim();
        if (idText.isEmpty()) {
            fineOutput.setText("Enter fine_id to update.");
            return;
        }

        String loanID = fineLoanIDField.getText().trim();
        String amountText = fineAmountField.getText().trim();
        String status = fineStatusField.getText().trim();
        String applied = fineAppliedDateField.getText().trim();

        String sql = "UPDATE Fine SET loan_id = ?, amount = ?, status = ?, applied_date = ? WHERE fine_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (loanID.isEmpty()) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, Integer.parseInt(loanID));

            if (amountText.isEmpty()) ps.setNull(2, Types.DECIMAL);
            else {
                BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(amountText));
                ps.setBigDecimal(2, bd);
            }

            if (status.isEmpty()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, status);

            if (applied.isEmpty()) ps.setNull(4, Types.DATE);
            else ps.setDate(4, java.sql.Date.valueOf(applied));

            ps.setInt(5, Integer.parseInt(idText));

            int rows = ps.executeUpdate();
            fineOutput.setText(rows + " Fine(s) updated.");

        } catch (Exception ex) {
            fineOutput.setText("Error: " + ex.getMessage());
        }
    }

    private void removeFine() {
        String idText = fineIDField.getText().trim();
        if (idText.isEmpty()) {
            fineOutput.setText("Enter fine_id to remove.");
            return;
        }

        String sql = "DELETE FROM Fine WHERE fine_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idText));
            int rows = ps.executeUpdate();
            fineOutput.setText(rows + " Fine(s) removed.");

        } catch (Exception ex) {
            fineOutput.setText("Error: " + ex.getMessage());
        }
    }

    // ====================== DB CONNECTION ======================

    private Connection getConn() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    // ====================== MAIN ======================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryTU::new);
    }
}
