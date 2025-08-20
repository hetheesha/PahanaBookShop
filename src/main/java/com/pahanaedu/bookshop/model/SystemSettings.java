package com.pahanaedu.bookshop.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * SystemSettings entity for storing application configuration
 */
public class SystemSettings {
    private Integer settingId;
    private String settingKey;
    private String settingValue;
    private SettingType settingType;
    private String description;
    private Boolean isEditable;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
    private String updatedByName; // For display purposes

    // Constructors
    public SystemSettings() {
        this.settingType = SettingType.STRING;
        this.isEditable = true;
        this.updatedAt = LocalDateTime.now();
    }

    public SystemSettings(String settingKey, String settingValue, SettingType settingType, String description) {
        this();
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingType = settingType;
        this.description = description;
    }

    // Getters and Setters
    public Integer getSettingId() {
        return settingId;
    }

    public void setSettingId(Integer settingId) {
        this.settingId = settingId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsEditable() {
        return isEditable;
    }

    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    // Utility methods
    public boolean isEditable() {
        return isEditable != null && isEditable;
    }

    public String getValueAsString() {
        return settingValue;
    }

    public Integer getValueAsInteger() {
        if (settingValue == null) return null;
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getValueAsDouble() {
        if (settingValue == null) return null;
        try {
            return Double.parseDouble(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getValueAsBoolean() {
        if (settingValue == null) return null;
        return Boolean.parseBoolean(settingValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemSettings that = (SystemSettings) o;
        return Objects.equals(settingId, that.settingId) && 
               Objects.equals(settingKey, that.settingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingId, settingKey);
    }

    @Override
    public String toString() {
        return "SystemSettings{" +
                "settingId=" + settingId +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", settingType=" + settingType +
                ", isEditable=" + isEditable +
                '}';
    }

    // Enum
    public enum SettingType {
        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        JSON("json");

        private final String value;

        SettingType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SettingType fromString(String value) {
            for (SettingType type : SettingType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid setting type: " + value);
        }
    }

    // Common setting keys
    public static final class Keys {
        public static final String COMPANY_NAME = "company.name";
        public static final String COMPANY_ADDRESS = "company.address";
        public static final String COMPANY_PHONE = "company.phone";
        public static final String COMPANY_EMAIL = "company.email";
        public static final String DEFAULT_TAX_RATE = "billing.default_tax_rate";
        public static final String DEFAULT_DISCOUNT_RATE = "billing.default_discount_rate";
        public static final String BILL_NUMBER_PREFIX = "billing.number_prefix";
        public static final String BILL_NUMBER_COUNTER = "billing.number_counter";
        public static final String LOW_STOCK_THRESHOLD = "inventory.low_stock_threshold";
        public static final String AUTO_GENERATE_ITEM_CODE = "inventory.auto_generate_item_code";
        public static final String CURRENCY_SYMBOL = "display.currency_symbol";
        public static final String DATE_FORMAT = "display.date_format";
        public static final String TIME_FORMAT = "display.time_format";
        public static final String ITEMS_PER_PAGE = "display.items_per_page";
        public static final String SESSION_TIMEOUT = "security.session_timeout";
        public static final String PASSWORD_MIN_LENGTH = "security.password_min_length";
        public static final String MAX_LOGIN_ATTEMPTS = "security.max_login_attempts";
        public static final String BACKUP_ENABLED = "backup.enabled";
        public static final String BACKUP_FREQUENCY = "backup.frequency";
    }
}
