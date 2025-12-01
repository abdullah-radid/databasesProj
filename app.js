import javax.swing.*;
import java.awt.event.*;

public class LoginPage {
    public static void main(String[] args) {
        // Create a new frame
        JFrame frame = new JFrame("Login Page");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null); // using absolute layout for simplicity

        // Username label and text field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        frame.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(100, 20, 160, 25);
        frame.add(userText);

        // Password label and password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        frame.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(100, 60, 160, 25);
        frame.add(passwordText);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 100, 80, 25);
        frame.add(loginButton);

        // Action listener for the button
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                if(username.equals("admin") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(frame, "Login successful!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid credentials.");
                }
            }
        });

        frame.setVisible(true);
    }
}
