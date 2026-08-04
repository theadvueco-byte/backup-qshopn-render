package com.synix.uwait_backend.service;

public interface EmailNotificationService {

    void sendMail(String to, String subject, String body);
    void sendMailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String fileName
    );
}
