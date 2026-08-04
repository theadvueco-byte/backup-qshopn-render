package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByShop(Shop shop);

    List<Subscription> findByStatus(SubscriptionStatus status);

    boolean existsByShop(Shop shop);

    Optional<Subscription> findByShopId(UUID shopId);

    List<Subscription> findAllByShop(Shop shop);

    Optional<Subscription> findFirstByShopOrderByExpiryDateDesc(Shop shop);

    void deleteByShop(Shop shop);


    long countDistinctByStatusIn(List<SubscriptionStatus> statuses);

    // 🔹 Expiring subscriptions
    @Query("""
        select s
        from Subscription s
        where s.status = 'ACTIVE'
        and s.expiryDate <= :date
        order by s.expiryDate
    """)
    List<Subscription> findExpiringSubscriptions(LocalDate date);


    @Query(
            value = """
            SELECT s.plan, COALESCE(SUM(s.total_amount), 0)
            FROM subscription s
            WHERE s.active = true
            GROUP BY s.plan
        """,
            nativeQuery = true
    )
    List<Object[]> fetchRevenueByPlan();

}

