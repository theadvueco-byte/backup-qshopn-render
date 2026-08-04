package com.synix.uwait_backend.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.model.OrderItem;
import com.synix.uwait_backend.model.Shop;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PdfInvoiceService {

    public byte[] generateInvoice(Order order) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 72, 72); // margins
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Attach footer event
            writer.setPageEvent(new PdfBillingInvoiceFooter());

            document.open();

            // ===== FONTS =====
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font smallGray = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            // ===== TITLE =====
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // ===== SHOP DETAILS =====
            Shop shop = order.getShop();
            PdfPTable shopTables = new PdfPTable(2);
            shopTables.setWidthPercentage(100);
            shopTables.setSpacingAfter(10);
            shopTables.setWidths(new float[]{3, 2});

            shopTables.addCell(noBorderCell("Shop Name: " + shop.getShopName(), normalFont));
            shopTables.addCell(noBorderCell("Verified Shop ✅", normalFont, Element.ALIGN_RIGHT));
            shopTables.addCell(noBorderCell("Owner Email: " + shop.getOwnerEmail(), normalFont));
            shopTables.addCell(noBorderCell("Shop Code: " + shop.getPublicCode(), normalFont,Element.ALIGN_RIGHT));

            document.add(shopTables);

            // ===== ORDER & SHOP DETAILS =====
            PdfPTable shopTable = new PdfPTable(2);
            shopTable.setWidthPercentage(100);
            shopTable.setWidths(new float[]{3, 2});
            shopTable.setSpacingAfter(15);

// Left: Shop Info
            PdfPCell soldByCell = new PdfPCell();
            soldByCell.setBorder(Rectangle.NO_BORDER);
            soldByCell.setPadding(8);

            Paragraph soldBy = new Paragraph();
            soldBy.add(new Phrase("SOLD BY:\n", headerFont));
            soldBy.add(new Phrase(order.getShop().getShopName() + "\n", normalFont));
            soldBy.add(new Phrase(order.getShop().getOwnerEmail() + "\n", normalFont));
            soldBy.add(new Phrase(order.getShop().getFullAddress(), normalFont)); // add shop address here

            soldByCell.addElement(soldBy);
            shopTable.addCell(soldByCell);

// Right: Order Info
            PdfPCell orderInfoCell = new PdfPCell();
            orderInfoCell.setBorder(Rectangle.NO_BORDER);
            orderInfoCell.setPadding(8);

            Paragraph orderInfo = new Paragraph();
            orderInfo.add(new Phrase("ORDER DETAILS:\n", headerFont));
            orderInfo.add(new Phrase("Order ID: " + order.getId() + "\n", normalFont));
            orderInfo.add(new Phrase("Order Date: " + order.getCreatedAt() + "\n", normalFont));
            orderInfo.add(new Phrase("Payment Method: " + order.getPaymentMode(), normalFont));

            orderInfoCell.addElement(orderInfo);
            shopTable.addCell(orderInfoCell);

            document.add(shopTable);

// ===== CUSTOMER & SHIPPING DETAILS =====
            PdfPTable custShipTable = new PdfPTable(2);
            custShipTable.setWidthPercentage(100);
            custShipTable.setWidths(new float[]{1, 1});
            custShipTable.setSpacingAfter(15);

// Left: Customer Info
            PdfPCell custCell = new PdfPCell();
            custCell.setBorder(Rectangle.NO_BORDER);
            custCell.setPadding(8);

            Paragraph customer = new Paragraph();
            customer.add(new Phrase("BILL TO:\n", headerFont));
            customer.add(new Phrase(order.getCustomerName() + "\n", normalFont));
            customer.add(new Phrase("Phone: " + order.getCustomerPhone() + "\n", normalFont));
            customer.add(new Phrase(order.getCustomerEmail() + "\n", normalFont)); // if you have email

            custCell.addElement(customer);
            custShipTable.addCell(custCell);

