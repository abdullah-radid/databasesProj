import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LibraryApp {

    public static void main(String[] args) {
        showLogin();
    }

    // Login screen
    public static void showLogin() {
        JFrame frame = new JFrame("Login Page");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        frame.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(100, 20, 160, 25);
        frame.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        frame.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(100, 60, 160, 25);
        frame.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 100, 80, 25);
        frame.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());

            if(username.equals("admin") && password.equals("1234")) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                frame.dispose(); // Close login window
                showDashboard(); // Open dashboard
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials.");
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Dashboard screen
    public static void showDashboard() {
        JFrame frame = new JFrame("Library Dashboard");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton insertBtn = new JButton("Insert");
        JButton deleteBtn = new JButton("Delete");
        JButton updateBtn = new JButton("Update");
        JButton reportBtn = new JButton("Report");

        topPanel.add(insertBtn);
        topPanel.add(deleteBtn);
        topPanel.add(updateBtn);
        topPanel.add(reportBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        // Center panel with CardLayout
        JPanel centerPanel = new JPanel(new CardLayout());

        String[] booksColumns = {"ID", "Title", "Author"};
        JTable booksTable = new JTable(new Object[][]{}, booksColumns);
        JScrollPane booksScroll = new JScrollPane(booksTable);

        String[] usersColumns = {"ID", "Name", "Email"};
        JTable usersTable = new JTable(new Object[][]{}, usersColumns);
        JScrollPane usersScroll = new JScrollPane(usersTable);

        centerPanel.add(booksScroll, "Books");
        centerPanel.add(usersScroll, "Users");
        frame.add(centerPanel, BorderLayout.CENTER);

        // Side panel to switch tables
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        JButton booksBtn = new JButton("Books");
        JButton usersBtn = new JButton("Users");

        sidePanel.add(booksBtn);
        sidePanel.add(usersBtn);
        frame.add(sidePanel, BorderLayout.WEST);

        CardLayout cl = (CardLayout)(centerPanel.getLayout());
        booksBtn.addActionListener(e -> cl.show(centerPanel, "Books"));
        usersBtn.addActionListener(e -> cl.show(centerPanel, "Users"));

        // Dummy actions for top buttons
        insertBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Insert clicked"));
        deleteBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Delete clicked"));
        updateBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Update clicked"));
        reportBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Report clicked"));

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
