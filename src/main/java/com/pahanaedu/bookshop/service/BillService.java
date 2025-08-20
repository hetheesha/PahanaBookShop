package com.pahanaedu.bookshop.service;

import com.pahanaedu.bookshop.dao.*;
import com.pahanaedu.bookshop.model.*;
import com.pahanaedu.bookshop.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for Bill-related business logic
 */
public class BillService {
    private static final Logger logger = LoggerFactory.getLogger(BillService.class);
    
    private final BillDAO billDAO;
    private final BillItemDAO billItemDAO;
    private final CustomerDAO customerDAO;
    private final ItemDAO itemDAO;
    private final StockMovementDAO stockMovementDAO;
    private final ActivityLogDAO activityLogDAO;
    private final SystemSettingsDAO systemSettingsDAO;

    public BillService() {
        this.billDAO = new BillDAO();
        this.billItemDAO = new BillItemDAO();
        this.customerDAO = new CustomerDAO();
        this.itemDAO = new ItemDAO();
        this.stockMovementDAO = new StockMovementDAO();
        this.activityLogDAO = new ActivityLogDAO();
        this.systemSettingsDAO = new SystemSettingsDAO();
    }

    /**
     * Create a new bill with items
     * @param bill Bill to create
     * @param billItems List of bill items
     * @param createdBy ID of user creating this bill
     * @return Created bill with items
     * @throws ServiceException if creation fails
     */
    public Bill createBill(Bill bill, List<BillItem> billItems, Integer createdBy) throws ServiceException {
        try {
            // Validate bill data
            Map<String, String> validationErrors = validateBill(bill, billItems);
            if (!validationErrors.isEmpty()) {
                throw new ServiceException("Validation failed: " + validationErrors);
            }

            // Validate customer exists
            if (!customerDAO.exists(bill.getCustomerId())) {
                throw new ServiceException("Customer not found with ID: " + bill.getCustomerId());
            }

            // Generate bill number if not provided
            if (bill.getBillNumber() == null || bill.getBillNumber().trim().isEmpty()) {
                bill.setBillNumber(generateBillNumber());
            }

            // Validate stock availability and reserve stock
            Map<Integer, Integer> originalStock = validateAndReserveStock(billItems);

            // Calculate totals
            calculateBillTotals(bill, billItems);

            bill.setCreatedBy(createdBy);
            Bill savedBill = billDAO.save(bill);

            // Save bill items
            for (BillItem billItem : billItems) {
                billItem.setBillId(savedBill.getBillId());
                billItem.recalculateLineTotal();
            }
            billItemDAO.saveBatch(billItems);
            savedBill.setBillItems(billItems);

            // Update stock quantities and create stock movements
            updateStockForBillSafe(billItems, savedBill.getBillId(), createdBy, originalStock);

            // Update customer totals
            updateCustomerTotals(bill.getCustomerId());

            // Log activity
            logActivity(createdBy, ActivityLog.Actions.BILL_CREATED, "bills", savedBill.getBillId(), 
                      "Bill created: " + savedBill.getBillNumber() + " - Amount: " + savedBill.getTotalAmount());

            logger.info("Bill created successfully: {} - Amount: {}", savedBill.getBillNumber(), savedBill.getTotalAmount());
            return savedBill;

        } catch (DAOException e) {
            logger.error("Error creating bill", e);
            throw new ServiceException("Failed to create bill", e);
        }
    }

    /**
     * Get bill by ID with items
     * @param billId Bill ID
     * @return Bill with items if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Bill> getBillById(Integer billId) throws ServiceException {
        try {
            Optional<Bill> billOpt = billDAO.findById(billId);
            if (billOpt.isPresent()) {
                Bill bill = billOpt.get();
                List<BillItem> billItems = billItemDAO.findByBill(billId);
                bill.setBillItems(billItems);
                return Optional.of(bill);
            }
            return Optional.empty();
        } catch (DAOException e) {
            logger.error("Error getting bill by ID: {}", billId, e);
            throw new ServiceException("Failed to get bill", e);
        }
    }

    /**
     * Get bill by number with items
     * @param billNumber Bill number
     * @return Bill with items if found
     * @throws ServiceException if retrieval fails
     */
    public Optional<Bill> getBillByNumber(String billNumber) throws ServiceException {
        try {
            Optional<Bill> billOpt = billDAO.findByNumber(billNumber);
            if (billOpt.isPresent()) {
                Bill bill = billOpt.get();
                List<BillItem> billItems = billItemDAO.findByBill(bill.getBillId());
                bill.setBillItems(billItems);
                return Optional.of(bill);
            }
            return Optional.empty();
        } catch (DAOException e) {
            logger.error("Error getting bill by number: {}", billNumber, e);
            throw new ServiceException("Failed to get bill", e);
        }
    }

    /**
     * Get all bills with pagination
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of bills
     * @throws ServiceException if retrieval fails
     */
    public List<Bill> getBills(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return billDAO.findAll(offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting bills with pagination", e);
            throw new ServiceException("Failed to get bills", e);
        }
    }

