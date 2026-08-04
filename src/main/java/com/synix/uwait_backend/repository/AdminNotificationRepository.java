package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.AdminNotification;
import com.synix.uwait_backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findByAdminOrderByCreatedAtDesc(Admin admin);

    void deleteByAdmin(Admin admin);
}