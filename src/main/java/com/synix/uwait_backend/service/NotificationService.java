package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Notification;
import com.synix.uwait_backend.model.Shop;

import java.util.List;


public interface NotificationService {

    List<Notification> getNotifications(Shop shop);

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);

    void createNotification(Shop shop, String title, String message);

    void deleteAllNotifications(Shop shop);
}
