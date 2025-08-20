package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.Bill;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for Bill entity
 */
public class BillDAO implements BaseDAO<Bill, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(BillDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO bills (bill_number, customer_id, bill_date, bill_time, subtotal, 
                          discount_percentage, discount_amount, tax_percentage, tax_amount, 
                          total_amount, payment_method, payment_status, notes, status, 
                          created_by, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE bills SET bill_number = ?, customer_id = ?, bill_date = ?, bill_time = ?, 
                        subtotal = ?, discount_percentage = ?, discount_amount = ?, 
                        tax_percentage = ?, tax_amount = ?, total_amount = ?, 
                        payment_method = ?, payment_status = ?, notes = ?, status = ?, 
                        updated_at = ?
        WHERE bill_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM bills WHERE bill_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT b.bill_id, b.bill_number, b.customer_id, c.full_name as customer_name, 
               c.account_no as customer_account_no, b.bill_date, b.bill_time, b.subtotal, 
               b.discount_percentage, b.discount_amount, b.tax_percentage, b.tax_amount, 
               b.total_amount, b.payment_method, b.payment_status, b.notes, b.status, 
               b.created_at, b.updated_at, b.created_by
        FROM bills b
        LEFT JOIN customers c ON b.customer_id = c.customer_id
        WHERE b.bill_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT b.bill_id, b.bill_number, b.customer_id, c.full_name as customer_name, 
               c.account_no as customer_account_no, b.bill_date, b.bill_time, b.subtotal, 
               b.discount_percentage, b.discount_amount, b.tax_percentage, b.tax_amount, 
               b.total_amount, b.payment_method, b.payment_status, b.notes, b.status, 
               b.created_at, b.updated_at, b.created_by
        FROM bills b
        LEFT JOIN customers c ON b.customer_id = c.customer_id
        ORDER BY b.created_at DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM bills";

    private static final String EXISTS_SQL = "SELECT 1 FROM bills WHERE bill_id = ?";

    private static final String FIND_BY_NUMBER_SQL = FIND_BY_ID_SQL.replace("b.bill_id = ?", "b.bill_number = ?");

    private static final String FIND_BY_CUSTOMER_SQL = """
        SELECT b.bill_id, b.bill_number, b.customer_id, c.full_name as customer_name, 
               c.account_no as customer_account_no, b.bill_date, b.bill_time, b.subtotal, 
               b.discount_percentage, b.discount_amount, b.tax_percentage, b.tax_amount, 
               b.total_amount, b.payment_method, b.payment_status, b.notes, b.status, 
               b.created_at, b.updated_at, b.created_by
        FROM bills b
        LEFT JOIN customers c ON b.customer_id = c.customer_id
        WHERE b.customer_id = ? AND b.status = 'active'
        ORDER BY b.created_at DESC
        LIMIT ? OFFSET ?
        """;

    private static final String FIND_BY_DATE_RANGE_SQL = """
        SELECT b.bill_id, b.bill_number, b.customer_id, c.full_name as customer_name, 
               c.account_no as customer_account_no, b.bill_date, b.bill_time, b.subtotal, 
               b.discount_percentage, b.discount_amount, b.tax_percentage, b.tax_amount, 
               b.total_amount, b.payment_method, b.payment_status, b.notes, b.status, 
               b.created_at, b.updated_at, b.created_by
        FROM bills b
        LEFT JOIN customers c ON b.customer_id = c.customer_id
        WHERE b.bill_date BETWEEN ? AND ? AND b.status = 'active'
        ORDER BY b.bill_date DESC, b.bill_time DESC
        LIMIT ? OFFSET ?
        """;

    @Override
    public Bill save(Bill bill) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            bill.setCreatedAt(now);
            bill.setUpdatedAt(now);

            stmt.setString(1, bill.getBillNumber());
            stmt.setInt(2, bill.getCustomerId());
            stmt.setDate(3, Date.valueOf(bill.getBillDate()));
            stmt.setTime(4, Time.valueOf(bill.getBillTime()));
            stmt.setBigDecimal(5, bill.getSubtotal());
            stmt.setBigDecimal(6, bill.getDiscountPercentage());
            stmt.setBigDecimal(7, bill.getDiscountAmount());
            stmt.setBigDecimal(8, bill.getTaxPercentage());
            stmt.setBigDecimal(9, bill.getTaxAmount());
            stmt.setBigDecimal(10, bill.getTotalAmount());
            stmt.setString(11, bill.getPaymentMethod().getValue());
            stmt.setString(12, bill.getPaymentStatus().getValue());
            stmt.setString(13, bill.getNotes());
            stmt.setString(14, bill.getStatus().getValue());
            stmt.setObject(15, bill.getCreatedBy());
            stmt.setTimestamp(16, Timestamp.valueOf(bill.getCreatedAt()));
            stmt.setTimestamp(17, Timestamp.valueOf(bill.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating bill failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bill.setBillId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating bill failed, no ID obtained");
                }
            }

            logger.info("Bill created successfully: {}", bill.getBillNumber());
            return bill;

        } catch (SQLException e) {
            logger.error("Error creating bill: {}", bill.getBillNumber(), e);
            throw new DAOException("Failed to create bill", e);
        }
    }

    @Override
    public Bill update(Bill bill) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            bill.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, bill.getBillNumber());
            stmt.setInt(2, bill.getCustomerId());
            stmt.setDate(3, Date.valueOf(bill.getBillDate()));
            stmt.setTime(4, Time.valueOf(bill.getBillTime()));
            stmt.setBigDecimal(5, bill.getSubtotal());
            stmt.setBigDecimal(6, bill.getDiscountPercentage());
            stmt.setBigDecimal(7, bill.getDiscountAmount());
            stmt.setBigDecimal(8, bill.getTaxPercentage());
            stmt.setBigDecimal(9, bill.getTaxAmount());
            stmt.setBigDecimal(10, bill.getTotalAmount());
            stmt.setString(11, bill.getPaymentMethod().getValue());
            stmt.setString(12, bill.getPaymentStatus().getValue());
            stmt.setString(13, bill.getNotes());
            stmt.setString(14, bill.getStatus().getValue());
            stmt.setTimestamp(15, Timestamp.valueOf(bill.getUpdatedAt()));
            stmt.setInt(16, bill.getBillId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Bill not found with ID: " + bill.getBillId());
            }

            logger.info("Bill updated successfully: {}", bill.getBillNumber());
            return bill;

        } catch (SQLException e) {
            logger.error("Error updating bill: {}", bill.getBillId(), e);
            throw new DAOException("Failed to update bill", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Bill deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting bill: {}", id, e);
            throw new DAOException("Failed to delete bill", e);
        }
    }

    @Override
    public Optional<Bill> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by ID: {}", id, e);
            throw new DAOException("Failed to find bill by ID", e);
        }
    }

    @Override
    public List<Bill> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Bill> bills = new ArrayList<>();
            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
            return bills;

        } catch (SQLException e) {
            logger.error("Error finding all bills", e);
            throw new DAOException("Failed to find all bills", e);
        }
    }

    @Override
    public List<Bill> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Bill> bills = new ArrayList<>();
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
                return bills;
            }

        } catch (SQLException e) {
            logger.error("Error finding bills with pagination", e);
            throw new DAOException("Failed to find bills with pagination", e);
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
            logger.error("Error counting bills", e);
            throw new DAOException("Failed to count bills", e);
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
            logger.error("Error checking if bill exists: {}", id, e);
            throw new DAOException("Failed to check if bill exists", e);
        }
    }

    /**
     * Find bill by bill number
     * @param billNumber Bill number to search for
     * @return Optional containing the bill if found
     * @throws DAOException if find operation fails
     */
    public Optional<Bill> findByNumber(String billNumber) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_NUMBER_SQL)) {

            stmt.setString(1, billNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by number: {}", billNumber, e);
            throw new DAOException("Failed to find bill by number", e);
        }
    }

    /**
     * Find bills by customer
     * @param customerId Customer ID
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of customer bills
     * @throws DAOException if find operation fails
     */
    public List<Bill> findByCustomer(Integer customerId, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_CUSTOMER_SQL)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Bill> bills = new ArrayList<>();
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
                return bills;
            }

        } catch (SQLException e) {
            logger.error("Error finding bills by customer: {}", customerId, e);
            throw new DAOException("Failed to find bills by customer", e);
        }
    }

    /**
     * Find bills by date range
     * @param startDate Start date
     * @param endDate End date
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of bills in date range
     * @throws DAOException if find operation fails
     */
    public List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_DATE_RANGE_SQL)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Bill> bills = new ArrayList<>();
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
                return bills;
            }

        } catch (SQLException e) {
            logger.error("Error finding bills by date range: {} to {}", startDate, endDate, e);
            throw new DAOException("Failed to find bills by date range", e);
        }
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setBillNumber(rs.getString("bill_number"));
        bill.setCustomerId(rs.getInt("customer_id"));
        bill.setCustomerName(rs.getString("customer_name"));
        bill.setCustomerAccountNo(rs.getString("customer_account_no"));
        bill.setBillDate(rs.getDate("bill_date").toLocalDate());
        bill.setBillTime(rs.getTime("bill_time").toLocalTime());
        bill.setSubtotal(rs.getBigDecimal("subtotal"));
        bill.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        bill.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        bill.setTaxPercentage(rs.getBigDecimal("tax_percentage"));
        bill.setTaxAmount(rs.getBigDecimal("tax_amount"));
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        bill.setPaymentMethod(Bill.PaymentMethod.fromString(rs.getString("payment_method")));
        bill.setPaymentStatus(Bill.PaymentStatus.fromString(rs.getString("payment_status")));
        bill.setNotes(rs.getString("notes"));
        bill.setStatus(Bill.BillStatus.fromString(rs.getString("status")));
        bill.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        bill.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Integer createdBy = rs.getObject("created_by", Integer.class);
        bill.setCreatedBy(createdBy);

        return bill;
    }
}
