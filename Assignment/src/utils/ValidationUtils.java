package utils;

import java.util.regex.Pattern;

/**
 * ValidationUtils - Provides validation methods for employee data
 */
public class ValidationUtils {
    
    // Malaysian IC format: YYMMDD-PB-###G (e.g., 900101-01-1234)
    private static final Pattern MALAYSIAN_IC_PATTERN = 
            Pattern.compile("^\\d{6}-\\d{2}-\\d{4}$");
    
    // Passport format: Alphanumeric, 6-9 characters
    private static final Pattern PASSPORT_PATTERN = 
            Pattern.compile("^[A-Z0-9]{6,9}$");
    
    // Email format
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Phone number format (Malaysian): 01X-XXXXXXX or 01X-XXXXXXXX
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^(\\+?6?01[0-9]-?[0-9]{7,8})|([0-9]{10,11})$");
    
    /**
     * Validates IC/Passport number format
     */
    public static boolean isValidIcPassport(String icPassport) {
        if (icPassport == null || icPassport.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = icPassport.trim().toUpperCase();
        
        // Check if it's a valid Malaysian IC
        if (MALAYSIAN_IC_PATTERN.matcher(trimmed).matches()) {
            return isValidMalaysianIC(trimmed);
        }
        
        // Check if it's a valid passport
        if (PASSPORT_PATTERN.matcher(trimmed).matches()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Validates Malaysian IC format and checks date validity
     */
    private static boolean isValidMalaysianIC(String ic) {
        try {
            String[] parts = ic.split("-");
            if (parts.length != 3) return false;
            
            String dateStr = parts[0]; // YYMMDD
            int year = Integer.parseInt(dateStr.substring(0, 2));
            int month = Integer.parseInt(dateStr.substring(2, 4));
            int day = Integer.parseInt(dateStr.substring(4, 6));
            
            // Basic date validation
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            
            // State code validation (01-16 are valid Malaysian states)
            int stateCode = Integer.parseInt(parts[1]);
            if (stateCode < 1 || stateCode > 16) return false;
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates phone number format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        String cleaned = phoneNumber.replaceAll("\\s+", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    /**
     * Validates that a string is not null or empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validates name (only letters and spaces, 2-50 characters)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && 
               trimmed.length() <= 50 && 
               trimmed.matches("^[a-zA-Z\\s]+$");
    }
    
    /**
     * Validates username (alphanumeric, 4-20 characters)
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String trimmed = username.trim();
        return trimmed.length() >= 4 && 
               trimmed.length() <= 20 && 
               trimmed.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * Validates password strength (min 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Validates password strength with detailed requirements
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        
        if (password.length() > 50) {
            return "Password must not exceed 50 characters";
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        // For production, you might want stricter requirements
        // Currently just checking minimum length
        
        return null; // Valid password
    }
    
    /**
     * Validates leave balance (must be non-negative)
     */
    public static boolean isValidLeaveBalance(double balance) {
        return balance >= 0 && balance <= 365;
    }
    
    /**
     * Validates number of leave days (must be positive)
     */
    public static boolean isValidLeaveDays(double days) {
        return days > 0 && days <= 365;
    }
    
    /**
     * Sanitizes string input to prevent injection attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                   .replaceAll("[<>\"']", "")  // Remove potential HTML/script chars
                   .replaceAll("\\r\\n|\\r|\\n", " "); // Replace newlines with spaces
    }
    
    /**
     * Validates role (must be HR or EMPLOYEE)
     */
    public static boolean isValidRole(String role) {
        return "HR".equalsIgnoreCase(role) || "EMPLOYEE".equalsIgnoreCase(role);
    }
    
    /**
     * Validates leave type
     */
    public static boolean isValidLeaveType(String leaveType) {
        if (leaveType == null) return false;
        String upper = leaveType.toUpperCase();
        return upper.equals("ANNUAL") || 
               upper.equals("SICK") || 
               upper.equals("EMERGENCY") || 
               upper.equals("UNPAID") ||
               upper.equals("MATERNITY") ||
               upper.equals("PATERNITY");
    }
    
    /**
     * Validates leave status
     */
    public static boolean isValidLeaveStatus(String status) {
        if (status == null) return false;
        String upper = status.toUpperCase();
        return upper.equals("PENDING") || 
               upper.equals("APPROVED") || 
               upper.equals("REJECTED");
    }
    
    /**
     * Comprehensive employee data validation
     */
    public static String validateEmployeeData(String firstName, String lastName, 
                                              String icPassport, String email, 
                                              String username, String password) {
        if (!isValidName(firstName)) {
            return "Invalid first name. Must be 2-50 characters, letters only.";
        }
        
        if (!isValidName(lastName)) {
            return "Invalid last name. Must be 2-50 characters, letters only.";
        }
        
        if (!isValidIcPassport(icPassport)) {
            return "Invalid IC/Passport format. Use format: YYMMDD-PB-###G for IC or alphanumeric 6-9 chars for passport.";
        }
        
        if (!isValidEmail(email)) {
            return "Invalid email format.";
        }
        
        if (!isValidUsername(username)) {
            return "Invalid username. Must be 4-20 characters, alphanumeric and underscores only.";
        }
        
        String passwordError = validatePasswordStrength(password);
        if (passwordError != null) {
            return passwordError;
        }
        
        return null; // All validations passed
    }
}
