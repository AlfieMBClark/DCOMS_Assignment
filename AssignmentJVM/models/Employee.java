package models;

import java.io.Serializable;

public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String employeeId;
    private String firstName;
    private String lastName;
    private String icPassport;
    private String email;
    private String department;
    private String position;
    private String username;
    private String password;
    private String role; // "HR" or "EMPLOYEE"
    private double leaveBalance;
    
    public Employee() {}
    
    public Employee(String employeeId, String firstName, String lastName, 
                   String icPassport, String email, String username, 
                   String password, String role) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = "";
        this.position = "";
        this.leaveBalance = 20.0;
    }
    
    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getIcPassport() { return icPassport; }
    public void setIcPassport(String icPassport) { this.icPassport = icPassport; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public double getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(double leaveBalance) { this.leaveBalance = leaveBalance; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s) - %s - Balance: %.1f days", 
            employeeId, firstName, lastName, username, role, leaveBalance);
    }
}
