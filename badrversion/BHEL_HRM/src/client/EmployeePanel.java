package client;

import common.models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

/**
 * Employee self-service dashboard.
 * Tabs: Profile, Family Details, Leave Balance & Applications.
 */
public class EmployeePanel extends JPanel {

    private final int employeeId;
    private JTabbedPane tabbedPane;

    public EmployeePanel(int employeeId) {
        this.employeeId = employeeId;
        setLayout(new BorderLayout());

        // Top bar with welcome and logout
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        topBar.setBackground(new Color(40, 60, 120));
        JLabel welcomeLabel = new JLabel("Employee Dashboard");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topBar.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> ClientMain.logout());
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Profile", createProfileTab());
        tabbedPane.addTab("Family Details", createFamilyTab());
        tabbedPane.addTab("Leave Management", createLeaveTab());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==================== PROFILE TAB ====================
    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField firstNameF = new JTextField(); firstNameF.setEditable(false);
        JTextField lastNameF = new JTextField(); lastNameF.setEditable(false);
        JTextField icF = new JTextField(); icF.setEditable(false);
        JTextField emailF = new JTextField();
        JTextField phoneF = new JTextField();
        JTextField deptF = new JTextField(); deptF.setEditable(false);
        JTextField posF = new JTextField(); posF.setEditable(false);
        JTextField dateF = new JTextField(); dateF.setEditable(false);

        formPanel.add(new JLabel("First Name:")); formPanel.add(firstNameF);
        formPanel.add(new JLabel("Last Name:")); formPanel.add(lastNameF);
        formPanel.add(new JLabel("IC/Passport:")); formPanel.add(icF);
        formPanel.add(new JLabel("Email:")); formPanel.add(emailF);
        formPanel.add(new JLabel("Phone:")); formPanel.add(phoneF);
        formPanel.add(new JLabel("Department:")); formPanel.add(deptF);
        formPanel.add(new JLabel("Position:")); formPanel.add(posF);
        formPanel.add(new JLabel("Date Joined:")); formPanel.add(dateF);

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        JButton updateEmailBtn = new JButton("Request Email Update");
        JButton updatePhoneBtn = new JButton("Request Phone Update");
        btnPanel.add(refreshBtn);
        btnPanel.add(updateEmailBtn);
        btnPanel.add(updatePhoneBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Load profile data
        Runnable loadProfile = () -> {
            try {
                Employee emp = ClientMain.hrmService.getProfile(employeeId, ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    firstNameF.setText(emp.getFirstName());
                    lastNameF.setText(emp.getLastName());
                    icF.setText(emp.getIcPassport());
                    emailF.setText(emp.getEmail() != null ? emp.getEmail() : "");
                    phoneF.setText(emp.getPhone() != null ? emp.getPhone() : "");
                    deptF.setText(emp.getDepartment() != null ? emp.getDepartment() : "");
                    posF.setText(emp.getPosition() != null ? emp.getPosition() : "");
                    dateF.setText(emp.getDateJoined() != null ? emp.getDateJoined() : "");
                });
            } catch (Exception ex) {
                ClientMain.showError("Failed to load profile: " + ex.getMessage());
            }
        };
        new Thread(loadProfile).start();

        refreshBtn.addActionListener(e -> new Thread(loadProfile).start());
        updateEmailBtn.addActionListener(e -> requestUpdate("email", emailF.getText()));
        updatePhoneBtn.addActionListener(e -> requestUpdate("phone", phoneF.getText()));

