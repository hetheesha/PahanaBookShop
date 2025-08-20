package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.SystemSettings;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for SystemSettings entity
 */
public class SystemSettingsDAO implements BaseDAO<SystemSettings, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(SystemSettingsDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO system_settings (setting_key, setting_value, setting_type, description, 
                                    is_editable, updated_at, updated_by)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE system_settings SET setting_key = ?, setting_value = ?, setting_type = ?, 
                                  description = ?, is_editable = ?, updated_at = ?, updated_by = ?
        WHERE setting_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM system_settings WHERE setting_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT ss.setting_id, ss.setting_key, ss.setting_value, ss.setting_type, 
               ss.description, ss.is_editable, ss.updated_at, ss.updated_by, 
               u.full_name as updated_by_name
        FROM system_settings ss
        LEFT JOIN users u ON ss.updated_by = u.user_id
        WHERE ss.setting_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT ss.setting_id, ss.setting_key, ss.setting_value, ss.setting_type, 
               ss.description, ss.is_editable, ss.updated_at, ss.updated_by, 
               u.full_name as updated_by_name
        FROM system_settings ss
        LEFT JOIN users u ON ss.updated_by = u.user_id
        ORDER BY ss.setting_key
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM system_settings";

    private static final String EXISTS_SQL = "SELECT 1 FROM system_settings WHERE setting_id = ?";

    private static final String FIND_BY_KEY_SQL = FIND_BY_ID_SQL.replace("ss.setting_id = ?", "ss.setting_key = ?");

    private static final String UPDATE_VALUE_SQL = """
        UPDATE system_settings SET setting_value = ?, updated_at = ?, updated_by = ?
        WHERE setting_key = ?
        """;

    private static final String FIND_EDITABLE_SQL = """
        SELECT ss.setting_id, ss.setting_key, ss.setting_value, ss.setting_type, 
               ss.description, ss.is_editable, ss.updated_at, ss.updated_by, 
               u.full_name as updated_by_name
        FROM system_settings ss
        LEFT JOIN users u ON ss.updated_by = u.user_id
        WHERE ss.is_editable = true
        ORDER BY ss.setting_key
        """;

    @Override
    public SystemSettings save(SystemSettings systemSettings) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            if (systemSettings.getUpdatedAt() == null) {
                systemSettings.setUpdatedAt(LocalDateTime.now());
            }

            stmt.setString(1, systemSettings.getSettingKey());
            stmt.setString(2, systemSettings.getSettingValue());
            stmt.setString(3, systemSettings.getSettingType().getValue());
            stmt.setString(4, systemSettings.getDescription());
            stmt.setBoolean(5, systemSettings.getIsEditable());
            stmt.setTimestamp(6, Timestamp.valueOf(systemSettings.getUpdatedAt()));
            stmt.setObject(7, systemSettings.getUpdatedBy());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating system setting failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    systemSettings.setSettingId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating system setting failed, no ID obtained");
                }
            }

            logger.info("System setting created successfully: {}", systemSettings.getSettingKey());
            return systemSettings;

        } catch (SQLException e) {
            logger.error("Error creating system setting: {}", systemSettings.getSettingKey(), e);
            throw new DAOException("Failed to create system setting", e);
        }
    }

    @Override
    public SystemSettings update(SystemSettings systemSettings) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            systemSettings.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, systemSettings.getSettingKey());
            stmt.setString(2, systemSettings.getSettingValue());
            stmt.setString(3, systemSettings.getSettingType().getValue());
            stmt.setString(4, systemSettings.getDescription());
            stmt.setBoolean(5, systemSettings.getIsEditable());
            stmt.setTimestamp(6, Timestamp.valueOf(systemSettings.getUpdatedAt()));
            stmt.setObject(7, systemSettings.getUpdatedBy());
            stmt.setInt(8, systemSettings.getSettingId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("System setting not found with ID: " + systemSettings.getSettingId());
            }

            logger.info("System setting updated successfully: {}", systemSettings.getSettingKey());
            return systemSettings;

        } catch (SQLException e) {
            logger.error("Error updating system setting: {}", systemSettings.getSettingId(), e);
            throw new DAOException("Failed to update system setting", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("System setting deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting system setting: {}", id, e);
            throw new DAOException("Failed to delete system setting", e);
        }
    }

    @Override
    public Optional<SystemSettings> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSystemSettings(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding system setting by ID: {}", id, e);
            throw new DAOException("Failed to find system setting by ID", e);
        }
    }

    @Override
    public List<SystemSettings> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<SystemSettings> systemSettings = new ArrayList<>();
            while (rs.next()) {
                systemSettings.add(mapResultSetToSystemSettings(rs));
            }
            return systemSettings;

        } catch (SQLException e) {
            logger.error("Error finding all system settings", e);
            throw new DAOException("Failed to find all system settings", e);
        }
    }

    @Override
    public List<SystemSettings> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<SystemSettings> systemSettings = new ArrayList<>();
                while (rs.next()) {
                    systemSettings.add(mapResultSetToSystemSettings(rs));
                }
                return systemSettings;
            }

        } catch (SQLException e) {
            logger.error("Error finding system settings with pagination", e);
            throw new DAOException("Failed to find system settings with pagination", e);
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
            logger.error("Error counting system settings", e);
            throw new DAOException("Failed to count system settings", e);
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
            logger.error("Error checking if system setting exists: {}", id, e);
            throw new DAOException("Failed to check if system setting exists", e);
        }
    }

    /**
     * Find system setting by key
     * @param settingKey Setting key to search for
     * @return Optional containing the setting if found
     * @throws DAOException if find operation fails
     */
    public Optional<SystemSettings> findByKey(String settingKey) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_KEY_SQL)) {

            stmt.setString(1, settingKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSystemSettings(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding system setting by key: {}", settingKey, e);
            throw new DAOException("Failed to find system setting by key", e);
        }
    }

    /**
     * Update setting value by key
     * @param settingKey Setting key
     * @param settingValue New setting value
     * @param updatedBy User ID who updated the setting
     * @throws DAOException if update operation fails
     */
    public void updateValue(String settingKey, String settingValue, Integer updatedBy) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_VALUE_SQL)) {

            stmt.setString(1, settingValue);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setObject(3, updatedBy);
            stmt.setString(4, settingKey);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("System setting not found with key: " + settingKey);
            }

            logger.info("System setting value updated: {}", settingKey);

        } catch (SQLException e) {
            logger.error("Error updating system setting value: {}", settingKey, e);
            throw new DAOException("Failed to update system setting value", e);
        }
    }

    /**
     * Find all editable system settings
     * @return List of editable settings
     * @throws DAOException if find operation fails
     */
    public List<SystemSettings> findEditable() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_EDITABLE_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<SystemSettings> systemSettings = new ArrayList<>();
            while (rs.next()) {
                systemSettings.add(mapResultSetToSystemSettings(rs));
            }
            return systemSettings;

        } catch (SQLException e) {
            logger.error("Error finding editable system settings", e);
            throw new DAOException("Failed to find editable system settings", e);
        }
    }

    /**
     * Get setting value by key
     * @param settingKey Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value
     * @throws DAOException if find operation fails
     */
    public String getValue(String settingKey, String defaultValue) throws DAOException {
        Optional<SystemSettings> setting = findByKey(settingKey);
        return setting.map(SystemSettings::getSettingValue).orElse(defaultValue);
    }

    private SystemSettings mapResultSetToSystemSettings(ResultSet rs) throws SQLException {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setSettingId(rs.getInt("setting_id"));
        systemSettings.setSettingKey(rs.getString("setting_key"));
        systemSettings.setSettingValue(rs.getString("setting_value"));
        systemSettings.setSettingType(SystemSettings.SettingType.fromString(rs.getString("setting_type")));
        systemSettings.setDescription(rs.getString("description"));
        systemSettings.setIsEditable(rs.getBoolean("is_editable"));
        systemSettings.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Integer updatedBy = rs.getObject("updated_by", Integer.class);
        systemSettings.setUpdatedBy(updatedBy);
        systemSettings.setUpdatedByName(rs.getString("updated_by_name"));

        return systemSettings;
    }
}
