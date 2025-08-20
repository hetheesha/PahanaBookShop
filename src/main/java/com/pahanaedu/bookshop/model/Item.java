package com.pahanaedu.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Item entity representing bookshop items/products
 */
public class Item {
    private Integer itemId;
    private String itemCode;
    private String itemName;
    private Integer categoryId;
    private String categoryName; // For display purposes
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String unit;
    private String barcode;
    private String isbn;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private ItemStatus status;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;

    // Constructors
    public Item() {
        this.stockQuantity = 0;
        this.minStockLevel = 5;
        this.maxStockLevel = 1000;
        this.unit = "piece";
        this.status = ItemStatus.ACTIVE;
        this.totalSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Item(String itemCode, String itemName, Integer categoryId, String description, 
                BigDecimal price, BigDecimal costPrice) {
        this();
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.description = description;
        this.price = price;
        this.costPrice = costPrice;
    }

    // Getters and Setters
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Integer getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(Integer maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Integer getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(Integer totalSold) {
        this.totalSold = totalSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
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
        return status == ItemStatus.ACTIVE;
    }

    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public BigDecimal getProfitMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(costPrice).divide(costPrice, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId) && 
               Objects.equals(itemCode, item.itemCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemCode);
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", status=" + status +
                '}';
    }

    // Enum
    public enum ItemStatus {
        ACTIVE("active"),
        INACTIVE("inactive"),
        DISCONTINUED("discontinued");

        private final String value;

        ItemStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ItemStatus fromString(String value) {
            for (ItemStatus status : ItemStatus.values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid item status: " + value);
        }
    }
}
