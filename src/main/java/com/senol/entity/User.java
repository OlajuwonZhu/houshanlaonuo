package com.senol.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String openId;
    
    @Column(unique = true)
    private String unionId;
    
    @Column(length = 50)
    private String name;
    
    @Column(name = "mountain_name", length = 50)
    private String mountainName;
    
    @Column(name = "enrollment_year")
    private Integer enrollmentYear;
    
    @Column(name = "class_name", length = 100)
    private String className;
    
    @Column(name = "current_job", length = 200)
    private String currentJob;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(length = 100)
    private String email;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
