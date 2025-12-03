package UI;

import javax.swing.*;

public class LoginPage extends JFrame {

    public LoginPage() {

        super("Login");
        this.setSize(350, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        this.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(110, 20, 200, 25);
        this.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        this.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(110, 60, 200, 25);
        this.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(110, 100, 100, 30);
        this.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());

            /// TODO
            if (username.equals("admin") && password.equals("1234")) {
                this.dispose();
                JOptionPane.showMessageDialog(null, "Login successful", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Enter key to login
        passwordText.addActionListener(e -> loginButton.doClick());

        this.setVisible(true);

    }



}
