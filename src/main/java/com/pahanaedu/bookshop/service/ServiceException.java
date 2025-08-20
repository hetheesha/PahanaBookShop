package com.pahanaedu.bookshop.service;

/**
 * Custom exception for service layer operations
 */
public class ServiceException extends Exception {
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ServiceException(Throwable cause) {
        super(cause);
    }
}
