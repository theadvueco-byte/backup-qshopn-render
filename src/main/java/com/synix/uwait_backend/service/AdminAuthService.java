package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.Admin;

public interface AdminAuthService {
    Admin registerAdmin(String fullName, String email, String password);
    Admin loginAdmin(String email, String password);
}
