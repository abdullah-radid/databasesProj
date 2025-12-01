import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPage {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Library Login");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Email label and text field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(20, 20, 80, 25);
        frame.add(emailLabel);

        JTextField emailField = new JTextField();
        emailField.setBounds(100, 20, 200, 25);
        frame.add(emailField);

        // Password label and field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 60, 80, 25);
        frame.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(100, 60, 200, 25);
        frame.add(passField);

        // Login button
        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(100, 100, 200, 30);
        frame.add(loginBtn);

        // Message label
        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setBounds(20, 140, 310, 25);
        messageLabel.setForeground(Color.RED);
        frame.add(messageLabel);

        // Action when login button is clicked
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passField.getPassword());

                // For now, just check dummy credentials
                if(email.equals("user@example.com") && password.equals("password")) {
                    messageLabel.setForeground(new Color(0, 128, 0));
                    messageLabel.setText("Login successful!");
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Invalid email or password.");
                }
            }
        });

        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
    }
}
