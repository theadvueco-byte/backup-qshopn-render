package com.synix.uwait_backend.dto.ownerstatus;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

public record SalesStockResponse(
        LocalDate date,
        Long stock,
        Long sales
) {}


