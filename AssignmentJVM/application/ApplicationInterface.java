package application;

import models.Employee;
import models.Leave;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// Remote interface for application logic
public interface ApplicationInterface extends Remote {
    
    // Authentication
    Employee authenticate(String username, String password) throws RemoteException;
    
    // Employee operations
    boolean registerEmployee(String firstName, String lastName, String icPassport,
                           String email, String username, String password, String role) throws RemoteException;
    Employee getEmployee(String employeeId) throws RemoteException;
    List<Employee> getAllEmployees() throws RemoteException;
    boolean updateEmployeeProfile(String employeeId, String email, String department, String position) throws RemoteException;
    
    // Leave operations
    boolean applyLeave(String employeeId, String startDate, String endDate, 
                      int numberOfDays, String leaveType, String reason) throws RemoteException;
    List<Leave> getEmployeeLeaves(String employeeId) throws RemoteException;
    List<Leave> getAllPendingLeaves() throws RemoteException;
    boolean processLeave(String leaveId, String status) throws RemoteException;
    double getLeaveBalance(String employeeId) throws RemoteException;
}
