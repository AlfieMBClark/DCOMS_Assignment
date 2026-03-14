package application;

import database.DatabaseInterface;
import models.Employee;
import models.Leave;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

// Application server implementation with business logic
public class ApplicationServerImpl extends UnicastRemoteObject implements ApplicationInterface {
    
    private DatabaseInterface database;
    
    public ApplicationServerImpl(DatabaseInterface database) throws RemoteException {
        super();
        this.database = database;
    }
    
    @Override
    public Employee authenticate(String username, String password) throws RemoteException {
        Employee emp = database.getEmployeeByUsername(username);
        if (emp != null && emp.getPassword().equals(password)) {
            System.out.println("User authenticated: " + username + " (Role: " + emp.getRole() + ")");
            return emp;
        }
        System.out.println("Authentication failed for: " + username);
        return null;
    }
    
    @Override
    public boolean registerEmployee(String firstName, String lastName, String icPassport,
                                   String email, String username, String password, String role) throws RemoteException {
        // Check if username already exists
        Employee existing = database.getEmployeeByUsername(username);
        if (existing != null) {
            System.out.println("Registration failed: Username already exists - " + username);
            return false;
        }
        
        // Create new employee
        String employeeId = database.generateNextEmployeeId();
        Employee newEmp = new Employee(employeeId, firstName, lastName, icPassport, 
                                      email, username, password, role);
        boolean saved = database.saveEmployee(newEmp);
        
        if (saved) {
            System.out.println("Employee registered: " + employeeId + " (" + username + ")");
        }
        return saved;
    }
    
    @Override
    public Employee getEmployee(String employeeId) throws RemoteException {
        return database.getEmployeeById(employeeId);
    }
    
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        return database.getAllEmployees();
    }
    
    @Override
    public boolean updateEmployeeProfile(String employeeId, String email, 
                                        String department, String position) throws RemoteException {
        Employee emp = database.getEmployeeById(employeeId);
        if (emp == null) {
            return false;
        }
        
        emp.setEmail(email);
        emp.setDepartment(department);
        emp.setPosition(position);
        
        boolean updated = database.updateEmployee(emp);
        if (updated) {
            System.out.println("Profile updated for: " + employeeId);
        }
        return updated;
    }
    
    @Override
    public boolean applyLeave(String employeeId, String startDate, String endDate,
                            int numberOfDays, String leaveType, String reason) throws RemoteException {
        // Check employee exists
        Employee emp = database.getEmployeeById(employeeId);
        if (emp == null) {
            System.out.println("Leave application failed: Employee not found - " + employeeId);
            return false;
        }
        
        // Check leave balance
        if (emp.getLeaveBalance() < numberOfDays) {
            System.out.println("Leave application failed: Insufficient balance for " + employeeId);
            return false;
        }
        
        // Create leave application
        String leaveId = database.generateNextLeaveId();
        Leave leave = new Leave(leaveId, employeeId, startDate, endDate, 
                               numberOfDays, leaveType, reason);
        boolean saved = database.saveLeave(leave);
        
        if (saved) {
            System.out.println("Leave application submitted: " + leaveId + " for " + employeeId);
        }
        return saved;
    }
    
    @Override
    public List<Leave> getEmployeeLeaves(String employeeId) throws RemoteException {
        return database.getLeavesByEmployeeId(employeeId);
    }
    
    @Override
    public List<Leave> getAllPendingLeaves() throws RemoteException {
        List<Leave> allLeaves = database.getAllLeaves();
        allLeaves.removeIf(leave -> !leave.getStatus().equals("PENDING"));
        return allLeaves;
    }
    
    @Override
    public boolean processLeave(String leaveId, String status) throws RemoteException {
        Leave leave = database.getLeaveById(leaveId);
        if (leave == null) {
            return false;
        }
        
        leave.setStatus(status);
        boolean updated = database.updateLeave(leave);
        
        // If approved, deduct from employee balance
        if (updated && status.equals("APPROVED")) {
            Employee emp = database.getEmployeeById(leave.getEmployeeId());
            if (emp != null) {
                emp.setLeaveBalance(emp.getLeaveBalance() - leave.getNumberOfDays());
                database.updateEmployee(emp);
                System.out.println("Leave approved: " + leaveId + " - Deducted " + 
                                 leave.getNumberOfDays() + " days from " + emp.getEmployeeId());
            }
        } else if (updated) {
            System.out.println("Leave " + status.toLowerCase() + ": " + leaveId);
        }
        
        return updated;
    }
    
    @Override
    public double getLeaveBalance(String employeeId) throws RemoteException {
        Employee emp = database.getEmployeeById(employeeId);
        return (emp != null) ? emp.getLeaveBalance() : 0.0;
    }
}
