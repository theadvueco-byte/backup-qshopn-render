package com.synix.uwait_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, String folder,String shopCode);

    String storePaymentScreenshot(MultipartFile file, Long orderId);

    String storeProductImage(MultipartFile file, String shopCode, Long productId, int index);

    void delete(String fileUrl);

}
