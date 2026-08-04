package com.synix.uwait_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl; // https://pub-xxxx.r2.dev

    @Override
    public String store(MultipartFile file, String folder, String shopCode) {

        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            String key = folder + "/" + shopCode;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return publicUrl + "/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to R2", e);
        }
    }

    @Override
    public String storePaymentScreenshot(MultipartFile file, Long orderId) {

        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            String key = "payments/order_" + orderId;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return publicUrl + "/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Payment screenshot upload failed", e);
        }
    }

    @Override
    public String storeProductImage(
            MultipartFile file,
            String shopCode,
            Long productId,
            int index
    ) {

        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            // ✅ Extract extension
            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // ✅ Proper key with extension
            String key = "products/" + shopCode + "/" + productId +
                    "/" + shopCode + "_" + productId + "_" + index + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType()) // VERY IMPORTANT
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return publicUrl + "/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Product upload failed", e);
        }
    }

    @Override
    public void delete(String fileUrl) {

        try {
            if (fileUrl == null || fileUrl.isBlank()) return;

            String key = fileUrl.replace(publicUrl + "/", "");

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from R2", e);
        }
    }
//
//    /**
//     * Upload a generic file to Cloudinary under a folder for the shop.
//     * @param file MultipartFile
//     * @param folder Folder name in Cloudinary (e.g., "logos")
//     * @param shopCode Shop identifier
//     * @return Secure URL of uploaded file
//     */
//    @Override
//    public String store(MultipartFile file, String folder, String shopCode) {
//
//        try {
//            // 1️⃣ Validate file
//            if (file == null || file.isEmpty()) {
//                throw new IllegalArgumentException("File is empty");
//            }
//
//            // 2️⃣ Upload with filename = shopCode
//            Map uploadResult = cloudinary.uploader().upload(
//                    file.getBytes(),
//                    ObjectUtils.asMap(
//                            "folder", folder,          // e.g. shops/SHOP123/products
//                            "public_id", shopCode,     // ✅ filename = shopCode
//                            "resource_type", "image",
//                            "overwrite", true          // replace if exists
//                    )
//            );
//
//            // 3️⃣ Return secure URL
//            return uploadResult.get("secure_url").toString();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to upload image to Cloudinary", e);
//        }
//    }
//
//    /**
//     * Upload a product image in folder: products/{shopCode}/{productId}/{index}
//     */
//    @Override
//    public String storeProductImage(
//            MultipartFile file,
//            String shopCode,
//            Long productId,
//            int index
//    ) {
//
//        try {
//            if (file == null || file.isEmpty()) {
//                throw new IllegalArgumentException("File is empty");
//            }
//
//            // 1️⃣ Folder structure
//            // products/SHOP123/45
//            String folderPath = "products/" + shopCode + "/" + productId;
//
//            // 2️⃣ File name (without extension)
//            // SHOP123_45_1
//            String fileName = shopCode + "_" + productId + "_" + index;
//
//            // 3️⃣ Upload to Cloudinary
//            Map uploadResult = cloudinary.uploader().upload(
//                    file.getBytes(),
//                    ObjectUtils.asMap(
//                            "folder", folderPath,     // ✅ folders
//                            "public_id", fileName,    // ✅ filename
//                            "overwrite", true,
//                            "resource_type", "image",
//                            "unique_filename", false
//                    )
//            );
//
//            // 4️⃣ Return image URL
//            return uploadResult.get("secure_url").toString();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Product image upload failed", e);
//        }
//    }
//
//    /**
//     * Upload payment screenshot in folder: payments/
//     */
//
//    @Override
//    public String storePaymentScreenshot(MultipartFile file, Long orderId) {
//
//        try {
//            if (file == null || file.isEmpty()) {
//                throw new IllegalArgumentException("File is empty");
//            }
//
//            // 1️⃣ Folder
//            String folderPath = "payments";
//
//            // 2️⃣ Filename (without extension)
//            String fileName = "order_" + orderId;
//
//            // 3️⃣ Upload
//            Map uploadResult = cloudinary.uploader().upload(
//                    file.getBytes(),
//                    ObjectUtils.asMap(
//                            "folder", folderPath,     // ✅ payments/
//                            "public_id", fileName,    // ✅ order_123
//                            "overwrite", true,
//                            "resource_type", "image",
//                            "unique_filename", false
//                    )
//            );
//
//            return uploadResult.get("secure_url").toString();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Payment screenshot upload failed", e);
//        }
//    }
//
//    /**
//     * Delete file from Cloudinary by its URL
//     */
//    @Override
//    public void delete(String fileUrl) {
//
//        try {
//            if (fileUrl == null || fileUrl.isBlank()) return;
//
//            // Extract after /upload/
//            String publicId = fileUrl.substring(fileUrl.indexOf("/upload/") + 8);
//
//            // Remove version if present (v123456/)
//            publicId = publicId.replaceFirst("^v[0-9]+/", "");
//
//            // Remove extension (.jpg, .png, etc.)
//            publicId = publicId.replaceAll("\\.[a-zA-Z0-9]+$", "");
//
//            cloudinary.uploader().destroy(
//                    publicId,
//                    ObjectUtils.asMap("resource_type", "image")
//            );
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to delete from Cloudinary", e);
//        }
//    }
}




