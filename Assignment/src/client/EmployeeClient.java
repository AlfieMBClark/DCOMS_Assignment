package client;

import interfaces.ApplicationInterface;
import models.*;
import java.rmi.Naming;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Employee Client Application
 * Provides menu-driven interface for employee self-service operations
 * 
 * Usage: java client.EmployeeClient [app-server-url]
 * Example: java client.EmployeeClient rmi://192.168.1.20:1100/ApplicationService
 */
public class EmployeeClient {
    
    private ApplicationInterface appServer;
    private Scanner scanner;
    private Employee currentUser;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public EmployeeClient(String appServerUrl) {
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
        
        EmployeeClient client = new EmployeeClient(appServerUrl);
        client.run();
    }
    
    public void run() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   BHEL HR MANAGEMENT SYSTEM - EMPLOYEE ║");
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
                        viewMyProfile();
                        break;
                    case 2:
                        updateMyProfile();
                        break;
                    case 3:
                        viewMyFamilyDetails();
                        break;
                    case 4:
                        addFamilyMember();
                        break;
                    case 5:
                        updateFamilyMember();
                        break;
                    case 6:
                        deleteFamilyMember();
                        break;
                    case 7:
                        viewLeaveBalance();
                        break;
                    case 8:
                        applyForLeave();
                        break;
                    case 9:
                        viewMyLeaveHistory();
                        break;
                    case 10:
                        checkLeaveStatus();
                        break;
                    case 11:
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
        System.out.println("\n--- EMPLOYEE LOGIN ---");
        
