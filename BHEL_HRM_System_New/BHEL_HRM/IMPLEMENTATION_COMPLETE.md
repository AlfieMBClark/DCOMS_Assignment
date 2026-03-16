# BHEL HRM System — Implementation Summary
## Overview
A distributed Human Resource Management System built with Java RMI using a 3-tier architecture with fault tolerance. Handles employee registration, profiles, leave, payroll, and audit logging across multiple users.
---

## Architecture
| Tier | Component | Port | Purpose |
|------|-----------|------|---------|
| 3 | Database Server | 1098 | CSV data storage & replication |
| 2 | Application Server | 1099 | Business logic & authentication |
| 1 | Client GUI | — | Swing user interface |
**Fault Tolerance:** Primary database replicates writes to backup in real-time. Client auto-failsover if primary dies.
---

## Project Structure
```
src/
├── client/                      # GUI (Swing)
├── server/                      # Database & App tier
├── common/interfaces/           # RMI remote interfaces
├── common/models/               # Data objects
└── utils/                       # Validation & hashing

data/                            # CSV files
```
---

## Features by Role
**Employee:**
- View/update profile
- Manage family members
- Apply for leave
- View leave balance & payroll
**HR:**
- Register employees
- Approve/reject leave & profile updates
- Generate reports
- Generate payroll
**Admin:**
- Manage user accounts
- View audit logs

---
## Security
- **Authentication:** Username/password with SHA-256 salted hashing
- **Sessions:** UUID tokens for each login
- **Authorization:** Role-based access control
- **SSL/TLS:** Optional encrypted communication
- **Audit:** Every action logged with timestamps
---

## How to Run

**Single Laptop:**
```batch
.\compile.bat
.\run-all.bat
```

**Multi-Laptop (with failover):**
```
Edit IP addresses in bat files first, then:
Laptop B: .\run-backup.bat
Laptop A: .\run-all-primary.bat
```

## Test Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Admin |
| hr1 | hr1234 | HR |
| testemp1 | emp123 | Employee |

---

## Notes

- **26 Java files, ~4,000 lines**
- **CSV-based storage** (no external DB needed)
- **Dynamic proxy failover** (transparent to UI)
- **Fire-and-forget replication** (async, non-blocking)
