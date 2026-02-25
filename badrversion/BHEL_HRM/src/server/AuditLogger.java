package server;

import common.models.UserAccount;

/**
 * Audit logging utility.
 * Wraps CSVDataStore to provide convenient audit logging methods.
 */
public class AuditLogger {

    private final CSVDataStore dataStore;

    public AuditLogger(CSVDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void log(UserAccount user, String action, String targetTable, int targetId, String details) {
        if (user != null) {
            dataStore.addAuditLog(user.getUserId(), user.getUsername(), user.getRole(),
                                  action, targetTable, targetId, details);
        } else {
            dataStore.addAuditLog(0, "SYSTEM", "SYSTEM", action, targetTable, targetId, details);
        }
        System.out.println("[AUDIT] " + (user != null ? user.getUsername() : "SYSTEM") + " - " + action + ": " + details);
    }

    public void logLogin(UserAccount user) {
        log(user, "LOGIN", "users", user.getUserId(), "User logged in");
    }

    public void logLogout(UserAccount user) {
        log(user, "LOGOUT", "users", user.getUserId(), "User logged out");
    }

    public void logFailedLogin(String username) {
        dataStore.addAuditLog(0, username, "UNKNOWN", "FAILED_LOGIN", "users", 0,
                              "Failed login attempt for: " + username);
        System.out.println("[AUDIT] FAILED LOGIN: " + username);
    }
}
