package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.model.Admin;
import com.synix.uwait_backend.model.AdminNotification;
import com.synix.uwait_backend.model.NotificationSourceType;
import com.synix.uwait_backend.repository.AdminNotificationRepository;
import com.synix.uwait_backend.repository.AdminRepository;
import com.synix.uwait_backend.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService notificationService;
    private final AdminRepository adminRepo;
    private final AdminNotificationRepository notificationRepo;

    // 🔹 Get all notifications
    @GetMapping
    public ResponseEntity<List<AdminNotification>> getAllNotifications(Authentication auth) {

        Admin admin = adminRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return ResponseEntity.ok(notificationService.getNotifications(admin));
    }

    // 🔹 Mark as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // 🔹 Delete single
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);

        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    // 🔹 Delete all
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAll(Authentication auth) {

        Admin admin = adminRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        notificationService.deleteAllNotifications(admin);

        return ResponseEntity.ok(Map.of("message", "All notifications deleted"));
    }
    @PostMapping("/create")
    public ResponseEntity<?> createNotification(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationSourceType sourceType,
            Authentication auth
    ) {
        Admin admin = adminRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        notificationService.createNotification(
                admin,
                title,
                message,
                sourceType
        );

        return ResponseEntity.ok(Map.of("message", "Notification created"));
    }


    }