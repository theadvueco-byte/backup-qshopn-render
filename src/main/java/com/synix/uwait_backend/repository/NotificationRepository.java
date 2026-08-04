package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Notification;
import com.synix.uwait_backend.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByShopOrderByCreatedAtDesc(Shop shop);

    long deleteByCreatedAtBefore(LocalDateTime todayStart);
}
