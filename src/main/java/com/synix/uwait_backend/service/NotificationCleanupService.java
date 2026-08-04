package com.synix.uwait_backend.service;

import com.synix.uwait_backend.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

    private final NotificationRepository notificationRepo;

    // Scheduler (auto)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteOldNotificationsJob() {
        deleteOldNotifications();
    }

    // Manual call (endpoint will use this)
    @Transactional
    public long deleteOldNotifications() {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return notificationRepo.deleteByCreatedAtBefore(todayStart);
    }
}

