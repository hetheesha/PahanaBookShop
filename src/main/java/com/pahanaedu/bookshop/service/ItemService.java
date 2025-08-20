package com.pahanaedu.bookshop.service;

import com.pahanaedu.bookshop.dao.*;
import com.pahanaedu.bookshop.model.*;
import com.pahanaedu.bookshop.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for Item-related business logic
 */
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    
    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;
    private final StockMovementDAO stockMovementDAO;
    private final ActivityLogDAO activityLogDAO;

    public ItemService() {
        this.itemDAO = new ItemDAO();
        this.categoryDAO = new CategoryDAO();
        this.stockMovementDAO = new StockMovementDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    /**
     * Create a new item
     * @param item Item to create
     * @param createdBy ID of user creating this item
     * @return Created item
     * @throws ServiceException if creation fails
     */
    public Item createItem(Item item, Integer createdBy) throws ServiceException {
        try {
            // Validate item data
            Map<String, String> validationErrors = validateItem(item);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if item code already exists
            if (itemDAO.findByCode(item.getItemCode()).isPresent()) {
                throw new ServiceException("Item code already exists: " + item.getItemCode());
            }

            // Validate category exists
            if (!categoryDAO.exists(item.getCategoryId())) {
                throw new ServiceException("Category not found with ID: " + item.getCategoryId());
            }

            item.setCreatedBy(createdBy);
            Item savedItem = itemDAO.save(item);

            // Create initial stock movement if stock quantity > 0
            if (savedItem.getStockQuantity() > 0) {
                createStockMovement(savedItem.getItemId(), StockMovement.MovementType.IN, 
                                  savedItem.getStockQuantity(), StockMovement.ReferenceType.INITIAL, 
                                  null, "Initial stock", createdBy);
            }

            // Log activity
            logActivity(createdBy, ActivityLog.Actions.CREATE, "items", savedItem.getItemId(), 
                      "Item created: " + savedItem.getItemCode());

            logger.info("Item created successfully: {}", savedItem.getItemCode());
            return savedItem;

        } catch (DAOException e) {
            logger.error("Error creating item: {}", item.getItemCode(), e);
            throw new ServiceException("Failed to create item", e);
        }
    }

    /**
     * Update an existing item
     * @param item Item to update
     * @param updatedBy ID of user performing the update
     * @return Updated item
     * @throws ServiceException if update fails
     */
    public Item updateItem(Item item, Integer updatedBy) throws ServiceException {
        try {
            // Validate item data
            Map<String, String> validationErrors = validateItem(item);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if item exists
            Optional<Item> existingItemOpt = itemDAO.findById(item.getItemId());
            if (existingItemOpt.isEmpty()) {
                throw new ServiceException("Item not found with ID: " + item.getItemId());
            }

            Item existingItem = existingItemOpt.get();

            // Check if item code is being changed and if new code already exists
            if (!existingItem.getItemCode().equals(item.getItemCode())) {
                if (itemDAO.findByCode(item.getItemCode()).isPresent()) {
                    throw new ServiceException("Item code already exists: " + item.getItemCode());
                }
            }

            // Validate category exists
            if (!categoryDAO.exists(item.getCategoryId())) {
                throw new ServiceException("Category not found with ID: " + item.getCategoryId());
            }

            Item updatedItem = itemDAO.update(item);

            // Log activity
            logActivity(updatedBy, ActivityLog.Actions.UPDATE, "items", updatedItem.getItemId(), 
                      "Item updated: " + updatedItem.getItemCode());

            logger.info("Item updated successfully: {}", updatedItem.getItemCode());
            return updatedItem;

        } catch (DAOException e) {
            logger.error("Error updating item: {}", item.getItemId(), e);
            throw new ServiceException("Failed to update item", e);
        }
    }

    /**
     * Adjust item stock
     * @param itemId Item ID
     * @param newQuantity New stock quantity
     * @param reason Reason for adjustment
     * @param adjustedBy ID of user performing the adjustment
     * @throws ServiceException if adjustment fails
     */
    public void adjustStock(Integer itemId, Integer newQuantity, String reason, Integer adjustedBy) throws ServiceException {
        try {
            Optional<Item> itemOpt = itemDAO.findById(itemId);
            if (itemOpt.isEmpty()) {
                throw new ServiceException("Item not found with ID: " + itemId);
            }

            Item item = itemOpt.get();
            int oldQuantity = item.getStockQuantity();
            int difference = newQuantity - oldQuantity;

            if (difference == 0) {
                return; // No change needed
            }

            // Update stock quantity
            itemDAO.updateStock(itemId, newQuantity);

            // Create stock movement
            StockMovement.MovementType movementType = difference > 0 ? 
                StockMovement.MovementType.IN : StockMovement.MovementType.OUT;
            
            createStockMovement(itemId, movementType, Math.abs(difference), 
                              StockMovement.ReferenceType.ADJUSTMENT, null, reason, adjustedBy);

            // Log activity
            logActivity(adjustedBy, ActivityLog.Actions.STOCK_ADJUSTMENT, "items", itemId, 
                      String.format("Stock adjusted from %d to %d: %s", oldQuantity, newQuantity, reason));

            logger.info("Stock adjusted for item {}: {} -> {}", item.getItemCode(), oldQuantity, newQuantity);

        } catch (DAOException e) {
            logger.error("Error adjusting stock for item: {}", itemId, e);
            throw new ServiceException("Failed to adjust stock", e);
        }
    }

    /**
     * Get item by ID
     * @param itemId Item ID
     * @return Item if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Item> getItemById(Integer itemId) throws ServiceException {
        try {
            return itemDAO.findById(itemId);
        } catch (DAOException e) {
            logger.error("Error getting item by ID: {}", itemId, e);
            throw new ServiceException("Failed to get item", e);
        }
    }

    /**
     * Get item by code
     * @param itemCode Item code
     * @return Item if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Item> getItemByCode(String itemCode) throws ServiceException {
        try {
            return itemDAO.findByCode(itemCode);
        } catch (DAOException e) {
            logger.error("Error getting item by code: {}", itemCode, e);
            throw new ServiceException("Failed to get item", e);
        }
    }

    /**
     * Get item by barcode
     * @param barcode Barcode
     * @return Item if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Item> getItemByBarcode(String barcode) throws ServiceException {
        try {
            return itemDAO.findByBarcode(barcode);
        } catch (DAOException e) {
            logger.error("Error getting item by barcode: {}", barcode, e);
            throw new ServiceException("Failed to get item", e);
        }
    }

    /**
     * Get all items with pagination
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of items
     * @throws ServiceException if retrieval fails
     */
    public List<Item> getItems(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return itemDAO.findAll(offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting items with pagination", e);
            throw new ServiceException("Failed to get items", e);
        }
    }

    /**
     * Search items
     * @param searchTerm Search term
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of matching items
     * @throws ServiceException if search fails
     */
    public List<Item> searchItems(String searchTerm, int page, int pageSize) throws ServiceException {
        try {
            if (!ValidationUtil.isNotEmpty(searchTerm)) {
                return getItems(page, pageSize);
            }
            
            int offset = (page - 1) * pageSize;
            return itemDAO.search(searchTerm.trim(), offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error searching items with term: {}", searchTerm, e);
            throw new ServiceException("Failed to search items", e);
        }
    }

    /**
     * Get items by category
     * @param categoryId Category ID
     * @return List of items in the category
     * @throws ServiceException if retrieval fails
     */
    public List<Item> getItemsByCategory(Integer categoryId) throws ServiceException {
        try {
            return itemDAO.findByCategory(categoryId);
        } catch (DAOException e) {
            logger.error("Error getting items by category: {}", categoryId, e);
            throw new ServiceException("Failed to get items by category", e);
        }
    }

    /**
     * Get low stock items
     * @return List of items with low stock
     * @throws ServiceException if retrieval fails
     */
    public List<Item> getLowStockItems() throws ServiceException {
        try {
            return itemDAO.findLowStock();
        } catch (DAOException e) {
            logger.error("Error getting low stock items", e);
            throw new ServiceException("Failed to get low stock items", e);
        }
    }

    /**
     * Get total count of items
     * @return Total count
     * @throws ServiceException if count fails
     */
    public long getItemCount() throws ServiceException {
        try {
            return itemDAO.count();
        } catch (DAOException e) {
            logger.error("Error counting items", e);
            throw new ServiceException("Failed to count items", e);
        }
    }

    /**
     * Delete an item
     * @param itemId Item ID to delete
     * @param deletedBy ID of user performing the deletion
     * @return true if deleted, false if not found
     * @throws ServiceException if deletion fails
     */
    public boolean deleteItem(Integer itemId, Integer deletedBy) throws ServiceException {
        try {
            Optional<Item> itemOpt = itemDAO.findById(itemId);
            if (itemOpt.isEmpty()) {
                return false;
            }

            Item item = itemOpt.get();
            boolean deleted = itemDAO.delete(itemId);

            if (deleted) {
                // Log activity
                logActivity(deletedBy, ActivityLog.Actions.DELETE, "items", itemId, 
                          "Item deleted: " + item.getItemCode());
                logger.info("Item deleted successfully: {}", item.getItemCode());
            }

            return deleted;

        } catch (DAOException e) {
            logger.error("Error deleting item: {}", itemId, e);
            throw new ServiceException("Failed to delete item", e);
        }
    }

    /**
     * Generate next item code
     * @param categoryId Category ID for prefix
     * @return Next available item code
     * @throws ServiceException if generation fails
     */
    public String generateItemCode(Integer categoryId) throws ServiceException {
        try {
            // Get category for prefix
            Optional<Category> categoryOpt = categoryDAO.findById(categoryId);
            String prefix = categoryOpt.map(cat -> cat.getCategoryName().substring(0, 
                Math.min(3, cat.getCategoryName().length())).toUpperCase()).orElse("ITM");
            
            long count = itemDAO.count();
            String itemCode;
            int attempts = 0;
            
            do {
                itemCode = String.format("%s%06d", prefix, count + 1 + attempts);
                attempts++;
                
                if (attempts > 100) {
                    throw new ServiceException("Unable to generate unique item code");
                }
            } while (itemDAO.findByCode(itemCode).isPresent());
            
            return itemCode;
            
        } catch (DAOException e) {
            logger.error("Error generating item code", e);
            throw new ServiceException("Failed to generate item code", e);
        }
    }

    /**
     * Create stock movement
     */
    private void createStockMovement(Integer itemId, StockMovement.MovementType movementType, 
                                   Integer quantity, StockMovement.ReferenceType referenceType, 
                                   Integer referenceId, String notes, Integer createdBy) throws DAOException {
        StockMovement movement = new StockMovement(itemId, movementType, quantity, referenceType, referenceId, notes);
        movement.setCreatedBy(createdBy);
        stockMovementDAO.save(movement);
    }

    /**
     * Validate item data
     * @param item Item to validate
     * @return Map of validation errors
     */
    private Map<String, String> validateItem(Item item) {
        Map<String, String> errors = new HashMap<>();

        if (!ValidationUtil.isValidItemCode(item.getItemCode())) {
            errors.put("itemCode", "Item code must be 3-20 characters and contain only letters, numbers, hyphens, and underscores");
        }

        if (!ValidationUtil.isValidLength(item.getItemName(), 2, 200)) {
            errors.put("itemName", "Item name must be 2-200 characters");
        }

        if (item.getCategoryId() == null || !ValidationUtil.isPositiveInteger(item.getCategoryId())) {
            errors.put("categoryId", "Valid category is required");
        }

        if (!ValidationUtil.isPositiveDecimal(item.getPrice())) {
            errors.put("price", "Price must be a positive number");
        }

        if (item.getCostPrice() != null && !ValidationUtil.isNonNegativeDecimal(item.getCostPrice())) {
            errors.put("costPrice", "Cost price must be a non-negative number");
        }

        if (!ValidationUtil.isNonNegativeInteger(item.getStockQuantity())) {
            errors.put("stockQuantity", "Stock quantity must be a non-negative number");
        }

        if (!ValidationUtil.isPositiveInteger(item.getMinStockLevel())) {
            errors.put("minStockLevel", "Minimum stock level must be a positive number");
        }

        if (!ValidationUtil.isPositiveInteger(item.getMaxStockLevel())) {
            errors.put("maxStockLevel", "Maximum stock level must be a positive number");
        }

        if (item.getMinStockLevel() != null && item.getMaxStockLevel() != null && 
            item.getMinStockLevel() >= item.getMaxStockLevel()) {
            errors.put("stockLevels", "Maximum stock level must be greater than minimum stock level");
        }

        if (item.getIsbn() != null && !ValidationUtil.isValidISBN(item.getIsbn())) {
            errors.put("isbn", "Invalid ISBN format");
        }

        if (item.getBarcode() != null && !ValidationUtil.isValidBarcode(item.getBarcode())) {
            errors.put("barcode", "Invalid barcode format");
        }

        if (item.getPublicationYear() != null && !ValidationUtil.isValidYear(item.getPublicationYear())) {
            errors.put("publicationYear", "Invalid publication year");
        }

        return errors;
    }

    /**
     * Log activity
     */
    private void logActivity(Integer userId, String action, String tableName, Integer recordId, String notes) {
        try {
            ActivityLog log = new ActivityLog(userId, action, tableName, recordId);
            log.setNewValues(notes);
            activityLogDAO.save(log);
        } catch (DAOException e) {
            logger.warn("Failed to log activity", e);
        }
    }
}
