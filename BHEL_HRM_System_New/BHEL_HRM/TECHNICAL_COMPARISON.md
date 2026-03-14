# Technical Comparison: Before vs After

## High-Level Architecture

### BEFORE (Monolithic 2-Tier)
```
Tier 1: GUI Clients
├─ ClientMain.java
├─ LoginPanel.java
├─ EmployeePanel.java
└─ HRPanel.java

Tier 2: Monolithic Server (Single Process)
├─ Port 1099
├─ AuthService
├─ HRMService
├─ PRSService
├─ CSVDataStore (Local File I/O)
├─ Minimal Audit Logging
└─ CSV Files (Same Process Directory)
```

### AFTER (3-Tier + Auditing)
```
Tier 1: GUI Clients (Unchanged)
├─ ClientMain.java
├─ LoginPanel.java
├─ EmployeePanel.java
└─ HRPanel.java
    ↓ RMI

Tier 2: Application Server (Port 1099) - NEW SEPARATION
├─ AuthServiceImpl
├─ HRMServiceImpl
├─ PRSServiceImpl
├─ Session Management
└─ Remote Calls to Tier 3
    ↓ RMI

Tier 3: Database Server (Port 1098) - NEW TIER
├─ DatabaseServiceImpl
├─ DatabaseService Interface
├─ CSVDataStore
├─ Comprehensive Audit Logging
└─ CSV Files + Audit Logs
```

## How Services Access Data

### BEFORE
```
HRMServiceImpl
  ├─ Has: CSVDataStore (local instance)
  ├─ Calls: dataStore.getEmployee(id)
  ├─ Calls: dataStore.getFamilyMembers(id)
  ├─ Issues: 
  │   - Only local file access (not networked)
  │   - Audit logging incomplete
  │   - Cannot scale
  │   - Mixed concerns in one process
```

### AFTER
```
HRMServiceImpl (on Application Server Tier 2)
  ├─ Has: DatabaseService (remote reference via RMI)
  ├─ Calls: dbService.getEmployee(id)  ← Remote call
  ├─ Calls: dbService.getFamilyMembers(id)  ← Remote call
  ├─ Calls: dbService.logAudit(...)  ← All logged
  └─ Benefits:
      - Networked data access
      - Complete audit trail
      - Scalable (multiple app servers)
      - Separated concerns
```

## Service Signatures - Before vs After

### AuthServiceImpl Constructor

**BEFORE:**
```java
public AuthServiceImpl(CSVDataStore dataStore, AuditLogger auditLogger,
                       int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) 
    throws RemoteException {
    super(port, csf, ssf);
    this.dataStore = dataStore;        // Local CSV file access
    this.auditLogger = auditLogger;
}
```

**AFTER:**
```java
public AuthServiceImpl(DatabaseService dbService, AuditLogger auditLogger,
                       int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) 
    throws RemoteException {
    super(port, csf, ssf);
    this.dbService = dbService;        // Remote RMI reference to Database Tier
    this.auditLogger = auditLogger;    // Enhanced to handle RemoteException
}
```

### HRMServiceImpl Data Access

**BEFORE:**
```java
public synchronized boolean addFamilyMember(FamilyMember member, String sessionToken) 
    throws RemoteException {
    UserAccount user = requireAuth(sessionToken);
    // ... validation ...
    int id = dataStore.addFamilyMember(member);  // Local call
    auditLogger.log(user, "ADD_FAMILY_MEMBER", "family_members", id, ...);
    return true;
}
```

**AFTER:**
```java
public synchronized boolean addFamilyMember(FamilyMember member, String sessionToken) 
    throws RemoteException {
    UserAccount user = requireAuth(sessionToken);
    // ... validation ...
    int id = dbService.addFamilyMember(member);  // Remote RMI call
    auditLogger.log(user, "ADD_FAMILY_MEMBER", "family_members", id, ...);
    // Audit logging now happens on Database Server tier
    return true;
}
```

## Key Refactoring Changes

### 1. ServerMain.java (Application Server Entry Point)

**BEFORE:**
```java
// Create local data store
CSVDataStore dataStore = new CSVDataStore(dataDir);
seedDefaultData(dataStore);

// Create services with local access
AuthServiceImpl authService = new AuthServiceImpl(dataStore, auditLogger, ...);
HRMServiceImpl hrmService = new HRMServiceImpl(dataStore, authService, ...);
PRSServiceImpl prsService = new PRSServiceImpl(dataStore, authService, ...);

// Bind to registry
registry.rebind("AuthService", authService);
registry.rebind("HRMService", hrmService);
registry.rebind("PRSService", prsService);
```

