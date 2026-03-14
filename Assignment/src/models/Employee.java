package models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Employee data model representing an employee in the system
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String employeeId;
    private String firstName;
    private String lastName;
    private String icPassport; // Unique identifier
    private String email;
    private String department;
    private String position;
    private String username;
    private String password; // In production, use hashed passwords
    private String role; // "HR" or "EMPLOYEE"
    private double leaveBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Employee() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Employee(String employeeId, String firstName, String lastName, String icPassport,
                   String email, String department, String position, String username, 
                   String password, String role, double leaveBalance) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.email = email;
        this.department = department;
        this.position = position;
        this.username = username;
        this.password = password;
        this.role = role;
        this.leaveBalance = leaveBalance;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getIcPassport() { return icPassport; }
    public void setIcPassport(String icPassport) { this.icPassport = icPassport; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { 
        this.department = department;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { 
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { 
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public double getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(double leaveBalance) { 
        this.leaveBalance = leaveBalance;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", icPassport='" + icPassport + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", role='" + role + '\'' +
                ", leaveBalance=" + leaveBalance +
                '}';
    }
}
