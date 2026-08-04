package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.dto.ownerearnings.TopProductResponse;
import com.synix.uwait_backend.model.OrderItem;
import com.synix.uwait_backend.model.OrderStatus;
import com.synix.uwait_backend.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProduct_Id(Long productId); // ✅ use _ to traverse nested entity


    List<OrderItem> findByOrderShopAndOrderStatus(Shop shop, OrderStatus orderStatus);

    @Query("""
    SELECT DATE(o.createdAt), SUM(oi.quantity)
    FROM OrderItem oi
    JOIN oi.order o
    WHERE o.shop = :shop
      AND o.status = 'COMPLETED'
      AND o.createdAt BETWEEN :start AND :end
    GROUP BY DATE(o.createdAt)
""")
    List<Object[]> findDailySales(
            @Param("shop") Shop shop,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}



