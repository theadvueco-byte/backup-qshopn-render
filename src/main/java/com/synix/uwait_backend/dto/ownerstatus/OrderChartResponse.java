package com.synix.uwait_backend.dto.ownerstatus;

import java.time.LocalDate;

public record OrderChartResponse(
        LocalDate date,
        long count
) {}
