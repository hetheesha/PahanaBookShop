package com.pahanaedu.bookshop.service;

import com.pahanaedu.bookshop.dao.ActivityLogDAO;
import com.pahanaedu.bookshop.dao.CategoryDAO;
import com.pahanaedu.bookshop.dao.DAOException;
import com.pahanaedu.bookshop.model.ActivityLog;
import com.pahanaedu.bookshop.model.Category;
import com.pahanaedu.bookshop.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for Category-related business logic
 */
public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    private final CategoryDAO categoryDAO;
    private final ActivityLogDAO activityLogDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    /**
     * Create a new category
     * @param category Category to create
     * @param createdBy ID of user creating this category
     * @return Created category
     * @throws ServiceException if creation fails
     */
    public Category createCategory(Category category, Integer createdBy) throws ServiceException {
        try {
            // Validate category data
            Map<String, String> validationErrors = validateCategory(category);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if category name already exists
            if (categoryDAO.findByName(category.getCategoryName()).isPresent()) {
                throw new ServiceException("Category name already exists: " + category.getCategoryName());
            }

            Category savedCategory = categoryDAO.save(category);

            // Log activity
            logActivity(createdBy, ActivityLog.Actions.CREATE, "categories", savedCategory.getCategoryId(), 
                      "Category created: " + savedCategory.getCategoryName());

            logger.info("Category created successfully: {}", savedCategory.getCategoryName());
            return savedCategory;

        } catch (DAOException e) {
            logger.error("Error creating category: {}", category.getCategoryName(), e);
            throw new ServiceException("Failed to create category", e);
        }
    }

    /**
     * Update an existing category
     * @param category Category to update
     * @param updatedBy ID of user performing the update
     * @return Updated category
     * @throws ServiceException if update fails
     */
    public Category updateCategory(Category category, Integer updatedBy) throws ServiceException {
        try {
            // Validate category data
            Map<String, String> validationErrors = validateCategory(category);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if category exists
            Optional<Category> existingCategoryOpt = categoryDAO.findById(category.getCategoryId());
            if (existingCategoryOpt.isEmpty()) {
                throw new ServiceException("Category not found with ID: " + category.getCategoryId());
            }

            Category existingCategory = existingCategoryOpt.get();

            // Check if category name is being changed and if new name already exists
            if (!existingCategory.getCategoryName().equals(category.getCategoryName())) {
                if (categoryDAO.findByName(category.getCategoryName()).isPresent()) {
                    throw new ServiceException("Category name already exists: " + category.getCategoryName());
                }
            }

            Category updatedCategory = categoryDAO.update(category);

            // Log activity
            logActivity(updatedBy, ActivityLog.Actions.UPDATE, "categories", updatedCategory.getCategoryId(), 
                      "Category updated: " + updatedCategory.getCategoryName());

            logger.info("Category updated successfully: {}", updatedCategory.getCategoryName());
            return updatedCategory;

        } catch (DAOException e) {
            logger.error("Error updating category: {}", category.getCategoryId(), e);
            throw new ServiceException("Failed to update category", e);
        }
    }

    /**
     * Get category by ID
     * @param categoryId Category ID
     * @return Category if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Category> getCategoryById(Integer categoryId) throws ServiceException {
        try {
            return categoryDAO.findById(categoryId);
        } catch (DAOException e) {
            logger.error("Error getting category by ID: {}", categoryId, e);
            throw new ServiceException("Failed to get category", e);
        }
    }

    /**
     * Get category by name
     * @param categoryName Category name
     * @return Category if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Category> getCategoryByName(String categoryName) throws ServiceException {
        try {
            return categoryDAO.findByName(categoryName);
        } catch (DAOException e) {
            logger.error("Error getting category by name: {}", categoryName, e);
            throw new ServiceException("Failed to get category", e);
        }
    }

    /**
     * Get all categories with pagination
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of categories
     * @throws ServiceException if retrieval fails
     */
    public List<Category> getCategories(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return categoryDAO.findAll(offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting categories with pagination", e);
            throw new ServiceException("Failed to get categories", e);
        }
    }

    /**
     * Get all active categories
     * @return List of active categories
     * @throws ServiceException if retrieval fails
     */
    public List<Category> getActiveCategories() throws ServiceException {
        try {
            return categoryDAO.findActive();
        } catch (DAOException e) {
            logger.error("Error getting active categories", e);
            throw new ServiceException("Failed to get active categories", e);
        }
    }

    /**
     * Get total count of categories
     * @return Total count
     * @throws ServiceException if count fails
     */
    public long getCategoryCount() throws ServiceException {
        try {
            return categoryDAO.count();
        } catch (DAOException e) {
            logger.error("Error counting categories", e);
            throw new ServiceException("Failed to count categories", e);
        }
    }

    /**
     * Delete a category
     * @param categoryId Category ID to delete
     * @param deletedBy ID of user performing the deletion
     * @return true if deleted, false if not found
     * @throws ServiceException if deletion fails
     */
    public boolean deleteCategory(Integer categoryId, Integer deletedBy) throws ServiceException {
        try {
            Optional<Category> categoryOpt = categoryDAO.findById(categoryId);
            if (categoryOpt.isEmpty()) {
                return false;
            }

            Category category = categoryOpt.get();
            boolean deleted = categoryDAO.delete(categoryId);

            if (deleted) {
                // Log activity
                logActivity(deletedBy, ActivityLog.Actions.DELETE, "categories", categoryId, 
                          "Category deleted: " + category.getCategoryName());
                logger.info("Category deleted successfully: {}", category.getCategoryName());
            }

            return deleted;

        } catch (DAOException e) {
            logger.error("Error deleting category: {}", categoryId, e);
            throw new ServiceException("Failed to delete category", e);
        }
    }

    /**
     * Validate category data
     * @param category Category to validate
     * @return Map of validation errors
     */
    private Map<String, String> validateCategory(Category category) {
        Map<String, String> errors = new HashMap<>();

        if (!ValidationUtil.isValidLength(category.getCategoryName(), 2, 50)) {
            errors.put("categoryName", "Category name must be 2-50 characters");
        }

        if (category.getDescription() != null && category.getDescription().length() > 500) {
            errors.put("description", "Description cannot exceed 500 characters");
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