// Right: Shipping Address
            PdfPCell shipCell = new PdfPCell();
            shipCell.setBorder(Rectangle.NO_BORDER);
            shipCell.setPadding(8);

            Paragraph shipping = new Paragraph();
            shipping.add(new Phrase("SHIP TO:\n", headerFont));
            shipping.add(new Phrase(order.getDeliveryAddress() + "\n", normalFont));


            shipCell.addElement(shipping);
            custShipTable.addCell(shipCell);

            document.add(custShipTable);


            // ===== TABLE OF ITEMS =====
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 1, 2});
            table.setSpacingAfter(15);

            // Table header
            addHeader(table, "Product", headerFont, Color.WHITE, new Color(33, 150, 243));
            addHeader(table, "Price", headerFont, Color.WHITE, new Color(33, 150, 243));
            addHeader(table, "Qty", headerFont, Color.WHITE, new Color(33, 150, 243));
            addHeader(table, "Total", headerFont, Color.WHITE, new Color(33, 150, 243));

            // Table rows with alternating background
            boolean shade = false;
            Color rowShade = new Color(245, 245, 245); // light gray
            for (OrderItem item : order.getItems()) {
                Color bg = shade ? rowShade : Color.WHITE;
                addCell(table, item.getProductName(), normalFont, bg, Element.ALIGN_LEFT);
                addCell(table, "₹ " + item.getUnitPrice(), normalFont, bg, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(item.getQuantity()), normalFont, bg, Element.ALIGN_LEFT);
                addCell(table, "₹ " + item.getTotalPrice().multiply(BigDecimal.valueOf(item.getQuantity())), normalFont, bg, Element.ALIGN_LEFT);
                shade = !shade;
            }

            document.add(table);

            // ===== DELIVERY & TOTAL =====
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setWidths(new float[]{2, 2});

            addCell(totalTable, "Delivery Charge", normalFont, Color.WHITE, Element.ALIGN_LEFT);
            addCell(totalTable, "₹ " + order.getDeliveryCharge(), normalFont, Color.WHITE, Element.ALIGN_RIGHT);

            addCell(totalTable, "TOTAL", totalFont, Color.WHITE, Element.ALIGN_LEFT);
            addCell(totalTable, "₹ " + order.getTotalAmount(), totalFont, Color.WHITE, Element.ALIGN_RIGHT);

            document.add(totalTable);

// ===== TOTAL IN WORDS =====
            long totalRounded = order.getTotalAmount().longValue(); // ignore decimals
            String amountInWords = convertNumberToWords(totalRounded) + " Rupees Only";

            Paragraph amountWordsPara = new Paragraph("Amount in Words: " + amountInWords, normalFont);
            amountWordsPara.setAlignment(Element.ALIGN_RIGHT);
            amountWordsPara.setSpacingBefore(5);

            document.add(amountWordsPara);


            // ===== LINKKART DISCLAIMER =====
            Paragraph disclaimer = new Paragraph(
                    "\n⚠️ This is an online-generated invoice for your order only. " +
                            "It is not a commercial tax invoice. All payments and deliveries are handled securely via LinkKart. " +
                            "Shop is verified and trusted.",
                    smallGray
            );
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            document.add(disclaimer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }

    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }
    private void addHeader(PdfPTable table, String text, Font font, Color fontColor, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.getPhrase().getFont().setColor(fontColor);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int alignLeft) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private PdfPCell noBorderCell(String text, Font font) {
        return noBorderCell(text, font, Element.ALIGN_LEFT);
    }

    private PdfPCell noBorderCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five",
            "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
            "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty",
            "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public String convertNumberToWords(long n) {
        if (n < 20) return units[(int) n];
        if (n < 100) return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " + units[(int) (n % 10)] : "");
        if (n < 1000) return units[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " " + convertNumberToWords(n % 100) : "");
        if (n < 100000) return convertNumberToWords(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " + convertNumberToWords(n % 1000) : "");
        if (n < 10000000) return convertNumberToWords(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " + convertNumberToWords(n % 100000) : "");
        return convertNumberToWords(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " + convertNumberToWords(n % 10000000) : "");
    }


}

