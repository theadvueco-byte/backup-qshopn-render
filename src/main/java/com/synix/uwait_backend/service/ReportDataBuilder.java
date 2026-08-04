package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.monthlyreport.MonthlyReportDTO;
import com.synix.uwait_backend.dto.monthlyreport.OrderReportRowDTO;
import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.model.OrderStatus;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportDataBuilder {

    private final OrderRepository orderRepo;

    public MonthlyReportDTO build(Shop shop) {

        // TEMP: use current month so data appears
        YearMonth lastMonth = YearMonth.now();

        LocalDate start = lastMonth.atDay(1);
        LocalDate end = lastMonth.atEndOfMonth();

        List<Order> orders =
                orderRepo.findByShopAndStatusAndCreatedAtBetween(
                        shop,
                        OrderStatus.COMPLETED,
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                );


        List<OrderReportRowDTO> rows = orders.stream()
                .map(o -> new OrderReportRowDTO(
                        o.getId(),
                        o.getCreatedAt().toLocalDate(),
                        o.getCustomerName(),
                        o.getPaymentMode().name(),
                        o.getStatus().name(),
                        o.getTotalAmount()
                ))
                .toList();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyReportDTO(
                shop.getShopName(),
                shop.getPublicCode(),
                shop.getOwner().getFullName(),
                shop.getOwnerEmail(),
                lastMonth.toString(),
                rows,
                rows.size(),
                totalRevenue
        );
    }
}

