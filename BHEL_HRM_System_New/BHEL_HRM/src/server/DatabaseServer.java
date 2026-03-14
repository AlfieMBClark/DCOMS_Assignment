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
 * Database Server - Tier 3 of the 3-tier architecture.
 * Handles all CSV data operations and maintains audit logs.
 * Runs on a separate port from the Application Server.
 *
 * Usage:
 *   Plain:  java -cp out/ server.DatabaseServer [port] [dataDir]
 *   SSL:    java -cp out/ -Dssl.enabled=true \
 *             -Djavax.net.ssl.keyStore=certs/server.keystore \
 *             -Djavax.net.ssl.keyStorePassword=bhel2024 \
 *             server.DatabaseServer [port] [dataDir]
 */
public class DatabaseServer {

    public static final int DEFAULT_PORT = 1098;
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
        System.out.println("  BHEL HRM System - Database Server");
        System.out.println("  (3-Tier Architecture - Tier 3)");
        System.out.println("============================================");
        System.out.println("  Port:      " + port);
        System.out.println("  Data Dir:  " + dataDir);
        System.out.println("  SSL/TLS:   " + (sslEnabled ? "ENABLED" : "DISABLED"));
        System.out.println("============================================");

        try {
            // Initialize CSV data store
            CSVDataStore dataStore = new CSVDataStore(dataDir);

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

            // Create database service
            DatabaseServiceImpl dbService = new DatabaseServiceImpl(dataStore, port, csf, ssf);

            // Create RMI registry
            Registry registry;
            if (sslEnabled) {
                registry = LocateRegistry.createRegistry(port, csf, ssf);
            } else {
                registry = LocateRegistry.createRegistry(port);
            }

            // Bind database service to registry
            registry.rebind("DatabaseService", dbService);

            System.out.println("============================================");
            System.out.println("  Database Service registered successfully");
            if (sslEnabled) {
                System.out.println("  Communication is encrypted (SSL/TLS)");
            }
            System.out.println("============================================");
            System.out.println("  Database Server listening on port " + port);
            System.out.println("  Press Ctrl+C to stop");
            System.out.println("============================================");

            // Log server startup
            dataStore.addAuditLog(0, "SYSTEM", "SYSTEM", "DB_SERVER_START", null, 0,
                "Database server started on port " + port + (sslEnabled ? " (SSL)" : ""));

        } catch (Exception e) {
            System.err.println("[DATABASE_SERVER] Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Seeds default admin, HR, and sample employee accounts if none exist.
     */
    private static void seedDefaultData(CSVDataStore dataStore) {
        if (!dataStore.getAllUsers().isEmpty()) {
            System.out.println("[DB] Existing data found - skipping seed");
            return;
        }

        System.out.println("[DB] No users found - seeding default data...");

        // Admin account
        UserAccount admin = new UserAccount(0, "admin",
            PasswordHasher.hashPassword("admin123"), "ADMIN", 0, true);
        dataStore.addUser(admin);
        System.out.println("[DB]   admin / admin123");

        // HR account
        UserAccount hr = new UserAccount(0, "hr1",
            PasswordHasher.hashPassword("hr1234"), "HR", 0, true);
        dataStore.addUser(hr);
        System.out.println("[DB]   hr1 / hr1234");

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
        System.out.println("[DB]   ahmad.ibrahim / emp123");

        System.out.println("[DB] Seed complete");
    }
}
