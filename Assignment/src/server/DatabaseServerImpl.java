package server;

import interfaces.DatabaseInterface;
import models.*;
import utils.CSVDataManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * DatabaseServer - RMI implementation for database operations
 * This server runs on a dedicated machine and manages CSV file storage
 */
public class DatabaseServerImpl extends UnicastRemoteObject implements DatabaseInterface {
    
    private static final long serialVersionUID = 1L;
    private final CSVDataManager dataManager;
    
    public DatabaseServerImpl() throws RemoteException {
        super();
        this.dataManager = new CSVDataManager();
        System.out.println("Database Server initialized with CSV storage.");
    }
    
    // ==================== Employee Operations ====================
    
    @Override
    public boolean createEmployee(Employee employee) throws RemoteException {
        try {
            System.out.println("DB: Creating employee: " + employee.getEmployeeId());
            return dataManager.createEmployee(employee);
        } catch (Exception e) {
            System.err.println("Error in createEmployee: " + e.getMessage());
            throw new RemoteException("Failed to create employee", e);
        }
    }
    
    @Override
    public Employee getEmployeeById(String employeeId) throws RemoteException {
        try {
            return dataManager.getEmployeeById(employeeId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get employee", e);
        }
    }
    
    @Override
    public Employee getEmployeeByUsername(String username) throws RemoteException {
        try {
            return dataManager.getEmployeeByUsername(username);
        } catch (Exception e) {
            throw new RemoteException("Failed to get employee by username", e);
        }
    }
    
    @Override
    public Employee getEmployeeByIcPassport(String icPassport) throws RemoteException {
        try {
            return dataManager.getEmployeeByIcPassport(icPassport);
        } catch (Exception e) {
            throw new RemoteException("Failed to get employee by IC/Passport", e);
        }
    }
    
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        try {
            return dataManager.getAllEmployees();
        } catch (Exception e) {
            throw new RemoteException("Failed to get all employees", e);
        }
    }
    
    @Override
    public boolean updateEmployee(Employee employee) throws RemoteException {
        try {
            System.out.println("DB: Updating employee: " + employee.getEmployeeId());
            return dataManager.updateEmployee(employee);
        } catch (Exception e) {
            throw new RemoteException("Failed to update employee", e);
        }
    }
    
    @Override
    public boolean deleteEmployee(String employeeId) throws RemoteException {
        try {
            System.out.println("DB: Deleting employee: " + employeeId);
            return dataManager.deleteEmployee(employeeId);
        } catch (Exception e) {
            throw new RemoteException("Failed to delete employee", e);
        }
    }
    
    // ==================== Family Details Operations ====================
    
    @Override
    public boolean createFamilyDetails(FamilyDetails familyDetails) throws RemoteException {
        try {
            System.out.println("DB: Creating family details: " + familyDetails.getFamilyId());
            return dataManager.createFamilyDetails(familyDetails);
        } catch (Exception e) {
            throw new RemoteException("Failed to create family details", e);
        }
    }
    
