package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Product;
import com.synix.uwait_backend.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(UUID shopId);
    List<Product> findByShopAndActiveTrue(Shop shop);
    List<Product> findByShopAndCategoryIdAndActiveTrue(
            Shop shop,
            Long categoryId
    );
    long countByShopId(UUID shopId);
    Product findByIdAndShopId(Long id, UUID shopId);

    Product findByNameAndShop(String productName, Shop shop);

    boolean existsByCategoryId(Long id);

    boolean deleteByIdAndShopId(Long productId, UUID shopId);

    List<Product> findByCategory_Id(Long id);

    List<Product> findByShopIdAndActiveTrue(UUID id);

    List<Product> findByShopAndCategoryIdAndActiveTrueAndStockGreaterThan(Shop shop, Long categoryId, int i);

    List<Product> findByShopAndActiveTrueAndStockGreaterThan(Shop shop, int i);


    List<Product> findByShop(Shop shop);

    @Query("""
SELECT DATE(p.createdAt), SUM(p.stock)
FROM Product p
WHERE p.shop = :shop
  AND p.createdAt BETWEEN :start AND :end
GROUP BY DATE(p.createdAt)
ORDER BY DATE(p.createdAt)
""")
    List<Object[]> findStockTrend(
            @Param("shop") Shop shop,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
SELECT c.name, SUM(p.stock)
FROM Product p
JOIN p.category c
WHERE p.shop = :shop
GROUP BY c.id, c.name
ORDER BY c.name
""")
    List<Object[]> findCategoryStock(@Param("shop") Shop shop);

    @Query("""
SELECT SUM(p.stock)
FROM Product p
WHERE p.shop = :shop
""")
    Long findTotalStock(@Param("shop") Shop shop);


    @Query("""
SELECT c.name,
       COUNT(p.id),
       SUM(p.stock)
FROM Product p
JOIN p.category c
WHERE p.shop = :shop
GROUP BY c.name
""")
    List<Object[]> findCategoryInventory(@Param("shop") Shop shop);
}

