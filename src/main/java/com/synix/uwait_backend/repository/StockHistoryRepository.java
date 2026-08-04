package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockHistoryRepository
        extends JpaRepository<StockHistory, Long> {

    Optional<StockHistory> findByShopAndDate(Shop shop, LocalDate date);

    @Query("""
        SELECT sh.date, sh.stock
        FROM StockHistory sh
        WHERE sh.shop = :shop
          AND sh.date BETWEEN :start AND :end
        ORDER BY sh.date
    """)
    List<Object[]> findStockTrend(
            @Param("shop") Shop shop,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}

