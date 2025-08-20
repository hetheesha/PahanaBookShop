package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.util.ResponseUtil;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base servlet class with common functionality
 */
public abstract class BaseServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    
    protected static final String SESSION_USER_KEY = "user";
    protected static final String SESSION_USER_ID_KEY = "userId";

    /**
     * Get current user from session
     * @param request HTTP request
     * @return Current user or null if not logged in
     */
    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(SESSION_USER_KEY);
        }
        return null;
    }

    /**
     * Get current user ID from session
     * @param request HTTP request
     * @return Current user ID or null if not logged in
     */
    protected Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Integer) session.getAttribute(SESSION_USER_ID_KEY);
        }
        return null;
    }

    /**
     * Check if user is authenticated
     * @param request HTTP request
     * @return true if authenticated, false otherwise
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    /**
     * Check if user has required role
     * @param request HTTP request
     * @param requiredRole Required role
     * @return true if user has role, false otherwise
     */
    protected boolean hasRole(HttpServletRequest request, User.UserRole requiredRole) {
        User user = getCurrentUser(request);
        return user != null && user.hasRole(requiredRole);
    }

    /**
     * Check if user has any of the required roles
     * @param request HTTP request
     * @param requiredRoles Required roles
     * @return true if user has any role, false otherwise
     */
    protected boolean hasAnyRole(HttpServletRequest request, User.UserRole... requiredRoles) {
        User user = getCurrentUser(request);
        return user != null && user.hasAnyRole(requiredRoles);
    }

    /**
     * Require authentication and send unauthorized response if not authenticated
     * @param request HTTP request
     * @param response HTTP response
     * @return true if authenticated, false if unauthorized response sent
     * @throws IOException if response writing fails
     */
    protected boolean requireAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAuthenticated(request)) {
            ResponseUtil.sendUnauthorizedResponse(response, "Authentication required");
            return false;
        }
        return true;
    }

    /**
     * Require specific role and send forbidden response if not authorized
     * @param request HTTP request
     * @param response HTTP response
     * @param requiredRole Required role
     * @return true if authorized, false if forbidden response sent
     * @throws IOException if response writing fails
     */
    protected boolean requireRole(HttpServletRequest request, HttpServletResponse response, User.UserRole requiredRole) throws IOException {
        if (!requireAuthentication(request, response)) {
            return false;
        }
        
        if (!hasRole(request, requiredRole)) {
            ResponseUtil.sendForbiddenResponse(response, "Insufficient privileges");
            return false;
        }
        return true;
    }

    /**
     * Require any of the specified roles
     * @param request HTTP request
     * @param response HTTP response
     * @param requiredRoles Required roles
     * @return true if authorized, false if forbidden response sent
     * @throws IOException if response writing fails
     */
    protected boolean requireAnyRole(HttpServletRequest request, HttpServletResponse response, User.UserRole... requiredRoles) throws IOException {
        if (!requireAuthentication(request, response)) {
            return false;
        }
        
        if (!hasAnyRole(request, requiredRoles)) {
            ResponseUtil.sendForbiddenResponse(response, "Insufficient privileges");
            return false;
        }
        return true;
    }

    /**
     * Read request body as string
     * @param request HTTP request
     * @return Request body as string
     * @throws IOException if reading fails
     */
    protected String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Get pagination parameters from request
     * @param request HTTP request
     * @return Map with page and pageSize parameters
     */
    protected Map<String, Integer> getPaginationParams(HttpServletRequest request) {
        Map<String, Integer> params = new HashMap<>();
        
        try {
            String pageParam = request.getParameter("page");
            int page = (pageParam != null) ? Integer.parseInt(pageParam) : 1;
            params.put("page", Math.max(1, page));
        } catch (NumberFormatException e) {
            params.put("page", 1);
        }
        
        try {
            String pageSizeParam = request.getParameter("pageSize");
            int pageSize = (pageSizeParam != null) ? Integer.parseInt(pageSizeParam) : 20;
            params.put("pageSize", Math.min(Math.max(1, pageSize), 100)); // Limit to 100
        } catch (NumberFormatException e) {
            params.put("pageSize", 20);
        }
        
        return params;
    }

    /**
     * Get client IP address
     * @param request HTTP request
     * @return Client IP address
     */
    protected String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Get user agent
     * @param request HTTP request
     * @return User agent string
     */
    protected String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Handle CORS preflight requests
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if response writing fails
     */
    protected void handleCorsPreflightRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handle OPTIONS requests for CORS
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleCorsPreflightRequest(request, response);
    }

    /**
     * Handle exceptions and send appropriate error response
     * @param response HTTP response
     * @param e Exception
     * @param defaultMessage Default error message
     * @throws IOException if response writing fails
     */
    protected void handleException(HttpServletResponse response, Exception e, String defaultMessage) throws IOException {
        logger.error("Error in servlet", e);
        
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? 
            e.getMessage() : defaultMessage;
            
        ResponseUtil.sendInternalServerErrorResponse(response, message);
    }

    /**
     * Validate required parameters
     * @param request HTTP request
     * @param paramNames Required parameter names
     * @return Map of validation errors (empty if all valid)
     */
    protected Map<String, String> validateRequiredParams(HttpServletRequest request, String... paramNames) {
        Map<String, String> errors = new HashMap<>();
        
        for (String paramName : paramNames) {
            String value = request.getParameter(paramName);
            if (value == null || value.trim().isEmpty()) {
                errors.put(paramName, "Parameter '" + paramName + "' is required");
            }
        }
        
        return errors;
    }

    /**
     * Parse integer parameter safely
     * @param request HTTP request
     * @param paramName Parameter name
     * @param defaultValue Default value if parsing fails
     * @return Parsed integer or default value
     */
    protected Integer parseIntParam(HttpServletRequest request, String paramName, Integer defaultValue) {
        try {
            String value = request.getParameter(paramName);
            return (value != null) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
