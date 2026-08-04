package com.synix.uwait_backend.dto.ownerstatus;

import lombok.AllArgsConstructor;
import lombok.Data;

public record CategoryInventoryResponse(
        String categoryName,
        Long totalProducts,
        Long totalStock
) {}