    @Override
    public FamilyDetails getFamilyDetailsById(String familyId) throws RemoteException {
        try {
            return dataManager.getFamilyDetailsById(familyId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get family details", e);
        }
    }
    
    @Override
    public List<FamilyDetails> getFamilyDetailsByEmployeeId(String employeeId) throws RemoteException {
        try {
            return dataManager.getFamilyDetailsByEmployeeId(employeeId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get family details by employee", e);
        }
    }
    
    @Override
    public boolean updateFamilyDetails(FamilyDetails familyDetails) throws RemoteException {
        try {
            System.out.println("DB: Updating family details: " + familyDetails.getFamilyId());
            return dataManager.updateFamilyDetails(familyDetails);
        } catch (Exception e) {
            throw new RemoteException("Failed to update family details", e);
        }
    }
    
    @Override
    public boolean deleteFamilyDetails(String familyId) throws RemoteException {
        try {
            System.out.println("DB: Deleting family details: " + familyId);
            return dataManager.deleteFamilyDetails(familyId);
        } catch (Exception e) {
            throw new RemoteException("Failed to delete family details", e);
        }
    }
    
    // ==================== Leave Operations ====================
    
    @Override
    public boolean createLeave(Leave leave) throws RemoteException {
        try {
            System.out.println("DB: Creating leave: " + leave.getLeaveId());
            return dataManager.createLeave(leave);
        } catch (Exception e) {
            throw new RemoteException("Failed to create leave", e);
        }
    }
    
    @Override
    public Leave getLeaveById(String leaveId) throws RemoteException {
        try {
            return dataManager.getLeaveById(leaveId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get leave", e);
        }
    }
    
    @Override
    public List<Leave> getLeavesByEmployeeId(String employeeId) throws RemoteException {
        try {
            return dataManager.getLeavesByEmployeeId(employeeId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get leaves by employee", e);
        }
    }
    
    @Override
    public List<Leave> getLeavesByStatus(String status) throws RemoteException {
        try {
            return dataManager.getLeavesByStatus(status);
        } catch (Exception e) {
            throw new RemoteException("Failed to get leaves by status", e);
        }
    }
    
    @Override
    public List<Leave> getAllLeaves() throws RemoteException {
        try {
            return dataManager.getAllLeaves();
        } catch (Exception e) {
            throw new RemoteException("Failed to get all leaves", e);
        }
    }
    
    @Override
    public boolean updateLeave(Leave leave) throws RemoteException {
        try {
            System.out.println("DB: Updating leave: " + leave.getLeaveId());
            return dataManager.updateLeave(leave);
        } catch (Exception e) {
            throw new RemoteException("Failed to update leave", e);
        }
    }
    
    @Override
    public boolean deleteLeave(String leaveId) throws RemoteException {
        try {
            System.out.println("DB: Deleting leave: " + leaveId);
            return dataManager.deleteLeave(leaveId);
        } catch (Exception e) {
            throw new RemoteException("Failed to delete leave", e);
        }
    }
    
    // ==================== Audit Log Operations ====================
    
    @Override
    public boolean createAuditLog(AuditLog auditLog) throws RemoteException {
        try {
            return dataManager.createAuditLog(auditLog);
        } catch (Exception e) {
            throw new RemoteException("Failed to create audit log", e);
        }
    }
    
    @Override
    public List<AuditLog> getAuditLogsByUserId(String userId) throws RemoteException {
        try {
            return dataManager.getAuditLogsByUserId(userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get audit logs", e);
        }
    }
    
    @Override
    public List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate) throws RemoteException {
        try {
            // For now, return all logs (can be enhanced with date filtering)
            return dataManager.getAllAuditLogs();
        } catch (Exception e) {
            throw new RemoteException("Failed to get audit logs by date range", e);
        }
    }
    
    @Override
    public List<AuditLog> getAllAuditLogs() throws RemoteException {
        try {
            return dataManager.getAllAuditLogs();
        } catch (Exception e) {
            throw new RemoteException("Failed to get all audit logs", e);
        }
    }
    
    // ==================== Utility Operations ====================
    
    @Override
    public String generateNextEmployeeId() throws RemoteException {
        try {
            return dataManager.generateNextEmployeeId();
        } catch (Exception e) {
            throw new RemoteException("Failed to generate employee ID", e);
        }
    }
    
    @Override
    public String generateNextLeaveId() throws RemoteException {
        try {
            return dataManager.generateNextLeaveId();
        } catch (Exception e) {
            throw new RemoteException("Failed to generate leave ID", e);
        }
    }
    
    @Override
    public String generateNextFamilyId() throws RemoteException {
        try {
            return dataManager.generateNextFamilyId();
        } catch (Exception e) {
            throw new RemoteException("Failed to generate family ID", e);
        }
    }
    
    @Override
    public String generateNextAuditLogId() throws RemoteException {
        try {
            return dataManager.generateNextAuditLogId();
        } catch (Exception e) {
            throw new RemoteException("Failed to generate audit log ID", e);
        }
    }
    
    @Override
    public boolean backupData() throws RemoteException {
        try {
            System.out.println("DB: Creating backup...");
            return dataManager.backupData();
        } catch (Exception e) {
            throw new RemoteException("Failed to backup data", e);
        }
    }
    
    @Override
    public boolean restoreData(String backupFileName) throws RemoteException {
        // To be implemented if needed
        throw new RemoteException("Restore not yet implemented");
    }
}
