package com.pahanaedu.bookshop.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database utility class for managing database connections using HikariCP connection pool
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static HikariDataSource dataSource;
    private static final String CONFIG_FILE = "database.properties";

    static {
        initializeDataSource();
    }

    private static void initializeDataSource() {
        try {
            Properties props = loadDatabaseProperties();
            HikariConfig config = new HikariConfig();
            
            // Database connection properties
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/pahana_edu_bookshop"));
            config.setUsername(props.getProperty("db.username", "root"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            
            // Connection pool properties
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "20")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            
            // Additional MySQL specific properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // Connection validation
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
            
            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                logger.info("Database properties loaded from {}", CONFIG_FILE);
            } else {
                logger.warn("Database properties file {} not found, using defaults", CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.warn("Error loading database properties, using defaults", e);
        }
        
        return props;
    }

    /**
     * Get a database connection from the connection pool
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Get the DataSource instance
     * @return DataSource instance
     */
    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Close a database connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Close database resources safely
     * @param autoCloseable Resource to close (Connection, Statement, ResultSet, etc.)
     */
    public static void closeResource(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                logger.error("Error closing database resource", e);
            }
        }
    }

    /**
     * Test database connectivity
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    /**
     * Get connection pool statistics
     * @return Connection pool info as string
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "DataSource not initialized";
    }

    /**
     * Shutdown the connection pool
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shutdown completed");
        }
    }

    // Private constructor to prevent instantiation
    private DatabaseUtil() {}
}
