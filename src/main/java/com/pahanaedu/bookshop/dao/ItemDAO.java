package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.Item;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for Item entity
 */
public class ItemDAO implements BaseDAO<Item, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(ItemDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO items (item_code, item_name, category_id, description, price, cost_price, 
                          stock_quantity, min_stock_level, max_stock_level, unit, barcode, isbn, 
                          author, publisher, publication_year, status, total_sold, total_revenue, 
                          created_by, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE items SET item_code = ?, item_name = ?, category_id = ?, description = ?, 
                        price = ?, cost_price = ?, stock_quantity = ?, min_stock_level = ?, 
                        max_stock_level = ?, unit = ?, barcode = ?, isbn = ?, author = ?, 
                        publisher = ?, publication_year = ?, status = ?, updated_at = ?
        WHERE item_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM items WHERE item_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT i.item_id, i.item_code, i.item_name, i.category_id, c.category_name, 
               i.description, i.price, i.cost_price, i.stock_quantity, i.min_stock_level, 
               i.max_stock_level, i.unit, i.barcode, i.isbn, i.author, i.publisher, 
               i.publication_year, i.status, i.total_sold, i.total_revenue, 
               i.created_at, i.updated_at, i.created_by
        FROM items i
        LEFT JOIN categories c ON i.category_id = c.category_id
        WHERE i.item_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT i.item_id, i.item_code, i.item_name, i.category_id, c.category_name, 
               i.description, i.price, i.cost_price, i.stock_quantity, i.min_stock_level, 
               i.max_stock_level, i.unit, i.barcode, i.isbn, i.author, i.publisher, 
               i.publication_year, i.status, i.total_sold, i.total_revenue, 
               i.created_at, i.updated_at, i.created_by
        FROM items i
        LEFT JOIN categories c ON i.category_id = c.category_id
        ORDER BY i.created_at DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM items";

    private static final String EXISTS_SQL = "SELECT 1 FROM items WHERE item_id = ?";

    private static final String FIND_BY_CODE_SQL = FIND_BY_ID_SQL.replace("i.item_id = ?", "i.item_code = ?");

    private static final String FIND_BY_BARCODE_SQL = FIND_BY_ID_SQL.replace("i.item_id = ?", "i.barcode = ?");

    private static final String FIND_LOW_STOCK_SQL = """
        SELECT i.item_id, i.item_code, i.item_name, i.category_id, c.category_name, 
               i.description, i.price, i.cost_price, i.stock_quantity, i.min_stock_level, 
               i.max_stock_level, i.unit, i.barcode, i.isbn, i.author, i.publisher, 
               i.publication_year, i.status, i.total_sold, i.total_revenue, 
               i.created_at, i.updated_at, i.created_by
        FROM items i
        LEFT JOIN categories c ON i.category_id = c.category_id
        WHERE i.stock_quantity <= i.min_stock_level AND i.status = 'active'
        ORDER BY i.stock_quantity ASC
        """;

    private static final String SEARCH_SQL = """
        SELECT i.item_id, i.item_code, i.item_name, i.category_id, c.category_name, 
               i.description, i.price, i.cost_price, i.stock_quantity, i.min_stock_level, 
               i.max_stock_level, i.unit, i.barcode, i.isbn, i.author, i.publisher, 
               i.publication_year, i.status, i.total_sold, i.total_revenue, 
               i.created_at, i.updated_at, i.created_by
        FROM items i
        LEFT JOIN categories c ON i.category_id = c.category_id
        WHERE (i.item_name LIKE ? OR i.item_code LIKE ? OR i.author LIKE ? OR i.isbn LIKE ?)
        AND i.status = 'active'
        ORDER BY i.item_name
        LIMIT ? OFFSET ?
        """;

    private static final String UPDATE_STOCK_SQL = """
        UPDATE items SET stock_quantity = ?, updated_at = ? WHERE item_id = ?
        """;

    private static final String UPDATE_SALES_TOTALS_SQL = """
        UPDATE items SET total_sold = ?, total_revenue = ?, updated_at = ? WHERE item_id = ?
        """;

    @Override
    public Item save(Item item) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            item.setCreatedAt(now);
            item.setUpdatedAt(now);

            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getItemName());
            stmt.setInt(3, item.getCategoryId());
            stmt.setString(4, item.getDescription());
            stmt.setBigDecimal(5, item.getPrice());
            stmt.setBigDecimal(6, item.getCostPrice());
            stmt.setInt(7, item.getStockQuantity());
            stmt.setInt(8, item.getMinStockLevel());
            stmt.setInt(9, item.getMaxStockLevel());
            stmt.setString(10, item.getUnit());
            stmt.setString(11, item.getBarcode());
            stmt.setString(12, item.getIsbn());
            stmt.setString(13, item.getAuthor());
            stmt.setString(14, item.getPublisher());
            stmt.setObject(15, item.getPublicationYear());
            stmt.setString(16, item.getStatus().getValue());
            stmt.setInt(17, item.getTotalSold());
            stmt.setBigDecimal(18, item.getTotalRevenue());
            stmt.setObject(19, item.getCreatedBy());
            stmt.setTimestamp(20, Timestamp.valueOf(item.getCreatedAt()));
            stmt.setTimestamp(21, Timestamp.valueOf(item.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating item failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setItemId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating item failed, no ID obtained");
                }
            }

            logger.info("Item created successfully: {}", item.getItemCode());
            return item;

        } catch (SQLException e) {
            logger.error("Error creating item: {}", item.getItemCode(), e);
            throw new DAOException("Failed to create item", e);
        }
    }

    @Override
    public Item update(Item item) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            item.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getItemName());
            stmt.setInt(3, item.getCategoryId());
            stmt.setString(4, item.getDescription());
            stmt.setBigDecimal(5, item.getPrice());
            stmt.setBigDecimal(6, item.getCostPrice());
            stmt.setInt(7, item.getStockQuantity());
            stmt.setInt(8, item.getMinStockLevel());
            stmt.setInt(9, item.getMaxStockLevel());
            stmt.setString(10, item.getUnit());
            stmt.setString(11, item.getBarcode());
            stmt.setString(12, item.getIsbn());
            stmt.setString(13, item.getAuthor());
            stmt.setString(14, item.getPublisher());
            stmt.setObject(15, item.getPublicationYear());
            stmt.setString(16, item.getStatus().getValue());
            stmt.setTimestamp(17, Timestamp.valueOf(item.getUpdatedAt()));
            stmt.setInt(18, item.getItemId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Item not found with ID: " + item.getItemId());
            }

            logger.info("Item updated successfully: {}", item.getItemCode());
            return item;

        } catch (SQLException e) {
            logger.error("Error updating item: {}", item.getItemId(), e);
            throw new DAOException("Failed to update item", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Item deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting item: {}", id, e);
            throw new DAOException("Failed to delete item", e);
        }
    }

    @Override
    public Optional<Item> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding item by ID: {}", id, e);
            throw new DAOException("Failed to find item by ID", e);
        }
    }

    @Override
    public List<Item> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
            return items;

        } catch (SQLException e) {
            logger.error("Error finding all items", e);
            throw new DAOException("Failed to find all items", e);
        }
    }

    @Override
    public List<Item> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Item> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
                return items;
            }

        } catch (SQLException e) {
            logger.error("Error finding items with pagination", e);
            throw new DAOException("Failed to find items with pagination", e);
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
            logger.error("Error counting items", e);
            throw new DAOException("Failed to count items", e);
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
            logger.error("Error checking if item exists: {}", id, e);
            throw new DAOException("Failed to check if item exists", e);
        }
    }

    /**
     * Find item by item code
     * @param itemCode Item code to search for
     * @return Optional containing the item if found
     * @throws DAOException if find operation fails
     */
    public Optional<Item> findByCode(String itemCode) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_CODE_SQL)) {

            stmt.setString(1, itemCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding item by code: {}", itemCode, e);
            throw new DAOException("Failed to find item by code", e);
        }
    }

    /**
     * Find item by barcode
     * @param barcode Barcode to search for
     * @return Optional containing the item if found
     * @throws DAOException if find operation fails
     */
    public Optional<Item> findByBarcode(String barcode) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_BARCODE_SQL)) {

            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding item by barcode: {}", barcode, e);
            throw new DAOException("Failed to find item by barcode", e);
        }
    }

    /**
     * Find items with low stock
     * @return List of items with low stock
     * @throws DAOException if find operation fails
     */
    public List<Item> findLowStock() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_LOW_STOCK_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
            return items;

        } catch (SQLException e) {
            logger.error("Error finding low stock items", e);
            throw new DAOException("Failed to find low stock items", e);
        }
    }

    /**
     * Search items by name, code, author, or ISBN
     * @param searchTerm Search term
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of matching items
     * @throws DAOException if search operation fails
     */
    public List<Item> search(String searchTerm, int offset, int limit) throws DAOException {
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
                List<Item> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
                return items;
            }

        } catch (SQLException e) {
            logger.error("Error searching items with term: {}", searchTerm, e);
            throw new DAOException("Failed to search items", e);
        }
    }

    /**
     * Update item stock quantity
     * @param itemId Item ID
     * @param newQuantity New stock quantity
     * @throws DAOException if update operation fails
     */
    public void updateStock(Integer itemId, Integer newQuantity) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STOCK_SQL)) {

            stmt.setInt(1, newQuantity);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, itemId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating stock for item: {}", itemId, e);
            throw new DAOException("Failed to update item stock", e);
        }
    }

    /**
     * Update item sales totals
     * @param itemId Item ID
     * @param totalSold Total quantity sold
     * @param totalRevenue Total revenue
     * @throws DAOException if update operation fails
     */
    public void updateSalesTotals(Integer itemId, Integer totalSold, java.math.BigDecimal totalRevenue) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SALES_TOTALS_SQL)) {

            stmt.setInt(1, totalSold);
            stmt.setBigDecimal(2, totalRevenue);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, itemId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating sales totals for item: {}", itemId, e);
            throw new DAOException("Failed to update item sales totals", e);
        }
    }

    /**
     * Find items by category
     * @param categoryId Category ID
     * @return List of items in the category
     * @throws DAOException if find operation fails
     */
    public List<Item> findByCategory(Integer categoryId) throws DAOException {
        String sql = FIND_ALL_SQL + " WHERE i.category_id = ? AND i.status = 'active'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Item> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
                return items;
            }

        } catch (SQLException e) {
            logger.error("Error finding items by category: {}", categoryId, e);
            throw new DAOException("Failed to find items by category", e);
        }
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setItemCode(rs.getString("item_code"));
        item.setItemName(rs.getString("item_name"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setCostPrice(rs.getBigDecimal("cost_price"));
        item.setStockQuantity(rs.getInt("stock_quantity"));
        item.setMinStockLevel(rs.getInt("min_stock_level"));
        item.setMaxStockLevel(rs.getInt("max_stock_level"));
        item.setUnit(rs.getString("unit"));
        item.setBarcode(rs.getString("barcode"));
        item.setIsbn(rs.getString("isbn"));
        item.setAuthor(rs.getString("author"));
        item.setPublisher(rs.getString("publisher"));

        Integer publicationYear = rs.getObject("publication_year", Integer.class);
        item.setPublicationYear(publicationYear);

        item.setStatus(Item.ItemStatus.fromString(rs.getString("status")));
        item.setTotalSold(rs.getInt("total_sold"));
        item.setTotalRevenue(rs.getBigDecimal("total_revenue"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Integer createdBy = rs.getObject("created_by", Integer.class);
        item.setCreatedBy(createdBy);

        return item;
    }
}
