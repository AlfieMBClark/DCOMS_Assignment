# BHEL HRM System - 3-Tier Architecture with Comprehensive Auditing

## Overview

The BHEL HRM System has been refactored from a monolithic 2-tier architecture to a robust **3-tier client-server architecture** with comprehensive audit logging. This design provides better scalability, security, and maintainability.

## Architecture

### Three Tiers

```
┌─────────────────────────┐
│   Client Tier (GUI)     │  ← Swing GUI (LoginPanel, AdminPanel, etc.)
│   Tier 1                │     Connects to Application Server on port 1099
└────────────┬────────────┘
             │ (RMI)
             │
┌────────────▼────────────────────────────┐
│  Application Server (Business Logic)    │  ← Tier 2
│  Port: 1099 (default)                    │     - Manages sessions
│  - AuthService                           │     - AuthService: Login/authentication
│  - HRMService: HR Management             │     - HRMService: HR operations
│  - PRSService: Payroll System            │     - PRSService: Payroll/reporting
└────────────┬────────────────────────────┘
             │ (RMI)
             │
┌────────────▼──────────────────────────┐
│  Database Server (Data Persistence)   │  ← Tier 3
│  Port: 1098 (default)                  │     - Handles CSV I/O
│  - DatabaseService                     │     - Manages all data operations
│  - CSV File Operations                 │     - Centralizes audit logging
│  - Audit Logging                       │     - Ensures data consistency
└───────────────────────────────────────┘
             │
             ▼
        CSV Data Files
     (in /data directory)
```

## Key Components

### 1. Database Service (Tier 3)
**File:** `src/server/DatabaseServer.java`, `src/server/DatabaseServiceImpl.java`

This is the data persistence layer that:
- Manages all CSV file operations
- Implements `DatabaseService` remote interface
- Provides synchronized access to prevent concurrent write conflicts
- Centralizes audit logging
- Runs on port **1098** by default

**CSV Files Managed:**
- `users.csv` - User accounts and credentials
- `employees.csv` - Employee information
- `family_members.csv` - Family member details
- `leave_balances.csv` - Leave entitlements per employee/year
- `leave_applications.csv` - Leave requests
- `profile_updates.csv` - Pending profile change requests
- `payroll_records.csv` - Payroll data
- `audit_log.csv` - **Complete audit trail** of all changes

### 2. Application Server (Tier 2)
**File:** `src/server/ServerMain.java`

The business logic layer that:
- Connects to DatabaseServer at startup
- Implements three services: AuthService, HRMService, PRSService
- Manages user sessions in memory
- Validates permissions and enforces business rules
- Delegates all data operations to DatabaseService
- Logs all operations through AuditLogger
- Runs on port **1099** by default

### 3. Client GUI (Tier 1)
**Files:** `src/client/ClientMain.java`, `src/client/LoginPanel.java`, etc.

The presentation layer that:
- Connects to Application Server via RMI
- Provides GUI for employees, HR staff, and administrators
- Maintains user sessions via session tokens

## Audit Logging

The system now includes **comprehensive audit logging** that tracks all data modifications:

### Audit Log Entry Fields
```java
public class AuditLogEntry {
    int logId;           // Unique audit entry ID
    int userId;          // User who made the change
    String username;     // Username for readability
    String role;         // User's role (EMPLOYEE, HR, ADMIN)
    String action;       // What was done (CREATE, UPDATE, DELETE, etc.)
    String targetTable;  // Which table was affected
    int targetId;        // ID of the record affected
    String details;      // Additional context about the change
    String timestamp;    // When it happened
}
```

### Example Audit Log Entries
```
[2024-03-14 10:30:45] ahmad.ibrahim (EMPLOYEE) - REQUEST_PROFILE_UPDATE on employees (#123)
  Details: email: 'old@email.com' -> 'new@email.com'

[2024-03-14 10:31:12] hr1 (HR) - APPROVE_PROFILE_UPDATE on employees (#123)
  Details: Employee #123 profile update approved

[2024-03-14 10:35:00] admin (ADMIN) - ADD_EMPLOYEE on employees (#124)
  Details: New employee: Fatima Ali, Department: Marketing

[2024-03-14 10:40:22] ahmad.ibrahim (EMPLOYEE) - APPLY_FOR_LEAVE on leaves (#567)
  Details: Annual leave: 5 days (2024-03-20 to 2024-03-24)
```

