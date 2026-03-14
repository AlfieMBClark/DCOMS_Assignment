package database;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DatabaseServer {
    public static void main(String[] args) {
        try {
            // Create the database service
            DatabaseInterface dbService = new DatabaseServerImpl();
            
            // Start RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Bind the service
            registry.rebind("DatabaseService", dbService);
            
            System.out.println("===========================================");
            System.out.println("Database Server is running on port 1099...");
            System.out.println("Service name: DatabaseService");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("Database Server failed to start:");
            e.printStackTrace();
        }
    }
}
