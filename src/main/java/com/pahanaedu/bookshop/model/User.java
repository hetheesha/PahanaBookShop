package com.pahanaedu.bookshop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User entity representing system users (admin, manager, staff)
 */
public class User {
    private Integer userId;
    private String username;
    
    @JsonIgnore
    private String passwordHash;
    
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;

    // Constructors
    public User() {}

    public User(String username, String passwordHash, String fullName, String email, 
                String phone, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    // Utility methods
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
            if (this.role == role) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && 
               Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }

    // Enums
    public enum UserRole {
        ADMIN("admin"),
        MANAGER("manager"),
        STAFF("staff");

        private final String value;

        UserRole(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UserRole fromString(String value) {
            for (UserRole role : UserRole.values()) {
                if (role.value.equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid user role: " + value);
        }
    }

    public enum UserStatus {
        ACTIVE("active"),
        INACTIVE("inactive"),
        SUSPENDED("suspended");

        private final String value;

        UserStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UserStatus fromString(String value) {
            for (UserStatus status : UserStatus.values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid user status: " + value);
        }
    }
}
