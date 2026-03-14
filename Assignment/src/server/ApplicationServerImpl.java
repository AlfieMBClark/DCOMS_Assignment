package server;

import interfaces.ApplicationInterface;
import interfaces.DatabaseInterface;
import models.*;
import utils.ValidationUtils;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * ApplicationServerImpl - Business logic layer
 * Connects to DatabaseServer and provides validated business operations
 * Implements authorization, validation, and audit logging
 */
public class ApplicationServerImpl extends UnicastRemoteObject implements ApplicationInterface {
    
    private static final long serialVersionUID = 1L;
    private DatabaseInterface dbServer;
    private String databaseServerUrl;
    
    public ApplicationServerImpl(String databaseServerUrl) throws RemoteException {
        super();
        this.databaseServerUrl = databaseServerUrl;
        connectToDatabase();
    }
    
    /**
     * Establish connection to Database Server
     */
    private void connectToDatabase() throws RemoteException {
        try {
            System.out.println("Connecting to Database Server: " + databaseServerUrl);
            dbServer = (DatabaseInterface) Naming.lookup(databaseServerUrl);
            System.out.println("Successfully connected to Database Server");
        } catch (Exception e) {
            System.err.println("Failed to connect to Database Server: " + e.getMessage());
            throw new RemoteException("Cannot connect to Database Server", e);
        }
    }
    
    // ==================== Authentication ====================
    
    @Override
    public Employee authenticate(String username, String password) throws RemoteException {
        try {
            if (!ValidationUtils.isNotEmpty(username) || !ValidationUtils.isNotEmpty(password)) {
                return null;
            }
            
            Employee employee = dbServer.getEmployeeByUsername(username);
            
            if (employee != null && employee.getPassword().equals(password)) {
                // Log successful login
                logAudit(employee.getEmployeeId(), employee.getRole(), "LOGIN", 
                        "SYSTEM", employee.getEmployeeId(), "Successful login");
                return employee;
            }
            
            return null;
        } catch (Exception e) {
            throw new RemoteException("Authentication failed", e);
        }
    }
    
