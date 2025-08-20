package com.pahanaedu.bookshop.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Utility class for password hashing and validation using BCrypt
 */
public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    
    // BCrypt cost factor (number of rounds)
    private static final int BCRYPT_COST = 12;
    
    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private static final Pattern WEAK_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$"
    );
    
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Hash a password using BCrypt
     * @param plainPassword The plain text password
     * @return Hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verify a password against its hash
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
            return result.verified;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    /**
     * Validate password strength
     * @param password The password to validate
     * @return PasswordStrength enum indicating the strength level
     */
    public static PasswordStrength validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.VERY_WEAK;
        }
        
        if (password.length() < 8) {
            return PasswordStrength.WEAK;
        }
        
        if (WEAK_PASSWORD_PATTERN.matcher(password).matches()) {
            return PasswordStrength.MEDIUM;
        }
        
        if (PASSWORD_PATTERN.matcher(password).matches()) {
            if (password.length() >= 12) {
                return PasswordStrength.VERY_STRONG;
            }
            return PasswordStrength.STRONG;
        }
        
        return PasswordStrength.WEAK;
    }

    /**
     * Check if password meets minimum requirements
     * @param password The password to check
     * @return true if password meets minimum requirements
     */
    public static boolean isPasswordValid(String password) {
        PasswordStrength strength = validatePasswordStrength(password);
        return strength.ordinal() >= PasswordStrength.MEDIUM.ordinal();
    }

    /**
     * Generate a random password
     * @param length The length of the password
     * @param includeSpecialChars Whether to include special characters
     * @return Generated password
     */
    public static String generateRandomPassword(int length, boolean includeSpecialChars) {
        if (length < 6) {
            throw new IllegalArgumentException("Password length must be at least 6 characters");
        }
        
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String specialChars = "@$!%*?&";
        
        StringBuilder charset = new StringBuilder();
        charset.append(lowercase).append(uppercase).append(digits);
        
        if (includeSpecialChars) {
            charset.append(specialChars);
        }
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each required category
        password.append(lowercase.charAt(secureRandom.nextInt(lowercase.length())));
        password.append(uppercase.charAt(secureRandom.nextInt(uppercase.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        
        if (includeSpecialChars) {
            password.append(specialChars.charAt(secureRandom.nextInt(specialChars.length())));
        }
        
        // Fill the rest randomly
        for (int i = password.length(); i < length; i++) {
            password.append(charset.charAt(secureRandom.nextInt(charset.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Generate a random password with default settings (12 characters, with special chars)
     * @return Generated password
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(12, true);
    }

    /**
     * Get password validation error message
     * @param password The password to validate
     * @return Error message or null if password is valid
     */
    public static String getPasswordValidationError(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        
        PasswordStrength strength = validatePasswordStrength(password);
        
        switch (strength) {
            case VERY_WEAK:
                return "Password is too weak. Must be at least 6 characters long";
            case WEAK:
                return "Password is weak. Should contain letters and numbers";
            case MEDIUM:
                return null; // Acceptable
            case STRONG:
            case VERY_STRONG:
                return null; // Good
            default:
                return "Invalid password";
        }
    }

    /**
     * Shuffle a string randomly
     * @param input The string to shuffle
     * @return Shuffled string
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * Password strength enumeration
     */
    public enum PasswordStrength {
        VERY_WEAK("Very Weak"),
        WEAK("Weak"),
        MEDIUM("Medium"),
        STRONG("Strong"),
        VERY_STRONG("Very Strong");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Private constructor to prevent instantiation
    private PasswordUtil() {}
}
