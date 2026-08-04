package com.synix.uwait_backend.controller.testmaildomain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceDomain {

    @Autowired
    private JavaMailSender mailSender;

    public String sendMail(String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("theadvue@qshopn.com");
            message.setTo(to);
            message.setSubject("Test Mail from Spring Boot");
            message.setText("Hello! Your SMTP is working 🚀");

            mailSender.send(message);
            return "Mail sent successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}