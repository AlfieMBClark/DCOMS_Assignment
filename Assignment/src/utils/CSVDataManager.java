package utils;

import models.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CSV Data Manager - Handles all CSV file operations with thread safety
 * Implements proper locking mechanisms for concurrent access
 */
public class CSVDataManager {
    
    private static final String DATA_DIR = "data";
    private static final String BACKUP_DIR = "backups";
    private static final String EMPLOYEES_FILE = DATA_DIR + "/employees.csv";
    private static final String FAMILY_FILE = DATA_DIR + "/family_details.csv";
    private static final String LEAVES_FILE = DATA_DIR + "/leaves.csv";
    private static final String AUDIT_FILE = DATA_DIR + "/audit_logs.csv";
    
    // ReadWriteLocks for each file to allow multiple readers or single writer
    private final ReadWriteLock employeeLock = new ReentrantReadWriteLock();
    private final ReadWriteLock familyLock = new ReentrantReadWriteLock();
    private final ReadWriteLock leaveLock = new ReentrantReadWriteLock();
    private final ReadWriteLock auditLock = new ReentrantReadWriteLock();
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public CSVDataManager() {
        initializeDataDirectory();
    }
    
    /**
     * Initialize data directory and create CSV files if they don't exist
     */
    private void initializeDataDirectory() {
        try {
            // Create directories
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
            
            // Initialize CSV files with headers if they don't exist
            initializeEmployeeFile();
            initializeFamilyFile();
            initializeLeaveFile();
            initializeAuditLogFile();
            
            System.out.println("Data directory initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error initializing data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeEmployeeFile() throws IOException {
        Path path = Paths.get(EMPLOYEES_FILE);
        if (!Files.exists(path)) {
            String header = "employeeId,firstName,lastName,icPassport,email,department,position," +
                          "username,password,role,leaveBalance,createdAt,updatedAt\n";
            Files.writeString(path, header);
        }
    }
    
    private void initializeFamilyFile() throws IOException {
        Path path = Paths.get(FAMILY_FILE);
        if (!Files.exists(path)) {
            String header = "familyId,employeeId,memberName,relationship,icPassport," +
                          "contactNumber,createdAt,updatedAt\n";
            Files.writeString(path, header);
        }
    }
    
    private void initializeLeaveFile() throws IOException {
        Path path = Paths.get(LEAVES_FILE);
        if (!Files.exists(path)) {
            String header = "leaveId,employeeId,startDate,endDate,numberOfDays,leaveType," +
                          "reason,status,approvedBy,remarks,appliedAt,processedAt\n";
            Files.writeString(path, header);
        }
    }
    
    private void initializeAuditLogFile() throws IOException {
        Path path = Paths.get(AUDIT_FILE);
        if (!Files.exists(path)) {
            String header = "logId,userId,role,action,targetEntity,targetId,details,timestamp,ipAddress\n";
            Files.writeString(path, header);
        }
    }
    
    // ==================== Employee Operations ====================
    
    public synchronized boolean createEmployee(Employee employee) {
        employeeLock.writeLock().lock();
        try {
            List<Employee> employees = getAllEmployeesInternal();
            
            // Check for duplicate IC/Passport
            for (Employee emp : employees) {
                if (emp.getIcPassport().equals(employee.getIcPassport())) {
                    return false; // Duplicate IC/Passport
                }
                if (emp.getUsername().equals(employee.getUsername())) {
                    return false; // Duplicate username
                }
            }
            
            employees.add(employee);
            return writeEmployeesToFile(employees);
        } finally {
            employeeLock.writeLock().unlock();
        }
    }
    
    public Employee getEmployeeById(String employeeId) {
        employeeLock.readLock().lock();
        try {
            List<Employee> employees = getAllEmployeesInternal();
            return employees.stream()
                    .filter(emp -> emp.getEmployeeId().equals(employeeId))
                    .findFirst()
                    .orElse(null);
        } finally {
            employeeLock.readLock().unlock();
        }
    }
    
    public Employee getEmployeeByUsername(String username) {
        employeeLock.readLock().lock();
        try {
            List<Employee> employees = getAllEmployeesInternal();
            return employees.stream()
                    .filter(emp -> emp.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
        } finally {
            employeeLock.readLock().unlock();
        }
    }
    
    public Employee getEmployeeByIcPassport(String icPassport) {
        employeeLock.readLock().lock();
        try {
            List<Employee> employees = getAllEmployeesInternal();
            return employees.stream()
                    .filter(emp -> emp.getIcPassport().equals(icPassport))
                    .findFirst()
                    .orElse(null);
        } finally {
            employeeLock.readLock().unlock();
        }
    }
    
    public List<Employee> getAllEmployees() {
        employeeLock.readLock().lock();
        try {
            return getAllEmployeesInternal();
        } finally {
            employeeLock.readLock().unlock();
        }
    }
    
    private List<Employee> getAllEmployeesInternal() {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEES_FILE))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                employees.add(parseEmployeeFromCSV(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading employees: " + e.getMessage());
        }
        return employees;
    }
    
    public synchronized boolean updateEmployee(Employee employee) {
        employeeLock.writeLock().lock();
        try {
            List<Employee> employees = getAllEmployeesInternal();
            boolean found = false;
            
            for (int i = 0; i < employees.size(); i++) {
                if (employees.get(i).getEmployeeId().equals(employee.getEmployeeId())) {
                    employee.setUpdatedAt(LocalDateTime.now());
                    employees.set(i, employee);
                    found = true;
                    break;
                }
            }
            
            return found && writeEmployeesToFile(employees);
        } finally {
            employeeLock.writeLock().unlock();
        }
    }
    
    public synchronized boolean deleteEmployee(String employeeId) {
        employeeLock.writeLock().lock();
        try {
            List<Employee> employees = getAllEmployeesInternal();
            boolean removed = employees.removeIf(emp -> emp.getEmployeeId().equals(employeeId));
            return removed && writeEmployeesToFile(employees);
        } finally {
            employeeLock.writeLock().unlock();
        }
    }
    
    private boolean writeEmployeesToFile(List<Employee> employees) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEES_FILE))) {
            // Write header
            writer.write("employeeId,firstName,lastName,icPassport,email,department,position," +
                       "username,password,role,leaveBalance,createdAt,updatedAt\n");
            
            // Write employees
            for (Employee emp : employees) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%s,%s\n",
                        escapeCsv(emp.getEmployeeId()),
                        escapeCsv(emp.getFirstName()),
                        escapeCsv(emp.getLastName()),
                        escapeCsv(emp.getIcPassport()),
                        escapeCsv(emp.getEmail()),
                        escapeCsv(emp.getDepartment()),
                        escapeCsv(emp.getPosition()),
                        escapeCsv(emp.getUsername()),
                        escapeCsv(emp.getPassword()),
                        escapeCsv(emp.getRole()),
                        emp.getLeaveBalance(),
                        emp.getCreatedAt().format(DATETIME_FORMATTER),
                        emp.getUpdatedAt().format(DATETIME_FORMATTER)));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing employees: " + e.getMessage());
            return false;
        }
    }
    
    private Employee parseEmployeeFromCSV(String line) {
        String[] parts = parseCsvLine(line);
        Employee emp = new Employee();
        emp.setEmployeeId(parts[0]);
        emp.setFirstName(parts[1]);
        emp.setLastName(parts[2]);
        emp.setIcPassport(parts[3]);
        emp.setEmail(parts[4]);
        emp.setDepartment(parts[5]);
        emp.setPosition(parts[6]);
        emp.setUsername(parts[7]);
        emp.setPassword(parts[8]);
        emp.setRole(parts[9]);
        emp.setLeaveBalance(Double.parseDouble(parts[10]));
        emp.setCreatedAt(LocalDateTime.parse(parts[11], DATETIME_FORMATTER));
        emp.setUpdatedAt(LocalDateTime.parse(parts[12], DATETIME_FORMATTER));
        return emp;
    }
    
    // ==================== Family Details Operations ====================
    
    public synchronized boolean createFamilyDetails(FamilyDetails familyDetails) {
        familyLock.writeLock().lock();
        try {
            List<FamilyDetails> allFamily = getAllFamilyDetailsInternal();
            allFamily.add(familyDetails);
            return writeFamilyDetailsToFile(allFamily);
        } finally {
            familyLock.writeLock().unlock();
        }
    }
    
    public FamilyDetails getFamilyDetailsById(String familyId) {
        familyLock.readLock().lock();
        try {
            List<FamilyDetails> allFamily = getAllFamilyDetailsInternal();
            return allFamily.stream()
                    .filter(fam -> fam.getFamilyId().equals(familyId))
                    .findFirst()
                    .orElse(null);
        } finally {
            familyLock.readLock().unlock();
        }
    }
    
    public List<FamilyDetails> getFamilyDetailsByEmployeeId(String employeeId) {
        familyLock.readLock().lock();
        try {
            List<FamilyDetails> allFamily = getAllFamilyDetailsInternal();
            List<FamilyDetails> result = new ArrayList<>();
            for (FamilyDetails fam : allFamily) {
                if (fam.getEmployeeId().equals(employeeId)) {
                    result.add(fam);
                }
            }
            return result;
        } finally {
            familyLock.readLock().unlock();
        }
    }
    
    private List<FamilyDetails> getAllFamilyDetailsInternal() {
        List<FamilyDetails> familyList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FAMILY_FILE))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                familyList.add(parseFamilyDetailsFromCSV(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading family details: " + e.getMessage());
        }
        return familyList;
    }
    
    public synchronized boolean updateFamilyDetails(FamilyDetails familyDetails) {
        familyLock.writeLock().lock();
        try {
            List<FamilyDetails> allFamily = getAllFamilyDetailsInternal();
            boolean found = false;
            
            for (int i = 0; i < allFamily.size(); i++) {
                if (allFamily.get(i).getFamilyId().equals(familyDetails.getFamilyId())) {
                    familyDetails.setUpdatedAt(LocalDateTime.now());
                    allFamily.set(i, familyDetails);
                    found = true;
                    break;
                }
            }
            
            return found && writeFamilyDetailsToFile(allFamily);
        } finally {
            familyLock.writeLock().unlock();
        }
    }
    
    public synchronized boolean deleteFamilyDetails(String familyId) {
        familyLock.writeLock().lock();
        try {
            List<FamilyDetails> allFamily = getAllFamilyDetailsInternal();
            boolean removed = allFamily.removeIf(fam -> fam.getFamilyId().equals(familyId));
            return removed && writeFamilyDetailsToFile(allFamily);
        } finally {
            familyLock.writeLock().unlock();
        }
    }
    
    private boolean writeFamilyDetailsToFile(List<FamilyDetails> familyList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FAMILY_FILE))) {
            writer.write("familyId,employeeId,memberName,relationship,icPassport," +
                       "contactNumber,createdAt,updatedAt\n");
            
            for (FamilyDetails fam : familyList) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(fam.getFamilyId()),
                        escapeCsv(fam.getEmployeeId()),
                        escapeCsv(fam.getMemberName()),
                        escapeCsv(fam.getRelationship()),
                        escapeCsv(fam.getIcPassport()),
                        escapeCsv(fam.getContactNumber()),
                        fam.getCreatedAt().format(DATETIME_FORMATTER),
                        fam.getUpdatedAt().format(DATETIME_FORMATTER)));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing family details: " + e.getMessage());
            return false;
        }
    }
    
    private FamilyDetails parseFamilyDetailsFromCSV(String line) {
        String[] parts = parseCsvLine(line);
        FamilyDetails fam = new FamilyDetails();
        fam.setFamilyId(parts[0]);
        fam.setEmployeeId(parts[1]);
        fam.setMemberName(parts[2]);
        fam.setRelationship(parts[3]);
        fam.setIcPassport(parts[4]);
        fam.setContactNumber(parts[5]);
        fam.setCreatedAt(LocalDateTime.parse(parts[6], DATETIME_FORMATTER));
        fam.setUpdatedAt(LocalDateTime.parse(parts[7], DATETIME_FORMATTER));
        return fam;
    }
    
    // ==================== Leave Operations ====================
    
    public synchronized boolean createLeave(Leave leave) {
        leaveLock.writeLock().lock();
        try {
            List<Leave> leaves = getAllLeavesInternal();
            leaves.add(leave);
            return writeLeavesToFile(leaves);
        } finally {
            leaveLock.writeLock().unlock();
        }
    }
    
    public Leave getLeaveById(String leaveId) {
        leaveLock.readLock().lock();
        try {
            List<Leave> leaves = getAllLeavesInternal();
            return leaves.stream()
                    .filter(leave -> leave.getLeaveId().equals(leaveId))
                    .findFirst()
                    .orElse(null);
        } finally {
            leaveLock.readLock().unlock();
        }
    }
    
    public List<Leave> getLeavesByEmployeeId(String employeeId) {
        leaveLock.readLock().lock();
        try {
            List<Leave> leaves = getAllLeavesInternal();
            List<Leave> result = new ArrayList<>();
            for (Leave leave : leaves) {
                if (leave.getEmployeeId().equals(employeeId)) {
                    result.add(leave);
                }
            }
            return result;
        } finally {
            leaveLock.readLock().unlock();
        }
    }
    
    public List<Leave> getLeavesByStatus(String status) {
        leaveLock.readLock().lock();
        try {
            List<Leave> leaves = getAllLeavesInternal();
            List<Leave> result = new ArrayList<>();
            for (Leave leave : leaves) {
                if (leave.getStatus().equals(status)) {
                    result.add(leave);
                }
            }
            return result;
        } finally {
            leaveLock.readLock().unlock();
        }
    }
    
    public List<Leave> getAllLeaves() {
        leaveLock.readLock().lock();
        try {
            return getAllLeavesInternal();
        } finally {
            leaveLock.readLock().unlock();
        }
    }
    
    private List<Leave> getAllLeavesInternal() {
        List<Leave> leaves = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LEAVES_FILE))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                leaves.add(parseLeaveFromCSV(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading leaves: " + e.getMessage());
        }
        return leaves;
    }
    
    public synchronized boolean updateLeave(Leave leave) {
        leaveLock.writeLock().lock();
        try {
            List<Leave> leaves = getAllLeavesInternal();
            boolean found = false;
            
            for (int i = 0; i < leaves.size(); i++) {
                if (leaves.get(i).getLeaveId().equals(leave.getLeaveId())) {
                    leaves.set(i, leave);
                    found = true;
                    break;
                }
            }
            
            return found && writeLeavesToFile(leaves);
        } finally {
            leaveLock.writeLock().unlock();
        }
    }
    
    public synchronized boolean deleteLeave(String leaveId) {
        leaveLock.writeLock().lock();
        try {
            List<Leave> leaves = getAllLeavesInternal();
            boolean removed = leaves.removeIf(leave -> leave.getLeaveId().equals(leaveId));
            return removed && writeLeavesToFile(leaves);
        } finally {
            leaveLock.writeLock().unlock();
        }
    }
    
    private boolean writeLeavesToFile(List<Leave> leaves) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LEAVES_FILE))) {
            writer.write("leaveId,employeeId,startDate,endDate,numberOfDays,leaveType," +
                       "reason,status,approvedBy,remarks,appliedAt,processedAt\n");
            
            for (Leave leave : leaves) {
                writer.write(String.format("%s,%s,%s,%s,%.1f,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(leave.getLeaveId()),
                        escapeCsv(leave.getEmployeeId()),
                        leave.getStartDate().format(DATE_FORMATTER),
                        leave.getEndDate().format(DATE_FORMATTER),
                        leave.getNumberOfDays(),
                        escapeCsv(leave.getLeaveType()),
                        escapeCsv(leave.getReason()),
                        escapeCsv(leave.getStatus()),
                        escapeCsv(leave.getApprovedBy() != null ? leave.getApprovedBy() : ""),
                        escapeCsv(leave.getRemarks() != null ? leave.getRemarks() : ""),
                        leave.getAppliedAt().format(DATETIME_FORMATTER),
                        leave.getProcessedAt() != null ? leave.getProcessedAt().format(DATETIME_FORMATTER) : ""));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing leaves: " + e.getMessage());
            return false;
        }
    }
    
    private Leave parseLeaveFromCSV(String line) {
        String[] parts = parseCsvLine(line);
        Leave leave = new Leave();
        leave.setLeaveId(parts[0]);
        leave.setEmployeeId(parts[1]);
        leave.setStartDate(LocalDate.parse(parts[2], DATE_FORMATTER));
        leave.setEndDate(LocalDate.parse(parts[3], DATE_FORMATTER));
        leave.setNumberOfDays(Double.parseDouble(parts[4]));
        leave.setLeaveType(parts[5]);
        leave.setReason(parts[6]);
        leave.setStatus(parts[7]);
        leave.setApprovedBy(!parts[8].isEmpty() ? parts[8] : null);
        leave.setRemarks(!parts[9].isEmpty() ? parts[9] : null);
        leave.setAppliedAt(LocalDateTime.parse(parts[10], DATETIME_FORMATTER));
        if (!parts[11].isEmpty()) {
            leave.setProcessedAt(LocalDateTime.parse(parts[11], DATETIME_FORMATTER));
        }
        return leave;
    }
    
    // ==================== Audit Log Operations ====================
    
    public synchronized boolean createAuditLog(AuditLog auditLog) {
        auditLock.writeLock().lock();
        try {
            // Append to audit log file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(AUDIT_FILE, true))) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(auditLog.getLogId()),
                        escapeCsv(auditLog.getUserId()),
                        escapeCsv(auditLog.getRole()),
                        escapeCsv(auditLog.getAction()),
                        escapeCsv(auditLog.getTargetEntity()),
                        escapeCsv(auditLog.getTargetId() != null ? auditLog.getTargetId() : ""),
                        escapeCsv(auditLog.getDetails()),
                        auditLog.getTimestamp().format(DATETIME_FORMATTER),
                        escapeCsv(auditLog.getIpAddress() != null ? auditLog.getIpAddress() : "")));
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error writing audit log: " + e.getMessage());
            return false;
        } finally {
            auditLock.writeLock().unlock();
        }
    }
    
    public List<AuditLog> getAuditLogsByUserId(String userId) {
        auditLock.readLock().lock();
        try {
            List<AuditLog> allLogs = getAllAuditLogsInternal();
            List<AuditLog> result = new ArrayList<>();
            for (AuditLog log : allLogs) {
                if (log.getUserId().equals(userId)) {
                    result.add(log);
                }
            }
            return result;
        } finally {
            auditLock.readLock().unlock();
        }
    }
    
    public List<AuditLog> getAllAuditLogs() {
        auditLock.readLock().lock();
        try {
            return getAllAuditLogsInternal();
        } finally {
            auditLock.readLock().unlock();
        }
    }
    
    private List<AuditLog> getAllAuditLogsInternal() {
        List<AuditLog> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(AUDIT_FILE))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                logs.add(parseAuditLogFromCSV(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading audit logs: " + e.getMessage());
        }
        return logs;
    }
    
    private AuditLog parseAuditLogFromCSV(String line) {
        String[] parts = parseCsvLine(line);
        AuditLog log = new AuditLog();
        log.setLogId(parts[0]);
        log.setUserId(parts[1]);
        log.setRole(parts[2]);
        log.setAction(parts[3]);
        log.setTargetEntity(parts[4]);
        log.setTargetId(!parts[5].isEmpty() ? parts[5] : null);
        log.setDetails(parts[6]);
        log.setTimestamp(LocalDateTime.parse(parts[7], DATETIME_FORMATTER));
        log.setIpAddress(!parts[8].isEmpty() ? parts[8] : null);
        return log;
    }
    
    // ==================== ID Generation ====================
    
    public synchronized String generateNextEmployeeId() {
        List<Employee> employees = getAllEmployees();
        int maxId = 0;
        for (Employee emp : employees) {
            try {
                int id = Integer.parseInt(emp.getEmployeeId().replace("EMP", ""));
                if (id > maxId) maxId = id;
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }
        return String.format("EMP%04d", maxId + 1);
    }
    
    public synchronized String generateNextLeaveId() {
        List<Leave> leaves = getAllLeaves();
        int maxId = 0;
        for (Leave leave : leaves) {
            try {
                int id = Integer.parseInt(leave.getLeaveId().replace("LVE", ""));
                if (id > maxId) maxId = id;
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }
        return String.format("LVE%04d", maxId + 1);
    }
    
    public synchronized String generateNextFamilyId() {
        familyLock.readLock().lock();
        try {
            List<FamilyDetails> families = getAllFamilyDetailsInternal();
            int maxId = 0;
            for (FamilyDetails fam : families) {
                try {
                    int id = Integer.parseInt(fam.getFamilyId().replace("FAM", ""));
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
            return String.format("FAM%04d", maxId + 1);
        } finally {
            familyLock.readLock().unlock();
        }
    }
    
    public synchronized String generateNextAuditLogId() {
        auditLock.readLock().lock();
        try {
            List<AuditLog> logs = getAllAuditLogsInternal();
            int maxId = 0;
            for (AuditLog log : logs) {
                try {
                    int id = Integer.parseInt(log.getLogId().replace("LOG", ""));
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
            return String.format("LOG%06d", maxId + 1);
        } finally {
            auditLock.readLock().unlock();
        }
    }
    
    // ==================== Backup and Restore ====================
    
    public synchronized boolean backupData() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFolder = BACKUP_DIR + "/backup_" + timestamp;
            Files.createDirectories(Paths.get(backupFolder));
            
            Files.copy(Paths.get(EMPLOYEES_FILE), Paths.get(backupFolder + "/employees.csv"));
            Files.copy(Paths.get(FAMILY_FILE), Paths.get(backupFolder + "/family_details.csv"));
            Files.copy(Paths.get(LEAVES_FILE), Paths.get(backupFolder + "/leaves.csv"));
            Files.copy(Paths.get(AUDIT_FILE), Paths.get(backupFolder + "/audit_logs.csv"));
            
            System.out.println("Backup created successfully: " + backupFolder);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== Utility Methods ====================
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
}
