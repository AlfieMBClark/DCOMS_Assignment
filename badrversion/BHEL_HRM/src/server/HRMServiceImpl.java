package server;

import common.interfaces.HRMService;
import common.models.*;
import utils.ValidationUtil;
import utils.PasswordHasher;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the HRM Service.
 * All write operations are synchronized to handle concurrent access safely.
 * Multi-threading is handled by the RMI runtime (thread per client request).
 */
public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {

    private final CSVDataStore dataStore;
    private final AuthServiceImpl authService;
    private final AuditLogger auditLogger;

    public HRMServiceImpl(CSVDataStore dataStore, AuthServiceImpl authService,
                          AuditLogger auditLogger) throws RemoteException {
        super();
        this.dataStore = dataStore;
        this.authService = authService;
        this.auditLogger = auditLogger;
    }

    // ==================== HELPER: SESSION VALIDATION ====================

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
        // Employees can only view their own profile, HR/Admin can view any
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden: Cannot view another employee's profile");
        }
        Employee emp = dataStore.getEmployee(employeeId);
        if (emp == null) throw new RemoteException("Employee not found: " + employeeId);
        return emp;
    }

    @Override
    public synchronized boolean requestProfileUpdate(int employeeId, String fieldName,
                                                      String oldValue, String newValue, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden: Cannot update another employee's profile");
        }

        // Validate the new value based on field name
        if ("email".equals(fieldName) && !ValidationUtil.validateEmail(newValue)) {
            throw new RemoteException("Invalid email format");
        }
        if ("phone".equals(fieldName) && !ValidationUtil.validatePhone(newValue)) {
            throw new RemoteException("Invalid phone format");
        }

        int requestId = dataStore.addProfileUpdateRequest(employeeId, fieldName, oldValue, newValue);
        auditLogger.log(user, "REQUEST_PROFILE_UPDATE", "employees", employeeId,
            "Requested update: " + fieldName + " from '" + oldValue + "' to '" + newValue + "'");
        System.out.println("[HRM] Profile update request #" + requestId + " created by " + user.getUsername());
        return true;
    }

    @Override
    public List<FamilyMember> getFamilyMembers(int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden: Cannot view another employee's family details");
        }
        return dataStore.getFamilyMembers(employeeId);
    }

    @Override
    public synchronized boolean addFamilyMember(FamilyMember member, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != member.getEmployeeId()) {
            throw new RemoteException("Forbidden: Cannot modify another employee's family details");
        }

        if (!ValidationUtil.isNotEmpty(member.getName())) {
            throw new RemoteException("Family member name is required");
        }
        if (!ValidationUtil.isNotEmpty(member.getRelationship())) {
            throw new RemoteException("Relationship is required");
        }

        int id = dataStore.addFamilyMember(member);
        auditLogger.log(user, "ADD_FAMILY_MEMBER", "family_members", id,
            "Added " + member.getName() + " (" + member.getRelationship() + ") for employee " + member.getEmployeeId());
        return true;
    }

    @Override
    public synchronized boolean updateFamilyMember(FamilyMember member, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != member.getEmployeeId()) {
            throw new RemoteException("Forbidden");
        }
        boolean success = dataStore.updateFamilyMember(member);
        if (success) {
            auditLogger.log(user, "UPDATE_FAMILY_MEMBER", "family_members", member.getFamilyId(),
                "Updated family member: " + member.getName());
        }
        return success;
    }

    @Override
    public synchronized boolean removeFamilyMember(int familyId, int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden");
        }
        boolean success = dataStore.removeFamilyMember(familyId, employeeId);
        if (success) {
            auditLogger.log(user, "REMOVE_FAMILY_MEMBER", "family_members", familyId,
                "Removed family member #" + familyId);
        }
        return success;
    }

    @Override
    public List<LeaveBalance> getLeaveBalances(int employeeId, int year, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden");
        }
        List<LeaveBalance> balances = dataStore.getLeaveBalances(employeeId, year);
        // Initialize if not found for this year
        if (balances.isEmpty()) {
            dataStore.initializeLeaveBalances(employeeId, year);
            balances = dataStore.getLeaveBalances(employeeId, year);
        }
        return balances;
    }

    @Override
    public synchronized boolean applyForLeave(LeaveApplication application, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != application.getEmployeeId()) {
            throw new RemoteException("Forbidden");
        }

        // Validate dates
        if (!ValidationUtil.validateDate(application.getStartDate()) ||
            !ValidationUtil.validateDate(application.getEndDate())) {
            throw new RemoteException("Invalid date format. Use YYYY-MM-DD");
        }
        if (!ValidationUtil.validateLeaveDays(application.getDaysRequested())) {
            throw new RemoteException("Invalid number of leave days");
        }

        // Check leave balance
        int year = Integer.parseInt(application.getStartDate().substring(0, 4));
        List<LeaveBalance> balances = dataStore.getLeaveBalances(application.getEmployeeId(), year);
        LeaveBalance balance = balances.stream()
            .filter(lb -> lb.getLeaveType().equals(application.getLeaveType()))
            .findFirst().orElse(null);

        if (balance == null) {
            throw new RemoteException("No leave balance found for type: " + application.getLeaveType());
        }
        if (balance.getRemainingDays() < application.getDaysRequested()) {
            throw new RemoteException("Insufficient leave balance. Remaining: " + balance.getRemainingDays()
                + ", Requested: " + application.getDaysRequested());
        }

        application.setStatus("PENDING");
        int leaveId = dataStore.addLeaveApplication(application);
        auditLogger.log(user, "APPLY_LEAVE", "leave_applications", leaveId,
            application.getLeaveType() + " leave: " + application.getStartDate()
            + " to " + application.getEndDate() + " (" + application.getDaysRequested() + " days)");
        System.out.println("[HRM] Leave application #" + leaveId + " submitted");
        return true;
    }

    @Override
    public List<LeaveApplication> getMyLeaveApplications(int employeeId, String sessionToken) throws RemoteException {
        UserAccount user = requireAuth(sessionToken);
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden");
        }
        return dataStore.getLeaveApplicationsByEmployee(employeeId);
    }

    // ==================== HR STAFF OPERATIONS ====================

    @Override
    public synchronized int registerEmployee(Employee emp, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");

        // Validate required fields
        if (!ValidationUtil.isNotEmpty(emp.getFirstName())) {
            throw new RemoteException("First name is required");
        }
        if (!ValidationUtil.isNotEmpty(emp.getLastName())) {
            throw new RemoteException("Last name is required");
        }
        if (!ValidationUtil.validateIcOrPassport(emp.getIcPassport())) {
            throw new RemoteException("Invalid IC/Passport format. IC: YYMMDD-PB-#### or 12 digits. Passport: Letter + 6-8 chars");
        }
        if (emp.getEmail() != null && !ValidationUtil.validateEmail(emp.getEmail())) {
            throw new RemoteException("Invalid email format");
        }

        int empId = dataStore.addEmployee(emp);
        if (empId == -1) {
            throw new RemoteException("Duplicate IC/Passport number. Employee already exists.");
        }

        // Auto-create employee user account
        String defaultPassword = emp.getIcPassport().replaceAll("-", "");
        String hash = PasswordHasher.hashPassword(defaultPassword);
        String username = emp.getFirstName().toLowerCase() + "." + emp.getLastName().toLowerCase();
        // Ensure unique username
        int suffix = 1;
        String baseUsername = username;
        while (dataStore.getUserByUsername(username) != null) {
            username = baseUsername + suffix++;
        }
        UserAccount newUser = new UserAccount(0, username, hash, "EMPLOYEE", empId, true);
        dataStore.addUser(newUser);

        auditLogger.log(user, "REGISTER_EMPLOYEE", "employees", empId,
            "Registered: " + emp.getFullName() + " (IC: " + emp.getIcPassport() + "), username: " + username);
        System.out.println("[HRM] Employee registered: " + emp.getFullName() + " -> username: " + username);
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
        if (request == null) throw new RemoteException("Profile update request not found");

        int employeeId = Integer.parseInt(request[1].trim());
        String fieldName = request[2].trim();
        String newValue = request[4].trim();

        // Apply the update to the employee record
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
            "Approved update for employee #" + employeeId + ": " + fieldName + " = " + newValue);
        return true;
    }

    @Override
    public synchronized boolean rejectProfileUpdate(int requestId, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        dataStore.updateProfileRequest(requestId, "REJECTED", user.getUserId());
        auditLogger.log(user, "REJECT_PROFILE_UPDATE", "profile_updates", requestId,
            "Rejected profile update request #" + requestId);
        return true;
    }

    @Override
    public List<LeaveApplication> getPendingLeaveApplications(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "HR", "ADMIN");
        List<LeaveApplication> pending = dataStore.getPendingLeaveApplications();
        // Attach employee names for display
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
        if (!"PENDING".equals(app.getStatus())) throw new RemoteException("Leave already processed");

        // Deduct from leave balance
        int year = Integer.parseInt(app.getStartDate().substring(0, 4));
        boolean deducted = dataStore.updateLeaveBalance(
            app.getEmployeeId(), app.getLeaveType(), year, app.getDaysRequested());
        if (!deducted) throw new RemoteException("Failed to update leave balance");

        // Update application status
        app.setStatus("APPROVED");
        app.setReviewedBy(user.getUserId());
        app.setReviewDate(LocalDateTime.now().toString());
        dataStore.updateLeaveApplication(app);

        Employee emp = dataStore.getEmployee(app.getEmployeeId());
        auditLogger.log(user, "APPROVE_LEAVE", "leave_applications", leaveId,
            "Approved " + app.getLeaveType() + " leave for " + (emp != null ? emp.getFullName() : "emp#" + app.getEmployeeId())
            + " (" + app.getDaysRequested() + " days)");
        return true;
    }

    @Override
    public synchronized boolean rejectLeave(int leaveId, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        LeaveApplication app = dataStore.getLeaveApplication(leaveId);
        if (app == null) throw new RemoteException("Leave application not found");
        if (!"PENDING".equals(app.getStatus())) throw new RemoteException("Leave already processed");

        app.setStatus("REJECTED");
        app.setReviewedBy(user.getUserId());
        app.setReviewDate(LocalDateTime.now().toString());
        dataStore.updateLeaveApplication(app);

        auditLogger.log(user, "REJECT_LEAVE", "leave_applications", leaveId,
            "Rejected leave application #" + leaveId);
        return true;
    }

    @Override
    public YearlyReport generateYearlyReport(int employeeId, int year, String sessionToken) throws RemoteException {
        UserAccount user = requireRole(sessionToken, "HR", "ADMIN");
        Employee emp = dataStore.getEmployee(employeeId);
        if (emp == null) throw new RemoteException("Employee not found");

        List<FamilyMember> family = dataStore.getFamilyMembers(employeeId);
        List<LeaveBalance> balances = dataStore.getLeaveBalances(employeeId, year);
        List<LeaveApplication> leaves = dataStore.getLeaveApplicationsByYear(employeeId, year);

        YearlyReport report = new YearlyReport(emp, family, balances, leaves, year, LocalDateTime.now().toString());

        auditLogger.log(user, "GENERATE_REPORT", "employees", employeeId,
            "Generated yearly report for " + emp.getFullName() + " (" + year + ")");
        System.out.println("[HRM] Yearly report generated for " + emp.getFullName() + " (" + year + ")");
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

        if (!ValidationUtil.validateUsername(username)) {
            throw new RemoteException("Invalid username. Use 3-20 alphanumeric characters.");
        }
        if (!ValidationUtil.validatePassword(password)) {
            throw new RemoteException("Password must be at least 6 characters.");
        }
        if (dataStore.getUserByUsername(username) != null) {
            throw new RemoteException("Username already exists: " + username);
        }

        String hash = PasswordHasher.hashPassword(password);
        UserAccount newUser = new UserAccount(0, username, hash, role, employeeId, true);
        int userId = dataStore.addUser(newUser);

        auditLogger.log(admin, "ADD_USER", "users", userId,
            "Added user: " + username + " [" + role + "]");
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

        if (success) {
            auditLogger.log(admin, "UPDATE_USER", "users", userId,
                "Updated user: " + username + " [" + role + "] active=" + isActive);
        }
        return success;
    }

    @Override
    public synchronized boolean removeUser(int userId, String sessionToken) throws RemoteException {
        UserAccount admin = requireRole(sessionToken, "ADMIN");
        boolean success = dataStore.removeUser(userId);
        if (success) {
            auditLogger.log(admin, "DEACTIVATE_USER", "users", userId,
                "Deactivated user account #" + userId);
        }
        return success;
    }

    @Override
    public List<AuditLogEntry> getAuditLog(String sessionToken) throws RemoteException {
        requireRole(sessionToken, "ADMIN");
        return dataStore.getAuditLog();
    }
}