    /**
     * Get bills by customer
     * @param customerId Customer ID
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of customer bills
     * @throws ServiceException if retrieval fails
     */
    public List<Bill> getBillsByCustomer(Integer customerId, int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return billDAO.findByCustomer(customerId, offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting bills by customer: {}", customerId, e);
            throw new ServiceException("Failed to get bills by customer", e);
        }
    }

    /**
     * Get bills by date range
     * @param startDate Start date
     * @param endDate End date
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return List of bills in date range
     * @throws ServiceException if retrieval fails
     */
    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate, int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return billDAO.findByDateRange(startDate, endDate, offset, pageSize);
        } catch (DAOException e) {
            logger.error("Error getting bills by date range: {} to {}", startDate, endDate, e);
            throw new ServiceException("Failed to get bills by date range", e);
        }
    }

    /**
     * Cancel a bill
     * @param billId Bill ID to cancel
     * @param cancelledBy ID of user cancelling the bill
     * @return Updated bill
     * @throws ServiceException if cancellation fails
     */
    public Bill cancelBill(Integer billId, Integer cancelledBy) throws ServiceException {
        try {
            Optional<Bill> billOpt = getBillById(billId);
            if (billOpt.isEmpty()) {
                throw new ServiceException("Bill not found with ID: " + billId);
            }

            Bill bill = billOpt.get();
            if (bill.getStatus() != Bill.BillStatus.ACTIVE) {
                throw new ServiceException("Only active bills can be cancelled");
            }

            // Update bill status
            bill.setStatus(Bill.BillStatus.CANCELLED);
            Bill updatedBill = billDAO.update(bill);

            // Restore stock quantities
            restoreStockForBill(bill.getBillItems(), billId, cancelledBy);

            // Update customer totals
            updateCustomerTotals(bill.getCustomerId());

            // Log activity
            logActivity(cancelledBy, ActivityLog.Actions.BILL_CANCELLED, "bills", billId, 
                      "Bill cancelled: " + bill.getBillNumber());

            logger.info("Bill cancelled successfully: {}", bill.getBillNumber());
            return updatedBill;

        } catch (DAOException e) {
            logger.error("Error cancelling bill: {}", billId, e);
            throw new ServiceException("Failed to cancel bill", e);
        }
    }

    /**
     * Get total count of bills
     * @return Total count
     * @throws ServiceException if count fails
     */
    public long getBillCount() throws ServiceException {
        try {
            return billDAO.count();
        } catch (DAOException e) {
            logger.error("Error counting bills", e);
            throw new ServiceException("Failed to count bills", e);
        }
    }

    /**
     * Generate next bill number
     * @return Next available bill number
     * @throws ServiceException if generation fails
     */
    public String generateBillNumber() throws ServiceException {
        try {
            String prefix = systemSettingsDAO.getValue("billing.number_prefix", "BILL");
            String counterKey = "billing.number_counter";
            
            String counterStr = systemSettingsDAO.getValue(counterKey, "0");
            int counter = Integer.parseInt(counterStr) + 1;
            
            String billNumber = String.format("%s%s%06d", prefix, 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")), counter);
            
            // Update counter
            systemSettingsDAO.updateValue(counterKey, String.valueOf(counter), null);
            
            return billNumber;
            
        } catch (Exception e) {
            logger.error("Error generating bill number", e);
            throw new ServiceException("Failed to generate bill number", e);
        }
    }

    /**
     * Validate stock availability and reserve stock for bill items
     * Returns a map of original stock quantities for rollback if needed
     */
    private Map<Integer, Integer> validateAndReserveStock(List<BillItem> billItems) throws ServiceException, DAOException {
        Map<Integer, Integer> originalStock = new HashMap<>();

        for (BillItem billItem : billItems) {
            Optional<Item> itemOpt = itemDAO.findById(billItem.getItemId());
            if (itemOpt.isEmpty()) {
                throw new ServiceException("Item not found with ID: " + billItem.getItemId());
            }

            Item item = itemOpt.get();
            if (!item.isActive()) {
                throw new ServiceException("Item is not active: " + item.getItemName());
            }

            if (item.getStockQuantity() < billItem.getQuantity()) {
                throw new ServiceException("Insufficient stock for item: " + item.getItemName() +
                    " (Available: " + item.getStockQuantity() + ", Required: " + billItem.getQuantity() + ")");
            }

            // Store original stock for reference
            originalStock.put(billItem.getItemId(), item.getStockQuantity());
        }

        return originalStock;
    }

    /**
     * Calculate bill totals
     */
    private void calculateBillTotals(Bill bill, List<BillItem> billItems) {
        BigDecimal subtotal = billItems.stream()
            .map(BillItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        bill.setSubtotal(subtotal);

        // Apply discount
        BigDecimal discountAmount = bill.getDiscountAmount();
        if (bill.getDiscountPercentage() != null && bill.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(bill.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            bill.setDiscountAmount(discountAmount);
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

        // Apply tax
        BigDecimal taxAmount = bill.getTaxAmount();
        if (bill.getTaxPercentage() != null && bill.getTaxPercentage().compareTo(BigDecimal.ZERO) > 0) {
            taxAmount = afterDiscount.multiply(bill.getTaxPercentage()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            bill.setTaxAmount(taxAmount);
        }

        BigDecimal totalAmount = afterDiscount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        bill.setTotalAmount(totalAmount);
    }

    /**
     * Update stock quantities for bill items using original stock values
     */
    private void updateStockForBillSafe(List<BillItem> billItems, Integer billId, Integer createdBy,
                                       Map<Integer, Integer> originalStock) throws DAOException {
        for (BillItem billItem : billItems) {
            Integer itemId = billItem.getItemId();
            Integer originalQuantity = originalStock.get(itemId);
            Integer billQuantity = billItem.getQuantity();
            Integer newQuantity = originalQuantity - billQuantity;

            // Debug logging
            logger.info("Stock Update Debug - Item ID: {}, Original Stock: {}, Bill Quantity: {}, New Stock: {}",
                itemId, originalQuantity, billQuantity, newQuantity);

            // Update stock using the calculated new quantity
            itemDAO.updateStock(itemId, newQuantity);

            // Create stock movement
            StockMovement movement = new StockMovement(itemId, StockMovement.MovementType.OUT,
                billQuantity, StockMovement.ReferenceType.SALE, billId, "Sale - Bill: " + billId);
            movement.setCreatedBy(createdBy);
            stockMovementDAO.save(movement);

            // Update item sales totals
            Optional<Item> itemOpt = itemDAO.findById(itemId);
            if (itemOpt.isPresent()) {
                Item item = itemOpt.get();
                int newTotalSold = item.getTotalSold() + billQuantity;
                BigDecimal newTotalRevenue = item.getTotalRevenue().add(billItem.getLineTotal());
                itemDAO.updateSalesTotals(itemId, newTotalSold, newTotalRevenue);
            }

            logger.info("Stock update completed for item ID: {}", itemId);
        }
    }

    /**
     * Restore stock quantities for cancelled bill
     */
    private void restoreStockForBill(List<BillItem> billItems, Integer billId, Integer cancelledBy) throws DAOException {
        for (BillItem billItem : billItems) {
            Optional<Item> itemOpt = itemDAO.findById(billItem.getItemId());
            if (itemOpt.isPresent()) {
                Item item = itemOpt.get();
                int newQuantity = item.getStockQuantity() + billItem.getQuantity();
                itemDAO.updateStock(billItem.getItemId(), newQuantity);

                // Create stock movement
                StockMovement movement = new StockMovement(billItem.getItemId(), StockMovement.MovementType.RETURN, 
                    billItem.getQuantity(), StockMovement.ReferenceType.RETURN, billId, "Return - Bill Cancelled: " + billId);
                movement.setCreatedBy(cancelledBy);
                stockMovementDAO.save(movement);

                // Update item sales totals
                int newTotalSold = Math.max(0, item.getTotalSold() - billItem.getQuantity());
                BigDecimal newTotalRevenue = item.getTotalRevenue().subtract(billItem.getLineTotal());
                if (newTotalRevenue.compareTo(BigDecimal.ZERO) < 0) {
                    newTotalRevenue = BigDecimal.ZERO;
                }
                itemDAO.updateSalesTotals(billItem.getItemId(), newTotalSold, newTotalRevenue);
            }
        }
    }

    /**
     * Update customer totals
     */
    private void updateCustomerTotals(Integer customerId) throws DAOException {
        // This would typically involve calculating totals from all active bills
        // For now, we'll skip this implementation as it would require complex aggregation
        logger.debug("Customer totals update skipped for customer: {}", customerId);
    }

    /**
     * Validate bill data
     */
    private Map<String, String> validateBill(Bill bill, List<BillItem> billItems) {
        Map<String, String> errors = new HashMap<>();

        if (bill.getCustomerId() == null || !ValidationUtil.isPositiveInteger(bill.getCustomerId())) {
            errors.put("customerId", "Valid customer is required");
        }

        if (billItems == null || billItems.isEmpty()) {
            errors.put("billItems", "At least one item is required");
        } else {
            for (int i = 0; i < billItems.size(); i++) {
                BillItem item = billItems.get(i);
                if (!ValidationUtil.isPositiveInteger(item.getItemId())) {
                    errors.put("billItems[" + i + "].itemId", "Valid item is required");
                }
                if (!ValidationUtil.isPositiveInteger(item.getQuantity())) {
                    errors.put("billItems[" + i + "].quantity", "Quantity must be positive");
                }
                if (!ValidationUtil.isPositiveDecimal(item.getUnitPrice())) {
                    errors.put("billItems[" + i + "].unitPrice", "Unit price must be positive");
                }
            }
        }

        if (bill.getDiscountPercentage() != null && !ValidationUtil.isValidPercentage(bill.getDiscountPercentage())) {
            errors.put("discountPercentage", "Discount percentage must be between 0 and 100");
        }

        if (bill.getTaxPercentage() != null && !ValidationUtil.isValidPercentage(bill.getTaxPercentage())) {
            errors.put("taxPercentage", "Tax percentage must be between 0 and 100");
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
