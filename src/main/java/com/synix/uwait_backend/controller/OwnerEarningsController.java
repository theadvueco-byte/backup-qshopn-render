package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.ownerearnings.*;
import com.synix.uwait_backend.dto.ownerorder.OwnerOrderSummaryResponse;
import com.synix.uwait_backend.dto.ownerstatus.*;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerEarningsController {

    private final OwnerService dashboardService;
    private final ShopRepository shopRepo;

    // 1. KPI Cards
    @GetMapping("/earnings/kpis")
    public ResponseEntity<EarningsKpiResponse> getKpis(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        String ownerEmail = auth.getName();
        return ResponseEntity.ok(dashboardService.getKpis(ownerEmail));
    }

    // 2. Revenue Trend
    @GetMapping("/earnings/revenue-trend")
    public ResponseEntity<List<RevenueTrendResponse>> getRevenueTrend(
            @RequestParam(defaultValue = "month") String dateRange,
            Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        String ownerEmail = auth.getName();
        return ResponseEntity.ok(dashboardService.getRevenueTrend(ownerEmail,dateRange));
    }

    // 3. Order Status Pie
    @GetMapping("/orders/status-summary")
    public ResponseEntity<List<StatusCountResponse>> getOrderStatusSummary(Authentication auth) {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        String ownerEmail = auth.getName();

        return ResponseEntity.ok(dashboardService.getOrderStatusSummary(ownerEmail));
    }

    // 4. Payment Methods
    @GetMapping("/payments/method-breakdown")
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        String ownerEmail = auth.getName();
        return ResponseEntity.ok(dashboardService.getPaymentMethods(ownerEmail));
    }

    // 5. Top Products
    @GetMapping("/top-products")
    public List<TopProductResponse> getTopProducts(Authentication authentication) {
        String ownerEmail = authentication.getName();
        return dashboardService.getTopProducts(ownerEmail);
    }


    // 6. Orders Table
    @GetMapping("/orders")
    public ResponseEntity<List<OwnerOrderSummaryResponse>> getOrders(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        String ownerEmail = auth.getName();
        return ResponseEntity.ok(dashboardService.getOrders(ownerEmail));
    }

    // 7. Export CSV
    @GetMapping("/orders/export")
    public ResponseEntity<String> exportOrdersCsv(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        String ownerEmail = auth.getName();
        String csvContent = dashboardService.exportOrdersToCsv(ownerEmail);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=orders.csv")
                .body(csvContent);
    }

    @GetMapping("/stock-report")
    public StockReportResponse getStockReport( Authentication auth) {
        String ownerEmail = auth.getName();
        return dashboardService.getStockReport(ownerEmail);
    }

    @GetMapping("/order-stats")
    public OrderStatsResponse orderStats( Authentication auth) {
        String ownerEmail = auth.getName();
        return dashboardService.getOrderStats(ownerEmail);
    }

    @GetMapping("/owner/dashboard/orders-chart")
    public List<OrderChartResponse> getOrdersChart( Authentication auth, @RequestParam(defaultValue = "7") int days ) {
        String ownerEmail = auth.getName();
        return dashboardService.getOrdersChart(ownerEmail, days);
    }


    @GetMapping("/owner/dashboard/subscription")
    public SubscriptionDashboardResponse subscriptionDashboard( Authentication auth) {
        String ownerEmail = auth.getName();
        return dashboardService
                .getSubscriptionDashboard(ownerEmail);
    }

    @GetMapping("/dashboard/sales-stats")
    public SalesStatsResponse getSalesStats( Authentication auth) {
        String ownerEmail = auth.getName();
        return dashboardService.getSalesStats(ownerEmail);
    }

    @GetMapping("/analytics/stock-trend")
    public List<StockTrendResponse> stockTrend(
            @RequestParam(defaultValue = "week") String range,
            Authentication auth
    ) {
        return dashboardService.getStockTrend(auth.getName(), range);
    }

    @GetMapping("/analytics/category-stock")
    public List<CategoryInventoryResponse> categoryStock(Authentication auth) {
        return dashboardService.getCategoryInventory(auth.getName());
    }

    @GetMapping("/analytics/sales-stock")
    public List<SalesStockResponse> salesVsStock(
            @RequestParam(defaultValue = "week") String range,
            Authentication auth
    ) {
        return dashboardService.getSalesVsStock(auth.getName(), range);
    }

}
