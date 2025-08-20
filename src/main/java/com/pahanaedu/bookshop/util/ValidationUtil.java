package com.pahanaedu.bookshop.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for data validation
 */
public class ValidationUtil {
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Phone number pattern (supports various formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    // Item code pattern (alphanumeric with optional hyphens/underscores)
    private static final Pattern ITEM_CODE_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_-]{3,20}$"
    );
    
    // Account number pattern
    private static final Pattern ACCOUNT_NO_PATTERN = Pattern.compile(
        "^[A-Za-z0-9]{5,20}$"
    );
    
    // Username pattern
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,50}$"
    );

    /**
     * Validate if a string is not null and not empty
     * @param value The string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return StringUtils.isNotBlank(value);
    }

    /**
     * Validate string length
     * @param value The string to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if valid, false otherwise
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) return false;
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validate email format
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number format
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) return false;
        // Remove spaces, hyphens, and parentheses for validation
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate username format
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (StringUtils.isBlank(username)) return false;
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validate item code format
     * @param itemCode The item code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidItemCode(String itemCode) {
        if (StringUtils.isBlank(itemCode)) return false;
        return ITEM_CODE_PATTERN.matcher(itemCode.trim()).matches();
    }

    /**
     * Validate account number format
     * @param accountNo The account number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAccountNo(String accountNo) {
        if (StringUtils.isBlank(accountNo)) return false;
        return ACCOUNT_NO_PATTERN.matcher(accountNo.trim()).matches();
    }

    /**
     * Validate positive integer
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isPositiveInteger(Integer value) {
        return value != null && value > 0;
    }

    /**
     * Validate non-negative integer
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNonNegativeInteger(Integer value) {
        return value != null && value >= 0;
    }

    /**
     * Validate positive decimal
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isPositiveDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Validate non-negative decimal
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNonNegativeDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Validate decimal within range
     * @param value The value to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if valid, false otherwise
     */
    public static boolean isDecimalInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) return false;
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Validate integer within range
     * @param value The value to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if valid, false otherwise
     */
    public static boolean isIntegerInRange(Integer value, int min, int max) {
        return value != null && value >= min && value <= max;
    }

    /**
     * Validate date is not in the future
     * @param date The date to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotFutureDate(LocalDate date) {
        if (date == null) return false;
        return !date.isAfter(LocalDate.now());
    }

    /**
     * Validate date is within reasonable range for birth date
     * @param birthDate The birth date to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBirthDate(LocalDate birthDate) {
        if (birthDate == null) return true; // Optional field
        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(120); // Maximum age 120 years
        return !birthDate.isAfter(now) && !birthDate.isBefore(minDate);
    }

    /**
     * Validate year is reasonable
     * @param year The year to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidYear(Integer year) {
        if (year == null) return true; // Optional field
        int currentYear = LocalDate.now().getYear();
        return year >= 1900 && year <= currentYear + 1;
    }

    /**
     * Validate percentage value (0-100)
     * @param percentage The percentage to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPercentage(BigDecimal percentage) {
        if (percentage == null) return true; // Optional field
        return percentage.compareTo(BigDecimal.ZERO) >= 0 && 
               percentage.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    /**
     * Parse and validate integer from string
     * @param value The string value
     * @return Parsed integer or null if invalid
     */
    public static Integer parseInteger(String value) {
        if (StringUtils.isBlank(value)) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse and validate BigDecimal from string
     * @param value The string value
     * @return Parsed BigDecimal or null if invalid
     */
    public static BigDecimal parseBigDecimal(String value) {
        if (StringUtils.isBlank(value)) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse and validate LocalDate from string
     * @param value The string value (yyyy-MM-dd format)
     * @return Parsed LocalDate or null if invalid
     */
    public static LocalDate parseDate(String value) {
        if (StringUtils.isBlank(value)) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Sanitize string input (trim and handle null)
     * @param value The string to sanitize
     * @return Sanitized string or null
     */
    public static String sanitizeString(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Validate ISBN format (basic validation)
     * @param isbn The ISBN to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidISBN(String isbn) {
        if (StringUtils.isBlank(isbn)) return true; // Optional field
        String cleanISBN = isbn.replaceAll("[\\s\\-]", "");
        return cleanISBN.length() == 10 || cleanISBN.length() == 13;
    }

    /**
     * Validate barcode format (basic validation)
     * @param barcode The barcode to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidBarcode(String barcode) {
        if (StringUtils.isBlank(barcode)) return true; // Optional field
        String cleanBarcode = barcode.replaceAll("[\\s\\-]", "");
        return cleanBarcode.matches("^[0-9]{8,14}$");
    }

    // Private constructor to prevent instantiation
    private ValidationUtil() {}
}
