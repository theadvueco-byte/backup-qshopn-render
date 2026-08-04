package com.synix.uwait_backend.service;

import com.lowagie.text.Font;
import java.awt.Color;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.synix.uwait_backend.dto.monthlyreport.MonthlyReportDTO;
import com.synix.uwait_backend.dto.monthlyreport.OrderReportRowDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Service
public class PdfReportGenerator {

    public byte[] generate(MonthlyReportDTO report) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PdfFooter());
            doc.open();

            // ====== FONTS ======
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.DARK_GRAY);
            Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY);
            Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.DARK_GRAY);
            Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
            Font tableHeaderFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font tableCellFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);


            // ====== HEADER ======
            Paragraph title = new Paragraph("Monthly Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            doc.add(title);

            Paragraph subtitle = new Paragraph(
                    "Shop: " + report.getShopName() +
                            "  |  Owner: " + report.getOwnerName() +
                            "  |  Month: " + report.getMonth(),
                    subtitleFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            doc.add(subtitle);

            // ====== TABLE ======
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(15);
            table.setWidths(new float[]{2.2f, 2.5f, 3f, 2.5f, 2.2f, 2.2f});

            Color headerBg = new Color(33, 150, 243); // Premium blue

            Stream.of("Order ID", "Date", "Customer", "Payment", "Status", "Amount")
                    .forEach(h -> {
                        PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                        cell.setBackgroundColor(headerBg);
                        cell.setPadding(8);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorderColor(Color.LIGHT_GRAY);
                        table.addCell(cell);
                    });

            for (OrderReportRowDTO o : report.getOrders()) {

                table.addCell(createCell(o.getOrderId().toString(), tableCellFont));
                table.addCell(createCell(o.getDate().toString(), tableCellFont));
                table.addCell(createCell(o.getCustomer(), tableCellFont));
                table.addCell(createCell(o.getPayment(), tableCellFont));
                table.addCell(createCell(o.getStatus(), tableCellFont));

                PdfPCell amountCell = createCell("₹ " + o.getAmount(), tableCellFont);
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(amountCell);
            }

            doc.add(table);

            // ====== SUMMARY ======
            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(50);
            summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summary.setSpacingBefore(10);
            summary.setWidths(new float[]{3, 2});

            summary.addCell(summaryCell("Total Orders", labelFont));
            summary.addCell(summaryCell(String.valueOf(report.getTotalOrders()), valueFont));

            summary.addCell(summaryCell("Total Revenue", labelFont));
            summary.addCell(summaryCell("₹ " + report.getTotalRevenue(), summaryFont));

            doc.add(summary);



            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ====== HELPER METHODS ======

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(7);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell summaryCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}
