package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.Category;
import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.service.CategoryService;
import com.pahanaedu.bookshop.service.ServiceException;
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
 * Controller for category management operations
 */
@WebServlet(name = "CategoryController", urlPatterns = {"/api/categories/*"})
public class CategoryController extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetCategories(request, response);
            } else if (pathInfo.equals("/active")) {
                handleGetActiveCategories(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetCategory(request, response);
            } else {
                ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(response, e, "Failed to retrieve categories");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAnyRole(request, response, User.UserRole.ADMIN, User.UserRole.MANAGER)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                handleCreateCategory(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to create category");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAnyRole(request, response, User.UserRole.ADMIN, User.UserRole.MANAGER)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            try {
                handleUpdateCategory(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to update category");
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
                handleDeleteCategory(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to delete category");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    /**
     * Handle get categories with pagination
     */
    private void handleGetCategories(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Category> categories = categoryService.getCategories(page, pageSize);
        long totalCount = categoryService.getCategoryCount();

        ResponseUtil.sendPaginatedResponse(response, categories, page, pageSize, totalCount);
    }

    /**
     * Handle get active categories
     */
    private void handleGetActiveCategories(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        List<Category> categories = categoryService.getActiveCategories();
        ResponseUtil.sendSuccessResponse(response, categories);
    }

    /**
     * Handle get single category
     */
    private void handleGetCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer categoryId = Integer.parseInt(pathInfo.substring(1));

        Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
        if (categoryOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, categoryOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Category not found");
        }
    }

    /**
     * Handle create category
     */
    private void handleCreateCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String requestBody = readRequestBody(request);
        
        try {
            Category category = ResponseUtil.parseJsonRequest(requestBody, Category.class);
            Integer createdBy = getCurrentUserId(request);
            
            Category createdCategory = categoryService.createCategory(category, createdBy);
            ResponseUtil.sendSuccessResponse(response, createdCategory, "Category created successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("Validation failed") || e.getMessage().contains("already exists")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle update category
     */
    private void handleUpdateCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer categoryId = Integer.parseInt(pathInfo.substring(1));
        String requestBody = readRequestBody(request);
        
        try {
            Category category = ResponseUtil.parseJsonRequest(requestBody, Category.class);
            category.setCategoryId(categoryId);
            Integer updatedBy = getCurrentUserId(request);
            
            Category updatedCategory = categoryService.updateCategory(category, updatedBy);
            ResponseUtil.sendSuccessResponse(response, updatedCategory, "Category updated successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("Validation failed") || e.getMessage().contains("not found") || 
                e.getMessage().contains("already exists")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle delete category
     */
    private void handleDeleteCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer categoryId = Integer.parseInt(pathInfo.substring(1));
        Integer deletedBy = getCurrentUserId(request);

        boolean deleted = categoryService.deleteCategory(categoryId, deletedBy);
        if (deleted) {
            ResponseUtil.sendSuccessResponse(response, "Category deleted successfully");
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Category not found");
        }
    }
}
