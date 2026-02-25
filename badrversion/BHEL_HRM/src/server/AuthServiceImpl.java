package server;

import common.interfaces.AuthService;
import common.models.UserAccount;
import utils.PasswordHasher;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the Authentication Service.
 * Manages login/logout and session tokens using thread-safe ConcurrentHashMap.
 */
public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {

    private final CSVDataStore dataStore;
    private final AuditLogger auditLogger;

    // Session management: token -> UserAccount
    private final Map<String, UserAccount> activeSessions = new ConcurrentHashMap<>();

    public AuthServiceImpl(CSVDataStore dataStore, AuditLogger auditLogger) throws RemoteException {
        super();
        this.dataStore = dataStore;
        this.auditLogger = auditLogger;
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        System.out.println("[AUTH] Login attempt: " + username);

        UserAccount user = dataStore.getUserByUsername(username);

        if (user == null) {
            auditLogger.logFailedLogin(username);
            System.out.println("[AUTH] Login failed - user not found: " + username);
            return null;
        }

        if (!user.isActive()) {
            auditLogger.logFailedLogin(username);
            System.out.println("[AUTH] Login failed - account disabled: " + username);
            return null;
        }

        if (!PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            auditLogger.logFailedLogin(username);
            System.out.println("[AUTH] Login failed - wrong password: " + username);
            return null;
        }

        // Generate session token
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);
        auditLogger.logLogin(user);
        System.out.println("[AUTH] Login successful: " + username + " [" + user.getRole() + "]");
        return token;
    }

    @Override
    public boolean logout(String sessionToken) throws RemoteException {
        UserAccount user = activeSessions.remove(sessionToken);
        if (user != null) {
            auditLogger.logLogout(user);
            System.out.println("[AUTH] Logout: " + user.getUsername());
            return true;
        }
        return false;
    }

    @Override
    public UserAccount getCurrentUser(String sessionToken) throws RemoteException {
        return activeSessions.get(sessionToken);
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword, String sessionToken) throws RemoteException {
        UserAccount user = activeSessions.get(sessionToken);
        if (user == null) return false;

        // Verify old password
        UserAccount stored = dataStore.getUserById(user.getUserId());
        if (stored == null || !PasswordHasher.verifyPassword(oldPassword, stored.getPasswordHash())) {
            return false;
        }

        // Update password
        stored.setPasswordHash(PasswordHasher.hashPassword(newPassword));
        boolean success = dataStore.updateUser(stored);
        if (success) {
            // Update session with new user data
            activeSessions.put(sessionToken, stored);
            auditLogger.log(user, "CHANGE_PASSWORD", "users", user.getUserId(), "Password changed");
        }
        return success;
    }

    /**
     * Validate a session token and return the user. Used by other services.
     */
    public UserAccount validateSession(String sessionToken) {
        return activeSessions.get(sessionToken);
    }

    /**
     * Check if a session has a specific role.
     */
    public boolean hasRole(String sessionToken, String role) {
        UserAccount user = activeSessions.get(sessionToken);
        return user != null && user.getRole().equals(role);
    }
}
