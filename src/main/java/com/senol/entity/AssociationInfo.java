package com.senol.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "association_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociationInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "info_type", nullable = false)
    private InfoType infoType;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum InfoType {
        FLAG("会旗"),
        EMBLEM("会徽"),
        SONG("会歌"),
        FOUNDATION("山诺基金会");
        
        private final String description;
        
        InfoType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
