package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.EmailOtp;
import com.synix.uwait_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByUserOrderByExpiryTimeDesc(User user);

    void deleteByUser(User user);
}


