package com.senol.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senol.util.FlexibleLocalDateTimeDeserializer;

public class GroupEventRequest {
    private String title;
    private String description;
    private String location;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime signupStartTime;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime signupEndTime;
    private Integer capacity; // null or 0 => unlimited
    private Boolean waitlistEnabled;
    private Boolean requireApproval;
    private String coverImageUrl;
    private String externalLink;
    private String formSchema; // JSON string

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
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getExternalLink() { return externalLink; }
    public void setExternalLink(String externalLink) { this.externalLink = externalLink; }
    public String getFormSchema() { return formSchema; }
    public void setFormSchema(String formSchema) { this.formSchema = formSchema; }
}
