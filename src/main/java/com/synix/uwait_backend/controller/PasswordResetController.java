package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.OtpVerifyRequest;
import com.synix.uwait_backend.dto.ResetPasswordRequest;
import com.synix.uwait_backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService resetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestOtp(@RequestParam String email) throws IOException {
        resetService.requestPasswordReset(email);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest req) {
        resetService.verifyOtp(req);
        return ResponseEntity.ok("OTP verified");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        resetService.resetPassword(req);
        return ResponseEntity.ok("Password reset successful");
    }

}

