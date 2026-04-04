package com.senol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.senol.entity.User;

@Entity
@Table(name = "group_events")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String location;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "signup_start_time")
    private LocalDateTime signupStartTime;

    @Column(name = "signup_end_time")
    private LocalDateTime signupEndTime;

    @Column
    private Integer capacity; // null or 0 => unlimited

    @Column(name = "waitlist_enabled")
    private Boolean waitlistEnabled = true;

    @Column(name = "require_approval")
    private Boolean requireApproval = false;

    public enum Status { DRAFT, PUBLISHED, CANCELED }

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_user_id")
    private User organizer;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "external_link", length = 500)
    private String externalLink;

    @Column(name = "form_schema", columnDefinition = "TEXT")
    private String formSchema;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getSignupStartTime() { return signupStartTime; }
    public void setSignupStartTime(LocalDateTime signupStartTime) { this.signupStartTime = signupStartTime; }

    public LocalDateTime getSignupEndTime() { return signupEndTime; }
    public void setSignupEndTime(LocalDateTime signupEndTime) { this.signupEndTime = signupEndTime; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Boolean getWaitlistEnabled() { return waitlistEnabled; }
    public void setWaitlistEnabled(Boolean waitlistEnabled) { this.waitlistEnabled = waitlistEnabled; }

    public Boolean getRequireApproval() { return requireApproval; }
    public void setRequireApproval(Boolean requireApproval) { this.requireApproval = requireApproval; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getExternalLink() { return externalLink; }
    public void setExternalLink(String externalLink) { this.externalLink = externalLink; }

    public String getFormSchema() { return formSchema; }
    public void setFormSchema(String formSchema) { this.formSchema = formSchema; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
