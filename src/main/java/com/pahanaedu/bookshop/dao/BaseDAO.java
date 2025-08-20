package com.pahanaedu.bookshop.dao;

import java.util.List;
import java.util.Optional;

/**
 * Base DAO interface defining common CRUD operations
 * @param <T> Entity type
 * @param <ID> Primary key type
 */
public interface BaseDAO<T, ID> {
    
    /**
     * Save a new entity
     * @param entity Entity to save
     * @return Saved entity with generated ID
     * @throws DAOException if save operation fails
     */
    T save(T entity) throws DAOException;
    
    /**
     * Update an existing entity
     * @param entity Entity to update
     * @return Updated entity
     * @throws DAOException if update operation fails
     */
    T update(T entity) throws DAOException;
    
    /**
     * Delete an entity by ID
     * @param id Entity ID
     * @return true if deleted, false if not found
     * @throws DAOException if delete operation fails
     */
    boolean delete(ID id) throws DAOException;
    
    /**
     * Find an entity by ID
     * @param id Entity ID
     * @return Optional containing the entity if found
     * @throws DAOException if find operation fails
     */
    Optional<T> findById(ID id) throws DAOException;
    
    /**
     * Find all entities
     * @return List of all entities
     * @throws DAOException if find operation fails
     */
    List<T> findAll() throws DAOException;
    
    /**
     * Find entities with pagination
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of entities
     * @throws DAOException if find operation fails
     */
    List<T> findAll(int offset, int limit) throws DAOException;
    
    /**
     * Count total number of entities
     * @return Total count
     * @throws DAOException if count operation fails
     */
    long count() throws DAOException;
    
    /**
     * Check if an entity exists by ID
     * @param id Entity ID
     * @return true if exists, false otherwise
     * @throws DAOException if check operation fails
     */
    boolean exists(ID id) throws DAOException;
}
