package com.pahanaedu.bookshop.service;

import com.pahanaedu.bookshop.dao.ActivityLogDAO;
import com.pahanaedu.bookshop.dao.CustomerDAO;
import com.pahanaedu.bookshop.dao.DAOException;
import com.pahanaedu.bookshop.model.ActivityLog;
import com.pahanaedu.bookshop.model.Customer;
import com.pahanaedu.bookshop.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for Customer-related business logic
 */
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    
    private final CustomerDAO customerDAO;
    private final ActivityLogDAO activityLogDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    /**
     * Create a new customer
     * @param customer Customer to create
     * @param createdBy ID of user creating this customer
     * @return Created customer
     * @throws ServiceException if creation fails
     */
    public Customer createCustomer(Customer customer, Integer createdBy) throws ServiceException {
        try {
            // Validate customer data
            Map<String, String> validationErrors = validateCustomer(customer);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if account number already exists
            if (customerDAO.findByAccountNo(customer.getAccountNo()).isPresent()) {
                throw new ServiceException("Account number already exists: " + customer.getAccountNo());
            }

            customer.setCreatedBy(createdBy);
            Customer savedCustomer = customerDAO.save(customer);

            // Log activity
            logActivity(createdBy, ActivityLog.Actions.CREATE, "customers", savedCustomer.getCustomerId(), 
                      "Customer created: " + savedCustomer.getAccountNo());

            logger.info("Customer created successfully: {}", savedCustomer.getAccountNo());
            return savedCustomer;

        } catch (DAOException e) {
            logger.error("Error creating customer: {}", customer.getAccountNo(), e);
            throw new ServiceException("Failed to create customer", e);
        }
    }

    /**
     * Update an existing customer
     * @param customer Customer to update
     * @param updatedBy ID of user performing the update
     * @return Updated customer
     * @throws ServiceException if update fails
     */
    public Customer updateCustomer(Customer customer, Integer updatedBy) throws ServiceException {
        try {
            // Validate customer data
            Map<String, String> validationErrors = validateCustomer(customer);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Check if customer exists
            Optional<Customer> existingCustomerOpt = customerDAO.findById(customer.getCustomerId());
            if (existingCustomerOpt.isEmpty()) {
                throw new ServiceException("Customer not found with ID: " + customer.getCustomerId());
            }

            Customer existingCustomer = existingCustomerOpt.get();

            // Check if account number is being changed and if new account number already exists
            if (!existingCustomer.getAccountNo().equals(customer.getAccountNo())) {
                if (customerDAO.findByAccountNo(customer.getAccountNo()).isPresent()) {
                    throw new ServiceException("Account number already exists: " + customer.getAccountNo());
                }
            }

            Customer updatedCustomer = customerDAO.update(customer);

            // Log activity
            logActivity(updatedBy, ActivityLog.Actions.UPDATE, "customers", updatedCustomer.getCustomerId(), 
                      "Customer updated: " + updatedCustomer.getAccountNo());

            logger.info("Customer updated successfully: {}", updatedCustomer.getAccountNo());
            return updatedCustomer;

        } catch (DAOException e) {
            logger.error("Error updating customer: {}", customer.getCustomerId(), e);
            throw new ServiceException("Failed to update customer", e);
        }
    }

    /**
     * Get customer by ID
     * @param customerId Customer ID
     * @return Customer if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Customer> getCustomerById(Integer customerId) throws ServiceException {
        try {
            return customerDAO.findById(customerId);
        } catch (DAOException e) {
            logger.error("Error getting customer by ID: {}", customerId, e);
            throw new ServiceException("Failed to get customer", e);
        }
    }

    /**
     * Get customer by account number
     * @param accountNo Account number
     * @return Customer if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Customer> getCustomerByAccountNo(String accountNo) throws ServiceException {
        try {
            return customerDAO.findByAccountNo(accountNo);
        } catch (DAOException e) {
            logger.error("Error getting customer by account number: {}", accountNo, e);
            throw new ServiceException("Failed to get customer", e);
        }
    }

    /**
     * Get all customers with pagination
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of customers
     * @throws ServiceException if retrieval fails
     */
    public List<Customer> getCustomers(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return customerDAO.findAll(offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting customers with pagination", e);
            throw new ServiceException("Failed to get customers", e);
        }
    }

    /**
     * Search customers
     * @param searchTerm Search term
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of matching customers
     * @throws ServiceException if search fails
     */
    public List<Customer> searchCustomers(String searchTerm, int page, int pageSize) throws ServiceException {
        try {
            if (!ValidationUtil.isNotEmpty(searchTerm)) {
                return getCustomers(page, pageSize);
            }
            
            int offset = (page - 1) * pageSize;
            return customerDAO.search(searchTerm.trim(), offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error searching customers with term: {}", searchTerm, e);
            throw new ServiceException("Failed to search customers", e);
        }
    }

    /**
     * Get total count of customers
     * @return Total count
     * @throws ServiceException if count fails
     */
    public long getCustomerCount() throws ServiceException {
        try {
            return customerDAO.count();
        } catch (DAOException e) {
            logger.error("Error counting customers", e);
            throw new ServiceException("Failed to count customers", e);
        }
    }

    /**
     * Delete a customer
     * @param customerId Customer ID to delete
     * @param deletedBy ID of user performing the deletion
     * @return true if deleted, false if not found
     * @throws ServiceException if deletion fails
     */
    public boolean deleteCustomer(Integer customerId, Integer deletedBy) throws ServiceException {
        try {
            Optional<Customer> customerOpt = customerDAO.findById(customerId);
            if (customerOpt.isEmpty()) {
                return false;
            }

            Customer customer = customerOpt.get();
            boolean deleted = customerDAO.delete(customerId);

            if (deleted) {
                // Log activity
                logActivity(deletedBy, ActivityLog.Actions.DELETE, "customers", customerId, 
                          "Customer deleted: " + customer.getAccountNo());
                logger.info("Customer deleted successfully: {}", customer.getAccountNo());
            }

            return deleted;

        } catch (DAOException e) {
            logger.error("Error deleting customer: {}", customerId, e);
            throw new ServiceException("Failed to delete customer", e);
        }
    }

    /**
     * Update customer totals (purchases and bills count)
     * @param customerId Customer ID
     * @param totalPurchases Total purchase amount
     * @param totalBills Total number of bills
     * @throws ServiceException if update fails
     */
    public void updateCustomerTotals(Integer customerId, BigDecimal totalPurchases, Integer totalBills) throws ServiceException {
        try {
            customerDAO.updateTotals(customerId, totalPurchases, totalBills);
            logger.debug("Customer totals updated: {} - purchases: {}, bills: {}", customerId, totalPurchases, totalBills);
        } catch (DAOException e) {
            logger.error("Error updating customer totals: {}", customerId, e);
            throw new ServiceException("Failed to update customer totals", e);
        }
    }

    /**
     * Generate next account number
     * @return Next available account number
     * @throws ServiceException if generation fails
     */
    public String generateAccountNumber() throws ServiceException {
        try {
            // Simple implementation - you can make this more sophisticated
            long count = customerDAO.count();
            String accountNo;
            int attempts = 0;
            
            do {
                accountNo = String.format("CUST%06d", count + 1 + attempts);
                attempts++;
                
                if (attempts > 100) {
                    throw new ServiceException("Unable to generate unique account number");
                }
            } while (customerDAO.findByAccountNo(accountNo).isPresent());
            
            return accountNo;
            
        } catch (DAOException e) {
            logger.error("Error generating account number", e);
            throw new ServiceException("Failed to generate account number", e);
        }
    }

    /**
     * Validate customer data
     * @param customer Customer to validate
     * @return Map of validation errors
     */
    private Map<String, String> validateCustomer(Customer customer) {
        Map<String, String> errors = new HashMap<>();

        if (!ValidationUtil.isValidAccountNo(customer.getAccountNo())) {
            errors.put("accountNo", "Account number must be 5-20 characters and contain only letters and numbers");
        }

        if (!ValidationUtil.isValidLength(customer.getFullName(), 2, 100)) {
            errors.put("fullName", "Full name must be 2-100 characters");
        }

        if (!ValidationUtil.isValidLength(customer.getAddress(), 5, 500)) {
            errors.put("address", "Address must be 5-500 characters");
        }

        if (!ValidationUtil.isValidPhone(customer.getPhone())) {
            errors.put("phone", "Invalid phone number format");
        }

        if (customer.getEmail() != null && !ValidationUtil.isValidEmail(customer.getEmail())) {
            errors.put("email", "Invalid email format");
        }

        if (customer.getDateOfBirth() != null && !ValidationUtil.isValidBirthDate(customer.getDateOfBirth())) {
            errors.put("dateOfBirth", "Invalid birth date");
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
