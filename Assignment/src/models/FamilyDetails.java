package models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * FamilyDetails data model for storing employee family information
 */
public class FamilyDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String familyId;
    private String employeeId;
    private String memberName;
    private String relationship; // Spouse, Child, Parent, etc.
    private String icPassport;
    private String contactNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public FamilyDetails() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public FamilyDetails(String familyId, String employeeId, String memberName, 
                        String relationship, String icPassport, String contactNumber) {
        this.familyId = familyId;
        this.employeeId = employeeId;
        this.memberName = memberName;
        this.relationship = relationship;
        this.icPassport = icPassport;
        this.contactNumber = contactNumber;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { 
        this.memberName = memberName;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { 
        this.relationship = relationship;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getIcPassport() { return icPassport; }
    public void setIcPassport(String icPassport) { 
        this.icPassport = icPassport;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { 
        this.contactNumber = contactNumber;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "FamilyDetails{" +
                "familyId='" + familyId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", memberName='" + memberName + '\'' +
                ", relationship='" + relationship + '\'' +
                ", icPassport='" + icPassport + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