**AFTER:**
```java
// Connect to Database Server (Tier 3)
String dbServiceURL = "rmi://" + dbHost + ":" + dbPort + "/DatabaseService";
DatabaseService dbService = (DatabaseService) java.rmi.Naming.lookup(dbServiceURL);

// Create services with remote data access
AuthServiceImpl authService = new AuthServiceImpl(dbService, auditLogger, ...);
HRMServiceImpl hrmService = new HRMServiceImpl(dbService, authService, ...);
PRSServiceImpl prsService = new PRSServiceImpl(dbService, authService, ...);

// Bind to registry
registry.rebind("AuthService", authService);
registry.rebind("HRMService", hrmService);
registry.rebind("PRSService", prsService);
```

### 2. AuditLogger.java

**BEFORE:**
```java
public class AuditLogger {
    private final CSVDataStore dataStore;

    public AuditLogger(CSVDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void log(UserAccount user, String action, String targetTable, 
                    int targetId, String details) {
        if (user != null) {
            dataStore.addAuditLog(user.getUserId(), user.getUsername(), ...);
            // Local method call
        }
    }
}
```

**AFTER:**
```java
public class AuditLogger {
    private final DatabaseService dbService;

    public AuditLogger(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public void log(UserAccount user, String action, String targetTable, 
                    int targetId, String details) {
        if (user != null) {
            try {
                dbService.logAudit(user.getUserId(), user.getUsername(), ...);
                // Remote RMI call with exception handling
            } catch (RemoteException e) {
                System.err.println("[AUDIT ERROR] Failed to log action: " + e.getMessage());
            }
        }
    }
}
```

### 3. Data Access Pattern

**BEFORE - Direct Local Access:**
```
+---------+
|dataStore| ← Direct method calls
+----+----+
     │
     ▼
+---------+
|CSV Files|
+---------+
```

**AFTER - Remote RMI Access:**
```
+-----+                               +---------+
|dbService| ← RMI call (serialized) →|dbService|
+----+────+   over network            +----+----+
                                           │
                                           ▼
                                      +---------+
                                      |CSVDataStore|
                                      +----+----+
                                           │
                                           ▼
                                      +---------+
                                      |CSV Files|
                                      +---------+
```

## New Interfaces

### NEW: DatabaseService (Remote Interface)

```java
public interface DatabaseService extends Remote {
    // User operations
    UserAccount getUserById(int userId) throws RemoteException;
    int addUser(UserAccount user) throws RemoteException;
    
    // Employee operations  
    Employee getEmployee(int employeeId) throws RemoteException;
    int addEmployee(Employee emp) throws RemoteException;
    List<Employee> searchEmployees(String query) throws RemoteException;
    
    // Leave operations
    List<LeaveBalance> getLeaveBalances(int employeeId, int year) throws RemoteException;
    int addLeaveApplication(LeaveApplication app) throws RemoteException;
    
    // Audit operations (NEW!)
    List<AuditLogEntry> getAuditLog() throws RemoteException;
    List<AuditLogEntry> getAuditLogForUser(int userId) throws RemoteException;
    List<AuditLogEntry> getAuditLogForTable(String tableName) throws RemoteException;
    void logAudit(int userId, String username, String role, String action,
                  String targetTable, int targetId, String details) throws RemoteException;
    
    // ... more methods
}
```

## Entry Points

### BEFORE
- Single entry point: `ServerMain.java`
- Starts on port 1099
- Creates CSVDataStore locally
- Seeds data locally
- No horizontal scaling

### AFTER
- **Two entry points:**
  - `DatabaseServer.java` on port 1098 (Tier 3)
    - Manages CSV files
    - Centralizes audit logging
    - Seeds default data
  - `ServerMain.java` on port 1099 (Tier 2)
    - Connects to DatabaseServer
    - Manages sessions
    - No local CSV file access

## File References

### Where dataStore was used

**BEFORE: 48+ places in HRMServiceImpl**
```
dataStore.getEmployee()
dataStore.addEmployee()
dataStore.updateEmployee()
dataStore.getFamilyMembers()
dataStore.addFamilyMember()
dataStore.getLeaveBalances()
dataStore.addLeaveApplication()
dataStore.getLeaveApplications()
... and 40+ more
```

