package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave data model for managing leave applications
 */
public class Leave implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String leaveId;
    private String employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double numberOfDays;
    private String leaveType; // Annual, Sick, Emergency, etc.
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String approvedBy; // HR Staff employee ID
    private String remarks;
    private LocalDateTime appliedAt;
    private LocalDateTime processedAt;
    
    // Constructors
    public Leave() {
        this.appliedAt = LocalDateTime.now();
    }
    
    public Leave(String leaveId, String employeeId, LocalDate startDate, LocalDate endDate,
                double numberOfDays, String leaveType, String reason) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = "PENDING";
        this.appliedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getLeaveId() { return leaveId; }
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public double getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(double numberOfDays) { this.numberOfDays = numberOfDays; }
    
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    @Override
    public String toString() {
        return "Leave{" +
                "leaveId='" + leaveId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", numberOfDays=" + numberOfDays +
                ", leaveType='" + leaveType + '\'' +
                ", status='" + status + '\'' +
                ", appliedAt=" + appliedAt +
                '}';
    }
}
