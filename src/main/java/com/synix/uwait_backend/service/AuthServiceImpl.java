package com.synix.uwait_backend.service;

import com.synix.uwait_backend.config.JwtUtil;
import com.synix.uwait_backend.dto.*;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.repository.AdminRepository;
import com.synix.uwait_backend.repository.EmailOtpRepository;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.repository.UserRepository;
import com.synix.uwait_backend.service.emailsend.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AdminNotificationService adminNotificationService;
    private final AdminRepository adminRepo;
    private final UserRepository userRepo;
    private final EmailOtpRepository otpRepo;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final EmailNotificationService emailNotificationService;
    private static final String OTP_CHARS = "0123456789";

    @Override
    public void register(RegisterRequest req) throws IOException {

        String email = req.getEmail().trim().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.setOnboarded(false);
        user.setRole(Role.CLIENT);

        userRepo.save(user);

        String otp = generateOtp(6);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUser(user);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        emailOtp.setVerified(false);
        otpRepo.save(emailOtp);

//        emailService.sendOtp(user.getEmail(), otp);

        sendOtpEmail(user.getEmail(), otp);

        List<Admin> admins = adminRepo.findAll();
        for (Admin admin : admins) {
            adminNotificationService.createNotification(
                    admin,
                    "New User Registration",
                    "New user registered: " + user.getFullName() + " (" + user.getEmail() + ")",
                    NotificationSourceType.SHOP_OWNER
            );
        }

    }

    @Override
    public String generateOtp(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(OTP_CHARS.length());
            sb.append(OTP_CHARS.charAt(idx));
        }
        return sb.toString();
    }

// directly send email without using email service
// Directly send email using SendGrid template, with plain text fallback
    private void sendOtpEmail(String toEmail, String otp) {
        try {
            emailService.sendOtp(toEmail, otp);
        } catch (Exception e) {
            emailNotificationService.sendMail(
                    toEmail,
                    "OTP Verification Code – Qshopn",
                    "Dear User,\n\n" +
                            "Thank you for registering with Qshopn.\n\n" +
                            "Your One-Time Password (OTP) for verification is:\n\n" +
                            "OTP: " + otp + "\n\n" +
                            "This OTP is valid for 5 minutes only.\n" +
                            "Please do not share this code with anyone for security reasons.\n\n" +
                            "If you did not request this verification, please ignore this email.\n\n" +
                            "Warm regards,\n" +
                            "TheAdvue Qshopn Team\n" +
                            "Secure. Reliable. Simple."
            );
        }
    }

    @Override
    public UserMeResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = authentication.getName();

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserMeResponse res = new UserMeResponse();
            res.setId(user.getId());
            res.setFullName(user.getFullName());
            res.setEmail(user.getEmail().trim().toLowerCase());
            res.setRole(user.getRole().name());
            res.setEmailVerified(user.isEnabled());

            // 🔥 Fetch shops
            List<Shop> shops = shopRepository.findAllByOwner(user);

            List<ShopSummaryResponse> shopResponses = shops.stream()
                    .map(shop -> {
                        ShopSummaryResponse s = new ShopSummaryResponse();
                        s.setId(shop.getPublicCode());
                        s.setName(shop.getShopName());
                        s.setStatus(shop.getStatus().name());
                        return s;
                    })
                    .toList();

            res.setShops(shopResponses);
            return res;
        }

        Optional<Admin> adminOpt = adminRepo.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            UserMeResponse res = new UserMeResponse();
            res.setId(admin.getId());
            res.setFullName(admin.getFullName());
            res.setEmail(admin.getEmail().trim().toLowerCase());
            res.setRole(admin.getRole().name());
            res.setEmailVerified(true);
            res.setShops(List.of());
            return res;
        }

        throw new RuntimeException("User not found");
    }



    @Override
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Email not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);

        return new LoginResponse(
                token,
                user.getRole().name(),
                user.getFullName(),
                user.isOnboarded()
        );
    }

}