//@Service
//@RequiredArgsConstructor
//public class FileStorageServiceImpl implements FileStorageService{
//
//    @Value("${file.upload-dir}")
//    private String baseDir;
//
//    @Override
//    public String store(MultipartFile file, String folder,String shopCode) {
//        try {
//            if (file == null || file.isEmpty()) {
//                throw new RuntimeException("Uploaded file is empty");
//            }
//
//            String extension = Objects.requireNonNull(file.getOriginalFilename())
//                    .substring(file.getOriginalFilename().lastIndexOf("."));
//
//            String fileName = shopCode + extension;
//
//            Path folderPath = Paths.get(baseDir, folder);
//            Files.createDirectories(folderPath);
//
//            Path filePath = folderPath.resolve(fileName);
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            // ✅ RETURN RELATIVE URL (store this in DB)
//            return "/uploads/" + folder + "/" + fileName;
//
//        } catch (Exception e) {
//            throw new RuntimeException("File upload failed", e);
//        }
//    }
//
//
//    @Override
//    public String storePaymentScreenshot(MultipartFile file, Long orderId) {
//        try {
//            String ext = Objects.requireNonNull(file.getOriginalFilename())
//                    .substring(file.getOriginalFilename().lastIndexOf("."));
//
//            String filename = "order_" + orderId + ext;
//
//            Path dir = Paths.get("uploads/payments");
//            Files.createDirectories(dir);
//
//            Path filePath = dir.resolve(filename);
//            Files.write(filePath, file.getBytes());
//
//            return "/uploads/payments/" + filename; // store THIS in DB
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to store payment screenshot", e);
//        }
//    }
//
//    @Override
//    public String storeProductImage(MultipartFile file, String shopCode, Long productId, int index) {
//        try {
//            String ext = Objects.requireNonNull(file.getOriginalFilename())
//                    .substring(file.getOriginalFilename().lastIndexOf("."));
//            String fileName =
//                    shopCode + "_" + productId + "_" + index + ext;
//            Path dir = Paths.get(baseDir, "products", shopCode, productId.toString());
//            Files.createDirectories(dir);
//            Path filePath = dir.resolve(fileName);
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//            return "/uploads/products/" + shopCode + "/" + productId + "/" + fileName;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to store product image", e);
//        }
//    }
//
//    @Override
//    public void delete(String fileUrl) {
//        try {
//            if (fileUrl == null || fileUrl.isBlank()) return;
//
//            // remove leading "/"
//            String relativePath = fileUrl.startsWith("/")
//                    ? fileUrl.substring(1)
//                    : fileUrl;
//
//            Path filePath = Paths.get(relativePath).toAbsolutePath();
//
//            Files.deleteIfExists(filePath);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
//        }
//    }
//}
