package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.OtpVerifyRequest;
import com.synix.uwait_backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class OtpVerificationController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/verify-email-otp")
    public ResponseEntity<?> verifyEmailOtp(
            @RequestBody OtpVerifyRequest request) {

        otpService.verifyOtp(request);
        return ResponseEntity.ok(Map.of("message","Email verified successfully"));
    }
}

