package models;

import java.io.Serializable;

public class Leave implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String leaveId;
    private String employeeId;
    private String startDate;
    private String endDate;
    private int numberOfDays;
    private String leaveType;
    private String reason;
    private String status; // "PENDING", "APPROVED", "REJECTED"
    
    public Leave() {}
    
    public Leave(String leaveId, String employeeId, String startDate, 
                String endDate, int numberOfDays, String leaveType, String reason) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = "PENDING";
    }
    
    // Getters and Setters
    public String getLeaveId() { return leaveId; }
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public int getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(int numberOfDays) { this.numberOfDays = numberOfDays; }
    
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s to %s (%d days) - %s - Status: %s", 
            leaveId, employeeId, startDate, endDate, numberOfDays, leaveType, status);
    }
}
