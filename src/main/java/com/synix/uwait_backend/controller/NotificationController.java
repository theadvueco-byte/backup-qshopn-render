package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.Notification;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/owner/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ShopRepository shopRepo;

    // Get all notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return ResponseEntity.ok(notificationService.getNotifications(shop));
    }

    // Delete all notifications for shop
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        notificationService.deleteAllNotifications(shop);

        return ResponseEntity.ok(Map.of("message", "All notifications deleted"));
    }

    // Mark as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }
}
