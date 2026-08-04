package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.ShopStatus;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    // Owner side
    Optional<Shop> findByOwner_Email(String ownerEmail);

    // Customer / Public side (QR / URL)
    Optional<Shop> findByPublicCode(String publicCode);

    // Slug checks (optional if you still use slug)
    boolean existsBySlug(String slug);

    Optional<Shop> findByPublicCodeAndActiveTrue(String code);

    long countByUpiVerifiedTrue();

    Optional<Shop> findByPublicCodeAndOwnerEmail(String publicCode, String ownerEmail);

    long countByActiveTrue();

    Optional<Shop> findByOwner(User owner);

    List<Shop> findAllByOwner(User user);

    void deleteByOwnerEmail(String ownerEmail);

    List<Shop> findAllByActiveTrue();

    // 🔹 Row 1 – Summary cards
    long countByStatus(ShopStatus status);


    long countByActiveFalse();

    long countByStatusAndCreatedAtAfter(
            ShopStatus status,
            LocalDateTime createdAt
    );

    // 🔹 Free shops (no active subscription)
    @Query("""
                select count(s)
                from Shop s
                where s.id not in (
                    select sub.shop.id
                    from Subscription sub
                    where sub.status in ('ACTIVE','TRIAL')
                )
            """)
    long countFreeShops();

    // 🔹 Verified shops = APPROVED
    default long countVerifiedShops() {
        return countByStatus(ShopStatus.APPROVE);
    }

    // 🔹 Suspended shops
    default long countSuspendedShops() {
        return countByStatus(ShopStatus.SUSPENSE);
    }

    // 🔹 Best sellers (shop-wise aggregation)
    @Query("""
                select s
                from Shop s
                order by (
                    select count(o)
                    from Order o
                    where o.shop = s and o.status = 'COMPLETED'
                ) desc
            """)
    List<Shop> findBestSellerShops();


    long countBySubscription_PlanAndActive(SubscriptionPlan plan, boolean active);

}

