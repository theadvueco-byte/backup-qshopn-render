package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.repository.OrderRepository;
import com.synix.uwait_backend.service.MonthlyReportService;
import com.synix.uwait_backend.service.NotificationCleanupService;
import com.synix.uwait_backend.service.PdfInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TestController {
    private final MonthlyReportService monthlyReportService;
    private final OrderRepository orderRepo;
    private final PdfInvoiceService pdfInvoiceService;
    private final NotificationCleanupService cleanupService;

    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupNotifications() {

        long deletedCount = cleanupService.deleteOldNotifications();

        return ResponseEntity.ok(
                Map.of(
                        "status", "SUCCESS",
                        "deletedNotifications", deletedCount,
                        "message", "Old notifications deleted successfully"
                )
        );
    }

    @PostMapping("/test/monthly-report")
    public ResponseEntity<?> testReport() throws IOException {
        monthlyReportService.generateAndSendMonthlyReports();
        return ResponseEntity.ok("Monthly report sent successfully");
    }

    @GetMapping("/invoice/{id}")
    public ResponseEntity<byte[]> generateInvoiceTest(@PathVariable Long id) {
        // Fetch the order
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Generate invoice PDF (only generate, do not update status)
        byte[] pdf = pdfInvoiceService.generateInvoice(order);
        // Return PDF as response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice_" + order.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
