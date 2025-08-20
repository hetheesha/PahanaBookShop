package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.ActivityLog;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for ActivityLog entity
 */
public class ActivityLogDAO implements BaseDAO<ActivityLog, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO activity_log (user_id, action, table_name, record_id, old_values, 
                                 new_values, ip_address, user_agent, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE activity_log SET user_id = ?, action = ?, table_name = ?, record_id = ?, 
                               old_values = ?, new_values = ?, ip_address = ?, user_agent = ?
        WHERE log_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM activity_log WHERE log_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT al.log_id, al.user_id, u.username, al.action, al.table_name, al.record_id, 
               al.old_values, al.new_values, al.ip_address, al.user_agent, al.created_at
        FROM activity_log al
        LEFT JOIN users u ON al.user_id = u.user_id
        WHERE al.log_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT al.log_id, al.user_id, u.username, al.action, al.table_name, al.record_id, 
               al.old_values, al.new_values, al.ip_address, al.user_agent, al.created_at
        FROM activity_log al
        LEFT JOIN users u ON al.user_id = u.user_id
        ORDER BY al.created_at DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM activity_log";

    private static final String EXISTS_SQL = "SELECT 1 FROM activity_log WHERE log_id = ?";

    private static final String FIND_BY_USER_SQL = """
        SELECT al.log_id, al.user_id, u.username, al.action, al.table_name, al.record_id, 
               al.old_values, al.new_values, al.ip_address, al.user_agent, al.created_at
        FROM activity_log al
        LEFT JOIN users u ON al.user_id = u.user_id
        WHERE al.user_id = ?
        ORDER BY al.created_at DESC
        LIMIT ? OFFSET ?
        """;

    private static final String FIND_BY_ACTION_SQL = """
        SELECT al.log_id, al.user_id, u.username, al.action, al.table_name, al.record_id, 
               al.old_values, al.new_values, al.ip_address, al.user_agent, al.created_at
        FROM activity_log al
        LEFT JOIN users u ON al.user_id = u.user_id
        WHERE al.action = ?
        ORDER BY al.created_at DESC
        LIMIT ? OFFSET ?
        """;

    private static final String FIND_BY_TABLE_SQL = """
        SELECT al.log_id, al.user_id, u.username, al.action, al.table_name, al.record_id, 
               al.old_values, al.new_values, al.ip_address, al.user_agent, al.created_at
        FROM activity_log al
        LEFT JOIN users u ON al.user_id = u.user_id
        WHERE al.table_name = ?
        ORDER BY al.created_at DESC
        LIMIT ? OFFSET ?
        """;

    private static final String DELETE_OLD_LOGS_SQL = """
        DELETE FROM activity_log WHERE created_at < ?
        """;

    @Override
    public ActivityLog save(ActivityLog activityLog) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            if (activityLog.getCreatedAt() == null) {
                activityLog.setCreatedAt(LocalDateTime.now());
            }

            stmt.setObject(1, activityLog.getUserId());
            stmt.setString(2, activityLog.getAction());
            stmt.setString(3, activityLog.getTableName());
            stmt.setObject(4, activityLog.getRecordId());

            // Handle JSON values safely
            String oldValues = activityLog.getOldValues();
            String newValues = activityLog.getNewValues();

            // If values are not valid JSON, wrap them in quotes or set to null
            stmt.setString(5, oldValues != null && !oldValues.trim().isEmpty() ? oldValues : null);
            stmt.setString(6, newValues != null && !newValues.trim().isEmpty() ? newValues : null);

            stmt.setString(7, activityLog.getIpAddress());
            stmt.setString(8, activityLog.getUserAgent());
            stmt.setTimestamp(9, Timestamp.valueOf(activityLog.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating activity log failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    activityLog.setLogId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating activity log failed, no ID obtained");
                }
            }

            return activityLog;

        } catch (SQLException e) {
            logger.error("Error creating activity log", e);
            throw new DAOException("Failed to create activity log", e);
        }
    }

    @Override
    public ActivityLog update(ActivityLog activityLog) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setObject(1, activityLog.getUserId());
            stmt.setString(2, activityLog.getAction());
            stmt.setString(3, activityLog.getTableName());
            stmt.setObject(4, activityLog.getRecordId());
            stmt.setString(5, activityLog.getOldValues());
            stmt.setString(6, activityLog.getNewValues());
            stmt.setString(7, activityLog.getIpAddress());
            stmt.setString(8, activityLog.getUserAgent());
            stmt.setInt(9, activityLog.getLogId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Activity log not found with ID: " + activityLog.getLogId());
            }

            return activityLog;

        } catch (SQLException e) {
            logger.error("Error updating activity log: {}", activityLog.getLogId(), e);
            throw new DAOException("Failed to update activity log", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error deleting activity log: {}", id, e);
            throw new DAOException("Failed to delete activity log", e);
        }
    }

    @Override
    public Optional<ActivityLog> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToActivityLog(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding activity log by ID: {}", id, e);
            throw new DAOException("Failed to find activity log by ID", e);
        }
    }

    @Override
    public List<ActivityLog> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<ActivityLog> activityLogs = new ArrayList<>();
            while (rs.next()) {
                activityLogs.add(mapResultSetToActivityLog(rs));
            }
            return activityLogs;

        } catch (SQLException e) {
            logger.error("Error finding all activity logs", e);
            throw new DAOException("Failed to find all activity logs", e);
        }
    }

    @Override
    public List<ActivityLog> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ActivityLog> activityLogs = new ArrayList<>();
                while (rs.next()) {
                    activityLogs.add(mapResultSetToActivityLog(rs));
                }
                return activityLogs;
            }

        } catch (SQLException e) {
            logger.error("Error finding activity logs with pagination", e);
            throw new DAOException("Failed to find activity logs with pagination", e);
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
            logger.error("Error counting activity logs", e);
            throw new DAOException("Failed to count activity logs", e);
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
            logger.error("Error checking if activity log exists: {}", id, e);
            throw new DAOException("Failed to check if activity log exists", e);
        }
    }

    /**
     * Find activity logs by user
     * @param userId User ID
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of activity logs
     * @throws DAOException if find operation fails
     */
    public List<ActivityLog> findByUser(Integer userId, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USER_SQL)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ActivityLog> activityLogs = new ArrayList<>();
                while (rs.next()) {
                    activityLogs.add(mapResultSetToActivityLog(rs));
                }
                return activityLogs;
            }

        } catch (SQLException e) {
            logger.error("Error finding activity logs by user: {}", userId, e);
            throw new DAOException("Failed to find activity logs by user", e);
        }
    }

    /**
     * Find activity logs by action
     * @param action Action type
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of activity logs
     * @throws DAOException if find operation fails
     */
    public List<ActivityLog> findByAction(String action, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ACTION_SQL)) {

            stmt.setString(1, action);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ActivityLog> activityLogs = new ArrayList<>();
                while (rs.next()) {
                    activityLogs.add(mapResultSetToActivityLog(rs));
                }
                return activityLogs;
            }

        } catch (SQLException e) {
            logger.error("Error finding activity logs by action: {}", action, e);
            throw new DAOException("Failed to find activity logs by action", e);
        }
    }

    /**
     * Find activity logs by table name
     * @param tableName Table name
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of activity logs
     * @throws DAOException if find operation fails
     */
    public List<ActivityLog> findByTable(String tableName, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_TABLE_SQL)) {

            stmt.setString(1, tableName);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ActivityLog> activityLogs = new ArrayList<>();
                while (rs.next()) {
                    activityLogs.add(mapResultSetToActivityLog(rs));
                }
                return activityLogs;
            }

        } catch (SQLException e) {
            logger.error("Error finding activity logs by table: {}", tableName, e);
            throw new DAOException("Failed to find activity logs by table", e);
        }
    }

    /**
     * Delete old activity logs before the specified date
     * @param beforeDate Date before which logs should be deleted
     * @return Number of deleted logs
     * @throws DAOException if delete operation fails
     */
    public int deleteOldLogs(LocalDateTime beforeDate) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_OLD_LOGS_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(beforeDate));
            int deletedCount = stmt.executeUpdate();

            logger.info("Deleted {} old activity logs before {}", deletedCount, beforeDate);
            return deletedCount;

        } catch (SQLException e) {
            logger.error("Error deleting old activity logs before: {}", beforeDate, e);
            throw new DAOException("Failed to delete old activity logs", e);
        }
    }

    private ActivityLog mapResultSetToActivityLog(ResultSet rs) throws SQLException {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setLogId(rs.getInt("log_id"));

        Integer userId = rs.getObject("user_id", Integer.class);
        activityLog.setUserId(userId);
        activityLog.setUsername(rs.getString("username"));

        activityLog.setAction(rs.getString("action"));
        activityLog.setTableName(rs.getString("table_name"));

        Integer recordId = rs.getObject("record_id", Integer.class);
        activityLog.setRecordId(recordId);

        activityLog.setOldValues(rs.getString("old_values"));
        activityLog.setNewValues(rs.getString("new_values"));
        activityLog.setIpAddress(rs.getString("ip_address"));
        activityLog.setUserAgent(rs.getString("user_agent"));
        activityLog.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return activityLog;
    }
}
