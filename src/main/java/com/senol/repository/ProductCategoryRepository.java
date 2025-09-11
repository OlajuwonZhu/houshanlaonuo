package com.senol.repository;

import com.senol.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    
    // 获取活跃的商品分类，按排序顺序
    List<ProductCategory> findByIsActiveTrueOrderBySortOrderAsc();
}
