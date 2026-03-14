package client;

import application.ApplicationInterface;
import models.Employee;
import models.Leave;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class HRClient {
    
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
            System.err.println("HR Client failed:");
            e.printStackTrace();
            System.err.println("\nMake sure Application Server is running!");
        }
    }
    
    private static boolean login() {
        System.out.println("\n=== HR LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        try {
            currentUser = appService.authenticate(username, password);
            if (currentUser != null && currentUser.getRole().equals("HR")) {
                System.out.println("\nWelcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
                return true;
            } else if (currentUser != null) {
                System.out.println("\nError: You need HR role to access this system.");
                return false;
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
            System.out.println("        HR MANAGEMENT SYSTEM");
            System.out.println("===========================================");
            System.out.println("1. Register New Employee");
            System.out.println("2. View All Employees");
            System.out.println("3. View Pending Leave Applications");
            System.out.println("4. Process Leave Application");
            System.out.println("5. Logout");
            System.out.println("===========================================");
            System.out.print("Choose option: ");
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1": registerEmployee(); break;
                    case "2": viewAllEmployees(); break;
                    case "3": viewPendingLeaves(); break;
                    case "4": processLeave(); break;
                    case "5": 
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
    
    private static void registerEmployee() throws Exception {
        System.out.println("\n=== REGISTER NEW EMPLOYEE ===");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("IC/Passport: ");
        String icPassport = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Role (HR/EMPLOYEE): ");
        String role = scanner.nextLine().toUpperCase();
        
        boolean success = appService.registerEmployee(firstName, lastName, icPassport, 
                                                     email, username, password, role);
        if (success) {
            System.out.println("\n✓ Employee registered successfully!");
        } else {
            System.out.println("\n✗ Registration failed. Username may already exist.");
        }
    }
    
    private static void viewAllEmployees() throws Exception {
        System.out.println("\n=== ALL EMPLOYEES ===");
        List<Employee> employees = appService.getAllEmployees();
        
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }
        
        System.out.println("\nTotal: " + employees.size() + " employees");
        System.out.println("─────────────────────────────────────────────────────────────────");
        for (Employee emp : employees) {
            System.out.println(emp);
        }
        System.out.println("─────────────────────────────────────────────────────────────────");
    }
    
    private static void viewPendingLeaves() throws Exception {
        System.out.println("\n=== PENDING LEAVE APPLICATIONS ===");
        List<Leave> leaves = appService.getAllPendingLeaves();
        
        if (leaves.isEmpty()) {
            System.out.println("No pending leaves.");
            return;
        }
        
        System.out.println("\nTotal: " + leaves.size() + " pending applications");
        System.out.println("─────────────────────────────────────────────────────────────────");
        for (Leave leave : leaves) {
            System.out.println(leave);
        }
        System.out.println("─────────────────────────────────────────────────────────────────");
    }
    
    private static void processLeave() throws Exception {
        System.out.println("\n=== PROCESS LEAVE APPLICATION ===");
        System.out.print("Enter Leave ID (e.g., LVE0001): ");
        String leaveId = scanner.nextLine();
        System.out.print("Action (APPROVED/REJECTED): ");
        String status = scanner.nextLine().toUpperCase();
        
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            System.out.println("\n✗ Invalid status! Use APPROVED or REJECTED.");
            return;
        }
        
        boolean success = appService.processLeave(leaveId, status);
        if (success) {
            System.out.println("\n✓ Leave " + leaveId + " has been " + status + "!");
        } else {
            System.out.println("\n✗ Failed to process leave. Check Leave ID.");
        }
    }
}
