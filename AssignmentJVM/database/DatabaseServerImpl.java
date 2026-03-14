package database;

import models.Employee;
import models.Leave;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Implementation of the database server
public class DatabaseServerImpl extends UnicastRemoteObject implements DatabaseInterface {
    
    private Map<String, Employee> employees;
    private Map<String, Leave> leaves;
    private int employeeCounter = 1;
    private int leaveCounter = 1;
    
    protected DatabaseServerImpl() throws RemoteException {
        super();
        employees = new ConcurrentHashMap<>();
        leaves = new ConcurrentHashMap<>();
        
        // Initialize with admin user
        Employee admin = new Employee("EMP0001", "Admin", "User", 
            "900101-01-1234", "admin@bhel.com", "admin", "admin123", "HR");
        employees.put(admin.getEmployeeId(), admin);
        employeeCounter = 2;
        
        System.out.println("Database initialized with admin user (username: admin, password: admin123)");
    }
    
    @Override
    public Employee getEmployeeById(String employeeId) throws RemoteException {
        return employees.get(employeeId);
    }
    
    @Override
    public Employee getEmployeeByUsername(String username) throws RemoteException {
        for (Employee emp : employees.values()) {
            if (emp.getUsername().equals(username)) {
                return emp;
            }
        }
        return null;
    }
    
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        return new ArrayList<>(employees.values());
    }
    
    @Override
    public boolean saveEmployee(Employee employee) throws RemoteException {
        if (employee.getEmployeeId() == null || employee.getEmployeeId().isEmpty()) {
            employee.setEmployeeId(generateNextEmployeeId());
        }
        employees.put(employee.getEmployeeId(), employee);
        System.out.println("Saved employee: " + employee.getEmployeeId());
        return true;
    }
    
    @Override
    public boolean updateEmployee(Employee employee) throws RemoteException {
        if (employees.containsKey(employee.getEmployeeId())) {
            employees.put(employee.getEmployeeId(), employee);
            System.out.println("Updated employee: " + employee.getEmployeeId());
            return true;
        }
        return false;
    }
    
    @Override
    public Leave getLeaveById(String leaveId) throws RemoteException {
        return leaves.get(leaveId);
    }
    
    @Override
    public List<Leave> getLeavesByEmployeeId(String employeeId) throws RemoteException {
        List<Leave> result = new ArrayList<>();
        for (Leave leave : leaves.values()) {
            if (leave.getEmployeeId().equals(employeeId)) {
                result.add(leave);
            }
        }
        return result;
    }
    
    @Override
    public List<Leave> getAllLeaves() throws RemoteException {
        return new ArrayList<>(leaves.values());
    }
    
    @Override
    public boolean saveLeave(Leave leave) throws RemoteException {
        if (leave.getLeaveId() == null || leave.getLeaveId().isEmpty()) {
            leave.setLeaveId(generateNextLeaveId());
        }
        leaves.put(leave.getLeaveId(), leave);
        System.out.println("Saved leave: " + leave.getLeaveId());
        return true;
    }
    
    @Override
    public boolean updateLeave(Leave leave) throws RemoteException {
        if (leaves.containsKey(leave.getLeaveId())) {
            leaves.put(leave.getLeaveId(), leave);
            System.out.println("Updated leave: " + leave.getLeaveId());
            return true;
        }
        return false;
    }
    
    @Override
    public String generateNextEmployeeId() throws RemoteException {
        return String.format("EMP%04d", employeeCounter++);
    }
    
    @Override
    public String generateNextLeaveId() throws RemoteException {
        return String.format("LVE%04d", leaveCounter++);
    }
}
