package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.monthlyreport.MonthlyReportDTO;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.SubscriptionFeature;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.service.emailsend.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyReportServiceImpl implements MonthlyReportService {

    private final ShopRepository shopRepo;
    private final ReportDataBuilder dataBuilder;
    private final FeatureGuardService featureGuard;

    private final PdfReportGenerator pdfGenerator;
    private final EmailService emailService;

    private final EmailNotificationService emailNotificationService;

//    @Override
//    public void generateAndSendMonthlyReports() throws IOException {
//
//        List<Shop> shops = shopRepo.findAllByActiveTrue();
//
//        for (Shop shop : shops) {
//
//            MonthlyReportDTO report =
//                    dataBuilder.build(shop);
//
//            byte[] pdf =
//                    pdfGenerator.generate(report);
//
////            emailNotificationService.sendMailWithAttachment(
////                    shop.getOwnerEmail(),
////                    "Monthly Sales Report - " + report.getMonth(),
////                    "Please find attached your monthly sales report.",
////                    pdf,
////                    "Sales_Report_" + report.getMonth() + ".pdf"
////            );
//            emailService.sendTemplateMailWithAttachment(
//                    shop.getOwnerEmail(),
//                    "Monthly Sales Report - " + report.getMonth(),
//                    "Monthly Sales Report",
//                    shop.getShopName(),
//                    "Please find attached your monthly sales report for " + report.getMonth() + ".",
//                    pdf,
//                    "Sales_Report_" + report.getMonth() + ".pdf"
//            );
//
//
//
//        }
//    }
//

    @Override
    public void generateAndSendMonthlyReports() throws IOException {

        List<Shop> shops = shopRepo.findAllByActiveTrue();

        for (Shop shop : shops) {

            // 🔐 FEATURE CHECK
            if (!featureGuard.hasFeature(
                    shop,
                    SubscriptionFeature.MONTHLY_REPORT_EMAIL
            )) {
                continue; // skip this shop silently
            }

            MonthlyReportDTO report = dataBuilder.build(shop);

            byte[] pdf = pdfGenerator.generate(report);

//            emailService.sendTemplateMailWithAttachment(
//                    shop.getOwnerEmail(),
//                    "Monthly Sales Report - " + report.getMonth(),
//                    "Monthly Sales Report",
//                    shop.getShopName(),
//                    "Please find attached your monthly sales report for " +
//                            report.getMonth() + ".",
//                    pdf,
//                    "Sales_Report_" + report.getMonth() + ".pdf"
//            );

            //smtp email service
            emailNotificationService.sendMailWithAttachment(
                    shop.getOwnerEmail(),
                    "Monthly Sales Report - " + report.getMonth(),
                    "Please find attached your monthly sales report.",
                    pdf,
                    "Sales_Report_" + report.getMonth() + ".pdf"
            );
        }
    }


}

