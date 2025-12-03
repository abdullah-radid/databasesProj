package UI;

import API.Database;

import javax.swing.*;
import java.awt.event.*;

public class LoginPage extends JFrame {
    private JTextField userText;
    private JPasswordField passwordText;

    public LoginPage() {
        setTitle("Login Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false); // it looks weird when maximized

        // Username label and text field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        add(userLabel);

        userText = new JTextField();
        userText.setBounds(100, 20, 160, 25);
        add(userText);
        userText.setText("root"); //default is root

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


        Database.SetLogin(username, password);
        if (Database.testConnection()) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            // Close login window
            dispose();

            new MainPage();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.","Login Failed",JOptionPane.ERROR_MESSAGE);
        }
    }


}
