package com.synix.uwait_backend.service.emailsend;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.synix.uwait_backend.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.sendgrid.helpers.mail.objects.Attachments;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.replyto.email}")
    private String replyToEmail;

    @Value("${sendgrid.otp.template-id}")
    private String templateId;

    @Value("${sendgrid.order.info.template-id}")
    private String orderInfoMessageTemplateId;

    @Value("${sendgrid.owner.welcome.template-id}")
    private String ownerWelcomeTemplateId;

    @Value("${sendgrid.support.ticket.template-id}")
    private String supportTicketTemplateId;

    @Value("${sendgrid.subscription.alert.template-id}")
    private String subscriptionAlertTemplateId;


    @Value("${sendgrid.attachment.template-id}")
    private String attachmentTemplateId;


    private final SendGrid sendGrid;

    public EmailService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    public void sendOtp(String toEmail, String otp) throws IOException {

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        // Reply-To (required by SendGrid UI)
        mail.setReplyTo(new Email(replyToEmail));

        Personalization personalization = new Personalization();
        personalization.addTo(to);

        // 🔹 Dynamic variable used in template {{otp}}
        personalization.addDynamicTemplateData("otp", otp);

        mail.addPersonalization(personalization);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        System.out.println("SendGrid Status: " + response.getStatusCode());
    }



    public void sendOwnerWelcome(String ownerEmail, String shopName) throws IOException {

        sendTemplateEmail(
                ownerEmail,
                ownerWelcomeTemplateId,
                Map.of("shopName", shopName)
        );
    }


//tier-last
    public void sendSubscriptionAlert(
            String ownerEmail,
            String ownerName,
            String shopName,
            int daysLeft
    ) throws IOException {

        sendTemplateEmail(
                ownerEmail,
                subscriptionAlertTemplateId,
                Map.of(
                        "ownerName", ownerName,
                        "shopName", shopName,
                        "daysLeft", daysLeft
                )
        );
    }



    public void sendOrderInfoMail(
            String toEmail,
            String subject,
            String title,
            String recipientName,
            Order order,
            String mainMessage,
            String extraMessage
    ) throws IOException {

        Map<String, Object> data = new HashMap<>();

        data.put("subject", subject);
        data.put("title", title);
        data.put("recipientName", recipientName);
        data.put("mainMessage", mainMessage);
        data.put("orderId", order.getId());
        data.put("shopName", order.getShop().getShopName());

        // Optional fields
        data.put(
                "amountLine",
                order.getTotalAmount() != null
                        ? "Amount: ₹" + order.getTotalAmount()
                        : ""
        );

        data.put(
                "extraMessage",
                extraMessage != null ? extraMessage : ""
        );

        sendTemplateEmail(
                toEmail,
                orderInfoMessageTemplateId,
                data
        );
    }





    public void sendSupportTicketEmail(
            String toEmail,
            String recipientRole,
            String shopName,
            String issueType,
            String message,
            String response // nullable
    ) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("recipientRole", recipientRole);
        data.put("shopName", shopName);
        data.put("issueType", issueType);
        data.put("message", message);

        if (response != null) {
            data.put("isResponse", true);
            data.put("response", response);
        } else {
            data.put("isResponse", false);
        }

        sendTemplateEmail(
                toEmail,
                supportTicketTemplateId,
                data
        );
    }

    public void sendTemplateMailWithAttachment(
            String toEmail,
            String subject,
            String title,
            String recipientName,
            String mainMessage,
            byte[] fileBytes,
            String fileName
    ) throws IOException {

        if (attachmentTemplateId == null || attachmentTemplateId.isBlank()) {
            throw new IllegalStateException("SendGrid attachment template ID is not configured");
        }

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail));
        mail.setReplyTo(new Email(replyToEmail));
        mail.setTemplateId(attachmentTemplateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(toEmail));

        personalization.addDynamicTemplateData("subject", subject);
        personalization.addDynamicTemplateData("title", title);
        personalization.addDynamicTemplateData("recipientName", recipientName);
        personalization.addDynamicTemplateData("mainMessage", mainMessage);

        mail.addPersonalization(personalization);

        // 🔹 Attachment
        Attachments attachment = new Attachments();
        attachment.setContent(Base64.getEncoder().encodeToString(fileBytes));
        attachment.setType("application/pdf");
        attachment.setFilename(fileName);
        attachment.setDisposition("attachment");

        mail.addAttachments(attachment);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        sendGrid.api(request);
    }

    public void sendSimpleContactEmail(
            String phone,
            String message
    ) throws IOException {

        Email from = new Email(fromEmail);
        Email to = new Email("theadvue@qshopn.com"); // or use property

        String subject = "New Contact Request";

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        String formattedTime = now.format(formatter);

        String cleanPhone = phone.replaceAll("[^0-9]", "");


        String whatsappLink = "https://wa.me/91" + cleanPhone +
                "?text=Hi%20I%20received%20your%20query.";;



        String contentText =
                "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9;'>"

                        + "<div style='max-width: 600px; margin: auto; background: #ffffff; "
                        + "padding: 20px; border-radius: 8px; border: 1px solid #e5e5e5;'>"

                        + "<h2 style='margin-top: 0; color: #111827;'>New WhatsApp Contact Request</h2>"

                        + "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;'/>"

                        + "<p style='margin: 8px 0;'><strong>Phone Number:</strong><br/>"
                        + phone + "</p>"

                        + "<p style='margin: 8px 0;'><strong>Message:</strong><br/>"
                        + (message != null && !message.isBlank() ? message : "N/A") + "</p>"

                        + "<p style='margin: 8px 0; color: #6b7280; font-size: 12px;'>"
                        + "Submitted at: " + formattedTime + "</p>"

                        + "<div style='margin-top:20px;'>"
                        + "<a href='" + whatsappLink + "' "
                        + "style='display:inline-block;"
                        + "padding:12px 18px;"
                        + "background-color:#25D366;"
                        + "color:#ffffff;"
                        + "text-decoration:none;"
                        + "border-radius:6px;"
                        + "font-weight:bold;"
                        + "font-size:14px;'>"
                        + "Open in WhatsApp"
                        + "</a>"
                        + "</div>"



                        + "</div></div>";


        com.sendgrid.helpers.mail.objects.Content content =
                new com.sendgrid.helpers.mail.objects.Content("text/html", contentText);

        Mail mail = new Mail(from, subject, to, content);
        mail.setReplyTo(new Email(replyToEmail));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        System.out.println("Contact Email Sent | Status: " + response.getStatusCode());
    }


// Utility method to send any template email with dynamic data
    private void sendTemplateEmail(
            String toEmail,
            String templateId,
            Map<String, Object> dynamicData
    ) throws IOException {

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail));
        mail.setReplyTo(new Email(replyToEmail));
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(toEmail));

        dynamicData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        System.out.println("Email sent | Status: " + response.getStatusCode());
    }


}

