package interfaces;

import models.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Remote interface for Application Server operations
 * This server handles all business logic, validation, and authorization
 */
public interface ApplicationInterface extends Remote {
    
    // Authentication
    Employee authenticate(String username, String password) throws RemoteException;
    boolean changePassword(String employeeId, String oldPassword, String newPassword) throws RemoteException;
    
    // HR Staff Operations - Employee Management
    boolean registerEmployee(String hrStaffId, String firstName, String lastName, String icPassport,
                           String email, String department, String position, String username, 
                           String password, String role, double initialLeaveBalance) throws RemoteException;
    List<Employee> viewAllEmployees(String hrStaffId) throws RemoteException;
    Employee viewEmployeeProfile(String hrStaffId, String targetEmployeeId) throws RemoteException;
    boolean updateEmployeeByHR(String hrStaffId, Employee employee) throws RemoteException;
    
    // HR Staff Operations - Leave Management
    List<Leave> viewAllLeaveApplications(String hrStaffId) throws RemoteException;
    List<Leave> viewPendingLeaveApplications(String hrStaffId) throws RemoteException;
    boolean processLeaveApplication(String hrStaffId, String leaveId, String status, String remarks) throws RemoteException;
    
    // HR Staff Operations - Reporting
    Map<String, Object> generateEmployeeYearlyReport(String hrStaffId, String employeeId, int year) throws RemoteException;
    List<Map<String, Object>> generateAllEmployeesReport(String hrStaffId) throws RemoteException;
    Map<String, Object> generateLeaveStatistics(String hrStaffId) throws RemoteException;
    
    // Employee Operations - Profile Management
    Employee viewOwnProfile(String employeeId) throws RemoteException;
    boolean updateOwnProfile(String employeeId, String email, String department, String position) throws RemoteException;
    
    // Employee Operations - Family Management
    boolean addFamilyDetails(String employeeId, String memberName, String relationship, 
                           String icPassport, String contactNumber) throws RemoteException;
    List<FamilyDetails> viewOwnFamilyDetails(String employeeId) throws RemoteException;
    boolean updateFamilyDetails(String employeeId, FamilyDetails familyDetails) throws RemoteException;
    boolean deleteFamilyDetails(String employeeId, String familyId) throws RemoteException;
    
    // Employee Operations - Leave Management
    double viewLeaveBalance(String employeeId) throws RemoteException;
    boolean applyLeave(String employeeId, LocalDate startDate, LocalDate endDate, 
                      String leaveType, String reason) throws RemoteException;
    List<Leave> viewOwnLeaveHistory(String employeeId) throws RemoteException;
    Leave checkLeaveStatus(String employeeId, String leaveId) throws RemoteException;
    
    // Audit and System Operations
    List<AuditLog> viewAuditLogs(String hrStaffId, String targetEmployeeId) throws RemoteException;
    boolean performSystemBackup(String hrStaffId) throws RemoteException;
    String getSystemStatus() throws RemoteException;
}
