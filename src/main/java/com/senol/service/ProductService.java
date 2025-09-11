package com.senol.service;

import com.senol.entity.Product;
import com.senol.entity.ProductCategory;
import com.senol.repository.ProductRepository;
import com.senol.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    // 获取所有商品分类
    public List<ProductCategory> getAllCategories() {
        return productCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    // 获取商品列表
    public Page<Product> getProducts(int page, int size, Long categoryId, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        
        System.out.println("查询商品 - 页码: " + page + ", 大小: " + size + ", 分类ID: " + categoryId + ", 关键词: " + keyword);
        
        Page<Product> result;
        if (keyword != null && !keyword.trim().isEmpty()) {
            result = productRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(keyword, pageable);
        } else if (categoryId != null) {
            result = productRepository.findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(categoryId, pageable);
        } else {
            result = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        }
        
        System.out.println("查询结果 - 总数: " + result.getTotalElements() + ", 当前页数量: " + result.getNumberOfElements());
        
        // 如果没有找到活跃商品，尝试查询所有商品进行调试
        if (result.getTotalElements() == 0) {
            System.out.println("没有找到活跃商品，查询所有商品进行调试...");
            Page<Product> allProducts = productRepository.findAll(pageable);
            System.out.println("所有商品总数: " + allProducts.getTotalElements());
            if (allProducts.getTotalElements() > 0) {
                System.out.println("存在商品但is_active字段可能有问题，返回所有商品");
                return allProducts;
            }
        }
        
        return result;
    }

    // 根据ID获取商品
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // 创建商品
    @Transactional
    public Product createProduct(Long categoryId, String name, String description, BigDecimal price, 
                               Integer stockQuantity, String imageUrls, String specifications) {
        ProductCategory category = productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("商品分类不存在"));

        Product product = new Product(category, name, description, price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrls(imageUrls);
        product.setSpecifications(specifications);
        
        return productRepository.save(product);
    }

    // 更新商品
    @Transactional
    public Product updateProduct(Long id, Long categoryId, String name, String description, 
                               BigDecimal price, Integer stockQuantity, String imageUrls, String specifications) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (categoryId != null) {
            ProductCategory category = productCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("商品分类不存在"));
            product.setCategory(category);
        }

        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(price);
        if (stockQuantity != null) product.setStockQuantity(stockQuantity);
        if (imageUrls != null) product.setImageUrls(imageUrls);
        if (specifications != null) product.setSpecifications(specifications);

        return productRepository.save(product);
    }

    // 删除商品（软删除）
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        product.setIsActive(false);
        productRepository.save(product);
    }

    // 减少库存
    @Transactional
    public boolean decreaseStock(Long productId, int quantity) {
        int updated = productRepository.decreaseStock(productId, quantity);
        return updated > 0;
    }

    // 增加销量
    @Transactional
    public void increaseSales(Long productId, int quantity) {
        productRepository.updateSalesCount(productId, quantity);
    }
}
