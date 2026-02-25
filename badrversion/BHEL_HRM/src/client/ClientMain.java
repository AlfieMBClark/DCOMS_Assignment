package client;

import common.interfaces.AuthService;
import common.interfaces.HRMService;
import common.interfaces.PRSService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;

/**
 * Client main entry point.
 * Connects to the RMI server and launches the login GUI.
 *
 * Implements fault tolerance with retry logic for connection failures.
 *
 * Usage: java -cp out/ client.ClientMain [host] [port]
 */
public class ClientMain {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1099;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    // Shared service references
    public static AuthService authService;
    public static HRMService hrmService;
    public static PRSService prsService;
    public static String sessionToken;
    public static JFrame mainFrame;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length >= 1) host = args[0];
        if (args.length >= 2) {
            try { port = Integer.parseInt(args[1]); } catch (NumberFormatException e) { /* default */ }
        }

        final String finalHost = host;
        final int finalPort = port;

        System.out.println("============================================");
        System.out.println("  BHEL HRM System - Client Starting...");
        System.out.println("  Connecting to: " + host + ":" + port);
        System.out.println("============================================");

        // Connect with retry logic (fault tolerance)
        boolean connected = false;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("[CLIENT] Connection attempt " + attempt + "/" + MAX_RETRIES + "...");
                Registry registry = LocateRegistry.getRegistry(finalHost, finalPort);
                authService = (AuthService) registry.lookup("AuthService");
                hrmService = (HRMService) registry.lookup("HRMService");
                prsService = (PRSService) registry.lookup("PRSService");
                connected = true;
                System.out.println("[CLIENT] Connected successfully!");
                break;
            } catch (Exception e) {
                System.err.println("[CLIENT] Connection failed: " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    System.out.println("[CLIENT] Retrying in " + (RETRY_DELAY_MS / 1000) + " seconds...");
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { break; }
                }
            }
        }

        if (!connected) {
            System.err.println("[CLIENT] Could not connect to server after " + MAX_RETRIES + " attempts.");
            JOptionPane.showMessageDialog(null,
                "Could not connect to the HRM server at " + finalHost + ":" + finalPort
                + "\n\nPlease ensure the server is running and try again.",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { /* use default */ }

            mainFrame = new JFrame("BHEL HRM System");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(900, 650);
            mainFrame.setLocationRelativeTo(null);

            // Show login panel
            showLoginPanel();

            mainFrame.setVisible(true);
        });
    }

    public static void showLoginPanel() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(new LoginPanel());
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setTitle("BHEL HRM System - Login");
    }

    public static void showEmployeePanel(int employeeId) {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(new EmployeePanel(employeeId));
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setTitle("BHEL HRM System - Employee Dashboard");
    }

    public static void showHRPanel() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(new HRPanel());
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setTitle("BHEL HRM System - HR Dashboard");
    }

    public static void showAdminPanel() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(new AdminPanel());
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setTitle("BHEL HRM System - Admin Dashboard");
    }

    public static void logout() {
        try {
            if (sessionToken != null) {
                authService.logout(sessionToken);
                sessionToken = null;
            }
        } catch (Exception e) {
            System.err.println("[CLIENT] Logout error: " + e.getMessage());
        }
        showLoginPanel();
    }

    /**
     * Utility method to show error dialogs.
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Utility method to show success dialogs.
     */
    public static void showSuccess(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
