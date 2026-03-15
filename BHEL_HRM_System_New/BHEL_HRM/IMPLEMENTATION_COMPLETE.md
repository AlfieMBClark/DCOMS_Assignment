# BHEL HRM System — Implementation Summary

## What We Built

We developed a distributed Human Resource Management System for BHEL using Java RMI, structured as a **3-tier architecture** with **primary-backup fault tolerance**. The system handles employee registration, profile management, leave applications, payroll, and audit logging — all accessible concurrently by multiple users over the network.

---

## Architecture Overview

We split the system into three independent tiers, each running as its own Java process:

| Tier | Component | Port | Responsibility |
|------|-----------|------|----------------|
| Tier 3 | Database Server | 1098 | CSV data persistence, audit logging, replication |
| Tier 2 | Application Server | 1099 | Business logic, authentication, session management |
| Tier 1 | Client GUI | — | Swing-based user interface |

The tiers communicate over RMI. The Application Server never touches CSV files directly — it calls the Database Server remotely for every read and write operation. This separation means we can deploy each tier on a different machine, and the database layer can be swapped out (e.g., to MySQL) without touching the business logic or client code.

### Fault Tolerance

We implemented a primary-backup replication model at the database tier. The primary Database Server forwards every write operation to a backup Database Server running on a separate machine. If the primary goes down, the client automatically detects the failure and switches to the backup's Application Server. The user sees a popup notification and is asked to re-login. All data created before the failure is preserved on the backup because it was replicated in real-time.

```
Laptop A (Primary)                      Laptop B (Backup)
├── Database Server (1098)              ├── Database Server (2098)
│   └── data/                           │   └── data_backup/
│   └── replicates writes ──────────→   │
│                                       │
├── Application Server (1099)           ├── Application Server (2099)
│                                       │
└── Client (optional)                   └── Client (failover enabled)
                                            └── connects to A first
                                            └── switches to B if A dies
```

---

## Project Structure

```
BHEL_HRM/
├── src/
│   ├── client/                          # Tier 1 — GUI
│   │   ├── ClientMain.java              # Entry point, failover proxies, dark theme
│   │   ├── LoginPanel.java              # Authentication screen
│   │   ├── EmployeePanel.java           # Employee self-service dashboard
│   │   ├── HRPanel.java                 # HR staff dashboard
│   │   └── AdminPanel.java              # Admin dashboard
│   │
│   ├── server/                          # Tier 2 & 3 — Server-side
│   │   ├── DatabaseServer.java          # Tier 3 entry point (with replication)
│   │   ├── DatabaseServiceImpl.java     # Remote database operations + replication receiver
│   │   ├── CSVDataStore.java            # CSV file I/O with replication hooks
│   │   ├── ServerMain.java              # Tier 2 entry point (connects to DB server)
│   │   ├── AuthServiceImpl.java         # Authentication and session management
│   │   ├── HRMServiceImpl.java          # HR business logic
│   │   ├── PRSServiceImpl.java          # Payroll business logic (SSL-secured)
│   │   ├── AuditLogger.java             # Audit logging utility
│   │   └── SSLRMISocketFactory.java     # Custom SSL socket factories
│   │
│   ├── common/
│   │   ├── interfaces/                  # RMI remote interfaces
│   │   │   ├── DatabaseService.java     # Database operations + replication endpoints
│   │   │   ├── AuthService.java         # Login, logout, password change
│   │   │   ├── HRMService.java          # Employee, leave, family, reporting
│   │   │   └── PRSService.java          # Payroll operations
│   │   │
│   │   └── models/                      # Serializable data objects
│   │       ├── Employee.java
│   │       ├── UserAccount.java
│   │       ├── LeaveApplication.java
│   │       ├── LeaveBalance.java
│   │       ├── FamilyMember.java
│   │       ├── PayrollRecord.java
│   │       ├── AuditLogEntry.java
│   │       └── YearlyReport.java
│   │
│   └── utils/
│       ├── PasswordHasher.java          # SHA-256 salted hashing
│       └── ValidationUtil.java          # IC/Passport, email, phone validation
│
├── data/                                # CSV data files (auto-created)
├── run-all.bat                          # Single laptop: all 3 tiers
├── run-all-primary.bat                  # Primary laptop with replication
├── run-backup.bat                       # Backup laptop with failover client
├── run-client-only.bat                  # Plain client for extra laptops
├── compile.bat                          # Build script
└── setup-ssl.bat                        # SSL certificate generator
```

**Total: 26 Java files, ~4,000 lines of code**

---

## Key Design Decisions

### Why 3-Tier Instead of 2-Tier

Our original implementation had the Application Server accessing CSV files directly. This worked but meant the business logic and data layer were tightly coupled — you couldn't move the database to another machine without rewriting the server. By introducing the Database Server as a separate RMI service, we achieved proper separation of concerns. The Application Server only knows about the `DatabaseService` interface, not how or where data is stored.

### Why CSV Over SQL

