package com.synix.uwait_backend.dto.ownerstatus;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


public record StockTrendResponse(
        LocalDate date,
        Long stock
) {}


