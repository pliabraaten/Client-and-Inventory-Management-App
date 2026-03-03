package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.service.InvoiceService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceServiceImpl implements InvoiceService {

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
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 4f, 1f, 1.5f, 1.5f });
            table.setSpacingBefore(10f);

            // Headers
            addTableHeader(table, "Item Description", tableHeaderFont);
            addTableHeader(table, "Qty", tableHeaderFont);
            addTableHeader(table, "Unit Price", tableHeaderFont);
            addTableHeader(table, "Amount", tableHeaderFont);

            for (OrderItem item : order.getOrderItems()) {
                table.addCell(new Phrase(item.getItem() != null ? item.getItem().getName() : "Unknown", normalFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                table.addCell(new Phrase(String.format("$%.2f", item.getPriceAtTimeOfOrder()), normalFont));
                table.addCell(new Phrase(String.format("$%.2f", item.getQuantity() * item.getPriceAtTimeOfOrder()),
                        normalFont));
            }

            document.add(table);

            // Total
            Paragraph total = new Paragraph("\nTotal: " + String.format("$%.2f", order.getTotalPrice()), sectionFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Notes
            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                document.add(new Paragraph("\nNotes:", sectionFont));
                document.add(new Paragraph(order.getNotes(), normalFont));
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
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
            e.printStackTrace();
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
