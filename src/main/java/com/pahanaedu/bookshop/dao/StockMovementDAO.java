package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.StockMovement;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for StockMovement entity
 */
public class StockMovementDAO implements BaseDAO<StockMovement, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(StockMovementDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO stock_movements (item_id, movement_type, quantity, reference_type, 
                                   reference_id, notes, movement_date, created_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE stock_movements SET item_id = ?, movement_type = ?, quantity = ?, 
                                  reference_type = ?, reference_id = ?, notes = ?
        WHERE movement_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM stock_movements WHERE movement_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT sm.movement_id, sm.item_id, i.item_code, i.item_name, sm.movement_type, 
               sm.quantity, sm.reference_type, sm.reference_id, sm.notes, sm.movement_date, 
               sm.created_by, u.full_name as created_by_name
        FROM stock_movements sm
        LEFT JOIN items i ON sm.item_id = i.item_id
        LEFT JOIN users u ON sm.created_by = u.user_id
        WHERE sm.movement_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT sm.movement_id, sm.item_id, i.item_code, i.item_name, sm.movement_type, 
               sm.quantity, sm.reference_type, sm.reference_id, sm.notes, sm.movement_date, 
               sm.created_by, u.full_name as created_by_name
        FROM stock_movements sm
        LEFT JOIN items i ON sm.item_id = i.item_id
        LEFT JOIN users u ON sm.created_by = u.user_id
        ORDER BY sm.movement_date DESC
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM stock_movements";

    private static final String EXISTS_SQL = "SELECT 1 FROM stock_movements WHERE movement_id = ?";

    private static final String FIND_BY_ITEM_SQL = """
        SELECT sm.movement_id, sm.item_id, i.item_code, i.item_name, sm.movement_type, 
               sm.quantity, sm.reference_type, sm.reference_id, sm.notes, sm.movement_date, 
               sm.created_by, u.full_name as created_by_name
        FROM stock_movements sm
        LEFT JOIN items i ON sm.item_id = i.item_id
        LEFT JOIN users u ON sm.created_by = u.user_id
        WHERE sm.item_id = ?
        ORDER BY sm.movement_date DESC
        LIMIT ? OFFSET ?
        """;

    private static final String FIND_BY_REFERENCE_SQL = """
        SELECT sm.movement_id, sm.item_id, i.item_code, i.item_name, sm.movement_type, 
               sm.quantity, sm.reference_type, sm.reference_id, sm.notes, sm.movement_date, 
               sm.created_by, u.full_name as created_by_name
        FROM stock_movements sm
        LEFT JOIN items i ON sm.item_id = i.item_id
        LEFT JOIN users u ON sm.created_by = u.user_id
        WHERE sm.reference_type = ? AND sm.reference_id = ?
        ORDER BY sm.movement_date DESC
        """;

    @Override
    public StockMovement save(StockMovement stockMovement) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            if (stockMovement.getMovementDate() == null) {
                stockMovement.setMovementDate(LocalDateTime.now());
            }

            stmt.setInt(1, stockMovement.getItemId());
            stmt.setString(2, stockMovement.getMovementType().getValue());
            stmt.setInt(3, stockMovement.getQuantity());
            stmt.setString(4, stockMovement.getReferenceType().getValue());
            stmt.setObject(5, stockMovement.getReferenceId());
            stmt.setString(6, stockMovement.getNotes());
            stmt.setTimestamp(7, Timestamp.valueOf(stockMovement.getMovementDate()));
            stmt.setObject(8, stockMovement.getCreatedBy());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating stock movement failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    stockMovement.setMovementId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating stock movement failed, no ID obtained");
                }
            }

            logger.debug("Stock movement created successfully for item: {}", stockMovement.getItemId());
            return stockMovement;

        } catch (SQLException e) {
            logger.error("Error creating stock movement for item: {}", stockMovement.getItemId(), e);
            throw new DAOException("Failed to create stock movement", e);
        }
    }

    @Override
    public StockMovement update(StockMovement stockMovement) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setInt(1, stockMovement.getItemId());
            stmt.setString(2, stockMovement.getMovementType().getValue());
            stmt.setInt(3, stockMovement.getQuantity());
            stmt.setString(4, stockMovement.getReferenceType().getValue());
            stmt.setObject(5, stockMovement.getReferenceId());
            stmt.setString(6, stockMovement.getNotes());
            stmt.setInt(7, stockMovement.getMovementId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Stock movement not found with ID: " + stockMovement.getMovementId());
            }

            logger.debug("Stock movement updated successfully: {}", stockMovement.getMovementId());
            return stockMovement;

        } catch (SQLException e) {
            logger.error("Error updating stock movement: {}", stockMovement.getMovementId(), e);
            throw new DAOException("Failed to update stock movement", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.debug("Stock movement deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting stock movement: {}", id, e);
            throw new DAOException("Failed to delete stock movement", e);
        }
    }

    @Override
    public Optional<StockMovement> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStockMovement(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding stock movement by ID: {}", id, e);
            throw new DAOException("Failed to find stock movement by ID", e);
        }
    }

    @Override
    public List<StockMovement> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<StockMovement> stockMovements = new ArrayList<>();
            while (rs.next()) {
                stockMovements.add(mapResultSetToStockMovement(rs));
            }
            return stockMovements;

        } catch (SQLException e) {
            logger.error("Error finding all stock movements", e);
            throw new DAOException("Failed to find all stock movements", e);
        }
    }

    @Override
    public List<StockMovement> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<StockMovement> stockMovements = new ArrayList<>();
                while (rs.next()) {
                    stockMovements.add(mapResultSetToStockMovement(rs));
                }
                return stockMovements;
            }

        } catch (SQLException e) {
            logger.error("Error finding stock movements with pagination", e);
            throw new DAOException("Failed to find stock movements with pagination", e);
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
            logger.error("Error counting stock movements", e);
            throw new DAOException("Failed to count stock movements", e);
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
            logger.error("Error checking if stock movement exists: {}", id, e);
            throw new DAOException("Failed to check if stock movement exists", e);
        }
    }

    /**
     * Find stock movements for a specific item
     * @param itemId Item ID
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of stock movements
     * @throws DAOException if find operation fails
     */
    public List<StockMovement> findByItem(Integer itemId, int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ITEM_SQL)) {

            stmt.setInt(1, itemId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<StockMovement> stockMovements = new ArrayList<>();
                while (rs.next()) {
                    stockMovements.add(mapResultSetToStockMovement(rs));
                }
                return stockMovements;
            }

        } catch (SQLException e) {
            logger.error("Error finding stock movements by item: {}", itemId, e);
            throw new DAOException("Failed to find stock movements by item", e);
        }
    }

    /**
     * Find stock movements by reference
     * @param referenceType Reference type
     * @param referenceId Reference ID
     * @return List of stock movements
     * @throws DAOException if find operation fails
     */
    public List<StockMovement> findByReference(StockMovement.ReferenceType referenceType, Integer referenceId) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_REFERENCE_SQL)) {

            stmt.setString(1, referenceType.getValue());
            stmt.setInt(2, referenceId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<StockMovement> stockMovements = new ArrayList<>();
                while (rs.next()) {
                    stockMovements.add(mapResultSetToStockMovement(rs));
                }
                return stockMovements;
            }

        } catch (SQLException e) {
            logger.error("Error finding stock movements by reference: {} {}", referenceType, referenceId, e);
            throw new DAOException("Failed to find stock movements by reference", e);
        }
    }

    private StockMovement mapResultSetToStockMovement(ResultSet rs) throws SQLException {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setMovementId(rs.getInt("movement_id"));
        stockMovement.setItemId(rs.getInt("item_id"));
        stockMovement.setItemCode(rs.getString("item_code"));
        stockMovement.setItemName(rs.getString("item_name"));
        stockMovement.setMovementType(StockMovement.MovementType.fromString(rs.getString("movement_type")));
        stockMovement.setQuantity(rs.getInt("quantity"));
        stockMovement.setReferenceType(StockMovement.ReferenceType.fromString(rs.getString("reference_type")));

        Integer referenceId = rs.getObject("reference_id", Integer.class);
        stockMovement.setReferenceId(referenceId);

        stockMovement.setNotes(rs.getString("notes"));
        stockMovement.setMovementDate(rs.getTimestamp("movement_date").toLocalDateTime());

        Integer createdBy = rs.getObject("created_by", Integer.class);
        stockMovement.setCreatedBy(createdBy);
        stockMovement.setCreatedByName(rs.getString("created_by_name"));

        return stockMovement;
    }
}
