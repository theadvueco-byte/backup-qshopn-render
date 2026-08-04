package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Admin;
import com.synix.uwait_backend.model.AdminNotification;
import com.synix.uwait_backend.model.NotificationSourceType;

import java.util.List;

public interface AdminNotificationService {

    List<AdminNotification> getNotifications(Admin admin);

    void createNotification(
            Admin admin,
            String title,
            String message,
            NotificationSourceType sourceType
    );

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);

    void deleteAllNotifications(Admin admin);
}