        for (int attempts = 0; attempts < 3; attempts++) {
            try {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                
                System.out.print("Password: ");
                String password = scanner.nextLine();
                
                currentUser = appServer.authenticate(username, password);
                
                if (currentUser != null) {
                    System.out.println("\n✓ Login successful!");
                    System.out.println("Welcome, " + currentUser.getFirstName() + " " + 
                                     currentUser.getLastName());
                    System.out.println("Current Leave Balance: " + currentUser.getLeaveBalance() + " days");
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
        System.out.println("║         EMPLOYEE SELF-SERVICE          ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  PROFILE MANAGEMENT                    ║");
        System.out.println("║  1. View My Profile                    ║");
        System.out.println("║  2. Update My Profile                  ║");
        System.out.println("║                                        ║");
        System.out.println("║  FAMILY MANAGEMENT                     ║");
        System.out.println("║  3. View Family Details                ║");
        System.out.println("║  4. Add Family Member                  ║");
        System.out.println("║  5. Update Family Member               ║");
        System.out.println("║  6. Delete Family Member               ║");
        System.out.println("║                                        ║");
        System.out.println("║  LEAVE MANAGEMENT                      ║");
        System.out.println("║  7. View Leave Balance                 ║");
        System.out.println("║  8. Apply for Leave                    ║");
        System.out.println("║  9. View Leave History                 ║");
        System.out.println("║ 10. Check Leave Status                 ║");
        System.out.println("║                                        ║");
        System.out.println("║  ACCOUNT                               ║");
        System.out.println("║ 11. Change Password                    ║");
        System.out.println("║  0. Logout                             ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
    
    private void viewMyProfile() {
        System.out.println("\n=== MY PROFILE ===");
        
        try {
            Employee emp = appServer.viewOwnProfile(currentUser.getEmployeeId());
            
            if (emp == null) {
                System.out.println("Unable to retrieve profile.");
                return;
            }
            
            System.out.println("\n--- Personal Information ---");
            System.out.println("Employee ID    : " + emp.getEmployeeId());
            System.out.println("Name           : " + emp.getFirstName() + " " + emp.getLastName());
            System.out.println("IC/Passport    : " + emp.getIcPassport());
            System.out.println("Email          : " + emp.getEmail());
            System.out.println("Department     : " + emp.getDepartment());
            System.out.println("Position       : " + emp.getPosition());
            System.out.println("Username       : " + emp.getUsername());
            System.out.println("Leave Balance  : " + emp.getLeaveBalance() + " days");
            System.out.println("Member Since   : " + emp.getCreatedAt().toLocalDate());
            
            // Update local cache
            currentUser = emp;
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void updateMyProfile() {
        System.out.println("\n=== UPDATE MY PROFILE ===");
        System.out.println("Note: You can only update Email, Department, and Position");
        
        try {
            System.out.print("New Email (current: " + currentUser.getEmail() + "): ");
            String email = scanner.nextLine();
            if (email.isEmpty()) email = currentUser.getEmail();
            
            System.out.print("New Department (current: " + currentUser.getDepartment() + "): ");
            String department = scanner.nextLine();
            if (department.isEmpty()) department = currentUser.getDepartment();
            
            System.out.print("New Position (current: " + currentUser.getPosition() + "): ");
            String position = scanner.nextLine();
            if (position.isEmpty()) position = currentUser.getPosition();
            
            boolean success = appServer.updateOwnProfile(currentUser.getEmployeeId(), 
                    email, department, position);
            
            if (success) {
                System.out.println("\n✓ Profile updated successfully!");
                currentUser.setEmail(email);
                currentUser.setDepartment(department);
                currentUser.setPosition(position);
            } else {
                System.out.println("\n✗ Failed to update profile.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewMyFamilyDetails() {
        System.out.println("\n=== MY FAMILY DETAILS ===");
        
        try {
            List<FamilyDetails> familyList = appServer.viewOwnFamilyDetails(
                    currentUser.getEmployeeId());
            
            if (familyList.isEmpty()) {
                System.out.println("No family details recorded.");
                return;
            }
            
            System.out.println("\n" + String.format("%-10s %-25s %-15s %-20s %-15s", 
                    "Family ID", "Name", "Relationship", "IC/Passport", "Contact"));
            System.out.println("─".repeat(90));
            
            for (FamilyDetails fam : familyList) {
                System.out.println(String.format("%-10s %-25s %-15s %-20s %-15s", 
                        fam.getFamilyId(),
                        fam.getMemberName(),
                        fam.getRelationship(),
                        fam.getIcPassport(),
                        fam.getContactNumber()));
            }
            
            System.out.println("\nTotal Family Members: " + familyList.size());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void addFamilyMember() {
        System.out.println("\n=== ADD FAMILY MEMBER ===");
        
        try {
            System.out.print("Member Name: ");
            String memberName = scanner.nextLine();
            
            System.out.print("Relationship (Spouse/Child/Parent/Sibling): ");
            String relationship = scanner.nextLine();
            
            System.out.print("IC/Passport: ");
            String icPassport = scanner.nextLine();
            
            System.out.print("Contact Number: ");
            String contactNumber = scanner.nextLine();
            
            boolean success = appServer.addFamilyDetails(currentUser.getEmployeeId(), 
                    memberName, relationship, icPassport, contactNumber);
            
            if (success) {
                System.out.println("\n✓ Family member added successfully!");
            } else {
                System.out.println("\n✗ Failed to add family member.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void updateFamilyMember() {
        System.out.println("\n=== UPDATE FAMILY MEMBER ===");
        
        try {
            viewMyFamilyDetails();
            
            System.out.print("\nEnter Family ID to update: ");
            String familyId = scanner.nextLine();
            
            List<FamilyDetails> familyList = appServer.viewOwnFamilyDetails(
                    currentUser.getEmployeeId());
            
            FamilyDetails familyToUpdate = null;
            for (FamilyDetails fam : familyList) {
                if (fam.getFamilyId().equals(familyId)) {
                    familyToUpdate = fam;
                    break;
                }
            }
            
            if (familyToUpdate == null) {
                System.out.println("Family member not found.");
                return;
            }
            
            System.out.print("New Name (current: " + familyToUpdate.getMemberName() + "): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) familyToUpdate.setMemberName(name);
            
            System.out.print("New Relationship (current: " + familyToUpdate.getRelationship() + "): ");
            String relationship = scanner.nextLine();
            if (!relationship.isEmpty()) familyToUpdate.setRelationship(relationship);
            
            System.out.print("New Contact (current: " + familyToUpdate.getContactNumber() + "): ");
            String contact = scanner.nextLine();
            if (!contact.isEmpty()) familyToUpdate.setContactNumber(contact);
            
            boolean success = appServer.updateFamilyDetails(currentUser.getEmployeeId(), 
                    familyToUpdate);
            
            if (success) {
                System.out.println("\n✓ Family member updated successfully!");
            } else {
                System.out.println("\n✗ Failed to update family member.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void deleteFamilyMember() {
        System.out.println("\n=== DELETE FAMILY MEMBER ===");
        
        try {
            viewMyFamilyDetails();
            
            System.out.print("\nEnter Family ID to delete: ");
            String familyId = scanner.nextLine();
            
            System.out.print("Are you sure? (yes/no): ");
            String confirm = scanner.nextLine();
            
            if (confirm.equalsIgnoreCase("yes")) {
                boolean success = appServer.deleteFamilyDetails(currentUser.getEmployeeId(), 
                        familyId);
                
                if (success) {
                    System.out.println("\n✓ Family member deleted successfully!");
                } else {
                    System.out.println("\n✗ Failed to delete family member.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewLeaveBalance() {
        System.out.println("\n=== MY LEAVE BALANCE ===");
        
        try {
            double balance = appServer.viewLeaveBalance(currentUser.getEmployeeId());
            
            System.out.println("\nCurrent Leave Balance: " + balance + " days");
            
            if (balance < 5) {
                System.out.println("⚠ Warning: Low leave balance!");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void applyForLeave() {
        System.out.println("\n=== APPLY FOR LEAVE ===");
        
        try {
            double balance = appServer.viewLeaveBalance(currentUser.getEmployeeId());
            System.out.println("Current Leave Balance: " + balance + " days\n");
            
            System.out.print("Start Date (YYYY-MM-DD): ");
            String startDateStr = scanner.nextLine();
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            
            System.out.print("End Date (YYYY-MM-DD): ");
            String endDateStr = scanner.nextLine();
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
            
            System.out.println("\nLeave Types:");
            System.out.println("1. ANNUAL");
            System.out.println("2. SICK");
            System.out.println("3. EMERGENCY");
            System.out.println("4. UNPAID");
            System.out.println("5. MATERNITY");
            System.out.println("6. PATERNITY");
            System.out.print("Select Leave Type (1-6): ");
            int typeChoice = scanner.nextInt();
            scanner.nextLine();
            
            String[] leaveTypes = {"ANNUAL", "SICK", "EMERGENCY", "UNPAID", "MATERNITY", "PATERNITY"};
            String leaveType = leaveTypes[typeChoice - 1];
            
            System.out.print("Reason for Leave: ");
            String reason = scanner.nextLine();
            
            boolean success = appServer.applyLeave(currentUser.getEmployeeId(), 
                    startDate, endDate, leaveType, reason);
            
            if (success) {
                System.out.println("\n✓ Leave application submitted successfully!");
                System.out.println("Your leave application is now pending HR approval.");
            } else {
                System.out.println("\n✗ Failed to submit leave application.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void viewMyLeaveHistory() {
        System.out.println("\n=== MY LEAVE HISTORY ===");
        
        try {
            List<Leave> leaves = appServer.viewOwnLeaveHistory(currentUser.getEmployeeId());
            
            if (leaves.isEmpty()) {
                System.out.println("No leave history found.");
                return;
            }
            
            System.out.println("\n" + String.format("%-10s %-12s %-12s %-6s %-12s %-10s %-20s", 
                    "Leave ID", "Start Date", "End Date", "Days", "Type", "Status", "Applied At"));
            System.out.println("─".repeat(100));
            
            for (Leave leave : leaves) {
                System.out.println(String.format("%-10s %-12s %-12s %-6.1f %-12s %-10s %-20s", 
                        leave.getLeaveId(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getNumberOfDays(),
                        leave.getLeaveType(),
                        leave.getStatus(),
                        leave.getAppliedAt().toLocalDate()));
            }
            
            System.out.println("\nTotal Leave Applications: " + leaves.size());
            
            long pending = leaves.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
            long approved = leaves.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
            long rejected = leaves.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();
            
            System.out.println("Pending: " + pending + " | Approved: " + approved + 
                             " | Rejected: " + rejected);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void checkLeaveStatus() {
        System.out.println("\n=== CHECK LEAVE STATUS ===");
        
        try {
            System.out.print("Enter Leave ID: ");
            String leaveId = scanner.nextLine();
            
            Leave leave = appServer.checkLeaveStatus(currentUser.getEmployeeId(), leaveId);
            
            if (leave == null) {
                System.out.println("Leave application not found.");
                return;
            }
            
            System.out.println("\n--- Leave Application Details ---");
            System.out.println("Leave ID       : " + leave.getLeaveId());
            System.out.println("Start Date     : " + leave.getStartDate());
            System.out.println("End Date       : " + leave.getEndDate());
            System.out.println("Number of Days : " + leave.getNumberOfDays());
            System.out.println("Leave Type     : " + leave.getLeaveType());
            System.out.println("Reason         : " + leave.getReason());
            System.out.println("Status         : " + leave.getStatus());
            System.out.println("Applied At     : " + leave.getAppliedAt());
            
            if (leave.getProcessedAt() != null) {
                System.out.println("Processed At   : " + leave.getProcessedAt());
                System.out.println("Approved By    : " + leave.getApprovedBy());
                if (leave.getRemarks() != null && !leave.getRemarks().isEmpty()) {
                    System.out.println("Remarks        : " + leave.getRemarks());
                }
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