The assignment allows any storage mechanism. We chose CSV because it keeps the project self-contained with no external dependencies (no MySQL installation needed). The `CSVDataStore` class abstracts all file operations behind clean method signatures, so replacing it with a JDBC-based implementation would only require changing one class.

### Why Dynamic Proxies for Failover

The client wraps all three remote service references (AuthService, HRMService, PRSService) in Java dynamic proxies. Every remote method call passes through a `FailoverHandler` that catches connection errors and transparently reconnects to the backup server. This approach means we didn't have to modify any of the panel classes (EmployeePanel, HRPanel, AdminPanel, LoginPanel) — failover is completely invisible to the UI layer.

### Why Fire-and-Forget Replication

The primary Database Server replicates writes to the backup asynchronously. If the backup is temporarily unreachable, the primary logs a warning and continues operating normally. We chose this over synchronous replication because blocking the primary on every write would degrade performance and create a single point of failure — if the backup is slow or down, the primary shouldn't be affected.

---

## What Each Role Can Do

### Employee
- View and request updates to their profile
- Manage family member details (add, edit, remove)
- View leave balance by year
- Apply for leave (annual, medical, emergency)
- Track leave application status
- View payroll stubs

### HR Staff
- Register new employees (auto-creates their login account)
- Search and view all employee records
- Approve or reject profile update requests
- Approve or reject leave applications
- Generate yearly reports (profile + family + leave history)
- Generate payroll records

### Admin
- Manage user accounts (add, update, deactivate)
- View complete system audit log

---

## Security Implementation

We addressed the assignment's secure communication requirement through several layers:

**Authentication:** Users authenticate with username and password. Passwords are hashed using SHA-256 with a random 16-byte salt before storage. The server returns a UUID session token on successful login, which the client includes in every subsequent request.

**Authorization:** Every service method validates the session token and checks the user's role before proceeding. Employees can only access their own data. HR and Admin operations require the appropriate role.

**SSL/TLS:** The system supports optional SSL/TLS encryption for all RMI communication. Running `setup-ssl.bat` generates a self-signed certificate, keystore, and truststore. When started with the `/ssl` flag, all data transmitted between client, application server, and database server is encrypted. This specifically satisfies the assignment requirement for secure communication between the employee/HR and the PRS (Payroll System).

**Audit Logging:** Every action in the system is recorded in `audit_log.csv` with the user ID, username, role, action type, target table, target record ID, details, and timestamp. This provides a complete trail for accountability.

---

## Concurrency Handling

The system supports multiple simultaneous users through several mechanisms:

- **RMI Threading:** Java RMI dispatches each incoming remote call to a separate thread, so multiple clients are served concurrently without blocking each other.
- **Synchronized Methods:** All write operations in `CSVDataStore` and `DatabaseServiceImpl` are synchronized to prevent race conditions when multiple users modify shared data.
- **ConcurrentHashMap:** Active sessions are stored in a `ConcurrentHashMap` for thread-safe session management without locking on reads.

---

## How to Run

### Single Laptop (No Fault Tolerance)

```batch
.\compile.bat
.\run-all.bat
```

### Multi-Laptop (With Fault Tolerance)

Edit the IP addresses in the bat files first (see QUICKSTART.md for details), then:

```
Laptop B:  .\run-backup.bat           # Start backup first
Laptop A:  .\run-all-primary.bat      # Then start primary
Laptop C:  .\run-client-only.bat      # Optional extra client
```

Kill Laptop A to trigger failover on Laptop B's client.

### Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Administrator |
| hr1 | hr1234 | HR Staff |
| ahmad.ibrahim | emp123 | Employee |

---

## Technologies Used

| Technology | Purpose |
|-----------|---------|
| Java RMI | Distributed communication between all tiers |
| Java Swing | GUI client with dark theme |
| SSL/TLS (SslRMISocketFactory) | Encrypted RMI communication |
| SHA-256 with salt | Password hashing |
| CSV file I/O (java.nio) | Data persistence |
| Java Dynamic Proxies | Transparent client failover |
| ConcurrentHashMap | Thread-safe session storage |
| Synchronized blocks | Concurrent write protection |
| UUID | Session token generation |
| Regex validation | IC/Passport, email, phone format checking |

---

## Limitations and Future Work

We're aware of several limitations in the current implementation that would be worth addressing in a production system:

- **CSV performance:** Reading the entire file for every query works for this scale but would need to be replaced with a proper database (MySQL/PostgreSQL) for larger datasets.
- **Session replication:** Sessions are stored in-memory on the Application Server. After failover, users must re-login because the backup server has its own independent session store. Shared session storage (e.g., Redis) would solve this.
- **Replication gap:** If the primary goes down mid-write before replication completes, the last operation may be lost. Write-ahead logging would address this.
- **Single backup:** The current design supports one backup. A production system would benefit from multiple replicas or a consensus protocol.

These are discussed in more detail in the report's Future Enhancement section.
