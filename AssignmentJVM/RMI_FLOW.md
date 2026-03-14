# RMI Communication Flow - AssignmentJVM

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      3-TIER ARCHITECTURE                        │
└─────────────────────────────────────────────────────────────────┘

   CLIENT TIER              APPLICATION TIER          DATABASE TIER
   (Device 3)               (Device 2)                (Device 1)

┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│              │          │              │          │              │
│  HRClient    │ ◄─RMI──► │ Application  │ ◄─RMI──► │  Database    │
│              │  :1100   │   Server     │  :1099   │   Server     │
│              │          │              │          │              │
└──────────────┘          └──────────────┘          └──────────────┘
                                 │                         │
┌──────────────┐                 │                         │
│              │                 │                         │
│  Employee    │ ◄───────────────┘                         │
│   Client     │                                           │
│              │                                           │
└──────────────┘                                           ▼
                                                  ┌─────────────────┐
  Multiple Clients                                │ ConcurrentHashMap│
  can connect                                     │  - Employees     │
                                                  │  - Leaves        │
                                                  └─────────────────┘
```

## 🔄 RMI Communication Pattern

### Step 1: Database Server Startup
```
DatabaseServer.main()
    │
    ├─► Create DatabaseServerImpl (extends UnicastRemoteObject)
    │
    ├─► LocateRegistry.createRegistry(1099)
    │
    └─► registry.rebind("DatabaseService", dbService)
        
        ✅ Database Server running on port 1099
```

### Step 2: Application Server Startup
```
ApplicationServer.main()
    │
    ├─► LocateRegistry.getRegistry("192.168.1.10", 1099)
    │
    ├─► lookup("DatabaseService") → DatabaseInterface
    │
    ├─► Create ApplicationServerImpl(databaseRef)
    │
    ├─► LocateRegistry.createRegistry(1100)
    │
    └─► registry.rebind("ApplicationService", appService)
        
        ✅ Application Server running on port 1100
        ✅ Connected to Database Server
```

### Step 3: Client Startup
```
HRClient.main()
    │
    ├─► LocateRegistry.getRegistry("192.168.1.20", 1100)
    │
    ├─► lookup("ApplicationService") → ApplicationInterface
    │
    └─► appService.authenticate("admin", "admin123")
        
        ✅ Client connected to Application Server
        ✅ Ready to call remote methods
```

## 📡 Example: Employee Registration Flow

```
┌─────────────┐
│  HR Client  │
└──────┬──────┘
       │
       │ 1. User enters employee data
       │
       ▼
   registerEmployee(firstName, lastName, ...)
       │
       │ RMI call over network
       │
       ▼
┌──────────────────────┐
│ Application Server   │
│ ApplicationServerImpl│
└──────┬───────────────┘
       │
       │ 2. Check username exists?
       │
       ▼
   database.getEmployeeByUsername(username)
       │
       │ RMI call over network
       │
       ▼
┌──────────────────────┐
│  Database Server     │
│  DatabaseServerImpl  │
└──────┬───────────────┘
       │
       │ 3. Query ConcurrentHashMap
       │
       ▼
   Return null (not found)
       │
       │ RMI return
       │
       ▼
┌──────────────────────┐
│ Application Server   │
└──────┬───────────────┘
       │
       │ 4. Generate ID, create Employee
       │
       ▼
   database.saveEmployee(newEmployee)
       │
       │ RMI call
       │
       ▼
┌──────────────────────┐
│  Database Server     │
└──────┬───────────────┘
       │
       │ 5. Store in ConcurrentHashMap
       │
       ▼
   employees.put(id, employee)
       │
       │ RMI return true
       │
       ▼
┌──────────────────────┐
│ Application Server   │
└──────┬───────────────┘
       │
       │ RMI return true
       │
       ▼
┌─────────────┐
│  HR Client  │
└──────┬──────┘
       │
       │ 6. Display success message
       │
       ▼
   "✓ Employee registered successfully!"
```

## 🌐 Network Communication

### Localhost (Single Machine)
```
Port 1099          Port 1100
    │                  │
    ▼                  ▼
Database ──────► Application ──────► Clients
(localhost)      (localhost)       (localhost)
```

### 3 Devices (Network)
```
192.168.1.10      192.168.1.20      192.168.1.30
Port 1099         Port 1100         (any)
    │                  │               │
    ▼                  ▼               ▼
Database ──────► Application ──────► Clients
(Device 1)       (Device 2)        (Device 3)
                                   (Multiple)
