import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class LibraryTU extends JFrame {

    // Database connection details
    String url = "jdbc:mysql://localhost:3306/library_db";
    String user = "root";
    String password = "Paniarup115!";

    // Input fields and output area (used by Member tab)
    JTextField memberIDField, firstNameField, middleNameField, lastNameField, memberTypeField, contactInfoField;
    JTextArea output;

    public LibraryTU() {
        // Basic window setup
        setTitle("Towson Library DB");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --------- TABS ----------
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Members", createMemberTab());
        tabs.addTab("Staff", createStaffTab());
        tabs.addTab("Rooms", createRoomTab());
        tabs.addTab("Books", createBookTab());
        tabs.addTab("Loans", createLoanTab());
        tabs.addTab("Fines", createFineTab());

        add(tabs, BorderLayout.CENTER);

        setVisible(true);   // Show window
    }

    // Build the MEMBERS tab (this is basically your old layout)
    private JPanel createMemberTab() {
        JPanel panel = new JPanel(new BorderLayout());

        // ---------- Top Panel: Input Fields ----------
        JPanel top = new JPanel(new GridLayout(4, 6));

        top.add(new JLabel("Member ID:"));
        memberIDField = new JTextField();
        top.add(memberIDField);

        top.add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        top.add(firstNameField);

        top.add(new JLabel("Middle Name:"));
        middleNameField = new JTextField();
        top.add(middleNameField);

        top.add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        top.add(lastNameField);

        top.add(new JLabel("Member Type:"));
        memberTypeField = new JTextField();
        top.add(memberTypeField);

        top.add(new JLabel("Contact Info:"));
        contactInfoField = new JTextField();
        top.add(contactInfoField);

        panel.add(top, BorderLayout.NORTH);

        // ---------- Center Area: Output ----------
        output = new JTextArea();
        output.setEditable(false);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);

        // ---------- Bottom Panel: Buttons ----------
        JPanel bottom = new JPanel(new GridLayout(1, 3));

        JButton viewBtn   = new JButton("View");
        JButton addBtn    = new JButton("Add");
        JButton updateBtn = new JButton("Update");

        bottom.add(viewBtn);
        bottom.add(addBtn);
        bottom.add(updateBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        // Button actions call your existing methods
        viewBtn.addActionListener(e -> viewMember());
        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());

        return panel;
    }

    // --------- STUB TABS FOR OTHER TABLES ---------
    // You can later give each of these its own fields, buttons, and queries.

    private JPanel createStaffTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Staff table UI goes here"), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTextArea("TODO: Staff view/add/update")), BorderLayout.CENTER);
        return p;
    }

    private JPanel createRoomTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Room table UI goes here"), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTextArea("TODO: Room view/add/update")), BorderLayout.CENTER);
        return p;
    }

    private JPanel createBookTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Book table UI goes here"), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTextArea("TODO: Book view/add/update")), BorderLayout.CENTER);
        return p;
    }

    private JPanel createLoanTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Loan table UI goes here"), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTextArea("TODO: Loan view/add/update")), BorderLayout.CENTER);
        return p;
    }

    private JPanel createFineTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Fine table UI goes here"), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTextArea("TODO: Fine view/add/update")), BorderLayout.CENTER);
        return p;
    }

    // ---------- DB Connection ----------
    private Connection getConn() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    // ---------- MEMBER: View ----------
    private void viewMember() {
        output.setText("");

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Member")) {

            while (rs.next()) {
                output.append(
                    rs.getInt("member_id") + " | " +
                    rs.getString("first_name") + " | " +
                    rs.getString("middle_name") + " | " +
                    rs.getString("last_name") + " | " +
                    rs.getString("member_type") + " | " +
                    rs.getString("contact_info") + "\n"
                );
            }

        } catch (Exception ex) {
            output.setText(ex.getMessage());
        }
    }

    // ---------- MEMBER: Add ----------
    private void addMember() {
        output.setText(""); 

        String memberIDText = memberIDField.getText().trim();
        String firstName    = firstNameField.getText().trim();
        String middleName   = middleNameField.getText().trim();
        String lastName     = lastNameField.getText().trim();
        String memberType   = memberTypeField.getText().trim();
        String contactInfo  = contactInfoField.getText().trim();

        StringBuilder missing = new StringBuilder();
        if (memberIDText.isEmpty()) missing.append("Member ID, ");
        if (firstName.isEmpty())    missing.append("First Name, ");
        if (middleName.isEmpty())   missing.append("Middle Name, ");
        if (lastName.isEmpty())     missing.append("Last Name, ");
        if (memberType.isEmpty())   missing.append("Member Type, ");
        if (contactInfo.isEmpty())  missing.append("Contact Info, ");

        if (missing.length() > 0) {
            missing.setLength(missing.length() - 2);
            output.setText("You are missing: " + missing);
            return;
        }

        int memberID;
        try {
            memberID = Integer.parseInt(memberIDText);
        } catch (NumberFormatException e) {
            output.setText("Member ID must be a number.");
            return;
        }

        String sql = "INSERT INTO Member(member_id, first_name, middle_name, last_name, member_type, contact_info) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberID);
            ps.setString(2, firstName);
            ps.setString(3, middleName);
            ps.setString(4, lastName);
            ps.setString(5, memberType);
            ps.setString(6, contactInfo);

            ps.executeUpdate();
            output.setText("Added: " + memberID);

        } catch (Exception ex) {
            output.setText(ex.getMessage());
        }
    }

    // ---------- MEMBER: Update ----------
    private void updateMember() {
        output.setText(""); 

        String memberIDText = memberIDField.getText().trim();
        String firstName    = firstNameField.getText().trim();
        String middleName   = middleNameField.getText().trim();
        String lastName     = lastNameField.getText().trim();
        String memberType   = memberTypeField.getText().trim();
        String contactInfo  = contactInfoField.getText().trim();

        StringBuilder missing = new StringBuilder();
        if (memberIDText.isEmpty()) missing.append("Member ID, ");
        if (firstName.isEmpty())    missing.append("First Name, ");
        if (middleName.isEmpty())   missing.append("Middle Name, ");
        if (lastName.isEmpty())     missing.append("Last Name, ");
        if (memberType.isEmpty())   missing.append("Member Type, ");
        if (contactInfo.isEmpty())  missing.append("Contact Info, ");

        if (missing.length() > 0) {
            missing.setLength(missing.length() - 2);
            output.setText("You are missing: " + missing);
            return;
        }

        int memberID;
        try {
            memberID = Integer.parseInt(memberIDText);
        } catch (NumberFormatException e) {
            output.setText("Member ID must be a number.");
            return;
        }

        String sql = "UPDATE Member " +
                     "SET first_name = ?, middle_name = ?, last_name = ?, " +
                     "    member_type = ?, contact_info = ? " +
                     "WHERE member_id = ?";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, middleName);
            stmt.setString(3, lastName);
            stmt.setString(4, memberType);
            stmt.setString(5, contactInfo);
            stmt.setInt(6, memberID);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                output.setText("Updated row with Member ID: " + memberID);
            } else {
                output.setText("No member found with ID: " + memberID);
            }

        } catch (Exception ex) {
            output.setText(ex.getMessage());
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        new LibraryTU();
    }
}
