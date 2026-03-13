package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.service.InvoiceService;
import com.PL.pig_ranch.exception.InvoiceGenerationException;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void generateInvoice(Order order, String filePath) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Header
            Paragraph header = new Paragraph("INVOICE", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            document.add(new Paragraph("\n"));

            // Order Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(10f);

            // Client Info
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.NO_BORDER);
            clientCell.addElement(new Paragraph("Bill To:", sectionFont));
            if (order.getClient() != null) {
                clientCell.addElement(new Paragraph(order.getClient().getName(), normalFont));
                clientCell.addElement(new Paragraph(order.getClient().getEmail(), normalFont));
                clientCell.addElement(new Paragraph(order.getClient().getPhoneNumber(), normalFont));
            } else {
                clientCell.addElement(new Paragraph("N/A", normalFont));
            }
            infoTable.addCell(clientCell);

            // Order Metadata
            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(new Paragraph("Order #: " + order.getId(), normalFont));
            metaCell.addElement(new Paragraph("Date: " +
                    (order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "N/A"), normalFont));
            metaCell.addElement(new Paragraph("Status: " + order.getStatus(), normalFont));
            infoTable.addCell(metaCell);

            document.add(infoTable);

            // Items Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3.5f, 1f, 1.5f, 1.25f, 1.25f });
            table.setSpacingBefore(10f);

            // Headers
            addTableHeader(table, "Item Description", tableHeaderFont);
            addTableHeader(table, "Qty", tableHeaderFont);
            addTableHeader(table, "Unit Price", tableHeaderFont);
            addTableHeader(table, "Discount", tableHeaderFont);
            addTableHeader(table, "Amount", tableHeaderFont);

            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal totalItemDiscounts = BigDecimal.ZERO;

            for (OrderItem item : order.getOrderItems()) {
                BigDecimal lineTotal = item.getPriceAtTimeOfOrder()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal itemDisc = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;

                subtotal = subtotal.add(lineTotal);
                totalItemDiscounts = totalItemDiscounts.add(itemDisc);

                table.addCell(new Phrase(item.getItem() != null ? item.getItem().getName() : "Unknown", normalFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                table.addCell(new Phrase(String.format("$%,.2f", item.getPriceAtTimeOfOrder()), normalFont));
                table.addCell(new Phrase(itemDisc.compareTo(BigDecimal.ZERO) > 0
                        ? String.format("-$%,.2f", itemDisc)
                        : "-", normalFont));
                table.addCell(new Phrase(String.format("$%,.2f", lineTotal.subtract(itemDisc)), normalFont));
            }

            document.add(table);

            // Totals
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setSpacingBefore(10f);

            PdfPCell emptyTotCell = new PdfPCell();
            emptyTotCell.setBorder(Rectangle.NO_BORDER);

            totalsTable.addCell(new Phrase("Subtotal:", normalFont));
            PdfPCell subtotalCell = new PdfPCell(new Phrase(String.format("$%,.2f", subtotal), normalFont));
            subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(subtotalCell);

            // Add hogs to subtotal if any
            if (order.getHogs() != null) {
                for (Hog hog : order.getHogs()) {
                    if (hog.getProcessingCost() != null) {
                        totalsTable.addCell(new Phrase("Processing (" + hog.getHogNumber() + "):", normalFont));
                        PdfPCell hogCell = new PdfPCell(
                                new Phrase(String.format("$%,.2f", hog.getProcessingCost()), normalFont));
                        hogCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        hogCell.setBorder(Rectangle.NO_BORDER);
                        totalsTable.addCell(hogCell);
                    }
                }
            }

            if (totalItemDiscounts.compareTo(BigDecimal.ZERO) > 0) {
                totalsTable.addCell(new Phrase("Item Discounts:", normalFont));
                PdfPCell discCell = new PdfPCell(
                        new Phrase(String.format("-$%,.2f", totalItemDiscounts), normalFont));
                discCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                discCell.setBorder(Rectangle.NO_BORDER);
                totalsTable.addCell(discCell);
            }

            BigDecimal globalDiscount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
            if (globalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                totalsTable.addCell(new Phrase("Order Discount:", normalFont));
                PdfPCell orderDiscCell = new PdfPCell(
                        new Phrase(String.format("-$%,.2f", globalDiscount), normalFont));
                orderDiscCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                orderDiscCell.setBorder(Rectangle.NO_BORDER);
                totalsTable.addCell(orderDiscCell);
            }

            totalsTable.addCell(new Phrase("Grand Total:", sectionFont));
            PdfPCell grandTotalCell = new PdfPCell(
                    new Phrase(String.format("$%,.2f", order.getTotalPrice()), sectionFont));
            grandTotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            grandTotalCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(grandTotalCell);

            document.add(totalsTable);

            // Notes
            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                document.add(new Paragraph("\nNotes:", sectionFont));
                document.add(new Paragraph(order.getNotes(), normalFont));
            }

            document.close();
        } catch (Exception e) {
            log.error("Error generating PDF invoice for order #{}", order.getId(), e);
            throw new InvoiceGenerationException("Failed to generate invoice for order #" + order.getId(), e);
        }
    }

    @Override
    public void generateAndOpenInvoice(Order order, String filePath) {
        generateInvoice(order, filePath);
        try {
            File file = new File(filePath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            log.error("Failed to open invoice file: {}", filePath, e);
            throw new InvoiceGenerationException("Failed to open generated invoice PDF", e);
        }
    }

    private void addTableHeader(PdfPTable table, String headerTitle, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle, font));
        table.addCell(header);
    }
}
