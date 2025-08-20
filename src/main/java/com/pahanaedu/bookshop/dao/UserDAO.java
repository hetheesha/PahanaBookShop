package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for User entity
 */
public class UserDAO implements BaseDAO<User, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO users (username, password_hash, full_name, email, phone, role, status, created_by, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE users SET username = ?, full_name = ?, email = ?, phone = ?, role = ?, status = ?, updated_at = ?
        WHERE user_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM users WHERE user_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT user_id, username, password_hash, full_name, email, phone, role, status, 
               last_login, created_at, updated_at, created_by
        FROM users WHERE user_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT user_id, username, password_hash, full_name, email, phone, role, status, 
               last_login, created_at, updated_at, created_by
        FROM users ORDER BY created_at DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM users";

    private static final String EXISTS_SQL = "SELECT 1 FROM users WHERE user_id = ?";

    private static final String FIND_BY_USERNAME_SQL = FIND_BY_ID_SQL.replace("user_id = ?", "username = ?");

    private static final String UPDATE_LAST_LOGIN_SQL = "UPDATE users SET last_login = ? WHERE user_id = ?";

    private static final String UPDATE_PASSWORD_SQL = "UPDATE users SET password_hash = ?, updated_at = ? WHERE user_id = ?";

    @Override
    public User save(User user) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getRole().getValue());
            stmt.setString(7, user.getStatus().getValue());
            stmt.setObject(8, user.getCreatedBy());
            stmt.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(user.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating user failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating user failed, no ID obtained");
                }
            }

            logger.info("User created successfully: {}", user.getUsername());
            return user;

        } catch (SQLException e) {
            logger.error("Error creating user: {}", user.getUsername(), e);
            throw new DAOException("Failed to create user", e);
        }
    }

    @Override
    public User update(User user) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            user.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getRole().getValue());
            stmt.setString(6, user.getStatus().getValue());
            stmt.setTimestamp(7, Timestamp.valueOf(user.getUpdatedAt()));
            stmt.setInt(8, user.getUserId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("User not found with ID: " + user.getUserId());
            }

            logger.info("User updated successfully: {}", user.getUsername());
            return user;

        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getUserId(), e);
            throw new DAOException("Failed to update user", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("User deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting user: {}", id, e);
            throw new DAOException("Failed to delete user", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw new DAOException("Failed to find user by ID", e);
        }
    }

    @Override
    public List<User> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;

        } catch (SQLException e) {
            logger.error("Error finding all users", e);
            throw new DAOException("Failed to find all users", e);
        }
    }

    @Override
    public List<User> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                return users;
            }

        } catch (SQLException e) {
            logger.error("Error finding users with pagination", e);
            throw new DAOException("Failed to find users with pagination", e);
        }
    }

    @Override
    public long count() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error counting users", e);
            throw new DAOException("Failed to count users", e);
        }
    }

    @Override
    public boolean exists(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if user exists: {}", id, e);
            throw new DAOException("Failed to check if user exists", e);
        }
    }

    /**
     * Find user by username
     * @param username Username to search for
     * @return Optional containing the user if found
     * @throws DAOException if find operation fails
     */
    public Optional<User> findByUsername(String username) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new DAOException("Failed to find user by username", e);
        }
    }

    /**
     * Update user's last login timestamp
     * @param userId User ID
     * @throws DAOException if update operation fails
     */
    public void updateLastLogin(Integer userId) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating last login for user: {}", userId, e);
            throw new DAOException("Failed to update last login", e);
        }
    }

    /**
     * Update user's password
     * @param userId User ID
     * @param passwordHash New password hash
     * @throws DAOException if update operation fails
     */
    public void updatePassword(Integer userId, String passwordHash) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PASSWORD_SQL)) {

            stmt.setString(1, passwordHash);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("User not found with ID: " + userId);
            }

        } catch (SQLException e) {
            logger.error("Error updating password for user: {}", userId, e);
            throw new DAOException("Failed to update password", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(User.UserRole.fromString(rs.getString("role")));
        user.setStatus(User.UserStatus.fromString(rs.getString("status")));

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }

        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Integer createdBy = rs.getObject("created_by", Integer.class);
        user.setCreatedBy(createdBy);

        return user;
    }
}