### How Auditing Works
1. **Any data change** (create, update, delete) is logged to `audit_log.csv`
2. **User information** is included (who made the change, what role they had)
3. **Complete context** is recorded (what was changed, from what to what)
4. **Timestamp** is automatically recorded for compliance

### Accessing Audit Logs
**Database Service provides three methods:**
- `getAuditLog()` - Get all audit entries
- `getAuditLogForUser(userId)` - Get entries for a specific user
- `getAuditLogForTable(tableName)` - Get entries for a specific table

Users with ADMIN or HR role can view audit logs to:
- Track who changed employee records
- Monitor system activity
- Investigate discrepancies
- Ensure compliance and accountability

## Running the System

### Prerequisites
- Java 11 or higher
- Windows Command Prompt or PowerShell

### Quick Start

**Step 1: Compile**
```batch
compile.bat
```

**Step 2: Run All Components**
```batch
run-all.bat
```

This opens 3 new windows automatically.

### Running Components Separately

**Terminal 1: Database Server (Tier 3)**
```batch
run-database.bat
```
Or with custom settings:
```batch
run-database.bat 1098 data
```

**Terminal 2: Application Server (Tier 2)**
```batch
run-server.bat
```
Or connecting to different database:
```batch
run-server.bat 1099 localhost 1098
```

**Terminal 3: Client GUI (Tier 1)**
```batch
run-client.bat
```
Or connecting to different server:
```batch
run-client.bat localhost 1099
```

## Default Credentials

After startup, the system seeds these default accounts:

| Username | Password | Role | Employee ID |
|----------|----------|------|-------------|
| admin | admin123 | ADMIN | 0 |
| hr1 | hr1234 | HR | 0 |
| ahmad.ibrahim | emp123 | EMPLOYEE | 1 |

## Architecture Benefits

### 1. **Separation of Concerns**
- Client handles UI only
- Application Server handles business logic
- Database Server handles persistence

### 2. **Scalability**
- Can run multiple Application Servers against one Database Server
- Database Server can be on a separate machine
- Enables load balancing

### 3. **Security**
- SSL/TLS encryption for all tier-to-tier communication
- Audit logging captures all changes
- Session tokens prevent unauthorized access
- Password hashing with salt

### 4. **Data Consistency**
- All data operations go through single DatabaseService
- Synchronized methods prevent write conflicts
- ACID-like properties for CSV-based storage

### 5. **Auditability**
- Complete audit trail of all operations
- Know who changed what and when
- Compliance with regulatory requirements
- Easy troubleshooting and investigation

### 6. **Maintainability**
- Clear separation makes code easier to understand
- Changes to one tier don't affect others
- Easy to modify data storage (CSV → Database)

## Remote Procedure Calls (RMI)

### Service Interfaces

**DatabaseService** (Tier 3 - Remote)
```java
public interface DatabaseService extends Remote {
    // User operations
    UserAccount getUserById(int userId);
    int addUser(UserAccount user);
    boolean updateUser(UserAccount user);
    
    // Employee operations
    Employee getEmployee(int employeeId);
    int addEmployee(Employee emp);
    
    // Leave operations
    List<LeaveApplication> getLeaveApplications(int employeeId);
    int addLeaveApplication(LeaveApplication app);
    
    // Audit operations
    List<AuditLogEntry> getAuditLog();
    List<AuditLogEntry> getAuditLogForUser(int userId);
    void logAudit(...);
    // ... more methods
}
```

**AuthService** (Tier 2 - Remote)
```java
public interface AuthService extends Remote {
    String login(String username, String password);
    boolean logout(String sessionToken);
    UserAccount getCurrentUser(String sessionToken);
    UserAccount validateSession(String sessionToken);
}
```

