package client;

import common.models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Dashboard.
 * Tabs: Manage User Accounts, View Audit Log.
 */
public class AdminPanel extends JPanel {

    public AdminPanel() {
        setLayout(new BorderLayout());

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        topBar.setBackground(new Color(120, 30, 30));
        JLabel title = new JLabel("Admin Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        topBar.add(title, BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> ClientMain.logout());
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("User Accounts", createUserManagementTab());
        tabs.addTab("Audit Log", createAuditLogTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ==================== USER MANAGEMENT ====================
    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"User ID", "Username", "Role", "Employee ID", "Active"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add User");
        JButton editBtn = new JButton("Edit User");
        JButton deactivateBtn = new JButton("Deactivate User");
        deactivateBtn.setBackground(new Color(180, 0, 0));
        deactivateBtn.setForeground(Color.WHITE);
        JButton refreshBtn = new JButton("Refresh");
        btnPanel.add(addBtn); btnPanel.add(editBtn);
        btnPanel.add(deactivateBtn); btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadUsers = () -> {
            try {
                List<UserAccount> users = ClientMain.hrmService.getAllUsers(ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (UserAccount u : users) {
                        model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole(),
                            u.getEmployeeId(), u.isActive() ? "Yes" : "No"});
                    }
                });
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        };
        new Thread(loadUsers).start();
        refreshBtn.addActionListener(e -> new Thread(loadUsers).start());

        addBtn.addActionListener(e -> {
            JTextField usernameF = new JTextField();
            JPasswordField passwordF = new JPasswordField();
            String[] roles = {"EMPLOYEE", "HR", "ADMIN"};
            JComboBox<String> roleBox = new JComboBox<>(roles);
            JTextField empIdF = new JTextField("0");

            Object[] fields = {"Username:", usernameF, "Password:", passwordF,
                "Role:", roleBox, "Employee ID (0 if none):", empIdF};
            int result = JOptionPane.showConfirmDialog(this, fields, "Add User", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    ClientMain.hrmService.addUser(
                        usernameF.getText().trim(),
                        new String(passwordF.getPassword()),
                        (String) roleBox.getSelectedItem(),
                        Integer.parseInt(empIdF.getText().trim()),
                        ClientMain.sessionToken);
                    ClientMain.showSuccess("User account created.");
                    new Thread(loadUsers).start();
                } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { ClientMain.showError("Select a user first."); return; }
            int userId = (int) model.getValueAt(row, 0);
            String currUsername = (String) model.getValueAt(row, 1);
            String currRole = (String) model.getValueAt(row, 2);
            boolean currActive = "Yes".equals(model.getValueAt(row, 4));

            JTextField usernameF = new JTextField(currUsername);
            String[] roles = {"EMPLOYEE", "HR", "ADMIN"};
            JComboBox<String> roleBox = new JComboBox<>(roles);
            roleBox.setSelectedItem(currRole);
            JCheckBox activeBox = new JCheckBox("Active", currActive);

            Object[] fields = {"Username:", usernameF, "Role:", roleBox, "Status:", activeBox};
            int result = JOptionPane.showConfirmDialog(this, fields, "Edit User", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    ClientMain.hrmService.updateUser(userId, usernameF.getText().trim(),
                        (String) roleBox.getSelectedItem(), activeBox.isSelected(), ClientMain.sessionToken);
                    ClientMain.showSuccess("User updated.");
                    new Thread(loadUsers).start();
                } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
            }
        });

        deactivateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { ClientMain.showError("Select a user first."); return; }
            int userId = (int) model.getValueAt(row, 0);
            String username = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate user '" + username + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ClientMain.hrmService.removeUser(userId, ClientMain.sessionToken);
                    ClientMain.showSuccess("User deactivated.");
                    new Thread(loadUsers).start();
                } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
            }
        });

        return panel;
    }

    // ==================== AUDIT LOG ====================
    private JPanel createAuditLogTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Log ID", "Timestamp", "User", "Role", "Action", "Details"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Audit Log");
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadLog = () -> {
            try {
                List<AuditLogEntry> entries = ClientMain.hrmService.getAuditLog(ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    // Show most recent first
                    for (int i = entries.size() - 1; i >= 0; i--) {
                        AuditLogEntry e = entries.get(i);
                        model.addRow(new Object[]{e.getLogId(), e.getTimestamp(),
                            e.getUsername(), e.getRole(), e.getAction(), e.getDetails()});
                    }
                });
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        };
        new Thread(loadLog).start();
        refreshBtn.addActionListener(e -> new Thread(loadLog).start());

        return panel;
    }
}
