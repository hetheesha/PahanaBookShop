package com.pahanaedu.bookshop.service;

import com.pahanaedu.bookshop.dao.ActivityLogDAO;
import com.pahanaedu.bookshop.dao.DAOException;
import com.pahanaedu.bookshop.dao.UserDAO;
import com.pahanaedu.bookshop.model.ActivityLog;
import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.util.PasswordUtil;
import com.pahanaedu.bookshop.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for User-related business logic
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserDAO userDAO;
    private final ActivityLogDAO activityLogDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    /**
     * Authenticate user with username and password
     * @param username Username
     * @param password Plain text password
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return Authenticated user or empty if authentication fails
     * @throws ServiceException if authentication process fails
     */
    public Optional<User> authenticate(String username, String password, String ipAddress, String userAgent) throws ServiceException {
        try {
            if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
                return Optional.empty();
            }

            Optional<User> userOpt = userDAO.findByUsername(username.trim());
            if (userOpt.isEmpty()) {
                logActivity(null, ActivityLog.Actions.LOGIN, "users", null, 
                          "Failed login attempt for username: " + username, ipAddress, userAgent);
                return Optional.empty();
            }

            User user = userOpt.get();
            if (!user.isActive()) {
                logActivity(user.getUserId(), ActivityLog.Actions.LOGIN, "users", user.getUserId(), 
                          "Login attempt for inactive user", ipAddress, userAgent);
                return Optional.empty();
            }

            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                logActivity(user.getUserId(), ActivityLog.Actions.LOGIN, "users", user.getUserId(), 
                          "Failed login attempt - invalid password", ipAddress, userAgent);
                return Optional.empty();
            }

            // Update last login
            userDAO.updateLastLogin(user.getUserId());
            
            // Log successful login
            logActivity(user.getUserId(), ActivityLog.Actions.LOGIN, "users", user.getUserId(), 
                      "Successful login", ipAddress, userAgent);

            logger.info("User authenticated successfully: {}", username);
            return Optional.of(user);

        } catch (DAOException e) {
            logger.error("Error during user authentication: {}", username, e);
            throw new ServiceException("Authentication failed", e);
        }
    }

    /**
     * Create a new user
     * @param user User to create
     * @param createdBy ID of user creating this user
     * @return Created user
     * @throws ServiceException if creation fails
     */
    public User createUser(User user, Integer createdBy) throws ServiceException {
        try {
            // Validate user data
            Map<String, String> validationErrors = validateUser(user, true);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if username already exists
            if (userDAO.findByUsername(user.getUsername()).isPresent()) {
                throw new ServiceException("Username already exists: " + user.getUsername());
            }

            // Hash password
            user.setPasswordHash(PasswordUtil.hashPassword(user.getPasswordHash()));
            user.setCreatedBy(createdBy);

            User savedUser = userDAO.save(user);

            // Log activity
            logActivity(createdBy, ActivityLog.Actions.CREATE, "users", savedUser.getUserId(), 
                      "User created: " + savedUser.getUsername(), null, null);

            logger.info("User created successfully: {}", savedUser.getUsername());
            return savedUser;

        } catch (DAOException e) {
            logger.error("Error creating user: {}", user.getUsername(), e);
            throw new ServiceException("Failed to create user", e);
        }
    }

    /**
     * Update an existing user
     * @param user User to update
     * @param updatedBy ID of user performing the update
     * @return Updated user
     * @throws ServiceException if update fails
     */
    public User updateUser(User user, Integer updatedBy) throws ServiceException {
        try {
            // Validate user data
            Map<String, String> validationErrors = validateUser(user, false);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if user exists
            Optional<User> existingUserOpt = userDAO.findById(user.getUserId());
            if (existingUserOpt.isEmpty()) {
                throw new ServiceException("User not found with ID: " + user.getUserId());
            }

            User existingUser = existingUserOpt.get();

            // Check if username is being changed and if new username already exists
            if (!existingUser.getUsername().equals(user.getUsername())) {
                if (userDAO.findByUsername(user.getUsername()).isPresent()) {
                    throw new ServiceException("Username already exists: " + user.getUsername());
                }
            }

            User updatedUser = userDAO.update(user);

            // Log activity
            logActivity(updatedBy, ActivityLog.Actions.UPDATE, "users", updatedUser.getUserId(), 
                      "User updated: " + updatedUser.getUsername(), null, null);

            logger.info("User updated successfully: {}", updatedUser.getUsername());
            return updatedUser;

        } catch (DAOException e) {
            logger.error("Error updating user: {}", user.getUserId(), e);
            throw new ServiceException("Failed to update user", e);
        }
    }

    /**
     * Change user password
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @param changedBy ID of user performing the change
     * @throws ServiceException if password change fails
     */
    public void changePassword(Integer userId, String currentPassword, String newPassword, Integer changedBy) throws ServiceException {
        try {
            Optional<User> userOpt = userDAO.findById(userId);
            if (userOpt.isEmpty()) {
                throw new ServiceException("User not found with ID: " + userId);
            }

            User user = userOpt.get();

            // Verify current password (unless admin is changing it)
            if (!userId.equals(changedBy)) {
                // Admin changing password - skip current password verification
            } else {
                if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
                    throw new ServiceException("Current password is incorrect");
                }
            }

            // Validate new password
            String passwordError = PasswordUtil.getPasswordValidationError(newPassword);
            if (passwordError != null) {
                throw new ServiceException(passwordError);
            }

            // Hash and update password
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            userDAO.updatePassword(userId, hashedPassword);

            // Log activity
            logActivity(changedBy, ActivityLog.Actions.PASSWORD_CHANGE, "users", userId, 
                      "Password changed for user: " + user.getUsername(), null, null);

            logger.info("Password changed successfully for user: {}", user.getUsername());

        } catch (DAOException e) {
            logger.error("Error changing password for user: {}", userId, e);
            throw new ServiceException("Failed to change password", e);
        }
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @return User if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<User> getUserById(Integer userId) throws ServiceException {
        try {
            return userDAO.findById(userId);
        } catch (DAOException e) {
            logger.error("Error getting user by ID: {}", userId, e);
            throw new ServiceException("Failed to get user", e);
        }
    }

    /**
     * Get user by username
     * @param username Username
     * @return User if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<User> getUserByUsername(String username) throws ServiceException {
        try {
            return userDAO.findByUsername(username);
        } catch (DAOException e) {
            logger.error("Error getting user by username: {}", username, e);
            throw new ServiceException("Failed to get user", e);
        }
    }

    /**
     * Get all users with pagination
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of users
     * @throws ServiceException if retrieval fails
     */
    public List<User> getUsers(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return userDAO.findAll(offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting users with pagination", e);
            throw new ServiceException("Failed to get users", e);
        }
    }

    /**
     * Get total count of users
     * @return Total count
     * @throws ServiceException if count fails
     */
    public long getUserCount() throws ServiceException {
        try {
            return userDAO.count();
        } catch (DAOException e) {
            logger.error("Error counting users", e);
            throw new ServiceException("Failed to count users", e);
        }
    }

    /**
     * Delete a user
     * @param userId User ID to delete
     * @param deletedBy ID of user performing the deletion
     * @return true if deleted, false if not found
     * @throws ServiceException if deletion fails
     */
    public boolean deleteUser(Integer userId, Integer deletedBy) throws ServiceException {
        try {
            Optional<User> userOpt = userDAO.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            boolean deleted = userDAO.delete(userId);

            if (deleted) {
                // Log activity
                logActivity(deletedBy, ActivityLog.Actions.DELETE, "users", userId, 
                          "User deleted: " + user.getUsername(), null, null);
                logger.info("User deleted successfully: {}", user.getUsername());
            }

            return deleted;

        } catch (DAOException e) {
            logger.error("Error deleting user: {}", userId, e);
            throw new ServiceException("Failed to delete user", e);
        }
    }

    /**
     * Validate user data
     * @param user User to validate
     * @param isNew Whether this is a new user (password required)
     * @return Map of validation errors
     */
    private Map<String, String> validateUser(User user, boolean isNew) {
        Map<String, String> errors = new HashMap<>();

        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            errors.put("username", "Username must be 3-50 characters and contain only letters, numbers, and underscores");
        }

        if (!ValidationUtil.isValidLength(user.getFullName(), 2, 100)) {
            errors.put("fullName", "Full name must be 2-100 characters");
        }

        if (user.getEmail() != null && !ValidationUtil.isValidEmail(user.getEmail())) {
            errors.put("email", "Invalid email format");
        }

        if (user.getPhone() != null && !ValidationUtil.isValidPhone(user.getPhone())) {
            errors.put("phone", "Invalid phone number format");
        }

        if (user.getRole() == null) {
            errors.put("role", "Role is required");
        }

        if (isNew) {
            String passwordError = PasswordUtil.getPasswordValidationError(user.getPasswordHash());
            if (passwordError != null) {
                errors.put("password", passwordError);
            }
        }

        return errors;
    }

    /**
     * Log activity
     */
    private void logActivity(Integer userId, String action, String tableName, Integer recordId, 
                           String notes, String ipAddress, String userAgent) {
        try {
            ActivityLog log = new ActivityLog(userId, action, tableName, recordId);
            log.setNewValues(notes);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            activityLogDAO.save(log);
        } catch (DAOException e) {
            logger.warn("Failed to log activity", e);
        }
    }
}
