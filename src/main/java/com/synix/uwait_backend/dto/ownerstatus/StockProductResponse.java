package com.synix.uwait_backend.dto.ownerstatus;

import java.math.BigDecimal;

public record StockProductResponse(
        String productName,
        BigDecimal sellingPrice,
        int stock,
        String status
) {}

