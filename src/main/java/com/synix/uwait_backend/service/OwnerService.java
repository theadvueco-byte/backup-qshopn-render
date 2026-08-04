package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.DeleteAccountConfirmRequest;
import com.synix.uwait_backend.dto.ownerearnings.*;
import com.synix.uwait_backend.dto.ownerorder.OwnerOrderSummaryResponse;
import com.synix.uwait_backend.dto.ownerstatus.*;
import com.synix.uwait_backend.model.Shop;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OwnerService {

    Map<String, Object> getEarnings(String ownerEmail);

    void updateShopStatus(String email, boolean active);

    Shop getShopByIdAndOwner(String shopId, String ownerEmail);


    // 1. KPI Cards
    EarningsKpiResponse getKpis(String ownerEmail);

    // 2. Revenue Trend
    List<RevenueTrendResponse> getRevenueTrend(String ownerEmail,
                                                      String dateRange);

    // 3. Order Status Pie
    List<StatusCountResponse> getOrderStatusSummary(String ownerEmail);

    // 4. Payment Methods
    List<PaymentMethodResponse> getPaymentMethods(String ownerEmail);

    // 5. Top Products
    List<TopProductResponse> getTopProducts(String ownerId);
    // 6. Orders Table
    List<OwnerOrderSummaryResponse> getOrders(String ownerEmail);


    // 7. Export CSV
    String exportOrdersToCsv(String ownerEmail);

    StockReportResponse getStockReport(String ownerEmail);

    OrderStatsResponse getOrderStats(String ownerEmail);

    List<OrderChartResponse> getOrdersChart(String ownerEmail, int i);

    SubscriptionDashboardResponse getSubscriptionDashboard(String ownerEmail);
//    void deleteOwnerAccount(String ownerEmail);
    SalesStatsResponse getSalesStats(String ownerEmail);

    void requestAccountDeletion(String ownerEmail) throws IOException;

    void confirmAccountDeletion(String ownerEmail,
                                DeleteAccountConfirmRequest req);

    List<StockTrendResponse> getStockTrend(
            String ownerEmail,
            String range
    );
    List<CategoryInventoryResponse> getCategoryInventory(
            String ownerEmail
    );
    List<SalesStockResponse> getSalesVsStock(
            String ownerEmail,
            String range
    );
}

