# Quick Start Guide
BHEL HRM System:
1. **Database Server** (Tier 3) - Manages all data on port 1098
2. **Application Server** (Tier 2) - Business logic on port 1099
3. **Client GUI** (Tier 1) - User interface

### Step 1:BHEL_HRM directory
cd c:\path\to\BHEL_HRM

### Step 2: Compile (first time only)
.\compile.bat

You should see: `Build SUCCESSFUL`

### Step 3: Run Everything
.\run-all.bat

 opens 3 new windows:
- **Database Server** window (port 1098)
- **Application Server** window (port 1099)
- **Client GUI** window
The login screen should appear.


## Running Components Individually
### Terminal 1: Database Server
.\run-database.bat
### Terminal 2: Application Server
.\run-server.bat
### Terminal 3: Client GUI
.\run-client.bat


## Test Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | Administrator |
| `hr1` | `hr1234` | HR Staff |
| `ahmad.ibrahim` | `emp123` | Employee |

### Quick Test
1. Login as `ahmad.ibrahim` / `emp123`
   - View your profile
   - Apply for leave
   - Add family member

2. Logout, then login as `hr1` / `hr1234`  
   - Approve the leave application
   - Register new employee
   - View audit log

3. Logout, then login as `admin` / `admin123`
   - Manage users
   - View system audit logs

---

## Security Features

✅ **Audit Logging** - Every action is logged (who, what, when)
✅ **SSL/TLS** - Encrypted communication (optional)
✅ **Session Tokens** - Secure authentication
✅ **Password Hashing** - Passwords never stored in plain text

---

## Data Files

All data is stored in CSV format in the `data/` directory:

- `users.csv` - Login accounts
- `employees.csv` - Employee records
- `leaves.csv` - Leave applications
- `family_members.csv` - Employee family info
- `payroll_records.csv` - Salary records
- **`audit_log.csv`** - Complete activity log

### View the Audit Log
```batch
REM Display audit log (Windows)
type data\audit_log.csv
```

## Using SSL/TLS (Encrypted)
For secure communication:
### Step 1: Generate SSL certificates (one-time)
```batch
setup-ssl.bat
```

### Step 2: Start with SSL
```batch
run-all.bat /ssl
```

Or individually:
```batch
run-database.bat 1098 data /ssl
run-server.bat 1099 localhost 1098 /ssl
run-client.bat localhost 1099 /ssl
```

---

## Architecture Diagram

```
YOUR COMPUTER
├─ Database Server (port 1098)
│  └─ data/ (CSV files)
│
├─ Application Server (port 1099)
│  └─ connects to Database Server
│
└─ Client GUI
   └─ connects to Application Server
```

## Stopping Everything

Press `Ctrl+C` in each window to stop:
1. Client GUI
2. Application Server  
3. Database Server

---
## Quick Reference
.\compile.bat                                     # Compile code
.\run-all.bat                                     # Run all 3 tiers
.\run-all.bat /ssl                               # With SSL encryption
.\run-database.bat                                # Database Server only
.\run-server.bat                                  # Application Server only
.\run-client.bat                                  # Client GUI only
.\setup-ssl.bat                                   # Generate SSL certificates
