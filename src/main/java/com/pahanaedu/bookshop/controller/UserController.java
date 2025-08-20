package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.service.ServiceException;
import com.pahanaedu.bookshop.service.UserService;
import com.pahanaedu.bookshop.util.ResponseUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for user management operations
 */
@WebServlet(name = "UserController", urlPatterns = {"/api/users/*"})
public class UserController extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAnyRole(request, response, User.UserRole.ADMIN, User.UserRole.MANAGER)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetUsers(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetUser(request, response);
            } else {
                ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(response, e, "Failed to retrieve users");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireRole(request, response, User.UserRole.ADMIN)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                handleCreateUser(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to create user");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireRole(request, response, User.UserRole.ADMIN)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            try {
                handleUpdateUser(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to update user");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireRole(request, response, User.UserRole.ADMIN)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            try {
                handleDeleteUser(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to delete user");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    /**
     * Handle get users with pagination
     */
    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<User> users = userService.getUsers(page, pageSize);
        long totalCount = userService.getUserCount();

        ResponseUtil.sendPaginatedResponse(response, users, page, pageSize, totalCount);
    }

    /**
     * Handle get single user
     */
    private void handleGetUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer userId = Integer.parseInt(pathInfo.substring(1));

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, userOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "User not found");
        }
    }

    /**
     * Handle create user
     */
    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String requestBody = readRequestBody(request);
        
        try {
            User user = ResponseUtil.parseJsonRequest(requestBody, User.class);
            Integer createdBy = getCurrentUserId(request);
            
            User createdUser = userService.createUser(user, createdBy);
            ResponseUtil.sendSuccessResponse(response, createdUser, "User created successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("Validation failed")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle update user
     */
    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer userId = Integer.parseInt(pathInfo.substring(1));
        String requestBody = readRequestBody(request);
        
        try {
            User user = ResponseUtil.parseJsonRequest(requestBody, User.class);
            user.setUserId(userId);
            Integer updatedBy = getCurrentUserId(request);
            
            User updatedUser = userService.updateUser(user, updatedBy);
            ResponseUtil.sendSuccessResponse(response, updatedUser, "User updated successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("Validation failed") || e.getMessage().contains("not found")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle delete user
     */
    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer userId = Integer.parseInt(pathInfo.substring(1));
        Integer deletedBy = getCurrentUserId(request);

        // Prevent self-deletion
        if (userId.equals(deletedBy)) {
            ResponseUtil.sendBadRequestResponse(response, "Cannot delete your own account");
            return;
        }

        boolean deleted = userService.deleteUser(userId, deletedBy);
        if (deleted) {
            ResponseUtil.sendSuccessResponse(response, "User deleted successfully");
        } else {
            ResponseUtil.sendNotFoundResponse(response, "User not found");
        }
    }
}
