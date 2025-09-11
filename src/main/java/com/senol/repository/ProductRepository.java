package com.senol.repository;

import com.senol.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // 获取活跃商品，按创建时间倒序
    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // 根据分类获取商品
    Page<Product> findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(Long categoryId, Pageable pageable);
    
    // 根据名称模糊搜索商品
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String name, Pageable pageable);
    
    // 更新销量
    @Modifying
    @Query("UPDATE Product p SET p.salesCount = p.salesCount + :increment WHERE p.id = :productId")
    void updateSalesCount(@Param("productId") Long productId, @Param("increment") int increment);
    
    // 更新库存
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