**HRMService** (Tier 2 - Remote)
```java
public interface HRMService extends Remote {
    Employee getProfile(int employeeId, String sessionToken);
    // Employee operations
    List<FamilyMember> getFamilyMembers(int employeeId, String sessionToken);
    boolean applyForLeave(LeaveApplication app, String sessionToken);
    // HR operations
    int registerEmployee(Employee emp, String sessionToken);
    List<Employee> searchEmployees(String query, String sessionToken);
    // ... more methods
}
```

**PRSService** (Tier 2 - Remote)
```java
public interface PRSService extends Remote {
    PayrollRecord getPayrollRecord(int employeeId, int month, int year, String sessionToken);
    List<PayrollRecord> getYearlyPayroll(int employeeId, int year, String sessionToken);
    int addPayrollRecord(PayrollRecord record, String sessionToken);
}
```

## Data Flow Examples

### Example 1: Employee Applies for Leave

```
1. Employee logs in via GUI
   → Client calls AuthService.login()
   → AuthService validates against DatabaseService
   → SessionToken returned

2. Employee submits leave application
   → Client calls HRMService.applyForLeave(application, sessionToken)
   → HRMService validates session via AuthService
   → HRMService calls DatabaseService.addLeaveApplication()
   → DatabaseServ writes to leave_applications.csv

3. Automatic audit logging
   → DatabaseService logs to audit_log.csv:
      [timestamp] ahmad.ibrahim (EMPLOYEE) - APPLY_FOR_LEAVE
      Target: leaves (#567)
      Details: Annual leave 5 days requested

4. HR reviews pending applications
   → HR Staff calls HRMService.getPendingLeaveApplications()
   → AuditLogger records the access
```

### Example 2: HR Approves Profile Update

```
1. HR Staff calls HRMService.approveProfileUpdate(requestId, sessionToken)
   
2. HRMService validates HR role
   
3. HRMService calls DatabaseService.approveProfileUpdate(requestId, userId)
   
4. DatabaseService processes:
   - Updates profile_updates.csv (status → APPROVED)
   - Gets the change details (field, old value, new value)
   - Updates employees.csv with new value
   - Logs two audit entries:
     a) APPROVE_PROFILE_UPDATE
     b) UPDATE_EMPLOYEE

5. Employee's profile is now updated with audit trail
```

## File Structure

```
BHEL_HRM/
├── src/
│   ├── client/              # GUI clients (Tier 1)
│   │   ├── ClientMain.java
│   │   ├── LoginPanel.java
│   │   ├── EmployeePanel.java
│   │   └── HRPanel.java
│   ├── common/
│   │   ├── interfaces/      # Remote RMI interfaces
│   │   │   ├── AuthService.java
│   │   │   ├── HRMService.java
│   │   │   ├── PRSService.java
│   │   │   └── DatabaseService.java  # NEW!
│   │   └── models/          # Data models
│   │       ├── Employee.java
│   │       ├── LeaveApplication.java
│   │       ├── AuditLogEntry.java
│   │       └── ...
│   ├── server/              # Server implementations (Tiers 2 & 3)
│   │   ├── ServerMain.java              # Tier 2 (Application Server)
│   │   ├── DatabaseServer.java          # Tier 3 (Database Server) - NEW!
│   │   ├── DatabaseServiceImpl.java      # NEW!
│   │   ├── AuthServiceImpl.java
│   │   ├── HRMServiceImpl.java
│   │   ├── PRSServiceImpl.java
│   │   ├── CSVDataStore.java
│   │   ├── AuditLogger.java             # Enhanced with DatabaseService
│   │   └── ...
│   └── utils/
│       ├── ValidationUtil.java
│       └── PasswordHasher.java
├── data/                    # CSV Data files (on Database Server)
│   ├── users.csv
│   ├── employees.csv
│   ├── audit_log.csv        # AUDIT TRAIL
│   └── ...
├── build.sh                 # Compile script
├── build.xml
├── run_database_server.sh   # OLD - Use run-database.bat instead
├── run_app_server.sh        # OLD - Use run-server.bat instead
├── run_server.sh            # (deprecated)
├── run_client.sh            # OLD - Use run-client.bat instead
├── compile.bat              # NEW! Compile script (Windows)
├── run-all.bat              # NEW! Run all 3 tiers in separate windows
├── run-database.bat         # NEW! Run Tier 3 (Database Server)
├── run-server.bat           # NEW! Run Tier 2 (Application Server)
├── run-client.bat           # NEW! Run Tier 1 (Client GUI)
├── setup-ssl.bat            # NEW! Generate SSL certificates
└── README.md                # This file
```

