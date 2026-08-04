package com.synix.uwait_backend.service;

import java.io.IOException;

public interface MonthlyReportService {
    void generateAndSendMonthlyReports() throws IOException;
}

