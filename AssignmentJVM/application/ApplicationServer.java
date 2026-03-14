package application;

import database.DatabaseInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ApplicationServer {
    public static void main(String[] args) {
        try {
            // Get database server IP from command line (default: localhost)
            String dbServerIP = (args.length > 0) ? args[0] : "localhost";
            
            System.out.println("Connecting to Database Server at " + dbServerIP + ":1099...");
            
            // Lookup database service
            Registry dbRegistry = LocateRegistry.getRegistry(dbServerIP, 1099);
            DatabaseInterface database = (DatabaseInterface) dbRegistry.lookup("DatabaseService");
            
            System.out.println("Connected to Database Server!");
            
            // Create application service
            ApplicationInterface appService = new ApplicationServerImpl(database);
            
            // Start RMI registry on port 1100
            Registry registry = LocateRegistry.createRegistry(1100);
            
            // Bind the service
            registry.rebind("ApplicationService", appService);
            
            System.out.println("===========================================");
            System.out.println("Application Server is running on port 1100");
            System.out.println("Service name: ApplicationService");
            System.out.println("Connected to DB at: " + dbServerIP);
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("Application Server failed to start:");
            e.printStackTrace();
            System.err.println("\nMake sure Database Server is running first!");
        }
    }
}
