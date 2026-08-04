package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.dto.ownerorder.OwnerOrderSummaryResponse;
import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.model.OrderStatus;
import com.synix.uwait_backend.model.PaymentMode;
import com.synix.uwait_backend.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByShopIdOrderByCreatedAtDesc(UUID shopId);

    long countByShop(Shop shop);

    long countByShopAndPaymentMode(Shop shop, PaymentMode mode);

    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.shop = :shop")
    Double sumTotalAmount(@Param("shop") Shop shop);

  //  long countCurrentMonthOrders(Long id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND MONTH(o.createdAt) = MONTH(CURRENT_DATE) AND YEAR(o.createdAt) = YEAR(CURRENT_DATE)")
    long countCurrentMonthOrders(@Param("shopId") UUID shopId);


    Optional<Order> findByIdAndShop(Long id, Shop shop);

    @Query("""
SELECT new com.synix.uwait_backend.dto.ownerorder.OwnerOrderSummaryResponse(
    o.id,
    o.customerName,
    o.customerPhone,
    o.paymentMode,
    o.status,
    o.totalAmount,
    o.createdAt
)
FROM Order o
WHERE o.shop.id = :shopId
ORDER BY o.createdAt DESC
""")
    List<OwnerOrderSummaryResponse> findOwnerOrders(@Param("shopId") UUID shopId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal sumCompletedOrdersAmount();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' AND MONTH(o.createdAt) = :month")
    BigDecimal sumCompletedOrdersAmountByMonth(int month);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'PENDING'")
    BigDecimal sumPendingPayments();


    @Query("""
    SELECT DATE(o.createdAt), SUM(o.totalAmount)
    FROM Order o
    WHERE o.shop = :shop
      AND o.status = 'COMPLETED'
      AND o.createdAt BETWEEN :start AND :end
    GROUP BY DATE(o.createdAt)
    ORDER BY DATE(o.createdAt)
""")
    List<Object[]> findRevenueTrend(
            @Param("shop") Shop shop,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT o.status, COUNT(o)
    FROM Order o
    WHERE o.shop = :shop
    GROUP BY o.status
""")
    List<Object[]> countOrdersByStatus(@Param("shop") Shop shop);

    @Query("""
    SELECT o.paymentMode, COUNT(o)
    FROM Order o
    WHERE o.shop = :shop
    GROUP BY o.paymentMode
""")
    List<Object[]> countPaymentMethods(@Param("shop") Shop shop);

    List<Order> findByShopOrderByCreatedAtDesc(Shop shop);

    long countByShopAndStatus(Shop shop, OrderStatus status);

    long countByShopAndStatusAndCreatedAtBetween(
            Shop shop,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end
    );


    @Query("""
SELECT DATE(o.createdAt), COUNT(o)
FROM Order o
WHERE o.shop = :shop
AND o.status = 'COMPLETED'
AND o.createdAt BETWEEN :start AND :end
GROUP BY DATE(o.createdAt)
ORDER BY DATE(o.createdAt)
""")
    List<Object[]> findDailyOrders(
            @Param("shop") Shop shop,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



    @Query("""
SELECT COALESCE(SUM(o.totalAmount), 0)
FROM Order o
WHERE o.shop = :shop
AND o.status = 'COMPLETED'
""")
    BigDecimal sumTotalSales(@Param("shop") Shop shop);


    @Query("""
SELECT COALESCE(SUM(o.totalAmount), 0)
FROM Order o
WHERE o.shop = :shop
AND o.status = 'COMPLETED'
AND o.createdAt BETWEEN :start AND :end
""")
    BigDecimal sumSalesBetween(
            @Param("shop") Shop shop,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.shop = :shop
      AND o.status = 'COMPLETED'
""")
    BigDecimal sumCompletedOrdersAmount(@Param("shop") Shop shop);

    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.shop = :shop
      AND o.status = 'COMPLETED'
      AND MONTH(o.createdAt) = :month
""")
    BigDecimal sumCompletedOrdersAmountByMonth(
            @Param("shop") Shop shop,
            @Param("month") int month
    );

    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.shop = :shop
      AND o.status = 'PENDING'
""")
    BigDecimal sumPendingPayments(@Param("shop") Shop shop);


    List<Order> findByShopAndCreatedAtBetween(Shop shop, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    List<Order> findByShopAndStatusAndCreatedAtBetween(Shop shop, OrderStatus orderStatus, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    @Query("""
        select o from Order o
        join fetch o.shop s
        join fetch s.owner
        order by o.createdAt desc
    """)
    List<Order> findAllForAdmin();

    // 🔹 Row 2 – Order summary
    long countByStatus(OrderStatus status);

    long countByStatusNot(OrderStatus status);


    // 🔹 Today orders
    long countByCreatedAtAfter(LocalDateTime start);

    // 🔹 Today + payment mode
    @Query("""
        select o.paymentMode, count(o)
        from Order o
        where o.createdAt >= :start
        group by o.paymentMode
    """)
    List<Object[]> countTodayOrdersByPaymentMode(LocalDateTime start);

    // 🔹 Today + shop
    @Query("""
        select o.shop.publicCode, count(o)
        from Order o
        where o.createdAt >= :start
        group by o.shop.publicCode
    """)
    List<Object[]> countTodayOrdersByShop(LocalDateTime start);

    // 🔹 Non-accountable orders
    long countByStatusIn(List<OrderStatus> statuses);

    // 🔹 Admin list view (avoid N+1)
//    @Query("""
//        select o
//        from Order o
//        join fetch o.shop s
//        join fetch s.owner
//        order by o.createdAt desc
//    """)
//    List<Order> findAllForAdmin();

    // 🔹 Graph: orders + revenue grouped by date
    @Query(
            value = """
        SELECT CAST(o.created_at AS DATE) AS day,
               COUNT(o.id) AS orders,
               COALESCE(SUM(o.total_amount), 0) AS revenue
        FROM orders o
        JOIN shops s ON o.shop_id = s.id
        JOIN subscription sub ON sub.shop_id = s.id
        WHERE o.created_at >= :start
          AND o.status = 'COMPLETED'
          AND sub.plan = 'FREE_TRIAL'
        GROUP BY CAST(o.created_at AS DATE)
        ORDER BY CAST(o.created_at AS DATE)
    """,
            nativeQuery = true
    )
    List<Object[]> freeTierOrdersAndRevenue(
            @Param("start") LocalDateTime start
    );

    @Query(
            value = """
        SELECT CAST(o.created_at AS DATE) AS day,
               COUNT(o.id) AS orders,
               COALESCE(SUM(o.total_amount), 0) AS revenue
        FROM orders o
        JOIN shops s ON o.shop_id = s.id
        JOIN subscription sub ON sub.shop_id = s.id
        WHERE o.created_at >= :start
          AND o.status = 'COMPLETED'
          AND sub.plan <> 'FREE_TRIAL'
        GROUP BY CAST(o.created_at AS DATE)
        ORDER BY CAST(o.created_at AS DATE)
    """,
            nativeQuery = true
    )
    List<Object[]> premiumTierOrdersAndRevenue(
            @Param("start") LocalDateTime start
    );





    long countByStatusAndShop(OrderStatus orderStatus, Shop shop);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.shop = :shop
          AND o.status = com.synix.uwait_backend.model.OrderStatus.COMPLETED
    """)
    BigDecimal sumRevenueByShop(@Param("shop") Shop shop);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.shop = :shop
          AND o.status = :status
    """)
    BigDecimal sumRevenueByShopAndStatus(
            @Param("shop") Shop shop,
            @Param("status") OrderStatus status
    );
    // ---------- GLOBAL DASHBOARD ----------

    @Query(
            value = """
        SELECT DATE(o.created_at) AS day,
               COUNT(*) AS totalOrders,

               SUM(
                   CASE
                       WHEN sub.plan = 'FREE_TRIAL' 
                       THEN 1 
                       ELSE 0 
                   END
               ) AS freePlanOrders,

               SUM(
                   CASE 
                       WHEN sub.plan != 'FREE_TRIAL' 
                       THEN 1 
                       ELSE 0 
                   END
               ) AS paidPlanOrders

        FROM orders o
        JOIN shops s ON o.shop_id = s.id
        JOIN subscription sub ON sub.shop_id = s.id

        WHERE o.created_at >= :startDate
        GROUP BY DATE(o.created_at)
        ORDER BY DATE(o.created_at)
    """,
            nativeQuery = true
    )
    List<Object[]> fetchOrdersTrend(@Param("startDate") LocalDateTime startDate);

    @Query("""
        SELECT o.paymentMode, COUNT(o)
        FROM Order o
        GROUP BY o.paymentMode
    """)
    List<Object[]> fetchPaymentModeSplit();

    // ---------- SHOP LEVEL ----------

    @Query("""
        SELECT DATE(o.createdAt),
               COUNT(o),
               SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END),
               SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END)
        FROM Order o
        WHERE o.shop.id = :shopId
          AND o.createdAt >= :startDate
        GROUP BY DATE(o.createdAt)
        ORDER BY DATE(o.createdAt)
    """)
    List<Object[]> fetchShopOrderTrend(UUID shopId, LocalDateTime startDate);

    @Query("""
        SELECT o.paymentMode, COUNT(o)
        FROM Order o
        WHERE o.shop.id = :shopId
        GROUP BY o.paymentMode
    """)
    List<Object[]> fetchShopPaymentSplit(UUID shopId);

    @Query("""
        SELECT DATE(o.createdAt),
               COUNT(o),
               SUM(o.totalAmount)
        FROM Order o
        WHERE o.shop.id = :shopId
          AND o.status = 'COMPLETED'
        GROUP BY DATE(o.createdAt)
        ORDER BY DATE(o.createdAt)
    """)
    List<Object[]> fetchOrdersVsRevenue(UUID shopId);

    Long countByShopAndStatusInAndCreatedAtBetween(Shop shop, List<OrderStatus> completed, LocalDateTime monthStart, LocalDateTime now);
}

