package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.Item;
import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.service.ItemService;
import com.pahanaedu.bookshop.service.ServiceException;
import com.pahanaedu.bookshop.util.ResponseUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for item management operations
 */
@WebServlet(name = "ItemController", urlPatterns = {"/api/items/*"})
public class ItemController extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    
    private final ItemService itemService;

    public ItemController() {
        this.itemService = new ItemService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetItems(request, response);
            } else if (pathInfo.equals("/search")) {
                handleSearchItems(request, response);
            } else if (pathInfo.equals("/low-stock")) {
                handleGetLowStockItems(request, response);
            } else if (pathInfo.startsWith("/generate-code/")) {
                handleGenerateItemCode(request, response);
            } else if (pathInfo.startsWith("/category/")) {
                handleGetItemsByCategory(request, response);
            } else if (pathInfo.startsWith("/code/")) {
                handleGetItemByCode(request, response);
            } else if (pathInfo.startsWith("/barcode/")) {
                handleGetItemByBarcode(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetItem(request, response);
            } else {
                ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(response, e, "Failed to retrieve items");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                handleCreateItem(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to create item");
            }
        } else if (pathInfo.matches("/\\d+/adjust-stock")) {
            try {
                handleAdjustStock(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to adjust stock");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            try {
                handleUpdateItem(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to update item");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAnyRole(request, response, User.UserRole.ADMIN, User.UserRole.MANAGER)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            try {
                handleDeleteItem(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to delete item");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    /**
     * Handle get items with pagination
     */
    private void handleGetItems(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Item> items = itemService.getItems(page, pageSize);
        long totalCount = itemService.getItemCount();

        ResponseUtil.sendPaginatedResponse(response, items, page, pageSize, totalCount);
    }

    /**
     * Handle search items
     */
    private void handleSearchItems(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String searchTerm = request.getParameter("q");
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            ResponseUtil.sendBadRequestResponse(response, "Search term is required");
            return;
        }

        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Item> items = itemService.searchItems(searchTerm, page, pageSize);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("items", items);
        responseData.put("searchTerm", searchTerm);
        
        ResponseUtil.sendSuccessResponse(response, responseData);
    }

    /**
     * Handle get low stock items
     */
    private void handleGetLowStockItems(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        List<Item> items = itemService.getLowStockItems();
        ResponseUtil.sendSuccessResponse(response, items);
    }

    /**
     * Handle get single item
     */
    private void handleGetItem(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer itemId = Integer.parseInt(pathInfo.substring(1));

        Optional<Item> itemOpt = itemService.getItemById(itemId);
        if (itemOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, itemOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Item not found");
        }
    }

    /**
     * Handle get item by code
     */
    private void handleGetItemByCode(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        String itemCode = pathInfo.substring("/code/".length());

        Optional<Item> itemOpt = itemService.getItemByCode(itemCode);
        if (itemOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, itemOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Item not found");
        }
    }

    /**
     * Handle get item by barcode
     */
    private void handleGetItemByBarcode(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        String barcode = pathInfo.substring("/barcode/".length());

        Optional<Item> itemOpt = itemService.getItemByBarcode(barcode);
        if (itemOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, itemOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Item not found");
        }
    }

    /**
     * Handle get items by category
     */
    private void handleGetItemsByCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer categoryId = Integer.parseInt(pathInfo.substring("/category/".length()));

        List<Item> items = itemService.getItemsByCategory(categoryId);
        ResponseUtil.sendSuccessResponse(response, items);
    }

    /**
     * Handle generate item code
     */
    private void handleGenerateItemCode(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer categoryId = Integer.parseInt(pathInfo.substring("/generate-code/".length()));

        String itemCode = itemService.generateItemCode(categoryId);
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("itemCode", itemCode);
        
        ResponseUtil.sendSuccessResponse(response, responseData);
    }

    /**
     * Handle create item
     */
    private void handleCreateItem(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String requestBody = readRequestBody(request);
        
        try {
            Item item = ResponseUtil.parseJsonRequest(requestBody, Item.class);
            Integer createdBy = getCurrentUserId(request);
            
            Item createdItem = itemService.createItem(item, createdBy);
            ResponseUtil.sendSuccessResponse(response, createdItem, "Item created successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("Validation failed") || e.getMessage().contains("already exists") ||
                e.getMessage().contains("not found")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle update item
     */
    private void handleUpdateItem(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer itemId = Integer.parseInt(pathInfo.substring(1));
        String requestBody = readRequestBody(request);
        
        try {
            Item item = ResponseUtil.parseJsonRequest(requestBody, Item.class);
            item.setItemId(itemId);
            Integer updatedBy = getCurrentUserId(request);
            
            Item updatedItem = itemService.updateItem(item, updatedBy);
            ResponseUtil.sendSuccessResponse(response, updatedItem, "Item updated successfully");
            
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
     * Handle adjust stock
     */
    private void handleAdjustStock(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer itemId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/adjust-stock")));
        String requestBody = readRequestBody(request);
        
        try {
            Map<String, Object> adjustmentData = ResponseUtil.parseJsonRequest(requestBody, Map.class);
            
            Integer newQuantity = ((Number) adjustmentData.get("newQuantity")).intValue();
            String reason = (String) adjustmentData.get("reason");
            
            if (newQuantity == null || newQuantity < 0) {
                ResponseUtil.sendBadRequestResponse(response, "Valid new quantity is required");
                return;
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Stock adjustment";
            }
            
            Integer adjustedBy = getCurrentUserId(request);
            itemService.adjustStock(itemId, newQuantity, reason, adjustedBy);
            
            ResponseUtil.sendSuccessResponse(response, "Stock adjusted successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("not found")) {
                ResponseUtil.sendNotFoundResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle delete item
     */
    private void handleDeleteItem(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer itemId = Integer.parseInt(pathInfo.substring(1));
        Integer deletedBy = getCurrentUserId(request);

        boolean deleted = itemService.deleteItem(itemId, deletedBy);
        if (deleted) {
            ResponseUtil.sendSuccessResponse(response, "Item deleted successfully");
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Item not found");
        }
    }
}
