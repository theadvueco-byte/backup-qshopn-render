package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Admin;
import com.synix.uwait_backend.model.NotificationSourceType;
import com.synix.uwait_backend.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {
    private final AdminNotificationService adminNotificationService;

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Admin registerAdmin(String fullName, String email, String password) {
        if (adminRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Admin admin = Admin.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .active(true)
                .build();



        List<Admin> admins = adminRepo.findAll();

        for (Admin ad : admins) {
            adminNotificationService.createNotification(
                    ad,
                    "New admin '" + fullName + "' registered with email '" + email + "'.",
                    "New Admin Registration",
                    NotificationSourceType.SYSTEM
            );
        }

        return adminRepo.save(admin);
    }

    public Admin loginAdmin(String email, String password) {
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return admin;
    }
}
