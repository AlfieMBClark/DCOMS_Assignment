package client;

import common.models.UserAccount;
import javax.swing.*;
import java.awt.*;

/**
 * Login panel with username/password fields.
 * Routes to appropriate dashboard based on user role.
 */
public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        JLabel title = new JLabel("BHEL Human Resource Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(40, 60, 120));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        JLabel subtitle = new JLabel("Distributed HRM System using Java RMI");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 12));
        subtitle.setForeground(Color.GRAY);
        gbc.gridy = 1;
        add(subtitle, gbc);

        // Separator
        gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JSeparator(), gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Username
        gbc.gridy = 3; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBackground(new Color(40, 60, 120));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(200, 35));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 6;
        add(statusLabel, gbc);

        // Default credentials hint
        JLabel hint = new JLabel("<html><center>Default accounts: admin/admin123 | hr1/hr1234 | ahmad.ibrahim/emp123</center></html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hint.setForeground(new Color(150, 150, 150));
        gbc.gridy = 7;
        add(hint, gbc);

        // Action listeners
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin()); // Enter key
        usernameField.addActionListener(e -> passwordField.requestFocus()); // Tab to password
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Logging in...");
        statusLabel.setForeground(Color.BLUE);

        // Run in background thread to avoid freezing GUI
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ClientMain.authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    String token = get();
                    if (token != null) {
                        ClientMain.sessionToken = token;
                        UserAccount user = ClientMain.authService.getCurrentUser(token);

                        // Route to appropriate dashboard
                        switch (user.getRole()) {
                            case "EMPLOYEE":
                                ClientMain.showEmployeePanel(user.getEmployeeId());
                                break;
                            case "HR":
                                ClientMain.showHRPanel();
                                break;
                            case "ADMIN":
                                ClientMain.showAdminPanel();
                                break;
                            default:
                                statusLabel.setText("Unknown role: " + user.getRole());
                                loginButton.setEnabled(true);
                        }
                    } else {
                        statusLabel.setText("Invalid username or password");
                        statusLabel.setForeground(Color.RED);
                        passwordField.setText("");
                        loginButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Connection error: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                    loginButton.setEnabled(true);
                }
            }
        }.execute();
    }
}
