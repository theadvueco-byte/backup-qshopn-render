package com.synix.uwait_backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService{

    private final JavaMailSender mailSender;

    @Override
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("theadvue@qshopn.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        System.out.println("Email sent to: " + to + " with subject: " + subject);
    }

    @Override
    public void sendMailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String fileName
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);
            helper.setFrom("theadvue@qshopn.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            helper.addAttachment(
                    fileName,
                    new ByteArrayResource(attachment)
            );

            mailSender.send(message);

            System.out.println("Email with attachment sent to: " + to + " with subject: " + subject);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}
