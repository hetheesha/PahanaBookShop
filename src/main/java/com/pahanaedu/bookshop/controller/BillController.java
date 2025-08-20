package com.pahanaedu.bookshop.controller;

import com.pahanaedu.bookshop.model.Bill;
import com.pahanaedu.bookshop.model.BillItem;
import com.pahanaedu.bookshop.service.BillService;
import com.pahanaedu.bookshop.service.PdfBillService;
import com.pahanaedu.bookshop.service.ServiceException;
import com.pahanaedu.bookshop.util.ResponseUtil;
import com.pahanaedu.bookshop.util.ValidationUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for bill management operations
 */
@WebServlet(name = "BillController", urlPatterns = {"/api/bills/*"})
public class BillController extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    
    private final BillService billService;
    private final PdfBillService pdfBillService;

    public BillController() {
        this.billService = new BillService();
        this.pdfBillService = new PdfBillService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                handleCreateBill(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to create bill");
            }
        } else if (pathInfo.matches("/\\d+/cancel")) {
            try {
                handleCancelBill(request, response);
            } catch (Exception e) {
                handleException(response, e, "Failed to cancel bill");
            }
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAuthentication(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetBills(request, response);
            } else if (pathInfo.equals("/generate-number")) {
                handleGenerateBillNumber(request, response);
            } else if (pathInfo.startsWith("/customer/")) {
                handleGetBillsByCustomer(request, response);
            } else if (pathInfo.startsWith("/date-range")) {
                handleGetBillsByDateRange(request, response);
            } else if (pathInfo.startsWith("/number/") && pathInfo.endsWith("/pdf")) {
                handleGetBillPdfByNumber(request, response);
            } else if (pathInfo.startsWith("/number/")) {
                handleGetBillByNumber(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetBill(request, response);
            } else if (pathInfo.matches("/\\d+/pdf")) {
                handleGetBillPdf(request, response);
            } else {
                ResponseUtil.sendNotFoundResponse(response, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(response, e, "Failed to retrieve bills");
        }
    }



    /**
     * Handle get bills with pagination
     */
    private void handleGetBills(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Bill> bills = billService.getBills(page, pageSize);
        long totalCount = billService.getBillCount();

        ResponseUtil.sendPaginatedResponse(response, bills, page, pageSize, totalCount);
    }

    /**
     * Handle get single bill
     */
    private void handleGetBill(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer billId = Integer.parseInt(pathInfo.substring(1));

        Optional<Bill> billOpt = billService.getBillById(billId);
        if (billOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, billOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Bill not found");
        }
    }

    /**
     * Handle get bill by number
     */
    private void handleGetBillByNumber(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        String billNumber = pathInfo.substring("/number/".length());

        Optional<Bill> billOpt = billService.getBillByNumber(billNumber);
        if (billOpt.isPresent()) {
            ResponseUtil.sendSuccessResponse(response, billOpt.get());
        } else {
            ResponseUtil.sendNotFoundResponse(response, "Bill not found");
        }
    }

    /**
     * Handle get bills by customer
     */
    private void handleGetBillsByCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer customerId = Integer.parseInt(pathInfo.substring("/customer/".length()));

        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Bill> bills = billService.getBillsByCustomer(customerId, page, pageSize);
        ResponseUtil.sendSuccessResponse(response, bills);
    }

    /**
     * Handle get bills by date range
     */
    private void handleGetBillsByDateRange(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        if (startDateStr == null || endDateStr == null) {
            ResponseUtil.sendBadRequestResponse(response, "Start date and end date are required");
            return;
        }

        LocalDate startDate = ValidationUtil.parseDate(startDateStr);
        LocalDate endDate = ValidationUtil.parseDate(endDateStr);

        if (startDate == null || endDate == null) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid date format. Use yyyy-MM-dd");
            return;
        }

        if (startDate.isAfter(endDate)) {
            ResponseUtil.sendBadRequestResponse(response, "Start date cannot be after end date");
            return;
        }

        Map<String, Integer> paginationParams = getPaginationParams(request);
        int page = paginationParams.get("page");
        int pageSize = paginationParams.get("pageSize");

        List<Bill> bills = billService.getBillsByDateRange(startDate, endDate, page, pageSize);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("bills", bills);
        responseData.put("startDate", startDate);
        responseData.put("endDate", endDate);
        
        ResponseUtil.sendSuccessResponse(response, responseData);
    }

    /**
     * Handle generate bill number
     */
    private void handleGenerateBillNumber(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String billNumber = billService.generateBillNumber();
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("billNumber", billNumber);
        
        ResponseUtil.sendSuccessResponse(response, responseData);
    }

    /**
     * Handle create bill
     */
    private void handleCreateBill(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String requestBody = readRequestBody(request);
        
        try {
            Map<String, Object> billData = ResponseUtil.parseJsonRequest(requestBody, Map.class);
            
            // Parse bill
            Bill bill = ResponseUtil.getObjectMapper().convertValue(billData.get("bill"), Bill.class);
            
            // Parse bill items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) billData.get("billItems");
            List<BillItem> billItems = itemsData.stream()
                .map(itemData -> ResponseUtil.getObjectMapper().convertValue(itemData, BillItem.class))
                .toList();
            
            Integer createdBy = getCurrentUserId(request);
            
            Bill createdBill = billService.createBill(bill, billItems, createdBy);
            ResponseUtil.sendSuccessResponse(response, createdBill, "Bill created successfully");
            
        } catch (IOException e) {
            ResponseUtil.sendBadRequestResponse(response, "Invalid JSON format");
        } catch (ServiceException e) {
            if (e.getMessage().contains("Validation failed") || e.getMessage().contains("not found") ||
                e.getMessage().contains("Insufficient stock")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle cancel bill
     */
    private void handleCancelBill(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer billId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/cancel")));
        Integer cancelledBy = getCurrentUserId(request);

        try {
            Bill cancelledBill = billService.cancelBill(billId, cancelledBy);
            ResponseUtil.sendSuccessResponse(response, cancelledBill, "Bill cancelled successfully");
        } catch (ServiceException e) {
            if (e.getMessage().contains("not found")) {
                ResponseUtil.sendNotFoundResponse(response, e.getMessage());
            } else if (e.getMessage().contains("Only active bills")) {
                ResponseUtil.sendBadRequestResponse(response, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Handle get bill as PDF
     */
    private void handleGetBillPdf(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        Integer billId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/pdf")));

        Optional<Bill> billOpt = billService.getBillById(billId);
        if (billOpt.isEmpty()) {
            ResponseUtil.sendNotFoundResponse(response, "Bill not found");
            return;
        }

        try {
            Bill bill = billOpt.get();
            byte[] pdfBytes = pdfBillService.generateBillPdf(bill);

            // Set response headers for PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"bill-" + bill.getBillNumber() + ".pdf\"");
            response.setContentLength(pdfBytes.length);

            // Write PDF to response
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (ServiceException e) {
            logger.error("Error generating PDF for bill: {}", billId, e);
            ResponseUtil.sendInternalServerErrorResponse(response, "Failed to generate PDF: " + e.getMessage());
        }
    }

    /**
     * Handle get bill PDF by number
     */
    private void handleGetBillPdfByNumber(HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        String pathInfo = request.getPathInfo();
        String billNumber = pathInfo.substring("/number/".length(), pathInfo.indexOf("/pdf"));

        Optional<Bill> billOpt = billService.getBillByNumber(billNumber);
        if (billOpt.isEmpty()) {
            ResponseUtil.sendNotFoundResponse(response, "Bill not found");
            return;
        }

        try {
            Bill bill = billOpt.get();
            byte[] pdfBytes = pdfBillService.generateBillPdf(bill);

            // Set response headers for PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"bill-" + bill.getBillNumber() + ".pdf\"");
            response.setContentLength(pdfBytes.length);

            // Write PDF to response
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (ServiceException e) {
            logger.error("Error generating PDF for bill number: {}", billNumber, e);
            ResponseUtil.sendInternalServerErrorResponse(response, "Failed to generate PDF: " + e.getMessage());
        }
    }
}
