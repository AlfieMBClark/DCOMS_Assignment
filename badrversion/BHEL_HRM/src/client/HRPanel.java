package client;

import common.models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Year;
import java.util.List;

/**
 * HR Staff Dashboard.
 * Tabs: Register Employee, Manage Employees, Profile Updates, Leave Applications, Reports.
 */
public class HRPanel extends JPanel {

    public HRPanel() {
        setLayout(new BorderLayout());

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        topBar.setBackground(new Color(0, 100, 60));
        JLabel title = new JLabel("HR Staff Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        topBar.add(title, BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> ClientMain.logout());
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Register Employee", createRegisterTab());
        tabs.addTab("Employee List", createEmployeeListTab());
        tabs.addTab("Profile Updates", createProfileUpdatesTab());
        tabs.addTab("Leave Applications", createLeaveTab());
        tabs.addTab("Yearly Report", createReportTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ==================== REGISTER EMPLOYEE ====================
    private JPanel createRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField firstNameF = new JTextField(20);
        JTextField lastNameF = new JTextField(20);
        JTextField icF = new JTextField(20);
        JTextField emailF = new JTextField(20);
        JTextField phoneF = new JTextField(20);
        JTextField deptF = new JTextField(20);
        JTextField posF = new JTextField(20);

        String[][] fields = {
            {"First Name *:", ""}, {"Last Name *:", ""}, {"IC/Passport *:", ""},
            {"Email:", ""}, {"Phone:", ""}, {"Department:", ""}, {"Position:", ""}
        };
        JTextField[] textFields = {firstNameF, lastNameF, icF, emailF, phoneF, deptF, posF};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            panel.add(new JLabel(fields[i][0]), gbc);
            gbc.gridx = 1;
            panel.add(textFields[i], gbc);
        }

        JButton registerBtn = new JButton("Register Employee");
        registerBtn.setBackground(new Color(0, 100, 60));
        registerBtn.setForeground(Color.WHITE);
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        gbc.gridx = 0; gbc.gridy = fields.length; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerBtn, gbc);
        gbc.gridy = fields.length + 1;
        panel.add(resultLabel, gbc);

        registerBtn.addActionListener(e -> {
            try {
                Employee emp = new Employee();
                emp.setFirstName(firstNameF.getText().trim());
                emp.setLastName(lastNameF.getText().trim());
                emp.setIcPassport(icF.getText().trim());
                emp.setEmail(emailF.getText().trim().isEmpty() ? null : emailF.getText().trim());
                emp.setPhone(phoneF.getText().trim().isEmpty() ? null : phoneF.getText().trim());
                emp.setDepartment(deptF.getText().trim().isEmpty() ? null : deptF.getText().trim());
                emp.setPosition(posF.getText().trim().isEmpty() ? null : posF.getText().trim());

                int id = ClientMain.hrmService.registerEmployee(emp, ClientMain.sessionToken);
                resultLabel.setForeground(new Color(0, 120, 0));
                resultLabel.setText("Employee registered! ID: " + id
                    + " | Username: " + emp.getFirstName().toLowerCase() + "." + emp.getLastName().toLowerCase()
                    + " | Default password: IC number without dashes");

                // Clear fields
                for (JTextField tf : textFields) tf.setText("");
            } catch (Exception ex) {
                resultLabel.setForeground(Color.RED);
                resultLabel.setText("Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    // ==================== EMPLOYEE LIST ====================
    private JPanel createEmployeeListTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(25);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField); searchPanel.add(searchBtn); searchPanel.add(showAllBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "First Name", "Last Name", "IC/Passport", "Email", "Department", "Position"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadAll = () -> {
            try {
                List<Employee> emps = ClientMain.hrmService.getAllEmployees(ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (Employee e : emps) {
                        model.addRow(new Object[]{e.getEmployeeId(), e.getFirstName(), e.getLastName(),
                            e.getIcPassport(), e.getEmail(), e.getDepartment(), e.getPosition()});
                    }
                });
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        };

        showAllBtn.addActionListener(e -> new Thread(loadAll).start());
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) { new Thread(loadAll).start(); return; }
            new Thread(() -> {
                try {
                    List<Employee> results = ClientMain.hrmService.searchEmployees(query, ClientMain.sessionToken);
                    SwingUtilities.invokeLater(() -> {
                        model.setRowCount(0);
                        for (Employee emp : results) {
                            model.addRow(new Object[]{emp.getEmployeeId(), emp.getFirstName(), emp.getLastName(),
                                emp.getIcPassport(), emp.getEmail(), emp.getDepartment(), emp.getPosition()});
                        }
                    });
                } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
            }).start();
        });

        new Thread(loadAll).start();
        return panel;
    }

    // ==================== PROFILE UPDATES ====================
    private JPanel createProfileUpdatesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Request ID", "Employee ID", "Field", "Old Value", "New Value", "Status", "Requested At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Approve");
        approveBtn.setBackground(new Color(0, 120, 0));
        approveBtn.setForeground(Color.WHITE);
        JButton rejectBtn = new JButton("Reject");
        rejectBtn.setBackground(new Color(180, 0, 0));
        rejectBtn.setForeground(Color.WHITE);
        JButton refreshBtn = new JButton("Refresh");
        btnPanel.add(approveBtn); btnPanel.add(rejectBtn); btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadUpdates = () -> {
            try {
                List<String[]> updates = ClientMain.hrmService.getPendingProfileUpdates(ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (String[] u : updates) {
                        model.addRow(new Object[]{u[0], u[1], u[2], u[3], u[4], u[5], u[6]});
                    }
                });
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        };
        new Thread(loadUpdates).start();
        refreshBtn.addActionListener(e -> new Thread(loadUpdates).start());

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { ClientMain.showError("Select a request first."); return; }
            try {
                int reqId = Integer.parseInt(model.getValueAt(row, 0).toString().trim());
                ClientMain.hrmService.approveProfileUpdate(reqId, ClientMain.sessionToken);
                ClientMain.showSuccess("Profile update approved.");
                new Thread(loadUpdates).start();
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        });

        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { ClientMain.showError("Select a request first."); return; }
            try {
                int reqId = Integer.parseInt(model.getValueAt(row, 0).toString().trim());
                ClientMain.hrmService.rejectProfileUpdate(reqId, ClientMain.sessionToken);
                ClientMain.showSuccess("Profile update rejected.");
                new Thread(loadUpdates).start();
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        });

        return panel;
    }

    // ==================== LEAVE APPLICATIONS ====================
    private JPanel createLeaveTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Leave ID", "Employee", "Type", "Start", "End", "Days", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Approve");
        approveBtn.setBackground(new Color(0, 120, 0));
        approveBtn.setForeground(Color.WHITE);
        JButton rejectBtn = new JButton("Reject");
        rejectBtn.setBackground(new Color(180, 0, 0));
        rejectBtn.setForeground(Color.WHITE);
        JButton refreshBtn = new JButton("Refresh");
        btnPanel.add(approveBtn); btnPanel.add(rejectBtn); btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadLeaves = () -> {
            try {
                List<LeaveApplication> apps = ClientMain.hrmService.getPendingLeaveApplications(ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (LeaveApplication la : apps) {
                        model.addRow(new Object[]{la.getLeaveId(),
                            la.getEmployeeName() != null ? la.getEmployeeName() : "Emp#" + la.getEmployeeId(),
                            la.getLeaveType(), la.getStartDate(), la.getEndDate(),
                            la.getDaysRequested(), la.getReason(), la.getStatus()});
                    }
                });
            } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
        };
        new Thread(loadLeaves).start();
        refreshBtn.addActionListener(e -> new Thread(loadLeaves).start());

        approveBtn.addActionListener(e -> processLeave(table, model, true, loadLeaves));
        rejectBtn.addActionListener(e -> processLeave(table, model, false, loadLeaves));

        return panel;
    }

    private void processLeave(JTable table, DefaultTableModel model, boolean approve, Runnable reload) {
        int row = table.getSelectedRow();
        if (row < 0) { ClientMain.showError("Select a leave application first."); return; }
        try {
            int leaveId = (int) model.getValueAt(row, 0);
            if (approve) {
                ClientMain.hrmService.approveLeave(leaveId, ClientMain.sessionToken);
                ClientMain.showSuccess("Leave approved.");
            } else {
                ClientMain.hrmService.rejectLeave(leaveId, ClientMain.sessionToken);
                ClientMain.showSuccess("Leave rejected.");
            }
            new Thread(reload).start();
        } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
    }

    // ==================== YEARLY REPORT ====================
    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input bar
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField empIdField = new JTextField(10);
        JTextField yearField = new JTextField(String.valueOf(Year.now().getValue()), 6);
        JButton generateBtn = new JButton("Generate Report");
        inputPanel.add(new JLabel("Employee ID:")); inputPanel.add(empIdField);
        inputPanel.add(new JLabel("Year:")); inputPanel.add(yearField);
        inputPanel.add(generateBtn);
        panel.add(inputPanel, BorderLayout.NORTH);

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setText("Enter an Employee ID and year, then click 'Generate Report'.");
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            try {
                int empId = Integer.parseInt(empIdField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());

                YearlyReport report = ClientMain.hrmService.generateYearlyReport(empId, year, ClientMain.sessionToken);
                StringBuilder sb = new StringBuilder();
                sb.append("========================================\n");
                sb.append("  YEARLY EMPLOYEE REPORT - ").append(year).append("\n");
                sb.append("  Generated: ").append(report.getGeneratedAt()).append("\n");
                sb.append("========================================\n\n");

                Employee emp = report.getEmployee();
                sb.append("--- EMPLOYEE PROFILE ---\n");
                sb.append("  Name:       ").append(emp.getFullName()).append("\n");
                sb.append("  IC/Passport:").append(emp.getIcPassport()).append("\n");
                sb.append("  Email:      ").append(emp.getEmail() != null ? emp.getEmail() : "N/A").append("\n");
                sb.append("  Phone:      ").append(emp.getPhone() != null ? emp.getPhone() : "N/A").append("\n");
                sb.append("  Department: ").append(emp.getDepartment() != null ? emp.getDepartment() : "N/A").append("\n");
                sb.append("  Position:   ").append(emp.getPosition() != null ? emp.getPosition() : "N/A").append("\n");
                sb.append("  Joined:     ").append(emp.getDateJoined()).append("\n\n");

                sb.append("--- FAMILY DETAILS ---\n");
                if (report.getFamilyMembers().isEmpty()) {
                    sb.append("  No family members on record.\n");
                } else {
                    for (FamilyMember fm : report.getFamilyMembers()) {
                        sb.append("  ").append(fm.getName()).append(" (").append(fm.getRelationship()).append(")")
                          .append(fm.getIcPassport() != null ? " IC: " + fm.getIcPassport() : "").append("\n");
                    }
                }

                sb.append("\n--- LEAVE BALANCE ---\n");
                for (LeaveBalance lb : report.getLeaveBalances()) {
                    sb.append("  ").append(String.format("%-12s: %d/%d used, %d remaining\n",
                        lb.getLeaveType(), lb.getUsedDays(), lb.getTotalDays(), lb.getRemainingDays()));
                }

                sb.append("\n--- LEAVE HISTORY ---\n");
                if (report.getLeaveApplications().isEmpty()) {
                    sb.append("  No leave applications for ").append(year).append(".\n");
                } else {
                    sb.append(String.format("  %-6s %-10s %-12s %-12s %-5s %-10s\n",
                        "ID", "Type", "Start", "End", "Days", "Status"));
                    sb.append("  " + "-".repeat(60) + "\n");
                    for (LeaveApplication la : report.getLeaveApplications()) {
                        sb.append(String.format("  %-6d %-10s %-12s %-12s %-5d %-10s\n",
                            la.getLeaveId(), la.getLeaveType(), la.getStartDate(),
                            la.getEndDate(), la.getDaysRequested(), la.getStatus()));
                    }
                }
                sb.append("\n========================================\n");

                reportArea.setText(sb.toString());
                reportArea.setCaretPosition(0);
            } catch (NumberFormatException ex) {
                ClientMain.showError("Please enter valid Employee ID and Year.");
            } catch (Exception ex) {
                ClientMain.showError("Error: " + ex.getMessage());
            }
        });

        return panel;
    }
}
