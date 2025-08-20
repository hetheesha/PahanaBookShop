package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.service.ServiceException;
import com.pahanaedu.bookshop.service.UserService;
import com.pahanaedu.bookshop.util.ResponseUtil;
import com.pahanaedu.bookshop.util.ValidationUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for authentication operations
 */
@WebServlet(name = "AuthController", urlPatterns = {"/api/auth/*"})
public class AuthController extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;

    public AuthController() {
        this.userService = new UserService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid endpoint");
            return;
        }

        try {
            switch (pathInfo) {
                case "/login":
                    handleLogin(request, response);
                    break;
                case "/logout":
                    handleLogout(request, response);
                    break;
                case "/change-password":
                    handleChangePassword(request, response);
                    break;
                default:
                    ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            handleException(response, e, "Authentication operation failed");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid endpoint");
            return;
        }

        try {
            switch (pathInfo) {
                case "/profile":
                    handleGetProfile(request, response);
                    break;
                case "/check":
                    handleCheckAuth(request, response);
                    break;
                default:
                    ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            handleException(response, e, "Authentication check failed");
        }
    }

    /**
     * Handle user login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String requestBody = readRequestBody(request);
        
        try {
            Map<String, Object> loginData = ResponseUtil.parseJsonRequest(requestBody, Map.class);
            
            String username = (String) loginData.get("username");
            String password = (String) loginData.get("password");
            
            // Validate input
            Map<String, String> errors = new HashMap<>();
            if (!ValidationUtil.isNotEmpty(username)) {
                errors.put("username", "Username is required");
            }
            if (!ValidationUtil.isNotEmpty(password)) {
                errors.put("password", "Password is required");
            }
            
            if (!errors.isEmpty()) {
                ResponseUtil.sendValidationErrorResponse(response, errors);
                return;
            }

            // Authenticate user
            String ipAddress = getClientIpAddress(request);
            String userAgent = getUserAgent(request);
            
            Optional<User> userOpt = userService.authenticate(username, password, ipAddress, userAgent);
            
            if (userOpt.isEmpty()) {
                ResponseUtil.sendUnauthorizedResponse(response, "Invalid username or password");
                return;
            }

            User user = userOpt.get();
            
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_USER_KEY, user);
            session.setAttribute(SESSION_USER_ID_KEY, user.getUserId());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", user);
            responseData.put("sessionId", session.getId());
            
            ResponseUtil.sendSuccessResponse(response, responseData, "Login successful");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        }
    }

    /**
     * Handle user logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        ResponseUtil.sendSuccessResponse(response, "Logout successful");
    }

    /**
     * Handle password change
     */
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String requestBody = readRequestBody(request);
        
        try {
            Map<String, Object> passwordData = ResponseUtil.parseJsonRequest(requestBody, Map.class);
            
            String currentPassword = (String) passwordData.get("currentPassword");
            String newPassword = (String) passwordData.get("newPassword");
            String confirmPassword = (String) passwordData.get("confirmPassword");
            
            // Validate input
            Map<String, String> errors = new HashMap<>();
            if (!ValidationUtil.isNotEmpty(currentPassword)) {
                errors.put("currentPassword", "Current password is required");
            }
            if (!ValidationUtil.isNotEmpty(newPassword)) {
                errors.put("newPassword", "New password is required");
            }
            if (!ValidationUtil.isNotEmpty(confirmPassword)) {
                errors.put("confirmPassword", "Confirm password is required");
            }
            if (ValidationUtil.isNotEmpty(newPassword) && ValidationUtil.isNotEmpty(confirmPassword) && 
                !newPassword.equals(confirmPassword)) {
                errors.put("confirmPassword", "Passwords do not match");
            }
            
            if (!errors.isEmpty()) {
                ResponseUtil.sendValidationErrorResponse(response, errors);
                return;
            }

            Integer userId = getCurrentUserId(request);
            userService.changePassword(userId, currentPassword, newPassword, userId);
            
            ResponseUtil.sendSuccessResponse(response, "Password changed successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        }
    }

    /**
     * Handle get user profile
     */
    private void handleGetProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        User user = getCurrentUser(request);
        ResponseUtil.sendSuccessResponse(response, user);
    }

    /**
     * Handle authentication check
     */
    private void handleCheckAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> authStatus = new HashMap<>();
        authStatus.put("authenticated", isAuthenticated(request));
        
        User user = getCurrentUser(request);
        if (user != null) {
            authStatus.put("user", user);
        }
        
        ResponseUtil.sendSuccessResponse(response, authStatus);
    }
}
