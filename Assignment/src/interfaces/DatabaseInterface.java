package interfaces;

import models.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for Database Server operations
 * This server manages all data persistence in CSV files
 */
public interface DatabaseInterface extends Remote {
    
    // Employee CRUD Operations
    boolean createEmployee(Employee employee) throws RemoteException;
    Employee getEmployeeById(String employeeId) throws RemoteException;
    Employee getEmployeeByUsername(String username) throws RemoteException;
    Employee getEmployeeByIcPassport(String icPassport) throws RemoteException;
    List<Employee> getAllEmployees() throws RemoteException;
    boolean updateEmployee(Employee employee) throws RemoteException;
    boolean deleteEmployee(String employeeId) throws RemoteException;
    
    // Family Details CRUD Operations
    boolean createFamilyDetails(FamilyDetails familyDetails) throws RemoteException;
    FamilyDetails getFamilyDetailsById(String familyId) throws RemoteException;
    List<FamilyDetails> getFamilyDetailsByEmployeeId(String employeeId) throws RemoteException;
    boolean updateFamilyDetails(FamilyDetails familyDetails) throws RemoteException;
    boolean deleteFamilyDetails(String familyId) throws RemoteException;
    
    // Leave CRUD Operations
    boolean createLeave(Leave leave) throws RemoteException;
    Leave getLeaveById(String leaveId) throws RemoteException;
    List<Leave> getLeavesByEmployeeId(String employeeId) throws RemoteException;
    List<Leave> getLeavesByStatus(String status) throws RemoteException;
    List<Leave> getAllLeaves() throws RemoteException;
    boolean updateLeave(Leave leave) throws RemoteException;
    boolean deleteLeave(String leaveId) throws RemoteException;
    
    // Audit Log Operations
    boolean createAuditLog(AuditLog auditLog) throws RemoteException;
    List<AuditLog> getAuditLogsByUserId(String userId) throws RemoteException;
    List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate) throws RemoteException;
    List<AuditLog> getAllAuditLogs() throws RemoteException;
    
    // Utility Operations
    String generateNextEmployeeId() throws RemoteException;
    String generateNextLeaveId() throws RemoteException;
    String generateNextFamilyId() throws RemoteException;
    String generateNextAuditLogId() throws RemoteException;
    boolean backupData() throws RemoteException;
    boolean restoreData(String backupFileName) throws RemoteException;
}
