package client;

import application.ApplicationInterface;
import models.Employee;
import models.Leave;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class EmployeeClient {
    
    private static ApplicationInterface appService;
    private static Employee currentUser;
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        try {
            // Get application server IP from command line (default: localhost)
            String appServerIP = (args.length > 0) ? args[0] : "localhost";
            
            System.out.println("Connecting to Application Server at " + appServerIP + ":1100...");
            
            // Lookup application service
            Registry registry = LocateRegistry.getRegistry(appServerIP, 1100);
            appService = (ApplicationInterface) registry.lookup("ApplicationService");
            
            System.out.println("Connected to Application Server!");
            System.out.println("===========================================");
            
            // Login
            if (!login()) {
                System.out.println("Login failed. Exiting...");
                return;
            }
            
            // Main menu
            showMainMenu();
            
        } catch (Exception e) {
            System.err.println("Employee Client failed:");
            e.printStackTrace();
            System.err.println("\nMake sure Application Server is running!");
        }
    }
    
    private static boolean login() {
        System.out.println("\n=== EMPLOYEE LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        try {
            currentUser = appService.authenticate(username, password);
            if (currentUser != null) {
                System.out.println("\nWelcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
                return true;
            } else {
                System.out.println("\nInvalid credentials!");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }
    
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n===========================================");
            System.out.println("        EMPLOYEE SELF-SERVICE");
            System.out.println("===========================================");
            System.out.println("1. View My Profile");
            System.out.println("2. Update My Profile");
            System.out.println("3. View Leave Balance");
            System.out.println("4. Apply for Leave");
            System.out.println("5. View My Leave History");
            System.out.println("6. Logout");
            System.out.println("===========================================");
            System.out.print("Choose option: ");
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1": viewProfile(); break;
                    case "2": updateProfile(); break;
                    case "3": viewLeaveBalance(); break;
                    case "4": applyLeave(); break;
                    case "5": viewLeaveHistory(); break;
                    case "6": 
                        System.out.println("Goodbye!");
                        return;
                    default: 
                        System.out.println("Invalid option!");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    private static void viewProfile() throws Exception {
        System.out.println("\n=== MY PROFILE ===");
        // Refresh user data
        currentUser = appService.getEmployee(currentUser.getEmployeeId());
        
        System.out.println("Employee ID: " + currentUser.getEmployeeId());
        System.out.println("Name: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        System.out.println("IC/Passport: " + currentUser.getIcPassport());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Department: " + currentUser.getDepartment());
        System.out.println("Position: " + currentUser.getPosition());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Leave Balance: " + currentUser.getLeaveBalance() + " days");
    }
    
    private static void updateProfile() throws Exception {
        System.out.println("\n=== UPDATE PROFILE ===");
        System.out.print("New Email (current: " + currentUser.getEmail() + "): ");
        String email = scanner.nextLine();
        System.out.print("New Department (current: " + currentUser.getDepartment() + "): ");
        String department = scanner.nextLine();
        System.out.print("New Position (current: " + currentUser.getPosition() + "): ");
        String position = scanner.nextLine();
        
        boolean success = appService.updateEmployeeProfile(currentUser.getEmployeeId(), 
                                                          email, department, position);
        if (success) {
            System.out.println("\n✓ Profile updated successfully!");
            currentUser = appService.getEmployee(currentUser.getEmployeeId());
        } else {
            System.out.println("\n✗ Update failed.");
        }
    }
    
    private static void viewLeaveBalance() throws Exception {
        double balance = appService.getLeaveBalance(currentUser.getEmployeeId());
        System.out.println("\n=== LEAVE BALANCE ===");
        System.out.println("Current Balance: " + balance + " days");
    }
    
    private static void applyLeave() throws Exception {
        System.out.println("\n=== APPLY FOR LEAVE ===");
        
        // Check balance first
        double balance = appService.getLeaveBalance(currentUser.getEmployeeId());
        System.out.println("Your current balance: " + balance + " days");
        
        System.out.print("Start Date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();
        System.out.print("End Date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();
        System.out.print("Number of Days: ");
        int days = Integer.parseInt(scanner.nextLine());
        System.out.print("Leave Type (Annual/Sick/Emergency): ");
        String leaveType = scanner.nextLine();
        System.out.print("Reason: ");
        String reason = scanner.nextLine();
        
        if (days > balance) {
            System.out.println("\n✗ Insufficient leave balance! You have " + balance + " days.");
            return;
        }
        
        boolean success = appService.applyLeave(currentUser.getEmployeeId(), startDate, 
                                               endDate, days, leaveType, reason);
        if (success) {
            System.out.println("\n✓ Leave application submitted successfully!");
            System.out.println("Status: PENDING (waiting for HR approval)");
        } else {
            System.out.println("\n✗ Leave application failed.");
        }
    }
    
    private static void viewLeaveHistory() throws Exception {
        System.out.println("\n=== MY LEAVE HISTORY ===");
        List<Leave> leaves = appService.getEmployeeLeaves(currentUser.getEmployeeId());
        
        if (leaves.isEmpty()) {
            System.out.println("No leave records found.");
            return;
        }
        
        System.out.println("\nTotal: " + leaves.size() + " applications");
        System.out.println("─────────────────────────────────────────────────────────────────");
        for (Leave leave : leaves) {
            System.out.println(leave);
        }
        System.out.println("─────────────────────────────────────────────────────────────────");
    }
}
