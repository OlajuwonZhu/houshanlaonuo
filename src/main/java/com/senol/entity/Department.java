package com.senol.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName; // 部门名称
    
    @Column(name = "department_code", unique = true, length = 50, nullable = true)
    private String departmentCode; // 部门代码 (office, wildlife, environment, training, media, external)
    
    @Column(name = "year")
    private Integer year; // 年份
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 部门描述
    
    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities; // 主要职责
    
    @Column(name = "current_head", length = 100)
    private String currentHead; // 现任部长
    
    @Column(name = "members", columnDefinition = "TEXT")
    private String members; // 成员列表，JSON格式存储
    
    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo; // 联系方式
    
    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements; // 主要成就
    
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls; // 图片链接，JSON格式存储
    
    @Column(name = "display_order")
    private Integer displayOrder; // 显示顺序
    
    @Column(name = "is_active")
    private Boolean isActive = true; // 是否激活
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
