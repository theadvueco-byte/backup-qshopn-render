package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.service.emailsend.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final PdfInvoiceService pdfInvoiceService;
  //  private final EmailService emailService;

    //smtp email service
    private final EmailNotificationService emailNotificationService;

    public void sendInvoiceToCustomer(Order order) throws IOException {

        byte[] pdf = pdfInvoiceService.generateInvoice(order);

        emailNotificationService.sendMailWithAttachment(
                order.getCustomerEmail(),
                "Invoice for Order #" + order.getId(),
                "Your order is out for delivery. Please find the invoice attached.",
                pdf,
                "Invoice_Order_" + order.getId() + ".pdf"
        );
//        emailService.sendTemplateMailWithAttachment(
//                order.getCustomerEmail(),
//                "Invoice for Order #" + order.getId(),
//                "Invoice Attached",
//                order.getCustomerName(),
//                "Your order is out for delivery. Please find the invoice attached.",
//                pdf,
//                "Invoice_Order_" + order.getId() + ".pdf"
//        );



    }
}

