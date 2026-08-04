package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.OtpVerifyRequest;
import com.synix.uwait_backend.dto.ResetPasswordRequest;

import java.io.IOException;

public interface PasswordResetService {
    void requestPasswordReset(String email) throws IOException;
    void verifyOtp(OtpVerifyRequest req);
    void resetPassword(ResetPasswordRequest req);
}
