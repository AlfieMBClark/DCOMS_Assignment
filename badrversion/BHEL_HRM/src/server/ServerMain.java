package server;

import common.models.UserAccount;
import utils.PasswordHasher;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Main server entry point.
 * Sets up the RMI registry, initializes the data store,
 * and binds all remote services (HRM, Auth, PRS).
 *
 * SSL/TLS is configured via JVM arguments:
 *   -Djavax.net.ssl.keyStore=certs/server.keystore
 *   -Djavax.net.ssl.keyStorePassword=changeit
 *
 * Usage: java -cp out/ server.ServerMain [port] [dataDir]
 */
public class ServerMain {

    public static final int DEFAULT_PORT = 1099;
    public static final String DEFAULT_DATA_DIR = "data";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String dataDir = DEFAULT_DATA_DIR;

        // Parse command line arguments
        if (args.length >= 1) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException e) { /* use default */ }
        }
        if (args.length >= 2) {
            dataDir = args[1];
        }

        System.out.println("============================================");
        System.out.println("  BHEL HRM System - Server Starting...");
        System.out.println("============================================");
        System.out.println("Port: " + port);
        System.out.println("Data Directory: " + dataDir);

        try {
            // Initialize data store
            CSVDataStore dataStore = new CSVDataStore(dataDir);
            AuditLogger auditLogger = new AuditLogger(dataStore);

            // Seed default data if no users exist
            seedDefaultData(dataStore);

            // Create service implementations
            AuthServiceImpl authService = new AuthServiceImpl(dataStore, auditLogger);
            HRMServiceImpl hrmService = new HRMServiceImpl(dataStore, authService, auditLogger);
            PRSServiceImpl prsService = new PRSServiceImpl(dataStore, authService, auditLogger);

            // Create and start RMI registry
            Registry registry = LocateRegistry.createRegistry(port);

            // Bind services to registry
            registry.rebind("AuthService", authService);
            registry.rebind("HRMService", hrmService);
            registry.rebind("PRSService", prsService);

            System.out.println("============================================");
            System.out.println("  Services registered successfully:");
            System.out.println("  - AuthService  (Authentication)");
            System.out.println("  - HRMService   (HR Management)");
            System.out.println("  - PRSService   (Payroll System)");
            System.out.println("============================================");
            System.out.println("  Server is running on port " + port);
            System.out.println("  Press Ctrl+C to stop");
            System.out.println("============================================");

            // Log server start
            auditLogger.log(null, "SERVER_START", null, 0, "Server started on port " + port);

            // Keep server running
            // The RMI runtime keeps the JVM alive as long as there are exported objects

        } catch (Exception e) {
            System.err.println("[SERVER] Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Seeds default admin and HR accounts if the users file is empty.
     * Default credentials:
     *   Admin:  admin / admin123
     *   HR:     hr1 / hr1234
     */
    private static void seedDefaultData(CSVDataStore dataStore) {
        if (!dataStore.getAllUsers().isEmpty()) {
            System.out.println("[SERVER] Existing data found - skipping seed");
            return;
        }

        System.out.println("[SERVER] No users found - seeding default data...");

        // Create admin account (no linked employee)
        UserAccount admin = new UserAccount(0, "admin",
            PasswordHasher.hashPassword("admin123"), "ADMIN", 0, true);
        dataStore.addUser(admin);
        System.out.println("[SERVER] Default admin created: admin / admin123");

        // Create HR account (no linked employee)
        UserAccount hr = new UserAccount(0, "hr1",
            PasswordHasher.hashPassword("hr1234"), "HR", 0, true);
        dataStore.addUser(hr);
        System.out.println("[SERVER] Default HR created: hr1 / hr1234");

        // Create sample employee
        common.models.Employee sampleEmp = new common.models.Employee();
        sampleEmp.setFirstName("Ahmad");
        sampleEmp.setLastName("Ibrahim");
        sampleEmp.setIcPassport("990101-14-1234");
        sampleEmp.setEmail("ahmad@bhel.com");
        sampleEmp.setPhone("+60123456789");
        sampleEmp.setDepartment("Engineering");
        sampleEmp.setPosition("Software Developer");
        sampleEmp.setDateJoined("2024-01-15");
        sampleEmp.setActive(true);
        int empId = dataStore.addEmployee(sampleEmp);

        // Create employee user account
        UserAccount empUser = new UserAccount(0, "ahmad.ibrahim",
            PasswordHasher.hashPassword("emp123"), "EMPLOYEE", empId, true);
        dataStore.addUser(empUser);
        System.out.println("[SERVER] Sample employee created: ahmad.ibrahim / emp123 (ID: " + empId + ")");

        // Add sample family member
        common.models.FamilyMember spouse = new common.models.FamilyMember(
            0, empId, "Siti Aminah", "SPOUSE", "990505-14-5678", "1999-05-05");
        dataStore.addFamilyMember(spouse);

        System.out.println("[SERVER] Default data seeded successfully!");
    }
}
