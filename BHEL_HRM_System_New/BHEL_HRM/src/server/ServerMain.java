package server;

import common.models.UserAccount;
import utils.PasswordHasher;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * Main server entry point.
 * Supports both plain and SSL/TLS RMI modes.
 *
 * SSL mode is enabled by setting -Dssl.enabled=true JVM property
 * along with keystore/truststore configuration.
 *
 * Usage:
 *   Plain:  java -cp out/ server.ServerMain [port] [dataDir]
 *   SSL:    java -cp out/ -Dssl.enabled=true \
 *             -Djavax.net.ssl.keyStore=certs/server.keystore \
 *             -Djavax.net.ssl.keyStorePassword=bhel2024 \
 *             server.ServerMain [port] [dataDir]
 */
public class ServerMain {

    public static final int DEFAULT_PORT = 1099;
    public static final String DEFAULT_DATA_DIR = "data";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String dataDir = DEFAULT_DATA_DIR;
        boolean sslEnabled = "true".equals(System.getProperty("ssl.enabled"));

        if (args.length >= 1) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException e) { /* use default */ }
        }
        if (args.length >= 2) {
            dataDir = args[1];
        }

        System.out.println("============================================");
        System.out.println("  BHEL HRM System - Server Starting...");
        System.out.println("============================================");
        System.out.println("  Port:      " + port);
        System.out.println("  Data Dir:  " + dataDir);
        System.out.println("  SSL/TLS:   " + (sslEnabled ? "ENABLED" : "DISABLED"));
        System.out.println("============================================");

        try {
            // Initialize data store
            CSVDataStore dataStore = new CSVDataStore(dataDir);
            AuditLogger auditLogger = new AuditLogger(dataStore);

            // Seed default data if no users exist
            seedDefaultData(dataStore);

            // SSL socket factories (null if SSL disabled)
            RMIClientSocketFactory csf = null;
            RMIServerSocketFactory ssf = null;

            if (sslEnabled) {
                csf = new SslRMIClientSocketFactory();
                ssf = new SslRMIServerSocketFactory(null, null, true);
                System.out.println("[SSL] Using SSL/TLS socket factories");
                System.out.println("[SSL] Protocol: TLSv1.2/TLSv1.3");
            }

            // Create service implementations (with or without SSL)
            AuthServiceImpl authService = new AuthServiceImpl(dataStore, auditLogger, port + 1, csf, ssf);
            HRMServiceImpl hrmService = new HRMServiceImpl(dataStore, authService, auditLogger, port + 2, csf, ssf);
            PRSServiceImpl prsService = new PRSServiceImpl(dataStore, authService, auditLogger, port + 3, csf, ssf);

            // Create RMI registry
            Registry registry;
            if (sslEnabled) {
                registry = LocateRegistry.createRegistry(port, csf, ssf);
            } else {
                registry = LocateRegistry.createRegistry(port);
            }

            // Bind services to registry
            registry.rebind("AuthService", authService);
            registry.rebind("HRMService", hrmService);
            registry.rebind("PRSService", prsService);

            System.out.println("============================================");
            System.out.println("  Services registered successfully:");
            System.out.println("  - AuthService  (Authentication)");
            System.out.println("  - HRMService   (HR Management)");
            System.out.println("  - PRSService   (Payroll System)");
            if (sslEnabled) {
                System.out.println("");
                System.out.println("  All services secured with SSL/TLS");
                System.out.println("  Communication is encrypted end-to-end");
            }
            System.out.println("============================================");
            System.out.println("  Server is running on port " + port);
            System.out.println("  Press Ctrl+C to stop");
            System.out.println("============================================");

            auditLogger.log(null, "SERVER_START", null, 0,
                "Server started on port " + port + (sslEnabled ? " (SSL)" : ""));

        } catch (Exception e) {
            System.err.println("[SERVER] Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Seeds default admin, HR, and sample employee accounts if none exist.
     */
    private static void seedDefaultData(CSVDataStore dataStore) {
        if (!dataStore.getAllUsers().isEmpty()) {
            System.out.println("[SERVER] Existing data found - skipping seed");
            return;
        }

        System.out.println("[SERVER] No users found - seeding default data...");

        // Admin account
        UserAccount admin = new UserAccount(0, "admin",
            PasswordHasher.hashPassword("admin123"), "ADMIN", 0, true);
        dataStore.addUser(admin);
        System.out.println("[SERVER]   admin / admin123");

        // HR account
        UserAccount hr = new UserAccount(0, "hr1",
            PasswordHasher.hashPassword("hr1234"), "HR", 0, true);
        dataStore.addUser(hr);
        System.out.println("[SERVER]   hr1 / hr1234");

        // Sample employee
        common.models.Employee emp = new common.models.Employee();
        emp.setFirstName("Ahmad");
        emp.setLastName("Ibrahim");
        emp.setIcPassport("990101-14-1234");
        emp.setEmail("ahmad@bhel.com");
        emp.setPhone("+60123456789");
        emp.setDepartment("Engineering");
        emp.setPosition("Software Developer");
        emp.setDateJoined("2024-01-15");
        emp.setActive(true);
        int empId = dataStore.addEmployee(emp);

        UserAccount empUser = new UserAccount(0, "ahmad.ibrahim",
            PasswordHasher.hashPassword("emp123"), "EMPLOYEE", empId, true);
        dataStore.addUser(empUser);
        System.out.println("[SERVER]   ahmad.ibrahim / emp123 (ID: " + empId + ")");

        // Family member
        common.models.FamilyMember spouse = new common.models.FamilyMember(
            0, empId, "Siti Aminah", "SPOUSE", "990505-14-5678", "1999-05-05");
        dataStore.addFamilyMember(spouse);

        System.out.println("[SERVER] Default data seeded successfully!");
    }
}
