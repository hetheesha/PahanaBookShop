package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.Customer;
import com.pahanaedu.bookshop.model.User;
import com.pahanaedu.bookshop.service.CustomerService;
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
 * Controller for customer management operations
 */
@WebServlet(name = "CustomerController", urlPatterns = {"/api/customers/*"})
public class CustomerController extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    
    private final CustomerService customerService;

    public CustomerController() {
        this.customerService = new CustomerService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetCustomers(request, response);
            } else if (pathInfo.equals("/search")) {
                handleSearchCustomers(request, response);
            } else if (pathInfo.equals("/generate-account-no")) {
                handleGenerateAccountNumber(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetCustomer(request, response);
            } else if (pathInfo.startsWith("/account/")) {
                handleGetCustomerByAccountNo(request, response);
            } else {
                ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(response, e, "Failed to retrieve customers");
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
                handleCreateCustomer(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to create customer");
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
                handleUpdateCustomer(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to update customer");
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
                handleDeleteCustomer(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to delete customer");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    /**
     * Handle get customers with pagination
     */
    private void handleGetCustomers(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Customer> customers = customerService.getCustomers(page, pageSize);
        long totalCount = customerService.getCustomerCount();

        ResponseUtil.sendPaginatedResponse(response, customers, page, pageSize, totalCount);
    }

    /**
     * Handle search customers
     */
    private void handleSearchCustomers(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String searchTerm = request.getParameter("q");
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            ResponseUtil.sendBadRequestResponse(response, "Search term is required");
            return;
        }

        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Customer> customers = customerService.searchCustomers(searchTerm, page, pageSize);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("customers", customers);
        responseData.put("searchTerm", searchTerm);
        
        ResponseUtil.sendSuccessResponse(response, responseData);
    }

    /**
     * Handle get single customer
     */
    private void handleGetCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer customerId = Integer.parseInt(pathInfo.substring(1));

        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (customerOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, customerOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Customer not found");
        }
    }

    /**
     * Handle get customer by account number
     */
    private void handleGetCustomerByAccountNo(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        String accountNo = pathInfo.substring("/account/".length());

        Optional<Customer> customerOpt = customerService.getCustomerByAccountNo(accountNo);
        if (customerOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, customerOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Customer not found");
        }
    }

    /**
     * Handle generate account number
     */
    private void handleGenerateAccountNumber(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String accountNo = customerService.generateAccountNumber();
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("accountNo", accountNo);
        
        ResponseUtil.sendSuccessResponse(response, responseData);
    }

    /**
     * Handle create customer
     */
    private void handleCreateCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String requestBody = readRequestBody(request);
        
        try {
            Customer customer = ResponseUtil.parseJsonRequest(requestBody, Customer.class);
            Integer createdBy = getCurrentUserId(request);
            
            Customer createdCustomer = customerService.createCustomer(customer, createdBy);
            ResponseUtil.sendSuccessResponse(response, createdCustomer, "Customer created successfully");
            
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
     * Handle update customer
     */
    private void handleUpdateCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer customerId = Integer.parseInt(pathInfo.substring(1));
        String requestBody = readRequestBody(request);
        
        try {
            Customer customer = ResponseUtil.parseJsonRequest(requestBody, Customer.class);
            customer.setCustomerId(customerId);
            Integer updatedBy = getCurrentUserId(request);
            
            Customer updatedCustomer = customerService.updateCustomer(customer, updatedBy);
            ResponseUtil.sendSuccessResponse(response, updatedCustomer, "Customer updated successfully");
            
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
     * Handle delete customer
     */
    private void handleDeleteCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer customerId = Integer.parseInt(pathInfo.substring(1));
        Integer deletedBy = getCurrentUserId(request);

        boolean deleted = customerService.deleteCustomer(customerId, deletedBy);
        if (deleted) {
            ResponseUtil.sendSuccessResponse(response, "Customer deleted successfully");
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Customer not found");
        }
    }
}
