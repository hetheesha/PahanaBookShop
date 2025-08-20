package com.pahanaedu.bookshop.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bill entity representing customer bills/invoices
 */
public class Bill {
    private Integer billId;
    private String billNumber;
    private Integer customerId;
    private String customerName; // For display purposes
    private String customerAccountNo; // For display purposes
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime billTime;
    
    private BigDecimal subtotal;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String notes;
    private BillStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    
    // Bill items (not stored in database, loaded separately)
    private List<BillItem> billItems;

    // Constructors
    public Bill() {
        this.billDate = LocalDate.now();
        this.billTime = LocalTime.now();
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxPercentage = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.paymentMethod = PaymentMethod.CASH;
        this.paymentStatus = PaymentStatus.PAID;
        this.status = BillStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.billItems = new ArrayList<>();
    }

    public Bill(String billNumber, Integer customerId, BigDecimal subtotal, BigDecimal totalAmount) {
        this();
        this.billNumber = billNumber;
        this.customerId = customerId;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAccountNo() {
        return customerAccountNo;
    }

    public void setCustomerAccountNo(String customerAccountNo) {
        this.customerAccountNo = customerAccountNo;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public LocalTime getBillTime() {
        return billTime;
    }

    public void setBillTime(LocalTime billTime) {
        this.billTime = billTime;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
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

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    // Utility methods
    public boolean isActive() {
        return status == BillStatus.ACTIVE;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    public void addBillItem(BillItem billItem) {
        if (this.billItems == null) {
            this.billItems = new ArrayList<>();
        }
        this.billItems.add(billItem);
    }

    public int getTotalItemCount() {
        if (billItems == null) return 0;
        return billItems.stream().mapToInt(BillItem::getQuantity).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return Objects.equals(billId, bill.billId) && 
               Objects.equals(billNumber, bill.billNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, billNumber);
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", billNumber='" + billNumber + '\'' +
                ", customerId=" + customerId +
                ", billDate=" + billDate +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }

    // Enums
    public enum PaymentMethod {
        CASH("cash"),
        CARD("card"),
        BANK_TRANSFER("bank_transfer"),
        CHEQUE("cheque");

        private final String value;

        PaymentMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static PaymentMethod fromString(String value) {
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.value.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("Invalid payment method: " + value);
        }
    }

    public enum PaymentStatus {
        PAID("paid"),
        PENDING("pending"),
        PARTIAL("partial"),
        CANCELLED("cancelled");

        private final String value;

        PaymentStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static PaymentStatus fromString(String value) {
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid payment status: " + value);
        }
    }

    public enum BillStatus {
        ACTIVE("active"),
        CANCELLED("cancelled"),
        RETURNED("returned");

        private final String value;

        BillStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static BillStatus fromString(String value) {
            for (BillStatus status : BillStatus.values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid bill status: " + value);
        }
    }
}
