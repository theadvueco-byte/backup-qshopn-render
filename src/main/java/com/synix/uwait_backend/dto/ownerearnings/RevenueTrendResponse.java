package com.synix.uwait_backend.dto.ownerearnings;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RevenueTrendResponse {
    private LocalDate date;
    private BigDecimal revenue;
}

