package com.synix.uwait_backend.service;


import com.synix.uwait_backend.model.Shop;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ShopOnboardingService {

    // Save basic business info
     void saveBusinessInfo(String email, String name, String category, String whatsapp);

    // Save address info
     void saveAddress(String email, Shop s);

    // Save branding info
     void saveBranding(String email, MultipartFile logo, String bio, String insta);

    // Submit shop location & photo
     void submitLocation(String email, MultipartFile photo,String gpsLocation);

    // Get UPI link for frontend
     String getUpiLink(String email);

    // Submit UPI verification
     void submitUpiProof(String email,  MultipartFile screenshot);

    // Activate shop & generate QR code
     void activateShop(Shop shop) throws IOException;

    // Generate QR code for shop
     void generateQrCodeForShop(Shop shop);

     Map<String, Object> getFullShopOnboardingDetails(String shopCode);

}
