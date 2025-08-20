package com.pahanaedu.bookshop.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * StockMovement entity representing stock movements (in/out/adjustments)
 */
public class StockMovement {
    private Integer movementId;
    private Integer itemId;
    private String itemCode; // For display purposes
    private String itemName; // For display purposes
    private MovementType movementType;
    private Integer quantity;
    private ReferenceType referenceType;
    private Integer referenceId;
    private String notes;
    private LocalDateTime movementDate;
    private Integer createdBy;
    private String createdByName; // For display purposes

    // Constructors
    public StockMovement() {
        this.movementDate = LocalDateTime.now();
    }

    public StockMovement(Integer itemId, MovementType movementType, Integer quantity, 
                        ReferenceType referenceType, Integer referenceId, String notes) {
        this();
        this.itemId = itemId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getMovementId() {
        return movementId;
    }

    public void setMovementId(Integer movementId) {
        this.movementId = movementId;
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

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    // Utility methods
    public boolean isStockIncrease() {
        return movementType == MovementType.IN || 
               (movementType == MovementType.ADJUSTMENT && quantity > 0) ||
               movementType == MovementType.RETURN;
    }

    public boolean isStockDecrease() {
        return movementType == MovementType.OUT || 
               (movementType == MovementType.ADJUSTMENT && quantity < 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockMovement that = (StockMovement) o;
        return Objects.equals(movementId, that.movementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movementId);
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "movementId=" + movementId +
                ", itemId=" + itemId +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", referenceType=" + referenceType +
                ", movementDate=" + movementDate +
                '}';
    }

    // Enums
    public enum MovementType {
        IN("in"),
        OUT("out"),
        ADJUSTMENT("adjustment"),
        RETURN("return");

        private final String value;

        MovementType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static MovementType fromString(String value) {
            for (MovementType type : MovementType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid movement type: " + value);
        }
    }

    public enum ReferenceType {
        PURCHASE("purchase"),
        SALE("sale"),
        ADJUSTMENT("adjustment"),
        RETURN("return"),
        INITIAL("initial");

        private final String value;

        ReferenceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ReferenceType fromString(String value) {
            for (ReferenceType type : ReferenceType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid reference type: " + value);
        }
    }
}
