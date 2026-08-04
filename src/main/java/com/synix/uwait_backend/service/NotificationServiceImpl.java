package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Notification;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepo;

    // Get all notifications for shop
    @Override
    public List<Notification> getNotifications(Shop shop) {
        return notificationRepo.findByShopOrderByCreatedAtDesc(shop);
    }

    // Mark as read
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepo.deleteById(notificationId);
        notificationRepo.save(notification);
    }

    // Delete notification
    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepo.deleteById(notificationId);
    }

    // Create notification
    @Override
    public void createNotification(Shop shop, String title, String message) {
        Notification notification = new Notification();
        notification.setShop(shop);
        notification.setTitle(title);
        notification.setMessage(message);

        notificationRepo.save(notification);
    }

    @Override
    public void deleteAllNotifications(Shop shop) {
        List<Notification> notifications = notificationRepo.findByShopOrderByCreatedAtDesc(shop);
        notificationRepo.deleteAll(notifications);
    }
}
