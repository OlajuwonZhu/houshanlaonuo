package com.senol.controller;

import com.senol.entity.Product;
import com.senol.entity.ProductCategory;
import com.senol.service.ProductService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 获取商品分类
    @GetMapping("/categories")
    public ResponseUtil<List<ProductCategory>> getCategories() {
        List<ProductCategory> categories = productService.getAllCategories();
        return ResponseUtil.success(categories);
    }

    // 获取商品列表
    @GetMapping
    public ResponseUtil<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        try {
            Page<Product> products = productService.getProducts(page, size, categoryId, keyword);
            return ResponseUtil.success(products);
        } catch (Exception e) {
            return ResponseUtil.error("获取商品列表失败: " + e.getMessage());
        }
    }

    // 根据ID获取商品详情
    @GetMapping("/{id}")
    public ResponseUtil<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseUtil.error("商品不存在");
        }
        return ResponseUtil.success(product);
    }

    // 创建商品
    @PostMapping
    public ResponseUtil<Product> createProduct(@RequestBody ProductRequest request) {
        try {
            Product product = productService.createProduct(
                request.getCategoryId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getImageUrls(),
                request.getSpecifications()
            );
            return ResponseUtil.success(product);
        } catch (Exception e) {
            return ResponseUtil.error("创建商品失败: " + e.getMessage());
        }
    }

    // 更新商品
    @PutMapping("/{id}")
    public ResponseUtil<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            Product product = productService.updateProduct(
                id,
                request.getCategoryId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getImageUrls(),
                request.getSpecifications()
            );
            return ResponseUtil.success(product);
        } catch (Exception e) {
            return ResponseUtil.error("更新商品失败: " + e.getMessage());
        }
    }

    // 删除商品
    @DeleteMapping("/{id}")
    public ResponseUtil<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("删除商品失败: " + e.getMessage());
        }
    }

    // 商品请求DTO
    public static class ProductRequest {
        private Long categoryId;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String imageUrls;
        private String specifications;

        public ProductRequest() {}

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public String getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(String imageUrls) {
            this.imageUrls = imageUrls;
        }

        public String getSpecifications() {
            return specifications;
        }

        public void setSpecifications(String specifications) {
            this.specifications = specifications;
        }
    }
}
