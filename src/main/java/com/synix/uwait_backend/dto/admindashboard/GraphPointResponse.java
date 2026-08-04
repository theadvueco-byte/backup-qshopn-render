package com.synix.uwait_backend.dto.admindashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GraphPointResponse(
        LocalDateTime date,
        long orders,
        BigDecimal revenue
) {}

