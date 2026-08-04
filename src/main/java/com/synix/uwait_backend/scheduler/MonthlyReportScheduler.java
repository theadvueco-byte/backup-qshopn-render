package com.synix.uwait_backend.scheduler;

import com.synix.uwait_backend.service.MonthlyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {

    private final MonthlyReportService reportService;

    // 1st day of month at 2 AM
    @Scheduled(cron = "0 0 2 1 * ?")
    public void runMonthlyReport() throws IOException {
        reportService.generateAndSendMonthlyReports();
    }
}