```

## 🔍 Deep Dive: RMI Registry

### Database Server Registry
```java
Registry registry = LocateRegistry.createRegistry(1099);
// Creates registry process on port 1099

registry.rebind("DatabaseService", databaseImpl);
// Binds remote object with name "DatabaseService"

// Now clients can lookup:
// LocateRegistry.getRegistry("host", 1099)
//   .lookup("DatabaseService")
```

### Application Server Registry
```java
Registry registry = LocateRegistry.createRegistry(1100);
// Creates registry process on port 1100

registry.rebind("ApplicationService", applicationImpl);
// Binds remote object with name "ApplicationService"

// Now clients can lookup:
// LocateRegistry.getRegistry("host", 1100)
//   .lookup("ApplicationService")
```

## 🎯 Method Call Chain

### Example: Apply for Leave

```
Employee Client
    │
    │ appService.applyLeave(empId, start, end, days, type, reason)
    ▼
Application Server (ApplicationServerImpl)
    │
    ├─► Check employee exists
    │   database.getEmployeeById(empId)
    │       └─► Database Server → returns Employee
    │
    ├─► Check leave balance
    │   if (employee.getBalance() < days) return false
    │
    ├─► Generate leave ID
    │   database.generateNextLeaveId()
    │       └─► Database Server → returns "LVE0001"
    │
    └─► Save leave
        database.saveLeave(newLeave)
            └─► Database Server → stores in HashMap
                    └─► returns true
                            └─► Application returns true
                                    └─► Client displays success
```

## 📊 Data Flow: Leave Approval

```
HR Client
    │
    │ processLeave(leaveId, "APPROVED")
    ▼
Application Server
    │
    ├─► Get leave by ID
    │   database.getLeaveById(leaveId)
    │       └─► Database Server → returns Leave object
    │
    ├─► Update leave status
    │   leave.setStatus("APPROVED")
    │   database.updateLeave(leave)
    │       └─► Database Server → updates HashMap
    │
    └─► Deduct employee balance
        database.getEmployeeById(leave.getEmployeeId())
            └─► Get employee
        employee.setLeaveBalance(balance - days)
        database.updateEmployee(employee)
            └─► Database Server → updates HashMap
                    └─► returns true
                            └─► Application returns true
                                    └─► HR Client displays "✓ Leave approved!"
```

## 🔒 Concurrency Handling

```
Multiple Clients → Application Server → Database Server
                                             ↓
                                    ConcurrentHashMap
                                    (Thread-safe)
                                             ↓
                                    Handles concurrent:
                                    - Reads (any number)
                                    - Writes (atomic)
```

## 🚀 Startup Sequence

```
1. Database Server
   ├─► Initialize ConcurrentHashMap
   ├─► Create admin user
   ├─► Start registry on 1099
   ├─► Bind DatabaseService
   └─► Wait for connections

2. Application Server
   ├─► Connect to Database Server (lookup)
   ├─► Create ApplicationServerImpl(db)
   ├─► Start registry on 1100
   ├─► Bind ApplicationService
   └─► Wait for client connections

3. Clients
   ├─► Connect to Application Server (lookup)
   ├─► Call authenticate()
   └─► Make remote method calls
```

## 💡 Key RMI Concepts

### 1. Remote Interface
```java
// Must extend Remote
public interface DatabaseInterface extends Remote {
    // All methods must throw RemoteException
    Employee getEmployee(String id) throws RemoteException;
}
```

### 2. Remote Object
```java
// Must extend UnicastRemoteObject
public class DatabaseServerImpl extends UnicastRemoteObject 
                                 implements DatabaseInterface {
    // Constructor must throw RemoteException
    protected DatabaseServerImpl() throws RemoteException {
        super(); // Exports object for remote calls
    }
}
```

### 3. Registry Operations
```java
// Server: Create and bind
Registry registry = LocateRegistry.createRegistry(1099);
registry.rebind("ServiceName", remoteObject);

// Client: Get and lookup
Registry registry = LocateRegistry.getRegistry("host", 1099);
Interface stub = (Interface) registry.lookup("ServiceName");
```

### 4. Stub and Skeleton
```
Client calls stub.method()
    ↓
Stub serializes call and sends to skeleton
    ↓
Skeleton deserializes and calls actual object
    ↓
Result serialized and returned to stub
    ↓
Stub deserializes and returns to client
```

---

**This is how RMI works under the hood! Simple, clean, and powerful. 🚀**
