package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.OtpVerifyRequest;
import com.synix.uwait_backend.dto.ResetPasswordRequest;
import com.synix.uwait_backend.model.EmailOtp;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.repository.EmailOtpRepository;
import com.synix.uwait_backend.repository.UserRepository;
import com.synix.uwait_backend.service.emailsend.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordRestServiceImpl implements PasswordResetService {
    private final UserRepository userRepo;
    private final EmailOtpRepository otpRepo;
    private final EmailService emailService;

    // smtp email service
    private final EmailNotificationService emailNotificationService;

    @Override
    @Transactional
    public void requestPasswordReset(String email) throws IOException {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old OTPs
        otpRepo.deleteByUser(user);

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        emailOtp.setVerified(false);
        emailOtp.setUser(user);

        otpRepo.save(emailOtp);

        // Email content
        String subject = "Password Reset OTP - Qshopn";
        String body =
                "Dear User,\n\n" +
                        "Your OTP for resetting your password is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "TheAdvue Qshopn Support Team";

        emailNotificationService.sendMail(email, subject, body);
       // emailService.sendOtp(email, otp);

    }

    @Override
    @Transactional
    public void verifyOtp(OtpVerifyRequest req) {

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailOtp otp = otpRepo.findTopByUserOrderByExpiryTimeDesc(user)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otp.isVerified()) {
            throw new RuntimeException("OTP already used");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getOtp().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        otp.setVerified(true);
        otpRepo.save(otp);
    }


    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailOtp otp = otpRepo.findTopByUserOrderByExpiryTimeDesc(user)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (!otp.isVerified()) {
            throw new RuntimeException("OTP not verified");
        }

        user.setPassword(
                new BCryptPasswordEncoder().encode(req.getNewPassword())
        );

        userRepo.save(user);

        // Cleanup OTP
        otpRepo.deleteByUser(user);
    }

}
