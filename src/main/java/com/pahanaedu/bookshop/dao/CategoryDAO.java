package com.pahanaedu.bookshop.dao;

import com.pahanaedu.bookshop.model.Category;
import com.pahanaedu.bookshop.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for Category entity
 */
public class CategoryDAO implements BaseDAO<Category, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

    private static final String INSERT_SQL = """
        INSERT INTO categories (category_name, description, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE categories SET category_name = ?, description = ?, status = ?, updated_at = ?
        WHERE category_id = ?
        """;

    private static final String DELETE_SQL = "DELETE FROM categories WHERE category_id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT category_id, category_name, description, status, created_at, updated_at
        FROM categories WHERE category_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT category_id, category_name, description, status, created_at, updated_at
        FROM categories ORDER BY category_name
        """;

    private static final String FIND_ALL_PAGINATED_SQL = FIND_ALL_SQL + " LIMIT ? OFFSET ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM categories";

    private static final String EXISTS_SQL = "SELECT 1 FROM categories WHERE category_id = ?";

    private static final String FIND_ACTIVE_SQL = """
        SELECT category_id, category_name, description, status, created_at, updated_at
        FROM categories WHERE status = 'active' ORDER BY category_name
        """;

    private static final String FIND_BY_NAME_SQL = FIND_BY_ID_SQL.replace("category_id = ?", "category_name = ?");

    @Override
    public Category save(Category category) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            category.setCreatedAt(now);
            category.setUpdatedAt(now);

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getStatus().getValue());
            stmt.setTimestamp(4, Timestamp.valueOf(category.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(category.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating category failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setCategoryId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating category failed, no ID obtained");
                }
            }

            logger.info("Category created successfully: {}", category.getCategoryName());
            return category;

        } catch (SQLException e) {
            logger.error("Error creating category: {}", category.getCategoryName(), e);
            throw new DAOException("Failed to create category", e);
        }
    }

    @Override
    public Category update(Category category) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            category.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getStatus().getValue());
            stmt.setTimestamp(4, Timestamp.valueOf(category.getUpdatedAt()));
            stmt.setInt(5, category.getCategoryId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Category not found with ID: " + category.getCategoryId());
            }

            logger.info("Category updated successfully: {}", category.getCategoryName());
            return category;

        } catch (SQLException e) {
            logger.error("Error updating category: {}", category.getCategoryId(), e);
            throw new DAOException("Failed to update category", e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Category deleted successfully: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error deleting category: {}", id, e);
            throw new DAOException("Failed to delete category", e);
        }
    }

    @Override
    public Optional<Category> findById(Integer id) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding category by ID: {}", id, e);
            throw new DAOException("Failed to find category by ID", e);
        }
    }

    @Override
    public List<Category> findAll() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            return categories;

        } catch (SQLException e) {
            logger.error("Error finding all categories", e);
            throw new DAOException("Failed to find all categories", e);
        }
    }

    @Override
    public List<Category> findAll(int offset, int limit) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PAGINATED_SQL)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Category> categories = new ArrayList<>();
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
                return categories;
            }

        } catch (SQLException e) {
            logger.error("Error finding categories with pagination", e);
            throw new DAOException("Failed to find categories with pagination", e);
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
            logger.error("Error counting categories", e);
            throw new DAOException("Failed to count categories", e);
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
            logger.error("Error checking if category exists: {}", id, e);
            throw new DAOException("Failed to check if category exists", e);
        }
    }

    /**
     * Find all active categories
     * @return List of active categories
     * @throws DAOException if find operation fails
     */
    public List<Category> findActive() throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ACTIVE_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            return categories;

        } catch (SQLException e) {
            logger.error("Error finding active categories", e);
            throw new DAOException("Failed to find active categories", e);
        }
    }

    /**
     * Find category by name
     * @param categoryName Category name to search for
     * @return Optional containing the category if found
     * @throws DAOException if find operation fails
     */
    public Optional<Category> findByName(String categoryName) throws DAOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_NAME_SQL)) {

            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding category by name: {}", categoryName, e);
            throw new DAOException("Failed to find category by name", e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        category.setStatus(Category.CategoryStatus.fromString(rs.getString("status")));
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return category;
    }
}