**AFTER: Replaced with dbService**
```
dbService.getEmployee()      ← Remote call
dbService.addEmployee()      ← Remote call
dbService.updateEmployee()   ← Remote call
... all handled remotely
```

## Exception Handling Changes

### BEFORE
- Most methods didn't throw RemoteException
- Local file I/O exceptions wrapped

### AFTER
- All methods throw RemoteException
- Must handle network failures
- AuditLogger catches and logs RemoteException

Example:
```java
// BEFORE
private int id = dataStore.addFamily("name");

// AFTER
private int id = -1;
try {
    id = dbService.addFamilyMember(member);
} catch (RemoteException e) {
    // Handle network error
    System.err.println("Failed to add family member: " + e);
}
```

## Startup Sequence

### BEFORE
1. Client connects to Application Server (port 1099)
2. Application Server initializes CSVDataStore
3. CSVDataStore seeds default data
4. All ready

### AFTER
1. **Start Database Server (port 1098)**
   - Initialize CSVDataStore
   - Seed default data
   - Register DatabaseService on RMI registry
   - Wait for clients

2. **Start Application Server (port 1099)**
   - Look up DatabaseService on port 1098 (remote!)
   - Initialize AuthService, HRMService, PRSService
   - Register on RMI registry (port 1099)
   - Ready for clients

3. **Start Client**
   - Connect to port 1099
   - Interact normally

## Dataset - CSV Files

**Location BEFORE:**
- Same directory as running application
- Lost when app stops (if not managed)
- Not centralized

**Location AFTER:**
- Dedicated Database Server (port 1098)
- Can be on different machine
- Centralized audit logging in same location
- Survives Application Server restarts

## Audit Trail - NEW Feature

### BEFORE
- Minimal audit logging
- Only in AuditLogEntry model
- Not comprehensive

### AFTER
- **All operations logged automatically:**
  - User login/logout
  - Employee CRUD operations
  - Leave application submissions
  - Leave approvals/rejections
  - Profile update requests
  - Payroll record creation
  - User account management

- **Audit Log Format (CSV):**
  ```
  log_id,user_id,username,role,action,target_table,target_id,details,timestamp
  1,1,ahmad.ibrahim,EMPLOYEE,APPLY_FOR_LEAVE,leaves,567,Annual 5 days,2024-03-14T10:30:45.123456
  ```

- **Query Capabilities:**
  - Get all audit entries
  - Get entries for specific user
  - Get entries for specific table
  - All via remote DatabaseService

## Performance Impact

### BEFORE
- Single process: Low latency
- File I/O on same machine
- No network overhead

### AFTER
- RMI serialization: ~1-5ms overhead per call
- Network latency: ~1ms (localhost) or more (remote)
- Better for distributed deployments
- Can scale with multiple app servers

## Deployment Options

### BEFORE
```
One machine:
├─ Database files
├─ Application code
└─ GUI clients
```

### AFTER - Option 1: Single Machine (Development)
```
One machine:
├─ Database Server (port 1098)
├─ Application Server (port 1099)
└─ Multiple GUI Clients
```

### AFTER - Option 2: Multiple Machines (Production)
```
Database Server Machine:
├─ port 1098
├─ /data/ directory
└─ audit_log.csv

App Server Machine 1:
├─ port 1099
└─ connects to DB at 10.0.1.10:1098

App Server Machine 2:
├─ port 1099
└─ connects to DB at 10.0.1.10:1098

Many Client Machines:
├─ connect to load balancer
└─ routed to App Servers
```

## Summary of Changes

| Aspect | Before | After |
|--------|--------|-------|
| Tiers | 2 | 3 |
| Architecture | Monolithic | Distributed |
| Database Access | Local | Remote RMI |
| Audit Logging | Minimal | Comprehensive |
| Scalability | Single server | Multiple app servers |
| Data Tier | Embedded | Separate process |
| Fault Isolation | App + DB coupled | Separated |
| Deployment | Single process | Multiple processes |
| Network | None | RMI between tiers |
| CSV Files | Embedded location | Centralized |
| Audit Queries | Basic | Advanced (by user, table) |

---

**Document Purpose:** Aid developers in understanding the refactoring from monolithic to 3-tier architecture.
