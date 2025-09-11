package com.senol.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "secretariat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Secretariat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(name = "secretary_general", length = 100)
    private String secretaryGeneral; // 秘书长
    
    @Column(name = "deputy_secretary", length = 100)
    private String deputySecretary; // 副秘书长
    
    @Column(name = "members", columnDefinition = "TEXT")
    private String members; // 成员列表，JSON格式存储
    
    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements; // 主要成就
    
    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities; // 主要活动
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 详细描述
    
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls; // 图片链接，JSON格式存储
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
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