    @Override
    public boolean changePassword(String employeeId, String oldPassword, String newPassword) 
            throws RemoteException {
        try {
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee == null) {
                return false;
            }
            
            // Verify old password
            if (!employee.getPassword().equals(oldPassword)) {
                return false;
            }
            
            // Validate new password
            if (!ValidationUtils.isValidPassword(newPassword)) {
                throw new RemoteException("New password does not meet requirements");
            }
            
            employee.setPassword(newPassword);
            boolean success = dbServer.updateEmployee(employee);
            
            if (success) {
                logAudit(employeeId, employee.getRole(), "CHANGE_PASSWORD", 
                        "EMPLOYEE", employeeId, "Password changed successfully");
            }
            
            return success;
        } catch (Exception e) {
            throw new RemoteException("Failed to change password", e);
        }
    }
    
    // ==================== HR Staff Operations - Employee Management ====================
    
    @Override
    public boolean registerEmployee(String hrStaffId, String firstName, String lastName, 
                                   String icPassport, String email, String department, 
                                   String position, String username, String password, 
                                   String role, double initialLeaveBalance) throws RemoteException {
        try {
            // Verify HR authorization
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can register employees");
            }
            
            // Validate input data
            String validationError = ValidationUtils.validateEmployeeData(
                    firstName, lastName, icPassport, email, username, password);
            
            if (validationError != null) {
                throw new RemoteException("Validation failed: " + validationError);
            }
            
            if (!ValidationUtils.isValidRole(role)) {
                throw new RemoteException("Invalid role. Must be HR or EMPLOYEE");
            }
            
            if (!ValidationUtils.isValidLeaveBalance(initialLeaveBalance)) {
                throw new RemoteException("Invalid leave balance");
            }
            
            // Check for duplicate IC/Passport
            if (dbServer.getEmployeeByIcPassport(icPassport) != null) {
                throw new RemoteException("IC/Passport number already exists");
            }
            
            // Check for duplicate username
            if (dbServer.getEmployeeByUsername(username) != null) {
                throw new RemoteException("Username already exists");
            }
            
            // Generate employee ID
            String employeeId = dbServer.generateNextEmployeeId();
            
            // Create employee object
            Employee employee = new Employee(employeeId, firstName, lastName, icPassport,
                    email, department, position, username, password, role.toUpperCase(), 
                    initialLeaveBalance);
            
            // Save to database
            boolean success = dbServer.createEmployee(employee);
            
            if (success) {
                logAudit(hrStaffId, "HR", "REGISTER_EMPLOYEE", "EMPLOYEE", 
                        employeeId, "Registered new employee: " + firstName + " " + lastName);
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to register employee", e);
        }
    }
    
    @Override
    public List<Employee> viewAllEmployees(String hrStaffId) throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can view all employees");
            }
            
            logAudit(hrStaffId, "HR", "VIEW_ALL_EMPLOYEES", "EMPLOYEE", 
                    null, "Viewed all employees list");
            
            return dbServer.getAllEmployees();
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve employees", e);
        }
    }
    
    @Override
    public Employee viewEmployeeProfile(String hrStaffId, String targetEmployeeId) 
            throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can view employee profiles");
            }
            
            Employee employee = dbServer.getEmployeeById(targetEmployeeId);
            
            if (employee != null) {
                logAudit(hrStaffId, "HR", "VIEW_EMPLOYEE_PROFILE", "EMPLOYEE", 
                        targetEmployeeId, "Viewed employee profile");
            }
            
            return employee;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve employee profile", e);
        }
    }
    
    @Override
    public boolean updateEmployeeByHR(String hrStaffId, Employee employee) throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can update employee records");
            }
            
            // Validate employee data
            if (!ValidationUtils.isValidName(employee.getFirstName()) ||
                !ValidationUtils.isValidName(employee.getLastName()) ||
                !ValidationUtils.isValidEmail(employee.getEmail())) {
                throw new RemoteException("Invalid employee data");
            }
            
            boolean success = dbServer.updateEmployee(employee);
            
            if (success) {
                logAudit(hrStaffId, "HR", "UPDATE_EMPLOYEE", "EMPLOYEE", 
                        employee.getEmployeeId(), "Updated employee record");
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to update employee", e);
        }
    }
    
    // ==================== HR Staff Operations - Leave Management ====================
    
    @Override
    public List<Leave> viewAllLeaveApplications(String hrStaffId) throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can view all leave applications");
            }
            
            logAudit(hrStaffId, "HR", "VIEW_ALL_LEAVES", "LEAVE", 
                    null, "Viewed all leave applications");
            
            return dbServer.getAllLeaves();
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve leave applications", e);
        }
    }
    
    @Override
    public List<Leave> viewPendingLeaveApplications(String hrStaffId) throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can view pending applications");
            }
            
            logAudit(hrStaffId, "HR", "VIEW_PENDING_LEAVES", "LEAVE", 
                    null, "Viewed pending leave applications");
            
            return dbServer.getLeavesByStatus("PENDING");
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve pending leave applications", e);
        }
    }
    
    @Override
    public synchronized boolean processLeaveApplication(String hrStaffId, String leaveId, 
                                                       String status, String remarks) 
            throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can process leave applications");
            }
            
            if (!ValidationUtils.isValidLeaveStatus(status)) {
                throw new RemoteException("Invalid leave status");
            }
            
            Leave leave = dbServer.getLeaveById(leaveId);
            if (leave == null) {
                throw new RemoteException("Leave application not found");
            }
            
            if (!leave.getStatus().equals("PENDING")) {
                throw new RemoteException("Leave application has already been processed");
            }
            
            // Update leave status
            leave.setStatus(status.toUpperCase());
            leave.setApprovedBy(hrStaffId);
            leave.setRemarks(remarks);
            leave.setProcessedAt(LocalDateTime.now());
            
            // If approved, deduct from employee's leave balance
            if (status.equalsIgnoreCase("APPROVED")) {
                Employee employee = dbServer.getEmployeeById(leave.getEmployeeId());
                if (employee != null) {
                    double newBalance = employee.getLeaveBalance() - leave.getNumberOfDays();
                    if (newBalance < 0) {
                        throw new RemoteException("Insufficient leave balance");
                    }
                    employee.setLeaveBalance(newBalance);
                    dbServer.updateEmployee(employee);
                }
            }
            
            boolean success = dbServer.updateLeave(leave);
            
            if (success) {
                logAudit(hrStaffId, "HR", "PROCESS_LEAVE", "LEAVE", 
                        leaveId, "Processed leave application: " + status);
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to process leave application", e);
        }
    }
    
    // ==================== HR Staff Operations - Reporting ====================
    
    @Override
    public Map<String, Object> generateEmployeeYearlyReport(String hrStaffId, 
                                                           String employeeId, int year) 
            throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can generate reports");
            }
            
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee == null) {
                throw new RemoteException("Employee not found");
            }
            
            List<FamilyDetails> familyDetails = dbServer.getFamilyDetailsByEmployeeId(employeeId);
            List<Leave> leaves = dbServer.getLeavesByEmployeeId(employeeId);
            
            // Filter leaves for the specified year
            List<Leave> yearlyLeaves = new ArrayList<>();
            for (Leave leave : leaves) {
                if (leave.getAppliedAt().getYear() == year) {
                    yearlyLeaves.add(leave);
                }
            }
            
            Map<String, Object> report = new HashMap<>();
            report.put("employee", employee);
            report.put("familyDetails", familyDetails);
            report.put("yearlyLeaves", yearlyLeaves);
            report.put("reportYear", year);
            report.put("generatedAt", LocalDateTime.now());
            report.put("generatedBy", hrStaffId);
            
            logAudit(hrStaffId, "HR", "GENERATE_YEARLY_REPORT", "EMPLOYEE", 
                    employeeId, "Generated yearly report for " + year);
            
            return report;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to generate report", e);
        }
    }
    
    @Override
    public List<Map<String, Object>> generateAllEmployeesReport(String hrStaffId) 
            throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can generate reports");
            }
            
            List<Employee> employees = dbServer.getAllEmployees();
            List<Map<String, Object>> reports = new ArrayList<>();
            
            for (Employee emp : employees) {
                Map<String, Object> empReport = new HashMap<>();
                empReport.put("employeeId", emp.getEmployeeId());
                empReport.put("name", emp.getFirstName() + " " + emp.getLastName());
                empReport.put("department", emp.getDepartment());
                empReport.put("position", emp.getPosition());
                empReport.put("email", emp.getEmail());
                empReport.put("leaveBalance", emp.getLeaveBalance());
                
                List<Leave> leaves = dbServer.getLeavesByEmployeeId(emp.getEmployeeId());
                empReport.put("totalLeaveApplications", leaves.size());
                
                long pendingLeaves = leaves.stream()
                        .filter(l -> l.getStatus().equals("PENDING")).count();
                empReport.put("pendingLeaves", pendingLeaves);
                
                reports.add(empReport);
            }
            
            logAudit(hrStaffId, "HR", "GENERATE_ALL_EMPLOYEES_REPORT", "EMPLOYEE", 
                    null, "Generated report for all employees");
            
            return reports;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to generate all employees report", e);
        }
    }
    
    @Override
    public Map<String, Object> generateLeaveStatistics(String hrStaffId) throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can view statistics");
            }
            
            List<Leave> allLeaves = dbServer.getAllLeaves();
            
            long totalLeaves = allLeaves.size();
            long pendingLeaves = allLeaves.stream()
                    .filter(l -> l.getStatus().equals("PENDING")).count();
            long approvedLeaves = allLeaves.stream()
                    .filter(l -> l.getStatus().equals("APPROVED")).count();
            long rejectedLeaves = allLeaves.stream()
                    .filter(l -> l.getStatus().equals("REJECTED")).count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalLeaves", totalLeaves);
            stats.put("pendingLeaves", pendingLeaves);
            stats.put("approvedLeaves", approvedLeaves);
            stats.put("rejectedLeaves", rejectedLeaves);
            stats.put("generatedAt", LocalDateTime.now());
            
            logAudit(hrStaffId, "HR", "VIEW_LEAVE_STATISTICS", "LEAVE", 
                    null, "Viewed leave statistics");
            
            return stats;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to generate leave statistics", e);
        }
    }
    
    // ==================== Employee Operations - Profile Management ====================
    
    @Override
    public Employee viewOwnProfile(String employeeId) throws RemoteException {
        try {
            Employee employee = dbServer.getEmployeeById(employeeId);
            
            if (employee != null) {
                logAudit(employeeId, employee.getRole(), "VIEW_OWN_PROFILE", 
                        "EMPLOYEE", employeeId, "Viewed own profile");
            }
            
            return employee;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve profile", e);
        }
    }
    
    @Override
    public synchronized boolean updateOwnProfile(String employeeId, String email, 
                                                String department, String position) 
            throws RemoteException {
        try {
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee == null) {
                throw new RemoteException("Employee not found");
            }
            
            // Validate email
            if (!ValidationUtils.isValidEmail(email)) {
                throw new RemoteException("Invalid email format");
            }
            
            // Update allowed fields
            employee.setEmail(email);
            employee.setDepartment(department);
            employee.setPosition(position);
            
            boolean success = dbServer.updateEmployee(employee);
            
            if (success) {
                logAudit(employeeId, employee.getRole(), "UPDATE_OWN_PROFILE", 
                        "EMPLOYEE", employeeId, "Updated own profile");
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to update profile", e);
        }
    }
    
    // ==================== Employee Operations - Family Management ====================
    
    @Override
    public synchronized boolean addFamilyDetails(String employeeId, String memberName, 
                                                String relationship, String icPassport, 
                                                String contactNumber) throws RemoteException {
        try {
            // Validate inputs
            if (!ValidationUtils.isValidName(memberName)) {
                throw new RemoteException("Invalid family member name");
            }
            
            if (!ValidationUtils.isValidIcPassport(icPassport)) {
                throw new RemoteException("Invalid IC/Passport format");
            }
            
            // Generate family ID
            String familyId = dbServer.generateNextFamilyId();
            
            // Create family details object
            FamilyDetails familyDetails = new FamilyDetails(familyId, employeeId, memberName,
                    relationship, icPassport, contactNumber);
            
            boolean success = dbServer.createFamilyDetails(familyDetails);
            
            if (success) {
                Employee employee = dbServer.getEmployeeById(employeeId);
                logAudit(employeeId, employee.getRole(), "ADD_FAMILY_DETAILS", 
                        "FAMILY", familyId, "Added family member: " + memberName);
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to add family details", e);
        }
    }
    
    @Override
    public List<FamilyDetails> viewOwnFamilyDetails(String employeeId) throws RemoteException {
        try {
            List<FamilyDetails> familyDetails = dbServer.getFamilyDetailsByEmployeeId(employeeId);
            
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee != null) {
                logAudit(employeeId, employee.getRole(), "VIEW_FAMILY_DETAILS", 
                        "FAMILY", null, "Viewed family details");
            }
            
            return familyDetails;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve family details", e);
        }
    }
    
    @Override
    public synchronized boolean updateFamilyDetails(String employeeId, FamilyDetails familyDetails) 
            throws RemoteException {
        try {
            // Verify ownership
            if (!familyDetails.getEmployeeId().equals(employeeId)) {
                throw new RemoteException("Unauthorized: Cannot update another employee's family details");
            }
            
            boolean success = dbServer.updateFamilyDetails(familyDetails);
            
            if (success) {
                Employee employee = dbServer.getEmployeeById(employeeId);
                logAudit(employeeId, employee.getRole(), "UPDATE_FAMILY_DETAILS", 
                        "FAMILY", familyDetails.getFamilyId(), "Updated family details");
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to update family details", e);
        }
    }
    
    @Override
    public synchronized boolean deleteFamilyDetails(String employeeId, String familyId) 
            throws RemoteException {
        try {
            // Verify ownership
            FamilyDetails familyDetails = dbServer.getFamilyDetailsById(familyId);
            if (familyDetails == null) {
                throw new RemoteException("Family details not found");
            }
            
            if (!familyDetails.getEmployeeId().equals(employeeId)) {
                throw new RemoteException("Unauthorized: Cannot delete another employee's family details");
            }
            
            boolean success = dbServer.deleteFamilyDetails(familyId);
            
            if (success) {
                Employee employee = dbServer.getEmployeeById(employeeId);
                logAudit(employeeId, employee.getRole(), "DELETE_FAMILY_DETAILS", 
                        "FAMILY", familyId, "Deleted family details");
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to delete family details", e);
        }
    }
    
    // ==================== Employee Operations - Leave Management ====================
    
    @Override
    public double viewLeaveBalance(String employeeId) throws RemoteException {
        try {
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee == null) {
                throw new RemoteException("Employee not found");
            }
            
            return employee.getLeaveBalance();
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve leave balance", e);
        }
    }
    
    @Override
    public synchronized boolean applyLeave(String employeeId, LocalDate startDate, 
                                          LocalDate endDate, String leaveType, String reason) 
            throws RemoteException {
        try {
            // Validate dates
            if (startDate == null || endDate == null) {
                throw new RemoteException("Start date and end date are required");
            }
            
            if (startDate.isAfter(endDate)) {
                throw new RemoteException("Start date must be before or equal to end date");
            }
            
            if (startDate.isBefore(LocalDate.now())) {
                throw new RemoteException("Cannot apply for past dates");
            }
            
            // Validate leave type
            if (!ValidationUtils.isValidLeaveType(leaveType)) {
                throw new RemoteException("Invalid leave type");
            }
            
            // Calculate number of days
            double numberOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            
            if (!ValidationUtils.isValidLeaveDays(numberOfDays)) {
                throw new RemoteException("Invalid number of leave days");
            }
            
            // Check leave balance
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee == null) {
                throw new RemoteException("Employee not found");
            }
            
            if (employee.getLeaveBalance() < numberOfDays) {
                throw new RemoteException("Insufficient leave balance. Available: " + 
                        employee.getLeaveBalance() + " days");
            }
            
            // Generate leave ID
            String leaveId = dbServer.generateNextLeaveId();
            
            // Create leave object
            Leave leave = new Leave(leaveId, employeeId, startDate, endDate, 
                    numberOfDays, leaveType.toUpperCase(), reason);
            
            boolean success = dbServer.createLeave(leave);
            
            if (success) {
                logAudit(employeeId, employee.getRole(), "APPLY_LEAVE", "LEAVE", 
                        leaveId, "Applied for " + numberOfDays + " days of " + leaveType + " leave");
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to apply for leave", e);
        }
    }
    
    @Override
    public List<Leave> viewOwnLeaveHistory(String employeeId) throws RemoteException {
        try {
            List<Leave> leaves = dbServer.getLeavesByEmployeeId(employeeId);
            
            Employee employee = dbServer.getEmployeeById(employeeId);
            if (employee != null) {
                logAudit(employeeId, employee.getRole(), "VIEW_LEAVE_HISTORY", 
                        "LEAVE", null, "Viewed leave history");
            }
            
            return leaves;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve leave history", e);
        }
    }
    
    @Override
    public Leave checkLeaveStatus(String employeeId, String leaveId) throws RemoteException {
        try {
            Leave leave = dbServer.getLeaveById(leaveId);
            
            if (leave == null) {
                return null;
            }
            
            // Verify ownership
            if (!leave.getEmployeeId().equals(employeeId)) {
                throw new RemoteException("Unauthorized: Cannot view another employee's leave");
            }
            
            return leave;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to check leave status", e);
        }
    }
    
    // ==================== Audit and System Operations ====================
    
    @Override
    public List<AuditLog> viewAuditLogs(String hrStaffId, String targetEmployeeId) 
            throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can view audit logs");
            }
            
            if (targetEmployeeId != null) {
                return dbServer.getAuditLogsByUserId(targetEmployeeId);
            } else {
                return dbServer.getAllAuditLogs();
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to retrieve audit logs", e);
        }
    }
    
    @Override
    public boolean performSystemBackup(String hrStaffId) throws RemoteException {
        try {
            if (!isHRStaff(hrStaffId)) {
                throw new RemoteException("Unauthorized: Only HR staff can perform backups");
            }
            
            boolean success = dbServer.backupData();
            
            if (success) {
                logAudit(hrStaffId, "HR", "SYSTEM_BACKUP", "SYSTEM", 
                        null, "Performed system backup");
            }
            
            return success;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to perform backup", e);
        }
    }
    
    @Override
    public String getSystemStatus() throws RemoteException {
        try {
            // Test database connectivity
            dbServer.getAllEmployees();
            
            return "System Status: OPERATIONAL\n" +
                   "Database Server: CONNECTED\n" +
                   "Application Server: RUNNING\n" +
                   "Timestamp: " + LocalDateTime.now();
        } catch (Exception e) {
            return "System Status: ERROR\n" +
                   "Database Server: DISCONNECTED\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Check if user is HR staff
     */
    private boolean isHRStaff(String employeeId) throws RemoteException {
        try {
            Employee employee = dbServer.getEmployeeById(employeeId);
            return employee != null && "HR".equalsIgnoreCase(employee.getRole());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Log audit entry
     */
    private void logAudit(String userId, String role, String action, String targetEntity, 
                         String targetId, String details) {
        try {
            String logId = dbServer.generateNextAuditLogId();
            AuditLog log = new AuditLog(logId, userId, role, action, targetEntity, 
                                       targetId, details);
            dbServer.createAuditLog(log);
        } catch (Exception e) {
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }
}
