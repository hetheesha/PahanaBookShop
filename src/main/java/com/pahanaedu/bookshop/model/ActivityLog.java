package com.pahanaedu.bookshop.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ActivityLog entity for tracking user activities and system changes
 */
public class ActivityLog {
    private Integer logId;
    private Integer userId;
    private String username; // For display purposes
    private String action;
    private String tableName;
    private Integer recordId;
    private String oldValues; // JSON string
    private String newValues; // JSON string
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    // Constructors
    public ActivityLog() {
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLog(Integer userId, String action, String tableName, Integer recordId) {
        this();
        this.userId = userId;
        this.action = action;
        this.tableName = tableName;
        this.recordId = recordId;
    }

    public ActivityLog(Integer userId, String action, String tableName, Integer recordId,
                      String oldValues, String newValues, String ipAddress, String userAgent) {
        this(userId, action, tableName, recordId);
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return Objects.equals(logId, that.logId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", tableName='" + tableName + '\'' +
                ", recordId=" + recordId +
                ", createdAt=" + createdAt +
                '}';
    }

    // Common action constants
    public static final class Actions {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String VIEW = "VIEW";
        public static final String SEARCH = "SEARCH";
        public static final String EXPORT = "EXPORT";
        public static final String IMPORT = "IMPORT";
        public static final String BILL_CREATED = "BILL_CREATED";
        public static final String BILL_CANCELLED = "BILL_CANCELLED";
        public static final String STOCK_ADJUSTMENT = "STOCK_ADJUSTMENT";
        public static final String PASSWORD_CHANGE = "PASSWORD_CHANGE";
        public static final String SETTINGS_CHANGE = "SETTINGS_CHANGE";
    }

    // Common table names
    public static final class Tables {
        public static final String USERS = "users";
        public static final String CUSTOMERS = "customers";
        public static final String CATEGORIES = "categories";
        public static final String ITEMS = "items";
        public static final String BILLS = "bills";
        public static final String BILL_ITEMS = "bill_items";
        public static final String STOCK_MOVEMENTS = "stock_movements";
        public static final String SYSTEM_SETTINGS = "system_settings";
    }
}
