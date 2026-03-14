package database;

import models.Employee;
import models.Leave;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// Remote interface - defines methods callable remotely
public interface DatabaseInterface extends Remote {
    
    // Employee operations
    Employee getEmployeeById(String employeeId) throws RemoteException;
    Employee getEmployeeByUsername(String username) throws RemoteException;
    List<Employee> getAllEmployees() throws RemoteException;
    boolean saveEmployee(Employee employee) throws RemoteException;
    boolean updateEmployee(Employee employee) throws RemoteException;
    
    // Leave operations
    Leave getLeaveById(String leaveId) throws RemoteException;
    List<Leave> getLeavesByEmployeeId(String employeeId) throws RemoteException;
    List<Leave> getAllLeaves() throws RemoteException;
    boolean saveLeave(Leave leave) throws RemoteException;
    boolean updateLeave(Leave leave) throws RemoteException;
    
    // Utility
    String generateNextEmployeeId() throws RemoteException;
    String generateNextLeaveId() throws RemoteException;
}
