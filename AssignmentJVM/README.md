# Simple 3-Tier RMI HR System

A simplified Java RMI implementation using the straightforward pattern - **NO .bat files needed!**

## 📁 Structure

```
AssignmentJVM/
├── models/
│   ├── Employee.java          # Employee data model
│   └── Leave.java             # Leave data model
├── database/
│   ├── DatabaseInterface.java      # Remote interface
│   ├── DatabaseServerImpl.java     # Implementation
│   └── DatabaseServer.java         # Main class (starts registry)
├── application/
│   ├── ApplicationInterface.java   # Remote interface
│   ├── ApplicationServerImpl.java  # Implementation (business logic)
│   └── ApplicationServer.java      # Main class (starts registry)
└── client/
    ├── HRClient.java              # HR staff interface
    └── EmployeeClient.java        # Employee self-service
```

## 🚀 How It Works (Like Your Google Example)

**Database Server:**
- Creates `DatabaseInterface` service
- Starts RMI registry on port **1099**
- Binds service as "DatabaseService"

**Application Server:**
- Looks up DatabaseService from Database Server
- Creates `ApplicationInterface` service (business logic)
- Starts RMI registry on port **1100**
- Binds service as "ApplicationService"

**Clients:**
- Look up ApplicationService from Application Server
- Call remote methods (login, register, apply leave, etc.)

## 📝 Running on Single Machine (Testing)

### Step 1: Compile Everything
```powershell
cd C:\Users\alfie\Desktop\DCOMS_Assignment\AssignmentJVM
javac models/*.java database/*.java application/*.java client/*.java
```

### Step 2: Start Database Server (Terminal 1)
```powershell
java database.DatabaseServer
```
You should see:
```
===========================================
Database Server is running on port 1099...
Service name: DatabaseService
===========================================
```

### Step 3: Start Application Server (Terminal 2)
```powershell
java application.ApplicationServer
```
You should see:
```
Connected to Database Server!
===========================================
Application Server is running on port 1100
Service name: ApplicationService
Connected to DB at: localhost
===========================================
```

### Step 4: Start HR Client (Terminal 3)
```powershell
java client.HRClient
```
Login with:
- Username: `admin`
- Password: `admin123`

### Step 5: Start Employee Client (Terminal 4)
```powershell
java client.EmployeeClient
```
(First register an employee using HR Client, then login here)

## 🌐 Running Across 3 Devices

### Device 1 (Database Server) - IP: 192.168.1.10
```powershell
cd C:\Users\alfie\Desktop\DCOMS_Assignment\AssignmentJVM
javac models/*.java database/*.java application/*.java client/*.java
java database.DatabaseServer
```

### Device 2 (Application Server) - IP: 192.168.1.20
```powershell
cd C:\Users\alfie\Desktop\DCOMS_Assignment\AssignmentJVM
javac models/*.java database/*.java application/*.java client/*.java
java application.ApplicationServer 192.168.1.10
```
Note: Pass Database Server IP as argument

**Important for Device 2:** Add this JVM option:
```powershell
java -Djava.rmi.server.hostname=192.168.1.20 application.ApplicationServer 192.168.1.10
```

### Device 3 (Clients) - Any IP
```powershell
cd C:\Users\alfie\Desktop\DCOMS_Assignment\AssignmentJVM
javac models/*.java database/*.java application/*.java client/*.java

# HR Client
java client.HRClient 192.168.1.20

# OR Employee Client
java client.EmployeeClient 192.168.1.20
```
Note: Pass Application Server IP as argument

## 🔑 Default Login

**Admin Account (HR Role):**
- Username: `admin`
- Password: `admin123`

## ✨ Features

### HR Client
1. Register New Employee
2. View All Employees
3. View Pending Leave Applications
4. Process Leave (Approve/Reject)

### Employee Client
1. View My Profile
2. Update My Profile (email, department, position)
3. View Leave Balance
4. Apply for Leave
5. View My Leave History

## 🔧 Firewall Settings

If running across devices, allow these ports:
- **Port 1099** - Database Server
- **Port 1100** - Application Server

Windows Firewall:
```powershell
netsh advfirewall firewall add rule name="Java RMI 1099" dir=in action=allow protocol=TCP localport=1099
netsh advfirewall firewall add rule name="Java RMI 1100" dir=in action=allow protocol=TCP localport=1100
```

## 🐛 Troubleshooting

**"Connection refused"**
- Check servers are running (Database first, then Application)
- Check firewall settings
- Verify IP addresses are correct
- Use `-Djava.rmi.server.hostname=<your-ip>` on Application Server

**"Service not found"**
- Database Server must start before Application Server
- Application Server must start before Clients

**"Cannot compile"**
- Ensure you're in AssignmentJVM directory
- Check Java is installed: `java -version`

## 📊 Comparison to Original Assignment

| Original Assignment | Simplified AssignmentJVM |
|---------------------|-------------------------|
| 6 .bat files | **0 .bat files** |
| Complex config | **Simple command args** |
| 11 Java files | **10 Java files** |
| CSV file storage | **In-memory storage** |
| Multiple utilities | **Minimal dependencies** |
| Command-line args in .bat | **Direct command-line args** |

## 💡 Key Simplifications

1. **No .bat files** - Just use `javac` and `java` commands directly
2. **Command-line args** - Pass IPs as arguments instead of prompts
3. **In-memory storage** - Uses ConcurrentHashMap instead of CSV files
4. **Registry in server** - Uses `LocateRegistry.createRegistry()` instead of separate rmiregistry
5. **Simplified validation** - Basic checks only
6. **Auto-admin** - Creates admin user on Database Server startup

## 🎯 What You Learn

1. **RMI Basics** - Just like the Google example
2. **3-Tier Architecture** - Clear separation: Database → Application → Client
3. **Remote Interfaces** - Extends `java.rmi.Remote`
4. **UnicastRemoteObject** - Implementation pattern
5. **Registry Operations** - `createRegistry()`, `getRegistry()`, `lookup()`
6. **Network Communication** - Passing IPs between tiers

## 🔄 Workflow Example

1. **HR registers employee:**
   ```
   HRClient → ApplicationServer.registerEmployee()
            → DatabaseServer.saveEmployee()
            → Stored in memory
   ```

2. **Employee applies for leave:**
   ```
   EmployeeClient → ApplicationServer.applyLeave()
                 → Check balance
                 → DatabaseServer.saveLeave()
                 → Leave status: PENDING
   ```

3. **HR approves leave:**
   ```
   HRClient → ApplicationServer.processLeave()
           → DatabaseServer.updateLeave()
           → Deduct employee balance
           → Leave status: APPROVED
   ```

---

**This is as simple as RMI gets! No magic, no complex scripts - just pure Java RMI communication. 🚀**
