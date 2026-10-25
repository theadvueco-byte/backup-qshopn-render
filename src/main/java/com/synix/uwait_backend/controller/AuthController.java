package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.config.JwtUtil;
import com.synix.uwait_backend.dto.LoginRequest;
import com.synix.uwait_backend.dto.LoginResponse;
import com.synix.uwait_backend.dto.RegisterRequest;
import com.synix.uwait_backend.dto.UserMeResponse;
import com.synix.uwait_backend.model.Admin;
import com.synix.uwait_backend.service.AdminAuthService;
import com.synix.uwait_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AdminAuthService adminAuthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) throws IOException {
        authService.register(req);
        return ResponseEntity.ok(Map.of("message","Registered. Verify email."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {

        LoginResponse response = authService.login(request);

        // Create HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("token", response.getToken())
                .httpOnly(true)
                .secure(true) // 🔥 localhost -> true in production
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("None") // 🔥 REQUIRED for cross-origin
                .build();

        httpResponse.setHeader("Set-Cookie", cookie.toString());

        // Return token in response body (and also set cookie)
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me() {
        UserMeResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }


    @PostMapping("admin/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String fullName = body.get("fullName");
        String email = body.get("email");
        String password = body.get("password");

        Admin admin = adminAuthService.registerAdmin(fullName, email, password);
        return ResponseEntity.ok(Map.of(
                "message", "Admin registered successfully",
                "adminId", admin.getId()
        ));
    }

    @PostMapping("admin/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpServletResponse httpResponse) {
        String email = body.get("email");
        String password = body.get("password");

        Admin admin = adminAuthService.loginAdmin(email, password);
        String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true) // 🔥 localhost -> true in production
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("None") // 🔥 REQUIRED for cross-origin
                .build();

        httpResponse.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", admin.getRole(),
                "adminId", admin.getId(),
                "email", admin.getEmail()
        ));
    }
}

