package com.synix.uwait_backend.dto.ownerstatus;

import java.util.List;

public record StockReportResponse(
        long totalProducts,
        long lowStockCount,
        long outOfStockCount,
        List<StockProductResponse> lowStockProducts,
        List<StockProductResponse> outOfStockProducts
) {}
