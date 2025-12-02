import javax.swing.*;
import java.awt.event.*;

public class LoginPage extends JFrame {

    // Hardcoded root credentials
    private static final String ROOT_USERNAME = "root";
    private static final String ROOT_PASSWORD = "password123";

    private JTextField userText;
    private JPasswordField passwordText;

    public LoginPage() {
        setTitle("Login Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // absolute layout like your example

        // Username label and text field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        add(userLabel);

        userText = new JTextField();
        userText.setBounds(100, 20, 160, 25);
        add(userText);

        // Password label and password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        add(passwordLabel);

        passwordText = new JPasswordField();
        passwordText.setBounds(100, 60, 160, 25);
        add(passwordText);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 100, 80, 25);
        add(loginButton);

        // Action listener for the button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        setLocationRelativeTo(null); // center on screen
        setVisible(true);
    }

    private void handleLogin() {
        String username = userText.getText().trim();
        String password = new String(passwordText.getPassword()).trim();

        if (username.equals(ROOT_USERNAME) && password.equals(ROOT_PASSWORD)) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            // Close login window
            dispose();

            // Open your main DB GUI window
            // Make sure LibraryTU has a no-arg constructor
            new LibraryTU();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}
