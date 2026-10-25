package com.synix.uwait_backend.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final SendGrid sendGrid;

    @Value("${sendgrid.from.email:theadvue@qshopn.com}")
    private String fromEmail;

    @Value("${sendgrid.replyto.email:theadvue@qshopn.com}")
    private String replyToEmail;

    @Override
    public void sendMail(String to, String subject, String body) {
        try {
            Email from = new Email(fromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);
            if (replyToEmail != null && !replyToEmail.isBlank()) {
                mail.setReplyTo(new Email(replyToEmail));
            }

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            System.out.println("Email sent to " + to + " via SendGrid | Status: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + " via SendGrid: " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachmentBytes,
            String fileName
    ) {
        try {
            Email from = new Email(fromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);
            if (replyToEmail != null && !replyToEmail.isBlank()) {
                mail.setReplyTo(new Email(replyToEmail));
            }

            if (attachmentBytes != null && attachmentBytes.length > 0) {
                Attachments attachment = new Attachments();
                attachment.setContent(Base64.getEncoder().encodeToString(attachmentBytes));
                attachment.setType("application/pdf");
                attachment.setFilename(fileName);
                attachment.setDisposition("attachment");
                mail.addAttachments(attachment);
            }

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            System.out.println("Email with attachment sent to " + to + " via SendGrid | Status: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Failed to send email with attachment to " + to + " via SendGrid: " + e.getMessage());
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }
}
