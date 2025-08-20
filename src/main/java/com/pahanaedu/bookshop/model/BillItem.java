package com.pahanaedu.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * BillItem entity representing individual items in a bill
 */
public class BillItem {
    private Integer billItemId;
    private Integer billId;
    private Integer itemId;
    private String itemCode; // For display purposes
    private String itemName; // For display purposes
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;

    // Constructors
    public BillItem() {
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public BillItem(Integer billId, Integer itemId, Integer quantity, BigDecimal unitPrice) {
        this();
        this.billId = billId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = calculateLineTotal();
    }

    // Getters and Setters
    public Integer getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(Integer billItemId) {
        this.billItemId = billItemId;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.lineTotal = calculateLineTotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.lineTotal = calculateLineTotal();
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
        this.discountAmount = calculateDiscountAmount();
        this.lineTotal = calculateLineTotal();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        this.lineTotal = calculateLineTotal();
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    private BigDecimal calculateDiscountAmount() {
        if (discountPercentage == null || unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateLineTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        return subtotal.subtract(discount);
    }

    public void recalculateLineTotal() {
        this.lineTotal = calculateLineTotal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItem billItem = (BillItem) o;
        return Objects.equals(billItemId, billItem.billItemId) && 
               Objects.equals(billId, billItem.billId) && 
               Objects.equals(itemId, billItem.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billItemId, billId, itemId);
    }

    @Override
    public String toString() {
        return "BillItem{" +
                "billItemId=" + billItemId +
                ", billId=" + billId +
                ", itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
