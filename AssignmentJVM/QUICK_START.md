# Quick Start Guide - AssignmentJVM

## ✅ What You Have

A **simple 3-tier RMI system** with:
- **Database Server** (port 1099) - stores data
- **Application Server** (port 1100) - business logic
- **Clients** (HR & Employee) - user interfaces

**NO .bat files needed!** Just simple `java` commands.

## 🚀 Quick Test (One Machine)

### Open 4 terminals in: `C:\Users\alfie\Desktop\DCOMS_Assignment`

**Terminal 1 - Database Server:**
```powershell
java -cp AssignmentJVM database.DatabaseServer
```
Wait for: `Database Server is running on port 1099...`

**Terminal 2 - Application Server:**
```powershell
java -cp AssignmentJVM application.ApplicationServer
```
Wait for: `Application Server is running on port 1100`

**Terminal 3 - HR Client:**
```powershell
java -cp AssignmentJVM client.HRClient
```
Login:
- Username: `admin`
- Password: `admin123`

**Terminal 4 - Employee Client:**
```powershell
java -cp AssignmentJVM client.EmployeeClient
```
(Register an employee using HR Client first, then login here)

## 🌐 Across 3 Devices

### Device 1 (Database) - 192.168.1.10
```powershell
java -cp AssignmentJVM database.DatabaseServer
```

### Device 2 (Application) - 192.168.1.20
```powershell
java -Djava.rmi.server.hostname=192.168.1.20 -cp AssignmentJVM application.ApplicationServer 192.168.1.10
```

### Device 3 (Clients)
```powershell
# HR Client
java -cp AssignmentJVM client.HRClient 192.168.1.20

# OR Employee Client
java -cp AssignmentJVM client.EmployeeClient 192.168.1.20
```

## 🎯 Test Workflow

1. **Start servers** (Database → Application)
2. **Login as HR** (admin/admin123)
3. **Register employee** (e.g., username: john, password: pass123, role: EMPLOYEE)
4. **Open Employee Client** and login (john/pass123)
5. **Apply for leave** (e.g., 3 days)
6. **Back to HR Client** → Process leave (approve/reject)
7. **Check Employee Client** → View leave history

## 🔑 Default Credentials

- Username: `admin`
- Password: `admin123`
- Role: HR

## 💡 Comparison

| Your Current Assignment | This AssignmentJVM |
|------------------------|-------------------|
| 6 .bat files | **0 .bat files** |
| Prompts for IPs | **Command-line args** |
| CSV file operations | **In-memory (simpler)** |
| Complex setup | **3 simple commands** |

## 📝 If You See Errors

**"Could not find or load main class"**
```powershell
# Make sure you're in DCOMS_Assignment folder (not AssignmentJVM)
cd C:\Users\alfie\Desktop\DCOMS_Assignment
java -cp AssignmentJVM database.DatabaseServer
```

**"Connection refused"**
- Start Database Server FIRST
- Then Application Server
- Then Clients

**Need to recompile?**
```powershell
cd AssignmentJVM
javac models/*.java database/*.java application/*.java client/*.java
```

---

**That's it! Simple RMI communication across 3 tiers with NO complex scripts! 🎉**
