package com.senol.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "climbing_team")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClimbingTeam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(name = "team_leader", length = 100)
    private String teamLeader; // 队长
    
    @Column(name = "vice_leader", length = 100)
    private String viceLeader; // 副队长
    
    @Column(name = "members", columnDefinition = "TEXT")
    private String members; // 成员列表，JSON格式存储
    
    @Column(name = "climbing_routes", columnDefinition = "TEXT")
    private String climbingRoutes; // 攀登路线
    
    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements; // 主要成就
    
    @Column(name = "training_activities", columnDefinition = "TEXT")
    private String trainingActivities; // 训练活动
    
    @Column(name = "competitions", columnDefinition = "TEXT")
    private String competitions; // 参加比赛
    
    @Column(name = "equipment", columnDefinition = "TEXT")
    private String equipment; // 装备情况
    
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
