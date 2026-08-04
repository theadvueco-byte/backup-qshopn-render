package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.OtpVerifyRequest;

public interface OtpService {

    void verifyOtp(OtpVerifyRequest request);
}

