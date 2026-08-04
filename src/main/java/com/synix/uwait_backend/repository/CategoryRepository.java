package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Category;
import com.synix.uwait_backend.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByShopId(UUID shopId);



    long countByShopId(UUID id);

    long countByShop(Shop shop);

}

