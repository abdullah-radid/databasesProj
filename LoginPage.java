import javax.swing.*;
import java.awt.event.*;

public class LoginPage extends JFrame {

    // Username and password
    private static final String ROOT_USERNAME = "root";
    private static final String ROOT_PASSWORD = "password123";

    private JTextField userText;
    private JPasswordField passwordText;

    public LoginPage() {
        setTitle("Login Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); 

        // Username label
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        add(userLabel);

        userText = new JTextField();
        userText.setBounds(100, 20, 160, 25);
        add(userText);

        // Password label 
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

        // Button activation
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        setLocationRelativeTo(null); 
        setVisible(true);
    }

    private void handleLogin() {
        String username = userText.getText().trim();
        String password = new String(passwordText.getPassword()).trim();

        if (username.equals(ROOT_USERNAME) && password.equals(ROOT_PASSWORD)) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            
            dispose();

            // MySQL login promt
            String dbPassword = promptForDatabasePassword();
            if (dbPassword != null) {
                
                new LibraryTU(dbPassword);
            } else {
                System.exit(0); 
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    private String promptForDatabasePassword() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Enter MySQL root password:");
        JPasswordField passField = new JPasswordField(20);

        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Database Configuration",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new String(passField.getPassword());
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}

