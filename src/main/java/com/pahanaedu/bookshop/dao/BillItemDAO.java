package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.BillItem;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for BillItem entity
 */
public class BillItemDAO implements BaseDAO<BillItem, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(BillItemDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO bill_items (bill_id, item_id, quantity, unit_price, discount_percentage, 
                               discount_amount, line_total, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE bill_items SET bill_id = ?, item_id = ?, quantity = ?, unit_price = ?, 
                             discount_percentage = ?, discount_amount = ?, line_total = ?
        WHERE bill_item_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM bill_items WHERE bill_item_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT bi.bill_item_id, bi.bill_id, bi.item_id, i.item_code, i.item_name, 
               bi.quantity, bi.unit_price, bi.discount_percentage, bi.discount_amount, 
               bi.line_total, bi.created_at
        FROM bill_items bi
        LEFT JOIN items i ON bi.item_id = i.item_id
        WHERE bi.bill_item_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT bi.bill_item_id, bi.bill_id, bi.item_id, i.item_code, i.item_name, 
               bi.quantity, bi.unit_price, bi.discount_percentage, bi.discount_amount, 
               bi.line_total, bi.created_at
        FROM bill_items bi
        LEFT JOIN items i ON bi.item_id = i.item_id
        ORDER BY bi.created_at DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM bill_items";

    private static final String EXISTS_SQL = "SELECT 1 FROM bill_items WHERE bill_item_id = ?";

    private static final String FIND_BY_BILL_SQL = """
        SELECT bi.bill_item_id, bi.bill_id, bi.item_id, i.item_code, i.item_name, 
               bi.quantity, bi.unit_price, bi.discount_percentage, bi.discount_amount, 
               bi.line_total, bi.created_at
        FROM bill_items bi
        LEFT JOIN items i ON bi.item_id = i.item_id
        WHERE bi.bill_id = ?
        ORDER BY bi.bill_item_id
        """;

    private static final String DELETE_BY_BILL_SQL = "DELETE FROM bill_items WHERE bill_id = ?";

    @Override
    public BillItem save(BillItem billItem) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            billItem.setCreatedAt(LocalDateTime.now());

            stmt.setInt(1, billItem.getBillId());
            stmt.setInt(2, billItem.getItemId());
            stmt.setInt(3, billItem.getQuantity());
            stmt.setBigDecimal(4, billItem.getUnitPrice());
            stmt.setBigDecimal(5, billItem.getDiscountPercentage());
            stmt.setBigDecimal(6, billItem.getDiscountAmount());
            stmt.setBigDecimal(7, billItem.getLineTotal());
            stmt.setTimestamp(8, Timestamp.valueOf(billItem.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating bill item failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    billItem.setBillItemId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating bill item failed, no ID obtained");
                }
            }

            logger.debug("Bill item created successfully for bill: {}", billItem.getBillId());
            return billItem;

        } catch (SQLException e) {
            logger.error("Error creating bill item for bill: {}", billItem.getBillId(), e);
            throw new DAOException("Failed to create bill item", e);
        }
    }

    @Override
    public BillItem update(BillItem billItem) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setInt(1, billItem.getBillId());
            stmt.setInt(2, billItem.getItemId());
            stmt.setInt(3, billItem.getQuantity());
            stmt.setBigDecimal(4, billItem.getUnitPrice());
            stmt.setBigDecimal(5, billItem.getDiscountPercentage());
            stmt.setBigDecimal(6, billItem.getDiscountAmount());
            stmt.setBigDecimal(7, billItem.getLineTotal());
            stmt.setInt(8, billItem.getBillItemId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Bill item not found with ID: " + billItem.getBillItemId());
            }

            logger.debug("Bill item updated successfully: {}", billItem.getBillItemId());
            return billItem;

        } catch (SQLException e) {
            logger.error("Error updating bill item: {}", billItem.getBillItemId(), e);
            throw new DAOException("Failed to update bill item", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.debug("Bill item deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting bill item: {}", id, e);
            throw new DAOException("Failed to delete bill item", e);
        }
    }

    @Override
    public Optional<BillItem> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBillItem(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill item by ID: {}", id, e);
            throw new DAOException("Failed to find bill item by ID", e);
        }
    }

    @Override
    public List<BillItem> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<BillItem> billItems = new ArrayList<>();
            while (rs.next()) {
                billItems.add(mapResultSetToBillItem(rs));
            }
            return billItems;

        } catch (SQLException e) {
            logger.error("Error finding all bill items", e);
            throw new DAOException("Failed to find all bill items", e);
        }
    }

    @Override
    public List<BillItem> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<BillItem> billItems = new ArrayList<>();
                while (rs.next()) {
                    billItems.add(mapResultSetToBillItem(rs));
                }
                return billItems;
            }

        } catch (SQLException e) {
            logger.error("Error finding bill items with pagination", e);
            throw new DAOException("Failed to find bill items with pagination", e);
        }
    }

    @Override
    public long count() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error counting bill items", e);
            throw new DAOException("Failed to count bill items", e);
        }
    }

    @Override
    public boolean exists(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if bill item exists: {}", id, e);
            throw new DAOException("Failed to check if bill item exists", e);
        }
    }

    /**
     * Find all bill items for a specific bill
     * @param billId Bill ID
     * @return List of bill items
     * @throws DAOException if find operation fails
     */
    public List<BillItem> findByBill(Integer billId) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_BILL_SQL)) {

            stmt.setInt(1, billId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<BillItem> billItems = new ArrayList<>();
                while (rs.next()) {
                    billItems.add(mapResultSetToBillItem(rs));
                }
                return billItems;
            }

        } catch (SQLException e) {
            logger.error("Error finding bill items by bill: {}", billId, e);
            throw new DAOException("Failed to find bill items by bill", e);
        }
    }

    /**
     * Delete all bill items for a specific bill
     * @param billId Bill ID
     * @return Number of deleted items
     * @throws DAOException if delete operation fails
     */
    public int deleteByBill(Integer billId) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_BILL_SQL)) {

            stmt.setInt(1, billId);
            int affectedRows = stmt.executeUpdate();

            logger.debug("Deleted {} bill items for bill: {}", affectedRows, billId);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Error deleting bill items by bill: {}", billId, e);
            throw new DAOException("Failed to delete bill items by bill", e);
        }
    }

    /**
     * Save multiple bill items in a batch
     * @param billItems List of bill items to save
     * @throws DAOException if batch save operation fails
     */
    public void saveBatch(List<BillItem> billItems) throws DAOException {
        if (billItems == null || billItems.isEmpty()) {
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);
            LocalDateTime now = LocalDateTime.now();

            for (BillItem billItem : billItems) {
                billItem.setCreatedAt(now);

                stmt.setInt(1, billItem.getBillId());
                stmt.setInt(2, billItem.getItemId());
                stmt.setInt(3, billItem.getQuantity());
                stmt.setBigDecimal(4, billItem.getUnitPrice());
                stmt.setBigDecimal(5, billItem.getDiscountPercentage());
                stmt.setBigDecimal(6, billItem.getDiscountAmount());
                stmt.setBigDecimal(7, billItem.getLineTotal());
                stmt.setTimestamp(8, Timestamp.valueOf(billItem.getCreatedAt()));

                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            
            // Get generated keys
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                int index = 0;
                while (generatedKeys.next() && index < billItems.size()) {
                    billItems.get(index).setBillItemId(generatedKeys.getInt(1));
                    index++;
                }
            }

            conn.commit();
            logger.info("Batch saved {} bill items", billItems.size());

        } catch (SQLException e) {
            logger.error("Error batch saving bill items", e);
            throw new DAOException("Failed to batch save bill items", e);
        }
    }

    private BillItem mapResultSetToBillItem(ResultSet rs) throws SQLException {
        BillItem billItem = new BillItem();
        billItem.setBillItemId(rs.getInt("bill_item_id"));
        billItem.setBillId(rs.getInt("bill_id"));
        billItem.setItemId(rs.getInt("item_id"));
        billItem.setItemCode(rs.getString("item_code"));
        billItem.setItemName(rs.getString("item_name"));
        billItem.setQuantity(rs.getInt("quantity"));
        billItem.setUnitPrice(rs.getBigDecimal("unit_price"));
        billItem.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        billItem.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        billItem.setLineTotal(rs.getBigDecimal("line_total"));
        billItem.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return billItem;
    }
}