## Deployment Scenarios

### Scenario 1: Single Machine (Development - Windows)
```
Your Machine
├─ Database Server (localhost:1098)
│  ├─ data/
│  │  ├─ users.csv
│  │  ├─ employees.csv
│  │  ├─ audit_log.csv
│  │  └─ ...
│  └─ Run: run-database.bat
│
├─ Application Server (localhost:1099)
│  ├─ Connects to localhost:1098
│  └─ Run: run-server.bat
│
├─ GUI Client 1 (localhost:1099)
├─ GUI Client 2
└─ GUI Client 3
└─ Run: run-client.bat

Command: run-all.bat (starts all 3 automatically)
```

### Scenario 2: Multiple Servers (Production)
```
┌──────────────────────────────────────────────────┐
│  Database Server Machine (10.0.1.10:1098)       │
│  ├─ /data/                                       │
│  │  ├─ users.csv                                │
│  │  ├─ employees.csv                            │
│  │  └─ audit_log.csv (500MB+)                   │
│  └─ Backups: /data/backups/                     │
└──────────────────────────────────────────────────┘
         │ RMI (SSL/TLS)
         ├── App Server 1 (10.0.1.11:1099) 
         │   └─ Session Pool 1
         ├── App Server 2 (10.0.1.12:1099)
         │   └─ Session Pool 2
         └── App Server 3 (10.0.1.13:1099)
             └─ Session Pool 3

N Clients → Load Balancer → Any App Server
```

## Migration from Old Architecture

The system automatically:
1. Seeds default data if no users exist
2. Migrates the dataset from old format if needed
3. Initializes audit logging for new entries

## Future Enhancements

1. **Database Support** - Replace CSV with MySQL/PostgreSQL
2. **Reporting** - Advanced audit and analytics reports
3. **Backup/Restore** - Automated data backup to Database Server
4. **Clustering** - Multiple Database Servers with replication
5. **REST API** - HTTP API for web clients
6. **Performance** - Caching layer in Application Server

## Troubleshooting (Windows)

### Database Server won't start
```
Error: Port 1098 already in use
Solution: Use different port or kill process on 1098
          run-database.bat 1099 data
```

### Application Server can't connect to Database
```
Error: Connection refused to localhost:1098
Solution: 
1. Check Database Server window is running
2. Verify it shows "listening on port 1098"
3. Close Application Server and retry
   (Database needs time to start)
```

### Audit log missing entries
```
Cause: Ensure all users complete operations (login success, actions complete)
Check: data\audit_log.csv for activity
```

### SSL Connection Failed
```
Solution:
1. Run setup-ssl.bat to generate certificates (one-time)
2. Use /ssl flag with all 3 tiers: run-all.bat /ssl
3. Check certs\ directory has .keystore and .truststore files
```

## Quick Help

**Won't compile?**
```batch
REM Delete old output and recompile
rmdir /s out
compile.bat
```

**Servers won't start?**
- Check compile.bat succeeded
- Ensure Java 11+ is installed (type: java -version)
- Check ports 1098, 1099 aren't in use

**Forgot test passwords?**
```
admin / admin123 (Administrator)
hr1 / hr1234 (HR Staff)
ahmad.ibrahim / emp123 (Employee)
```

---

**BHEL HRM System - 3-Tier Architecture with Comprehensive Auditing** | **v3.0** (2024-03-14)
