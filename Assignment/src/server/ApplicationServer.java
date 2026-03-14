package server;

import interfaces.ApplicationInterface;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Application Server Main Class
 * Run this on the machine designated as the application server
 * This server connects to the Database Server and provides business logic
 * 
 * Usage: java server.ApplicationServer [port] [db-server-url]
 * Example: java server.ApplicationServer 1100 rmi://localhost:1099/DatabaseService
 */
public class ApplicationServer {
    
    public static void main(String[] args) {
        try {
            // Default values
            String host = "localhost";
            int registryPort = 1100;
            String databaseServerUrl = "rmi://localhost:1099/DatabaseService";
            
            // Parse command line arguments
            if (args.length >= 1) {
                registryPort = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                databaseServerUrl = args[1];
            }
            
            // Create and start RMI registry
            try {
                LocateRegistry.createRegistry(registryPort);
                System.out.println("RMI Registry created on port " + registryPort);
            } catch (Exception e) {
                System.out.println("RMI Registry already running on port " + registryPort);
            }
            
            // Create application server instance
            ApplicationInterface appServer = new ApplicationServerImpl(databaseServerUrl);
            
            // Bind the remote object to the registry
            String serviceName = "rmi://" + host + ":" + registryPort + "/ApplicationService";
            Naming.rebind(serviceName, appServer);
            
            System.out.println("===========================================");
            System.out.println("   APPLICATION SERVER IS RUNNING");
            System.out.println("===========================================");
            System.out.println("Service Name: ApplicationService");
            System.out.println("Host: " + host);
            System.out.println("Port: " + registryPort);
            System.out.println("Full URL: " + serviceName);
            System.out.println("===========================================");
            System.out.println("Connected to Database Server:");
            System.out.println(databaseServerUrl);
            System.out.println("===========================================");
            System.out.println("Ready to accept client connections");
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("Application Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
