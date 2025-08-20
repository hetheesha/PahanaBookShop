package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.Customer;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for Customer entity
 */
public class CustomerDAO implements BaseDAO<Customer, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO customers (account_no, full_name, address, phone, email, date_of_birth, gender, 
                              status, total_purchases, total_bills, created_by, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE customers SET account_no = ?, full_name = ?, address = ?, phone = ?, email = ?, 
                            date_of_birth = ?, gender = ?, status = ?, updated_at = ?
        WHERE customer_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM customers WHERE customer_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT customer_id, account_no, full_name, address, phone, email, date_of_birth, gender,
               status, total_purchases, total_bills, created_at, updated_at, created_by
        FROM customers WHERE customer_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT customer_id, account_no, full_name, address, phone, email, date_of_birth, gender,
               status, total_purchases, total_bills, created_at, updated_at, created_by
        FROM customers ORDER BY created_at DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM customers";

    private static final String EXISTS_SQL = "SELECT 1 FROM customers WHERE customer_id = ?";

    private static final String FIND_BY_ACCOUNT_NO_SQL = FIND_BY_ID_SQL.replace("customer_id = ?", "account_no = ?");

    private static final String SEARCH_SQL = """
        SELECT customer_id, account_no, full_name, address, phone, email, date_of_birth, gender,
               status, total_purchases, total_bills, created_at, updated_at, created_by
        FROM customers 
        WHERE (full_name LIKE ? OR account_no LIKE ? OR phone LIKE ? OR email LIKE ?)
        AND status = 'active'
        ORDER BY full_name
        LIMIT ? OFFSET ?
        """;

    private static final String UPDATE_TOTALS_SQL = """
        UPDATE customers SET total_purchases = ?, total_bills = ?, updated_at = ?
        WHERE customer_id = ?
        """;

    @Override
    public Customer save(Customer customer) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            customer.setCreatedAt(now);
            customer.setUpdatedAt(now);

            stmt.setString(1, customer.getAccountNo());
            stmt.setString(2, customer.getFullName());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getEmail());
            
            if (customer.getDateOfBirth() != null) {
                stmt.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            
            if (customer.getGender() != null) {
                stmt.setString(7, customer.getGender().getValue());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            
            stmt.setString(8, customer.getStatus().getValue());
            stmt.setBigDecimal(9, customer.getTotalPurchases());
            stmt.setInt(10, customer.getTotalBills());
            stmt.setObject(11, customer.getCreatedBy());
            stmt.setTimestamp(12, Timestamp.valueOf(customer.getCreatedAt()));
            stmt.setTimestamp(13, Timestamp.valueOf(customer.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating customer failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setCustomerId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating customer failed, no ID obtained");
                }
            }

            logger.info("Customer created successfully: {}", customer.getAccountNo());
            return customer;

        } catch (SQLException e) {
            logger.error("Error creating customer: {}", customer.getAccountNo(), e);
            throw new DAOException("Failed to create customer", e);
        }
    }

    @Override
    public Customer update(Customer customer) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            customer.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, customer.getAccountNo());
            stmt.setString(2, customer.getFullName());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getEmail());
            
            if (customer.getDateOfBirth() != null) {
                stmt.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            
            if (customer.getGender() != null) {
                stmt.setString(7, customer.getGender().getValue());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            
            stmt.setString(8, customer.getStatus().getValue());
            stmt.setTimestamp(9, Timestamp.valueOf(customer.getUpdatedAt()));
            stmt.setInt(10, customer.getCustomerId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Customer not found with ID: " + customer.getCustomerId());
            }

            logger.info("Customer updated successfully: {}", customer.getAccountNo());
            return customer;

        } catch (SQLException e) {
            logger.error("Error updating customer: {}", customer.getCustomerId(), e);
            throw new DAOException("Failed to update customer", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Customer deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting customer: {}", id, e);
            throw new DAOException("Failed to delete customer", e);
        }
    }

    @Override
    public Optional<Customer> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding customer by ID: {}", id, e);
            throw new DAOException("Failed to find customer by ID", e);
        }
    }

    @Override
    public List<Customer> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            return customers;

        } catch (SQLException e) {
            logger.error("Error finding all customers", e);
            throw new DAOException("Failed to find all customers", e);
        }
    }

    @Override
    public List<Customer> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Customer> customers = new ArrayList<>();
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
                return customers;
            }

        } catch (SQLException e) {
            logger.error("Error finding customers with pagination", e);
            throw new DAOException("Failed to find customers with pagination", e);
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
            logger.error("Error counting customers", e);
            throw new DAOException("Failed to count customers", e);
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
            logger.error("Error checking if customer exists: {}", id, e);
            throw new DAOException("Failed to check if customer exists", e);
        }
    }

    /**
     * Find customer by account number
     * @param accountNo Account number to search for
     * @return Optional containing the customer if found
     * @throws DAOException if find operation fails
     */
    public Optional<Customer> findByAccountNo(String accountNo) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ACCOUNT_NO_SQL)) {

            stmt.setString(1, accountNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding customer by account number: {}", accountNo, e);
            throw new DAOException("Failed to find customer by account number", e);
        }
    }

    /**
     * Search customers by name, account number, phone, or email
     * @param searchTerm Search term
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of matching customers
     * @throws DAOException if search operation fails
     */
    public List<Customer> search(String searchTerm, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_SQL)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setInt(5, limit);
            stmt.setInt(6, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Customer> customers = new ArrayList<>();
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
                return customers;
            }

        } catch (SQLException e) {
            logger.error("Error searching customers with term: {}", searchTerm, e);
            throw new DAOException("Failed to search customers", e);
        }
    }

    /**
     * Update customer totals (purchases and bills count)
     * @param customerId Customer ID
     * @param totalPurchases Total purchase amount
     * @param totalBills Total number of bills
     * @throws DAOException if update operation fails
     */
    public void updateTotals(Integer customerId, java.math.BigDecimal totalPurchases, Integer totalBills) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_TOTALS_SQL)) {

            stmt.setBigDecimal(1, totalPurchases);
            stmt.setInt(2, totalBills);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, customerId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating totals for customer: {}", customerId, e);
            throw new DAOException("Failed to update customer totals", e);
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setAccountNo(rs.getString("account_no"));
        customer.setFullName(rs.getString("full_name"));
        customer.setAddress(rs.getString("address"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));

        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            customer.setDateOfBirth(dateOfBirth.toLocalDate());
        }

        String gender = rs.getString("gender");
        if (gender != null) {
            customer.setGender(Customer.Gender.fromString(gender));
        }

        customer.setStatus(Customer.CustomerStatus.fromString(rs.getString("status")));
        customer.setTotalPurchases(rs.getBigDecimal("total_purchases"));
        customer.setTotalBills(rs.getInt("total_bills"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        customer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Integer createdBy = rs.getObject("created_by", Integer.class);
        customer.setCreatedBy(createdBy);

        return customer;
    }
}
