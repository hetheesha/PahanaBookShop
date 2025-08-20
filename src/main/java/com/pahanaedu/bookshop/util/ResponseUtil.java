package com.pahanaedu.bookshop.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling HTTP responses and JSON serialization
 */
public class ResponseUtil {
    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Send JSON response
     * @param response HttpServletResponse
     * @param statusCode HTTP status code
     * @param data Data to serialize as JSON
     * @throws IOException if writing response fails
     */
    public static void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Add CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse = objectMapper.writeValueAsString(data);
            writer.write(jsonResponse);
            writer.flush();
        } catch (Exception e) {
            logger.error("Error writing JSON response", e);
            throw new IOException("Failed to write JSON response", e);
        }
    }

    /**
     * Send success response with data
     * @param response HttpServletResponse
     * @param data Data to include in response
     * @throws IOException if writing response fails
     */
    public static void sendSuccessResponse(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("data", data);
        responseMap.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(response, HttpServletResponse.SC_OK, responseMap);
    }

    /**
     * Send success response with message
     * @param response HttpServletResponse
     * @param message Success message
     * @throws IOException if writing response fails
     */
    public static void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", message);
        responseMap.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(response, HttpServletResponse.SC_OK, responseMap);
    }

    /**
     * Send success response with data and message
     * @param response HttpServletResponse
     * @param data Data to include in response
     * @param message Success message
     * @throws IOException if writing response fails
     */
    public static void sendSuccessResponse(HttpServletResponse response, Object data, String message) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("data", data);
        responseMap.put("message", message);
        responseMap.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(response, HttpServletResponse.SC_OK, responseMap);
    }

    /**
     * Send error response
     * @param response HttpServletResponse
     * @param statusCode HTTP status code
     * @param message Error message
     * @throws IOException if writing response fails
     */
    public static void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", false);
        responseMap.put("error", message);
        responseMap.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(response, statusCode, responseMap);
    }

    /**
     * Send validation error response
     * @param response HttpServletResponse
     * @param errors Map of field errors
     * @throws IOException if writing response fails
     */
    public static void sendValidationErrorResponse(HttpServletResponse response, Map<String, String> errors) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", false);
        responseMap.put("error", "Validation failed");
        responseMap.put("validationErrors", errors);
        responseMap.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, responseMap);
    }

    /**
     * Send bad request response
     * @param response HttpServletResponse
     * @param message Error message
     * @throws IOException if writing response fails
     */
    public static void sendBadRequestResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    /**
     * Send unauthorized response
     * @param response HttpServletResponse
     * @param message Error message
     * @throws IOException if writing response fails
     */
    public static void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /**
     * Send forbidden response
     * @param response HttpServletResponse
     * @param message Error message
     * @throws IOException if writing response fails
     */
    public static void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, message);
    }

    /**
     * Send not found response
     * @param response HttpServletResponse
     * @param message Error message
     * @throws IOException if writing response fails
     */
    public static void sendNotFoundResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, message);
    }

    /**
     * Send internal server error response
     * @param response HttpServletResponse
     * @param message Error message
     * @throws IOException if writing response fails
     */
    public static void sendInternalServerErrorResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Send paginated response
     * @param response HttpServletResponse
     * @param data List of data items
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param totalItems Total number of items
     * @throws IOException if writing response fails
     */
    public static void sendPaginatedResponse(HttpServletResponse response, Object data, int page, int pageSize, long totalItems) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("data", data);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("pageSize", pageSize);
        pagination.put("totalItems", totalItems);
        pagination.put("totalPages", (int) Math.ceil((double) totalItems / pageSize));
        pagination.put("hasNext", page * pageSize < totalItems);
        pagination.put("hasPrevious", page > 1);
        
        responseMap.put("pagination", pagination);
        responseMap.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(response, HttpServletResponse.SC_OK, responseMap);
    }

    /**
     * Parse JSON request body
     * @param requestBody JSON string
     * @param valueType Class type to deserialize to
     * @param <T> Type parameter
     * @return Deserialized object
     * @throws IOException if parsing fails
     */
    public static <T> T parseJsonRequest(String requestBody, Class<T> valueType) throws IOException {
        try {
            return objectMapper.readValue(requestBody, valueType);
        } catch (Exception e) {
            logger.error("Error parsing JSON request", e);
            throw new IOException("Invalid JSON format", e);
        }
    }

    /**
     * Convert object to JSON string
     * @param object Object to serialize
     * @return JSON string
     * @throws IOException if serialization fails
     */
    public static String toJson(Object object) throws IOException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Error converting object to JSON", e);
            throw new IOException("JSON serialization failed", e);
        }
    }

    /**
     * Get ObjectMapper instance
     * @return ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // Private constructor to prevent instantiation
    private ResponseUtil() {}
}
