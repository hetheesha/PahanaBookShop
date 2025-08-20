package com.pahanaedu.bookshop.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Customer entity representing bookshop customers
 */
public class Customer {
    private Integer customerId;
    private String accountNo;
    private String fullName;
    private String address;
    private String phone;
    private String email;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private Gender gender;
    private CustomerStatus status;
    private BigDecimal totalPurchases;
    private Integer totalBills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;

    // Constructors
    public Customer() {
        this.totalPurchases = BigDecimal.ZERO;
        this.totalBills = 0;
        this.status = CustomerStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Customer(String accountNo, String fullName, String address, String phone, 
                   String email, LocalDate dateOfBirth, Gender gender) {
        this();
        this.accountNo = accountNo;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Getters and Setters
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(BigDecimal totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public Integer getTotalBills() {
        return totalBills;
    }

    public void setTotalBills(Integer totalBills) {
        this.totalBills = totalBills;
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
        return status == CustomerStatus.ACTIVE;
    }

    public BigDecimal getAverageBillAmount() {
        if (totalBills == null || totalBills == 0) {
            return BigDecimal.ZERO;
        }
        return totalPurchases.divide(BigDecimal.valueOf(totalBills), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId) && 
               Objects.equals(accountNo, customer.accountNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, accountNo);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", accountNo='" + accountNo + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }

    // Enums
    public enum Gender {
        MALE("male"),
        FEMALE("female"),
        OTHER("other");

        private final String value;

        Gender(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Gender fromString(String value) {
            if (value == null) return null;
            for (Gender gender : Gender.values()) {
                if (gender.value.equalsIgnoreCase(value)) {
                    return gender;
                }
            }
            throw new IllegalArgumentException("Invalid gender: " + value);
        }
    }

    public enum CustomerStatus {
        ACTIVE("active"),
        INACTIVE("inactive");

        private final String value;

        CustomerStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static CustomerStatus fromString(String value) {
            for (CustomerStatus status : CustomerStatus.values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid customer status: " + value);
        }
    }
}
