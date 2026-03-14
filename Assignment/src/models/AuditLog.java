package models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AuditLog data model for tracking all system actions
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String logId;
    private String userId; // Employee ID who performed the action
    private String role; // HR or EMPLOYEE
    private String action; // LOGIN, CREATE_EMPLOYEE, UPDATE_PROFILE, APPLY_LEAVE, etc.
    private String targetEntity; // EMPLOYEE, LEAVE, FAMILY
    private String targetId; // ID of the affected entity
    private String details; // Additional information about the action
    private LocalDateTime timestamp;
    private String ipAddress;
    
    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditLog(String logId, String userId, String role, String action, 
                   String targetEntity, String targetId, String details) {
        this.logId = logId;
        this.userId = userId;
        this.role = role;
        this.action = action;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    @Override
    public String toString() {
        return String.format("[%s] User: %s (%s) | Action: %s | Target: %s(%s) | Details: %s",
                timestamp, userId, role, action, targetEntity, targetId, details);
    }
}
