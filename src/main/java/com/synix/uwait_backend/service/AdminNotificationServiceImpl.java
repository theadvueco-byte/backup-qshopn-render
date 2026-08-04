package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Admin;
import com.synix.uwait_backend.model.AdminNotification;
import com.synix.uwait_backend.model.NotificationSourceType;
import com.synix.uwait_backend.repository.AdminNotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final AdminNotificationRepository notificationRepo;

    // 🔹 Get all notifications for admin
    @Override
    public List<AdminNotification> getNotifications(Admin admin) {
        return notificationRepo.findByAdminOrderByCreatedAtDesc(admin);
    }

    // 🔹 Create notification
    @Override
    public void createNotification(
            Admin admin,
            String title,
            String message,
            NotificationSourceType sourceType
    ) {
        AdminNotification notification = new AdminNotification();
        notification.setAdmin(admin);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSourceType(sourceType);

        notificationRepo.save(notification);
    }

    // 🔹 Mark as read
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        AdminNotification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        // No save() needed because of @Transactional
    }

    // 🔹 Delete single
    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepo.deleteById(notificationId);
    }

    // 🔹 Delete all for admin
    @Override
    @Transactional
    public void deleteAllNotifications(Admin admin) {
        notificationRepo.deleteByAdmin(admin);
    }
}