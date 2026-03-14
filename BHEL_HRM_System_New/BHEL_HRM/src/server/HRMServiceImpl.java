package server;

import common.interfaces.HRMService;
import common.models.*;
import utils.ValidationUtil;
import utils.PasswordHasher;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the HRM Service.
 * All write operations are synchronized for thread-safe concurrent access.
 * Supports SSL/TLS when socket factories are provided.
 */
public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {

    private final CSVDataStore dataStore;
    private final AuthServiceImpl authService;
    private final AuditLogger auditLogger;

    public HRMServiceImpl(CSVDataStore dataStore, AuthServiceImpl authService,
                          AuditLogger auditLogger, int port,
                          RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
        this.dataStore = dataStore;
        this.authService = authService;
        this.auditLogger = auditLogger;
    }

    // ==================== SESSION VALIDATION ====================

    private UserAccount requireAuth(String token) throws RemoteException {
        UserAccount user = authService.validateSession(token);
        if (user == null) throw new RemoteException("Unauthorized: Invalid or expired session");
        return user;
    }

    private UserAccount requireRole(String token, String... roles) throws RemoteException {
        UserAccount user = requireAuth(token);
        for (String role : roles) {
            if (user.getRole().equals(role)) return user;
        }
        throw new RemoteException("Forbidden: Insufficient permissions. Required: " + String.join("/", roles));
    }

    // ==================== EMPLOYEE SELF-SERVICE ====================

    @Override
    public Employee getProfile(int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId)
            throw new RemoteException("Forbidden: Cannot view another employee's profile");
        Employee emp = dataStore.getEmployee(employeeId);
        if (emp == null) throw new RemoteException("Employee not found: " + employeeId);
        return emp;
    }

    @Override
    public synchronized boolean requestProfileUpdate(int employeeId, String fieldName,
                                                      String oldValue, String newValue, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId)
            throw new RemoteException("Forbidden");
        if ("email".equals(fieldName) && !ValidationUtil.validateEmail(newValue))
            throw new RemoteException("Invalid email format");
        if ("phone".equals(fieldName) && !ValidationUtil.validatePhone(newValue))
            throw new RemoteException("Invalid phone format");

        int requestId = dataStore.addProfileUpdateRequest(employeeId, fieldName, oldValue, newValue);
        auditLogger.log(user, "REQUEST_PROFILE_UPDATE", "employees", employeeId,
            fieldName + ": '" + oldValue + "' -> '" + newValue + "'");
        return true;
    }

    @Override
    public List<FamilyMember> getFamilyMembers(int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId)
            throw new RemoteException("Forbidden");
        return dataStore.getFamilyMembers(employeeId);
    }

    @Override
    public synchronized boolean addFamilyMember(FamilyMember member, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != member.getEmployeeId())
            throw new RemoteException("Forbidden");
        if (!ValidationUtil.isNotEmpty(member.getName()))
            throw new RemoteException("Family member name is required");
        if (!ValidationUtil.isNotEmpty(member.getRelationship()))
            throw new RemoteException("Relationship is required");

        int id = dataStore.addFamilyMember(member);
        auditLogger.log(user, "ADD_FAMILY_MEMBER", "family_members", id,
            member.getName() + " (" + member.getRelationship() + ") for emp#" + member.getEmployeeId());
        return true;
    }

    @Override
    public synchronized boolean updateFamilyMember(FamilyMember member, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != member.getEmployeeId())
            throw new RemoteException("Forbidden");
        boolean success = dataStore.updateFamilyMember(member);
        if (success) auditLogger.log(user, "UPDATE_FAMILY_MEMBER", "family_members", member.getFamilyId(), "Updated: " + member.getName());
        return success;
    }

    @Override
    public synchronized boolean removeFamilyMember(int familyId, int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId)
            throw new RemoteException("Forbidden");
        boolean success = dataStore.removeFamilyMember(familyId, employeeId);
        if (success) auditLogger.log(user, "REMOVE_FAMILY_MEMBER", "family_members", familyId, "Removed family member #" + familyId);
        return success;
    }

    @Override
    public List<LeaveBalance> getLeaveBalances(int employeeId, int year, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId)
            throw new RemoteException("Forbidden");
        List<LeaveBalance> balances = dataStore.getLeaveBalances(employeeId, year);
        if (balances.isEmpty()) {
            dataStore.initializeLeaveBalances(employeeId, year);
            balances = dataStore.getLeaveBalances(employeeId, year);
        }
        return balances;
    }

    @Override
    public synchronized boolean applyForLeave(LeaveApplication application, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != application.getEmployeeId())
            throw new RemoteException("Forbidden");
        if (!ValidationUtil.validateDate(application.getStartDate()) || !ValidationUtil.validateDate(application.getEndDate()))
            throw new RemoteException("Invalid date format. Use YYYY-MM-DD");
        if (!ValidationUtil.validateLeaveDays(application.getDaysRequested()))
            throw new RemoteException("Invalid number of leave days");

        int year = Integer.parseInt(application.getStartDate().substring(0, 4));
        List<LeaveBalance> balances = dataStore.getLeaveBalances(application.getEmployeeId(), year);
        LeaveBalance balance = balances.stream()
            .filter(lb -> lb.getLeaveType().equals(application.getLeaveType()))
            .findFirst().orElse(null);
        if (balance == null)
            throw new RemoteException("No leave balance found for type: " + application.getLeaveType());
        if (balance.getRemainingDays() < application.getDaysRequested())
            throw new RemoteException("Insufficient balance. Remaining: " + balance.getRemainingDays());

        application.setStatus("PENDING");
        int leaveId = dataStore.addLeaveApplication(application);
        auditLogger.log(user, "APPLY_LEAVE", "leave_applications", leaveId,
            application.getLeaveType() + ": " + application.getStartDate() + " to " + application.getEndDate());
        return true;
    }

    @Override
    public List<LeaveApplication> getMyLeaveApplications(int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId)
            throw new RemoteException("Forbidden");
        return dataStore.getLeaveApplicationsByEmployee(employeeId);
    }

    // ==================== HR STAFF OPERATIONS ====================

    @Override
    public synchronized int registerEmployee(Employee emp, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        if (!ValidationUtil.isNotEmpty(emp.getFirstName()))
            throw new RemoteException("First name is required");
        if (!ValidationUtil.isNotEmpty(emp.getLastName()))
            throw new RemoteException("Last name is required");
        if (!ValidationUtil.validateIcOrPassport(emp.getIcPassport()))
            throw new RemoteException("Invalid IC/Passport format");
        if (emp.getEmail() != null && !ValidationUtil.validateEmail(emp.getEmail()))
            throw new RemoteException("Invalid email format");

        int empId = dataStore.addEmployee(emp);
        if (empId == -1)
            throw new RemoteException("Duplicate IC/Passport number");

        // Auto-create employee user account
        String defaultPassword = emp.getIcPassport().replaceAll("-", "");
        String hash = PasswordHasher.hashPassword(defaultPassword);
        String username = emp.getFirstName().toLowerCase() + "." + emp.getLastName().toLowerCase();
        int suffix = 1;
        String base = username;
        while (dataStore.getUserByUsername(username) != null) username = base + suffix++;
        dataStore.addUser(new UserAccount(0, username, hash, "EMPLOYEE", empId, true));

        auditLogger.log(user, "REGISTER_EMPLOYEE", "employees", empId,
            emp.getFullName() + " (IC: " + emp.getIcPassport() + "), user: " + username);
        return empId;
    }

    @Override
    public List<Employee> searchEmployees(String query, String sessionToken) throws RemoteException {
        requireRole(sessionToken, "HR", "ADMIN");
        return dataStore.searchEmployees(query);
    }

    @Override
    public List<Employee> getAllEmployees(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "HR", "ADMIN");
        return dataStore.getAllEmployees();
    }

    @Override
    public List<String[]> getPendingProfileUpdates(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "HR", "ADMIN");
        return dataStore.getPendingProfileUpdates();
    }

    @Override
    public synchronized boolean approveProfileUpdate(int requestId, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        String[] request = dataStore.getProfileUpdateRequest(requestId);
        if (request == null) throw new RemoteException("Request not found");

        int employeeId = Integer.parseInt(request[1].trim());
        String fieldName = request[2].trim();
        String newValue = request[4].trim();

        Employee emp = dataStore.getEmployee(employeeId);
        if (emp == null) throw new RemoteException("Employee not found");

        switch (fieldName.toLowerCase()) {
            case "email": emp.setEmail(newValue); break;
            case "phone": emp.setPhone(newValue); break;
            case "department": emp.setDepartment(newValue); break;
            case "position": emp.setPosition(newValue); break;
            case "firstname": emp.setFirstName(newValue); break;
            case "lastname": emp.setLastName(newValue); break;
            default: throw new RemoteException("Unknown field: " + fieldName);
        }
        dataStore.updateEmployee(emp);
        dataStore.updateProfileRequest(requestId, "APPROVED", user.getUserId());
        auditLogger.log(user, "APPROVE_PROFILE_UPDATE", "profile_updates", requestId,
            "emp#" + employeeId + ": " + fieldName + " = " + newValue);
        return true;
    }

    @Override
    public synchronized boolean rejectProfileUpdate(int requestId, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        dataStore.updateProfileRequest(requestId, "REJECTED", user.getUserId());
        auditLogger.log(user, "REJECT_PROFILE_UPDATE", "profile_updates", requestId, "Rejected #" + requestId);
        return true;
    }

    @Override
    public List<LeaveApplication> getPendingLeaveApplications(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "HR", "ADMIN");
        List<LeaveApplication> pending = dataStore.getPendingLeaveApplications();
        for (LeaveApplication la : pending) {
            Employee emp = dataStore.getEmployee(la.getEmployeeId());
            if (emp != null) la.setEmployeeName(emp.getFullName());
        }
        return pending;
    }

    @Override
    public synchronized boolean approveLeave(int leaveId, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        LeaveApplication app = dataStore.getLeaveApplication(leaveId);
        if (app == null) throw new RemoteException("Leave application not found");
        if (!"PENDING".equals(app.getStatus())) throw new RemoteException("Already processed");

        int year = Integer.parseInt(app.getStartDate().substring(0, 4));
        if (!dataStore.updateLeaveBalance(app.getEmployeeId(), app.getLeaveType(), year, app.getDaysRequested()))
            throw new RemoteException("Failed to update leave balance");

        app.setStatus("APPROVED");
        app.setReviewedBy(user.getUserId());
        app.setReviewDate(LocalDateTime.now().toString());
        dataStore.updateLeaveApplication(app);

        Employee emp = dataStore.getEmployee(app.getEmployeeId());
        auditLogger.log(user, "APPROVE_LEAVE", "leave_applications", leaveId,
            "Approved " + app.getLeaveType() + " for " + (emp != null ? emp.getFullName() : "emp#" + app.getEmployeeId()));
        return true;
    }

    @Override
    public synchronized boolean rejectLeave(int leaveId, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        LeaveApplication app = dataStore.getLeaveApplication(leaveId);
        if (app == null) throw new RemoteException("Leave application not found");
        if (!"PENDING".equals(app.getStatus())) throw new RemoteException("Already processed");

        app.setStatus("REJECTED");
        app.setReviewedBy(user.getUserId());
        app.setReviewDate(LocalDateTime.now().toString());
        dataStore.updateLeaveApplication(app);
        auditLogger.log(user, "REJECT_LEAVE", "leave_applications", leaveId, "Rejected leave #" + leaveId);
        return true;
    }

    @Override
    public YearlyReport generateYearlyReport(int employeeId, int year, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        Employee emp = dataStore.getEmployee(employeeId);
        if (emp == null) throw new RemoteException("Employee not found");

        YearlyReport report = new YearlyReport(emp,
            dataStore.getFamilyMembers(employeeId),
            dataStore.getLeaveBalances(employeeId, year),
            dataStore.getLeaveApplicationsByYear(employeeId, year),
            year, LocalDateTime.now().toString());

        auditLogger.log(user, "GENERATE_REPORT", "employees", employeeId,
            "Yearly report for " + emp.getFullName() + " (" + year + ")");
        return report;
    }

    // ==================== ADMIN OPERATIONS ====================

    @Override
    public List<UserAccount> getAllUsers(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "ADMIN");
        return dataStore.getAllUsers();
    }

    @Override
    public synchronized boolean addUser(String username, String password, String role,
                                         int employeeId, String sessionToken) throws RemoteException {
        UserAccount admin = requireRole(sessionToken, "ADMIN");
        if (!ValidationUtil.validateUsername(username))
            throw new RemoteException("Invalid username. Use 3-30 chars: letters, digits, dots, hyphens.");
        if (!ValidationUtil.validatePassword(password))
            throw new RemoteException("Password must be at least 6 characters.");
        if (dataStore.getUserByUsername(username) != null)
            throw new RemoteException("Username already exists: " + username);

        String hash = PasswordHasher.hashPassword(password);
        int userId = dataStore.addUser(new UserAccount(0, username, hash, role, employeeId, true));
        auditLogger.log(admin, "ADD_USER", "users", userId, "Created user: " + username + " [" + role + "]");
        return true;
    }

    @Override
    public synchronized boolean updateUser(int userId, String username, String role,
                                            boolean isActive, String sessionToken) throws RemoteException {
        UserAccount admin = requireRole(sessionToken, "ADMIN");
        UserAccount target = dataStore.getUserById(userId);
        if (target == null) throw new RemoteException("User not found");
        target.setUsername(username);
        target.setRole(role);
        target.setActive(isActive);
        boolean success = dataStore.updateUser(target);
        if (success) auditLogger.log(admin, "UPDATE_USER", "users", userId,
            "Updated: " + username + " [" + role + "] active=" + isActive);
        return success;
    }

    @Override
    public synchronized boolean removeUser(int userId, String sessionToken) throws RemoteException {
        UserAccount admin = requireRole(sessionToken, "ADMIN");
        boolean success = dataStore.removeUser(userId);
        if (success) auditLogger.log(admin, "DEACTIVATE_USER", "users", userId, "Deactivated user #" + userId);
        return success;
    }

    @Override
    public List<AuditLogEntry> getAuditLog(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "ADMIN");
        return dataStore.getAuditLog();
    }
}
