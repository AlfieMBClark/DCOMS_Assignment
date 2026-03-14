package client;

import interfaces.ApplicationInterface;
import models.*;
import java.rmi.Naming;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * HR Staff Client Application
 * Provides menu-driven interface for HR operations
 * 
 * Usage: java client.HRClient [app-server-url]
 * Example: java client.HRClient rmi://192.168.1.20:1100/ApplicationService
 */
public class HRClient {
    
    private ApplicationInterface appServer;
    private Scanner scanner;
    private Employee currentUser;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public HRClient(String appServerUrl) {
        try {
            System.out.println("Connecting to Application Server...");
            appServer = (ApplicationInterface) Naming.lookup(appServerUrl);
            System.out.println("Connected successfully!");
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            System.err.println("Failed to connect to Application Server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        String appServerUrl = "rmi://localhost:1100/ApplicationService";
        
        if (args.length >= 1) {
            appServerUrl = args[0];
        }
        
        HRClient client = new HRClient(appServerUrl);
        client.run();
    }
    
    public void run() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   BHEL HR MANAGEMENT SYSTEM - HR       ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        
        boolean running = true;
        while (running) {
            displayMenu();
            
            try {
                System.out.print("\nEnter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        registerNewEmployee();
                        break;
                    case 2:
                        viewAllEmployees();
                        break;
                    case 3:
                        viewEmployeeDetails();
                        break;
                    case 4:
                        viewAllLeaveApplications();
                        break;
                    case 5:
                        viewPendingLeaveApplications();
                        break;
                    case 6:
                        processLeaveApplication();
                        break;
                    case 7:
                        generateEmployeeYearlyReport();
                        break;
                    case 8:
                        generateAllEmployeesReport();
                        break;
                    case 9:
                        viewLeaveStatistics();
                        break;
                    case 10:
                        viewAuditLogs();
                        break;
                    case 11:
                        performBackup();
                        break;
                    case 12:
                        changePassword();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Logging out... Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
                if (running && choice != 0) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                scanner.nextLine(); // clear buffer
            }
        }
        
        scanner.close();
    }
    
    private boolean login() {
        System.out.println("\n--- HR STAFF LOGIN ---");
        
        for (int attempts = 0; attempts < 3; attempts++) {
            try {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                
                System.out.print("Password: ");
                String password = scanner.nextLine();
                
                currentUser = appServer.authenticate(username, password);
                
                if (currentUser != null) {
                    if (!"HR".equalsIgnoreCase(currentUser.getRole())) {
                        System.out.println("Error: This client is for HR staff only.");
                        currentUser = null;
                        return false;
                    }
                    
                    System.out.println("\n✓ Login successful!");
                    System.out.println("Welcome, " + currentUser.getFirstName() + " " + 
                                     currentUser.getLastName());
                    return true;
                } else {
                    System.out.println("✗ Invalid credentials. " + (2 - attempts) + " attempts remaining.");
                }
            } catch (Exception e) {
                System.err.println("Login error: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    private void displayMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           HR STAFF MENU                ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  EMPLOYEE MANAGEMENT                   ║");
        System.out.println("║  1. Register New Employee              ║");
        System.out.println("║  2. View All Employees                 ║");
        System.out.println("║  3. View Employee Details              ║");
        System.out.println("║                                        ║");
        System.out.println("║  LEAVE MANAGEMENT                      ║");
        System.out.println("║  4. View All Leave Applications        ║");
        System.out.println("║  5. View Pending Leave Applications    ║");
        System.out.println("║  6. Process Leave Application          ║");
        System.out.println("║                                        ║");
        System.out.println("║  REPORTING                             ║");
        System.out.println("║  7. Generate Employee Yearly Report    ║");
        System.out.println("║  8. Generate All Employees Report      ║");
        System.out.println("║  9. View Leave Statistics              ║");
        System.out.println("║                                        ║");
        System.out.println("║  SYSTEM                                ║");
        System.out.println("║ 10. View Audit Logs                    ║");
        System.out.println("║ 11. Perform System Backup              ║");
        System.out.println("║ 12. Change Password                    ║");
        System.out.println("║  0. Logout                             ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
    
    private void registerNewEmployee() {
        System.out.println("\n=== REGISTER NEW EMPLOYEE ===");
        
        try {
            System.out.print("First Name: ");
            String firstName = scanner.nextLine();
            
            System.out.print("Last Name: ");
            String lastName = scanner.nextLine();
            
            System.out.print("IC/Passport (Format: YYMMDD-PB-###G or Passport): ");
            String icPassport = scanner.nextLine();
            
            System.out.print("Email: ");
            String email = scanner.nextLine();
            
            System.out.print("Department: ");
            String department = scanner.nextLine();
            
            System.out.print("Position: ");
            String position = scanner.nextLine();
            
            System.out.print("Username: ");
            String username = scanner.nextLine();
            
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            System.out.print("Role (HR/EMPLOYEE): ");
            String role = scanner.nextLine();
            
            System.out.print("Initial Leave Balance (days): ");
            double leaveBalance = scanner.nextDouble();
            scanner.nextLine();
            
            boolean success = appServer.registerEmployee(currentUser.getEmployeeId(), 
                    firstName, lastName, icPassport, email, department, position, 
                    username, password, role, leaveBalance);
            
            if (success) {
                System.out.println("\n✓ Employee registered successfully!");
            } else {
                System.out.println("\n✗ Failed to register employee.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewAllEmployees() {
        System.out.println("\n=== ALL EMPLOYEES ===");
        
        try {
            List<Employee> employees = appServer.viewAllEmployees(currentUser.getEmployeeId());
            
            if (employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }
            
            System.out.println("\n" + String.format("%-10s %-20s %-30s %-15s %-10s", 
                    "ID", "Name", "Email", "Department", "Leave Bal"));
            System.out.println("─".repeat(90));
            
            for (Employee emp : employees) {
                String fullName = emp.getFirstName() + " " + emp.getLastName();
                System.out.println(String.format("%-10s %-20s %-30s %-15s %.1f days", 
                        emp.getEmployeeId(), 
                        fullName.length() > 20 ? fullName.substring(0, 17) + "..." : fullName,
                        emp.getEmail().length() > 30 ? emp.getEmail().substring(0, 27) + "..." : emp.getEmail(),
                        emp.getDepartment(),
                        emp.getLeaveBalance()));
            }
            
            System.out.println("\nTotal Employees: " + employees.size());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewEmployeeDetails() {
        System.out.println("\n=== VIEW EMPLOYEE DETAILS ===");
        
        try {
            System.out.print("Enter Employee ID: ");
            String employeeId = scanner.nextLine();
            
            Employee emp = appServer.viewEmployeeProfile(currentUser.getEmployeeId(), employeeId);
            
            if (emp == null) {
                System.out.println("Employee not found.");
                return;
            }
            
            System.out.println("\n--- Employee Profile ---");
            System.out.println("Employee ID    : " + emp.getEmployeeId());
            System.out.println("Name           : " + emp.getFirstName() + " " + emp.getLastName());
            System.out.println("IC/Passport    : " + emp.getIcPassport());
            System.out.println("Email          : " + emp.getEmail());
            System.out.println("Department     : " + emp.getDepartment());
            System.out.println("Position       : " + emp.getPosition());
            System.out.println("Role           : " + emp.getRole());
            System.out.println("Leave Balance  : " + emp.getLeaveBalance() + " days");
            System.out.println("Created At     : " + emp.getCreatedAt());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewAllLeaveApplications() {
        System.out.println("\n=== ALL LEAVE APPLICATIONS ===");
        
        try {
            List<Leave> leaves = appServer.viewAllLeaveApplications(currentUser.getEmployeeId());
            
            if (leaves.isEmpty()) {
                System.out.println("No leave applications found.");
                return;
            }
            
            displayLeaveList(leaves);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewPendingLeaveApplications() {
        System.out.println("\n=== PENDING LEAVE APPLICATIONS ===");
        
        try {
            List<Leave> leaves = appServer.viewPendingLeaveApplications(currentUser.getEmployeeId());
            
            if (leaves.isEmpty()) {
                System.out.println("No pending leave applications.");
                return;
            }
            
            displayLeaveList(leaves);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void displayLeaveList(List<Leave> leaves) {
        System.out.println("\n" + String.format("%-10s %-12s %-12s %-12s %-6s %-12s %-10s", 
                "Leave ID", "Employee ID", "Start Date", "End Date", "Days", "Type", "Status"));
        System.out.println("─".repeat(90));
        
        for (Leave leave : leaves) {
            System.out.println(String.format("%-10s %-12s %-12s %-12s %-6.1f %-12s %-10s", 
                    leave.getLeaveId(),
                    leave.getEmployeeId(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    leave.getNumberOfDays(),
                    leave.getLeaveType(),
                    leave.getStatus()));
        }
        
        System.out.println("\nTotal: " + leaves.size() + " applications");
    }
    
    private void processLeaveApplication() {
        System.out.println("\n=== PROCESS LEAVE APPLICATION ===");
        
        try {
            System.out.print("Enter Leave ID: ");
            String leaveId = scanner.nextLine();
            
            System.out.print("Action (APPROVE/REJECT): ");
            String status = scanner.nextLine();
            
            System.out.print("Remarks: ");
            String remarks = scanner.nextLine();
            
            boolean success = appServer.processLeaveApplication(
                    currentUser.getEmployeeId(), leaveId, status, remarks);
            
            if (success) {
                System.out.println("\n✓ Leave application " + status.toLowerCase() + "d successfully!");
            } else {
                System.out.println("\n✗ Failed to process leave application.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void generateEmployeeYearlyReport() {
        System.out.println("\n=== GENERATE EMPLOYEE YEARLY REPORT ===");
        
        try {
            System.out.print("Enter Employee ID: ");
            String employeeId = scanner.nextLine();
            
            System.out.print("Enter Year: ");
            int year = scanner.nextInt();
            scanner.nextLine();
            
            Map<String, Object> report = appServer.generateEmployeeYearlyReport(
                    currentUser.getEmployeeId(), employeeId, year);
            
            Employee emp = (Employee) report.get("employee");
            @SuppressWarnings("unchecked")
            List<FamilyDetails> family = (List<FamilyDetails>) report.get("familyDetails");
            @SuppressWarnings("unchecked")
            List<Leave> leaves = (List<Leave>) report.get("yearlyLeaves");
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║      YEARLY EMPLOYEE REPORT " + year + "      ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            System.out.println("\n--- Employee Profile ---");
            System.out.println("ID        : " + emp.getEmployeeId());
            System.out.println("Name      : " + emp.getFirstName() + " " + emp.getLastName());
            System.out.println("Email     : " + emp.getEmail());
            System.out.println("Department: " + emp.getDepartment());
            System.out.println("Position  : " + emp.getPosition());
            
            System.out.println("\n--- Family Details ---");
            if (family.isEmpty()) {
                System.out.println("No family details recorded.");
            } else {
                for (FamilyDetails fam : family) {
                    System.out.println("• " + fam.getMemberName() + " (" + 
                                     fam.getRelationship() + ")");
                }
            }
            
            System.out.println("\n--- Leave History " + year + " ---");
            if (leaves.isEmpty()) {
                System.out.println("No leave applications for " + year);
            } else {
                for (Leave leave : leaves) {
                    System.out.println(String.format("• %s to %s: %.1f days of %s - %s", 
                            leave.getStartDate(), leave.getEndDate(), 
                            leave.getNumberOfDays(), leave.getLeaveType(), 
                            leave.getStatus()));
                }
                
                double totalDays = leaves.stream()
                        .filter(l -> "APPROVED".equals(l.getStatus()))
                        .mapToDouble(Leave::getNumberOfDays)
                        .sum();
                System.out.println("\nTotal Approved Leave Days: " + totalDays);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void generateAllEmployeesReport() {
        System.out.println("\n=== ALL EMPLOYEES SUMMARY REPORT ===");
        
        try {
            List<Map<String, Object>> reports = appServer.generateAllEmployeesReport(
                    currentUser.getEmployeeId());
            
            System.out.println("\n" + String.format("%-10s %-25s %-20s %-15s %-10s %-10s", 
                    "ID", "Name", "Department", "Position", "Leave Bal", "Pending"));
            System.out.println("─".repeat(95));
            
            for (Map<String, Object> report : reports) {
                System.out.println(String.format("%-10s %-25s %-20s %-15s %-10.1f %-10d", 
                        report.get("employeeId"),
                        report.get("name"),
                        report.get("department"),
                        report.get("position"),
                        (Double) report.get("leaveBalance"),
                        (Long) report.get("pendingLeaves")));
            }
            
            System.out.println("\nTotal Employees: " + reports.size());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewLeaveStatistics() {
        System.out.println("\n=== LEAVE STATISTICS ===");
        
        try {
            Map<String, Object> stats = appServer.generateLeaveStatistics(
                    currentUser.getEmployeeId());
            
            System.out.println("\nTotal Leave Applications : " + stats.get("totalLeaves"));
            System.out.println("Pending Applications     : " + stats.get("pendingLeaves"));
            System.out.println("Approved Applications    : " + stats.get("approvedLeaves"));
            System.out.println("Rejected Applications    : " + stats.get("rejectedLeaves"));
            System.out.println("\nGenerated at: " + stats.get("generatedAt"));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewAuditLogs() {
        System.out.println("\n=== VIEW AUDIT LOGS ===");
        
        try {
            System.out.print("Enter Employee ID (or press Enter for all): ");
            String targetId = scanner.nextLine();
            
            List<AuditLog> logs = appServer.viewAuditLogs(currentUser.getEmployeeId(), 
                    targetId.isEmpty() ? null : targetId);
            
            if (logs.isEmpty()) {
                System.out.println("No audit logs found.");
                return;
            }
            
            System.out.println("\nShowing last 20 audit log entries:");
            System.out.println("─".repeat(100));
            
            int count = 0;
            for (int i = logs.size() - 1; i >= 0 && count < 20; i--, count++) {
                AuditLog log = logs.get(i);
                System.out.println(log);
            }
            
            System.out.println("\nTotal log entries: " + logs.size());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void performBackup() {
        System.out.println("\n=== PERFORM SYSTEM BACKUP ===");
        
        try {
            System.out.print("Are you sure you want to backup the system? (yes/no): ");
            String confirm = scanner.nextLine();
            
            if (confirm.equalsIgnoreCase("yes")) {
                boolean success = appServer.performSystemBackup(currentUser.getEmployeeId());
                
                if (success) {
                    System.out.println("\n✓ System backup completed successfully!");
                } else {
                    System.out.println("\n✗ Backup failed.");
                }
            } else {
                System.out.println("Backup cancelled.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void changePassword() {
        System.out.println("\n=== CHANGE PASSWORD ===");
        
        try {
            System.out.print("Current Password: ");
            String oldPassword = scanner.nextLine();
            
            System.out.print("New Password: ");
            String newPassword = scanner.nextLine();
            
            System.out.print("Confirm New Password: ");
            String confirmPassword = scanner.nextLine();
            
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("\n✗ Passwords do not match!");
                return;
            }
            
            boolean success = appServer.changePassword(currentUser.getEmployeeId(), 
                    oldPassword, newPassword);
            
            if (success) {
                System.out.println("\n✓ Password changed successfully!");
            } else {
                System.out.println("\n✗ Failed to change password. Check your current password.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
