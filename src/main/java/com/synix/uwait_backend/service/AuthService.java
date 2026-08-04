package com.synix.uwait_backend.service;


import com.synix.uwait_backend.dto.*;

import java.io.IOException;


public interface AuthService {

    void register(RegisterRequest req) throws IOException;


    String generateOtp(int length);


//    void sendOtpEmail(String toEmail, String otp);

    UserMeResponse getCurrentUser();

    LoginResponse login(LoginRequest request);
}

