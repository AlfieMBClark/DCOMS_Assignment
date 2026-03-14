package server;

import interfaces.DatabaseInterface;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Database Server Main Class
 * Run this on the machine designated as the database server
 * 
 * Usage: java server.DatabaseServer [registry-port]
 * Example: java server.DatabaseServer 1099
 */
public class DatabaseServer {
    
    public static void main(String[] args) {
        try {
            // Default values
            String host = "localhost";
            int registryPort = 1099;
            
            // Parse command line arguments
            if (args.length >= 1) {
                registryPort = Integer.parseInt(args[0]);
            }
            
            // Create and start RMI registry
            try {
                LocateRegistry.createRegistry(registryPort);
                System.out.println("RMI Registry created on port " + registryPort);
            } catch (Exception e) {
                System.out.println("RMI Registry already running on port " + registryPort);
            }
            
            // Create database server instance
            DatabaseInterface dbServer = new DatabaseServerImpl();
            
            // Bind the remote object to the registry
            String serviceName = "rmi://" + host + ":" + registryPort + "/DatabaseService";
            Naming.rebind(serviceName, dbServer);
            
            System.out.println("===========================================");
            System.out.println("   DATABASE SERVER IS RUNNING");
            System.out.println("===========================================");
            System.out.println("Service Name: DatabaseService");
            System.out.println("Host: " + host);
            System.out.println("Port: " + registryPort);
            System.out.println("Full URL: " + serviceName);
            System.out.println("===========================================");
            System.out.println("CSV files location: ./data/");
            System.out.println("Backups location: ./backups/");
            System.out.println("===========================================");
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("Database Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