        return panel;
    }

    private void requestUpdate(String field, String currentValue) {
        String newValue = JOptionPane.showInputDialog(this,
            "Enter new " + field + ":", "Request " + field + " Update", JOptionPane.PLAIN_MESSAGE);
        if (newValue != null && !newValue.trim().isEmpty()) {
            try {
                ClientMain.hrmService.requestProfileUpdate(
                    employeeId, field, currentValue, newValue.trim(), ClientMain.sessionToken);
                ClientMain.showSuccess("Update request submitted. Waiting for HR approval.");
            } catch (Exception ex) {
                ClientMain.showError("Error: " + ex.getMessage());
            }
        }
    }

    // ==================== FAMILY DETAILS TAB ====================
    private JPanel createFamilyTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"ID", "Name", "Relationship", "IC/Passport", "Date of Birth"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Family Member");
        JButton editBtn = new JButton("Edit Selected");
        JButton removeBtn = new JButton("Remove Selected");
        JButton refreshBtn = new JButton("Refresh");
        btnPanel.add(addBtn); btnPanel.add(editBtn);
        btnPanel.add(removeBtn); btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadFamily = () -> {
            try {
                List<FamilyMember> members = ClientMain.hrmService.getFamilyMembers(employeeId, ClientMain.sessionToken);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (FamilyMember fm : members) {
                        model.addRow(new Object[]{fm.getFamilyId(), fm.getName(), fm.getRelationship(),
                            fm.getIcPassport(), fm.getDateOfBirth()});
                    }
                });
            } catch (Exception ex) {
                ClientMain.showError("Error loading family: " + ex.getMessage());
            }
        };
        new Thread(loadFamily).start();

        refreshBtn.addActionListener(e -> new Thread(loadFamily).start());

        addBtn.addActionListener(e -> {
            JTextField nameF = new JTextField();
            String[] rels = {"SPOUSE", "CHILD", "PARENT", "SIBLING", "OTHER"};
            JComboBox<String> relBox = new JComboBox<>(rels);
            JTextField icF = new JTextField();
            JTextField dobF = new JTextField("YYYY-MM-DD");

            Object[] fields = {"Name:", nameF, "Relationship:", relBox, "IC/Passport:", icF, "Date of Birth:", dobF};
            int result = JOptionPane.showConfirmDialog(this, fields, "Add Family Member", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    FamilyMember fm = new FamilyMember(0, employeeId, nameF.getText().trim(),
                        (String) relBox.getSelectedItem(), icF.getText().trim(),
                        dobF.getText().trim().equals("YYYY-MM-DD") ? "" : dobF.getText().trim());
                    ClientMain.hrmService.addFamilyMember(fm, ClientMain.sessionToken);
                    ClientMain.showSuccess("Family member added.");
                    new Thread(loadFamily).start();
                } catch (Exception ex) {
                    ClientMain.showError("Error: " + ex.getMessage());
                }
            }
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { ClientMain.showError("Select a family member first."); return; }
            int famId = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove " + model.getValueAt(row, 1) + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ClientMain.hrmService.removeFamilyMember(famId, employeeId, ClientMain.sessionToken);
                    new Thread(loadFamily).start();
                } catch (Exception ex) { ClientMain.showError("Error: " + ex.getMessage()); }
            }
        });

        return panel;
    }

    // ==================== LEAVE MANAGEMENT TAB ====================
    private JPanel createLeaveTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: Leave balance
        JPanel balancePanel = new JPanel(new GridLayout(1, 3, 10, 0));
        balancePanel.setBorder(BorderFactory.createTitledBorder("Leave Balance (" + Year.now().getValue() + ")"));
        JLabel annualLabel = new JLabel("Annual: Loading...", SwingConstants.CENTER);
        JLabel medicalLabel = new JLabel("Medical: Loading...", SwingConstants.CENTER);
        JLabel emergencyLabel = new JLabel("Emergency: Loading...", SwingConstants.CENTER);
        balancePanel.add(annualLabel); balancePanel.add(medicalLabel); balancePanel.add(emergencyLabel);
        panel.add(balancePanel, BorderLayout.NORTH);

        // Center: Applications table
        String[] columns = {"ID", "Type", "Start", "End", "Days", "Status", "Applied"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton applyBtn = new JButton("Apply for Leave");
        JButton refreshBtn = new JButton("Refresh");
        btnPanel.add(applyBtn); btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadLeaveData = () -> {
            try {
                int year = Year.now().getValue();
                List<LeaveBalance> balances = ClientMain.hrmService.getLeaveBalances(employeeId, year, ClientMain.sessionToken);
                List<LeaveApplication> apps = ClientMain.hrmService.getMyLeaveApplications(employeeId, ClientMain.sessionToken);

                SwingUtilities.invokeLater(() -> {
                    for (LeaveBalance lb : balances) {
                        String text = lb.getLeaveType() + ": " + lb.getRemainingDays() + "/" + lb.getTotalDays();
                        switch (lb.getLeaveType()) {
                            case "ANNUAL": annualLabel.setText(text); break;
                            case "MEDICAL": medicalLabel.setText(text); break;
                            case "EMERGENCY": emergencyLabel.setText(text); break;
                        }
                    }
                    model.setRowCount(0);
                    for (LeaveApplication la : apps) {
                        model.addRow(new Object[]{la.getLeaveId(), la.getLeaveType(),
                            la.getStartDate(), la.getEndDate(), la.getDaysRequested(),
                            la.getStatus(), la.getAppliedAt()});
                    }
                });
            } catch (Exception ex) {
                ClientMain.showError("Error loading leave data: " + ex.getMessage());
            }
        };
        new Thread(loadLeaveData).start();

        refreshBtn.addActionListener(e -> new Thread(loadLeaveData).start());

        applyBtn.addActionListener(e -> {
            String[] types = {"ANNUAL", "MEDICAL", "EMERGENCY"};
            JComboBox<String> typeBox = new JComboBox<>(types);
            JTextField startF = new JTextField(LocalDate.now().toString());
            JTextField endF = new JTextField(LocalDate.now().plusDays(1).toString());
            JTextField daysF = new JTextField("1");
            JTextField reasonF = new JTextField();

            Object[] fields = {"Leave Type:", typeBox, "Start Date (YYYY-MM-DD):", startF,
                "End Date (YYYY-MM-DD):", endF, "Days Requested:", daysF, "Reason:", reasonF};
            int result = JOptionPane.showConfirmDialog(this, fields, "Apply for Leave", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    LeaveApplication app = new LeaveApplication();
                    app.setEmployeeId(employeeId);
                    app.setLeaveType((String) typeBox.getSelectedItem());
                    app.setStartDate(startF.getText().trim());
                    app.setEndDate(endF.getText().trim());
                    app.setDaysRequested(Integer.parseInt(daysF.getText().trim()));
                    app.setReason(reasonF.getText().trim());

                    ClientMain.hrmService.applyForLeave(app, ClientMain.sessionToken);
                    ClientMain.showSuccess("Leave application submitted!");
                    new Thread(loadLeaveData).start();
                } catch (Exception ex) {
                    ClientMain.showError("Error: " + ex.getMessage());
                }
            }
        });

        return panel;
    }
}
