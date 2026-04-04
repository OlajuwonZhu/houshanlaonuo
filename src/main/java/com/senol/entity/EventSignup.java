package com.senol.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.senol.entity.User;

@Entity
@Table(name = "event_signups", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventSignup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private GroupEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum Status {
        PENDING,
        APPROVED,
        WAITLISTED,
        CANCELED,
        REJECTED,
        CHECKED_IN
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "signup_at")
    private LocalDateTime signupAt;

    @Column(name = "cancel_at")
    private LocalDateTime cancelAt;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(columnDefinition = "TEXT")
    private String answers; // JSON

    @Column(length = 200)
    private String note;

    @Column(length = 50)
    private String source; // e.g. miniapp

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (signupAt == null) signupAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupEvent getEvent() { return event; }
    public void setEvent(GroupEvent event) { this.event = event; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getSignupAt() { return signupAt; }
    public void setSignupAt(LocalDateTime signupAt) { this.signupAt = signupAt; }

    public LocalDateTime getCancelAt() { return cancelAt; }
    public void setCancelAt(LocalDateTime cancelAt) { this.cancelAt = cancelAt; }

    public LocalDateTime getCheckInAt() { return checkInAt; }
    public void setCheckInAt(LocalDateTime checkInAt) { this.checkInAt = checkInAt; }

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
