package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.OtpVerifyRequest;
import com.synix.uwait_backend.model.EmailOtp;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.repository.EmailOtpRepository;
import com.synix.uwait_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService{

    private final EmailOtpRepository otpRepo;
    private final UserRepository userRepo;

    @Transactional
    @Override
    public void verifyOtp(OtpVerifyRequest request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailOtp emailOtp = otpRepo
                .findTopByUserOrderByExpiryTimeDesc(user)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (emailOtp.isVerified()) {
            throw new RuntimeException("OTP already used");
        }

        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!emailOtp.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        emailOtp.setVerified(true);
        user.setEnabled(true);

        otpRepo.save(emailOtp);
        userRepo.save(user);
    }
}
