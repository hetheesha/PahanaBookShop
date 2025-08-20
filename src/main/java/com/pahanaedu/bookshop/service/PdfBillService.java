package com.pahanaedu.bookshop.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.pahanaedu.bookshop.model.Bill;
import com.pahanaedu.bookshop.model.BillItem;
import com.pahanaedu.bookshop.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating PDF bills with professional design
 */
public class PdfBillService {
    private static final Logger logger = LoggerFactory.getLogger(PdfBillService.class);
    
    // Colors for the design
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185); // Blue
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(52, 152, 219); // Light Blue
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(44, 62, 80);     // Dark Gray
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(236, 240, 241);  // Light Gray
    private static final DeviceRgb WHITE_COLOR = new DeviceRgb(255, 255, 255); // White
    
    private final CustomerService customerService;
    private final ItemService itemService;

    public PdfBillService() {
        this.customerService = new CustomerService();
        this.itemService = new ItemService();
    }

    /**
     * Generate PDF bill as byte array
     * @param bill Bill to generate PDF for
     * @return PDF as byte array
     * @throws ServiceException if generation fails
     */
    public byte[] generateBillPdf(Bill bill) throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            
            // Set margins
            document.setMargins(40, 40, 40, 40);
            
            // Load fonts
            PdfFont boldFont = PdfFontFactory.createFont();
            PdfFont regularFont = PdfFontFactory.createFont();
            
            // Add content
            addHeader(document, boldFont);
            addBillInfo(document, bill, boldFont, regularFont);
            addCustomerInfo(document, bill, boldFont, regularFont);
            addItemsTable(document, bill, boldFont, regularFont);
            addTotals(document, bill, boldFont, regularFont);
            addFooter(document, regularFont);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Error generating PDF bill", e);
            throw new ServiceException("Failed to generate PDF bill: " + e.getMessage(), e);
        }
    }

    /**
     * Add header with company info
     */
    private void addHeader(Document document, PdfFont boldFont) {
        // Company header with background
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth()
                .setBackgroundColor(HEADER_COLOR)
                .setBorder(Border.NO_BORDER);

        // Company info
        Paragraph companyName = new Paragraph("PAHANA EDU BOOKSHOP")
                .setFont(boldFont)
                .setFontSize(24)
                .setFontColor(ColorConstants.WHITE)
                .setMargin(0);
        
        Paragraph companyDetails = new Paragraph("123 Bookshop Lane, Reading City\nPhone: +1-555-BOOKS-1\nEmail: info@pahanaedu-bookshop.com")
                .setFontSize(10)
                .setFontColor(ColorConstants.WHITE)
                .setMargin(0);

        Cell companyCell = new Cell()
                .add(companyName)
                .add(companyDetails)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Invoice label
        Paragraph invoiceLabel = new Paragraph("INVOICE")
                .setFont(boldFont)
                .setFontSize(20)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0);

        Cell invoiceCell = new Cell()
                .add(invoiceLabel)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        headerTable.addCell(companyCell);
        headerTable.addCell(invoiceCell);
        
        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add bill information
     */
    private void addBillInfo(Document document, Bill bill, PdfFont boldFont, PdfFont regularFont) {
        Table billInfoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        // Left side - Bill details
        Paragraph billDetails = new Paragraph()
                .add(new Text("Bill Number: ").setFont(boldFont).setFontColor(TEXT_COLOR))
                .add(new Text(bill.getBillNumber()).setFont(regularFont))
                .add("\n")
                .add(new Text("Date: ").setFont(boldFont).setFontColor(TEXT_COLOR))
                .add(new Text(bill.getBillDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFont(regularFont))
                .add("\n")
                .add(new Text("Time: ").setFont(boldFont).setFontColor(TEXT_COLOR))
                .add(new Text(bill.getBillTime().toString()).setFont(regularFont));

        Cell billDetailsCell = new Cell()
                .add(billDetails)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);

        // Right side - Payment info
        Paragraph paymentDetails = new Paragraph()
                .add(new Text("Payment Method: ").setFont(boldFont).setFontColor(TEXT_COLOR))
                .add(new Text(bill.getPaymentMethod().toString().toUpperCase()).setFont(regularFont))
                .add("\n")
                .add(new Text("Status: ").setFont(boldFont).setFontColor(TEXT_COLOR))
                .add(new Text(bill.getPaymentStatus().toString().toUpperCase()).setFont(regularFont))
                .setTextAlignment(TextAlignment.RIGHT);

        Cell paymentDetailsCell = new Cell()
                .add(paymentDetails)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);

        billInfoTable.addCell(billDetailsCell);
        billInfoTable.addCell(paymentDetailsCell);
        
        document.add(billInfoTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add customer information
     */
    private void addCustomerInfo(Document document, Bill bill, PdfFont boldFont, PdfFont regularFont) throws ServiceException {
        try {
            Customer customer = customerService.getCustomerById(bill.getCustomerId()).orElse(null);
            if (customer == null) {
                return;
            }

            // Customer info section with background
            Paragraph customerTitle = new Paragraph("BILL TO:")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(HEADER_COLOR)
                    .setMarginBottom(5);

            Paragraph customerDetails = new Paragraph()
                    .add(new Text(customer.getFullName()).setFont(boldFont).setFontSize(14))
                    .add("\n")
                    .add(new Text("Account: " + customer.getAccountNo()).setFont(regularFont))
                    .add("\n")
                    .add(new Text(customer.getAddress()).setFont(regularFont))
                    .add("\n")
                    .add(new Text("Phone: " + customer.getPhone()).setFont(regularFont));

            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                customerDetails.add("\n").add(new Text("Email: " + customer.getEmail()).setFont(regularFont));
            }

            Table customerTable = new Table(1)
                    .useAllAvailableWidth()
                    .setBackgroundColor(LIGHT_GRAY);

            Cell customerCell = new Cell()
                    .add(customerTitle)
                    .add(customerDetails)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(15);

            customerTable.addCell(customerCell);
            document.add(customerTable);
            document.add(new Paragraph("\n"));

        } catch (Exception e) {
            logger.warn("Could not load customer details for bill", e);
        }
    }

    /**
     * Add items table
     */
    private void addItemsTable(Document document, Bill bill, PdfFont boldFont, PdfFont regularFont) {
        // Table headers
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{10, 40, 15, 15, 20}))
                .useAllAvailableWidth();

        // Header row
        String[] headers = {"#", "Item Description", "Qty", "Unit Price", "Total"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setFont(boldFont).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(10);
            itemsTable.addHeaderCell(headerCell);
        }

        // Item rows
        List<BillItem> billItems = bill.getBillItems();
        for (int i = 0; i < billItems.size(); i++) {
            BillItem item = billItems.get(i);
            
            // Get item details
            String itemName = "Item #" + item.getItemId();
            try {
                itemName = itemService.getItemById(item.getItemId())
                        .map(it -> it.getItemName())
                        .orElse("Item #" + item.getItemId());
            } catch (Exception e) {
                logger.warn("Could not load item details", e);
            }

            DeviceRgb rowColor = (i % 2 == 0) ? WHITE_COLOR : LIGHT_GRAY;

            // Row cells
            itemsTable.addCell(createItemCell(String.valueOf(i + 1), regularFont, rowColor, TextAlignment.CENTER));
            itemsTable.addCell(createItemCell(itemName, regularFont, rowColor, TextAlignment.LEFT));
            itemsTable.addCell(createItemCell(String.valueOf(item.getQuantity()), regularFont, rowColor, TextAlignment.CENTER));
            itemsTable.addCell(createItemCell("$" + item.getUnitPrice().toString(), regularFont, rowColor, TextAlignment.RIGHT));
            itemsTable.addCell(createItemCell("$" + item.getLineTotal().toString(), regularFont, rowColor, TextAlignment.RIGHT));
        }

        document.add(itemsTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Create item cell with consistent styling
     */
    private Cell createItemCell(String content, PdfFont font, DeviceRgb backgroundColor, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(content).setFont(font).setFontColor(TEXT_COLOR))
                .setBackgroundColor(backgroundColor)
                .setTextAlignment(alignment)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);
    }

    /**
     * Add totals section
     */
    private void addTotals(Document document, Bill bill, PdfFont boldFont, PdfFont regularFont) {
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth();

        // Empty cell for spacing
        totalsTable.addCell(new Cell().setBorder(Border.NO_BORDER));

        // Totals section
        Table totalsDetailTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        // Subtotal
        addTotalRow(totalsDetailTable, "Subtotal:", "$" + bill.getSubtotal().toString(), regularFont, false);

        // Discount
        if (bill.getDiscountAmount() != null && bill.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            String discountText = "$" + bill.getDiscountAmount().toString();
            if (bill.getDiscountPercentage() != null && bill.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                discountText += " (" + bill.getDiscountPercentage() + "%)";
            }
            addTotalRow(totalsDetailTable, "Discount:", "-" + discountText, regularFont, false);
        }

        // Tax
        if (bill.getTaxAmount() != null && bill.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            String taxText = "$" + bill.getTaxAmount().toString();
            if (bill.getTaxPercentage() != null && bill.getTaxPercentage().compareTo(BigDecimal.ZERO) > 0) {
                taxText += " (" + bill.getTaxPercentage() + "%)";
            }
            addTotalRow(totalsDetailTable, "Tax:", taxText, regularFont, false);
        }

        // Total
        addTotalRow(totalsDetailTable, "TOTAL:", "$" + bill.getTotalAmount().toString(), boldFont, true);

        Cell totalsCell = new Cell()
                .add(totalsDetailTable)
                .setBorder(Border.NO_BORDER);

        totalsTable.addCell(totalsCell);
        document.add(totalsTable);
    }

    /**
     * Add a total row
     */
    private void addTotalRow(Table table, String label, String value, PdfFont font, boolean isTotal) {
        DeviceRgb backgroundColor = isTotal ? ACCENT_COLOR : WHITE_COLOR;
        DeviceRgb textColor = isTotal ? WHITE_COLOR : TEXT_COLOR;

        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(font).setFontColor(textColor))
                .setBackgroundColor(backgroundColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(font).setFontColor(textColor))
                .setBackgroundColor(backgroundColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Add footer
     */
    private void addFooter(Document document, PdfFont regularFont) {
        document.add(new Paragraph("\n\n"));
        
        Paragraph footer = new Paragraph("Thank you for your business!")
                .setFont(regularFont)
                .setFontSize(12)
                .setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();

        Paragraph terms = new Paragraph("Terms & Conditions: All sales are final. Please retain this invoice for your records.")
                .setFont(regularFont)
                .setFontSize(8)
                .setFontColor(TEXT_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);

        document.add(footer);
        document.add(terms);
    }
}
