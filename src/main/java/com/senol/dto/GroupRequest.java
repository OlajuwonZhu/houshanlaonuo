package com.senol.dto;

public class GroupRequest {
    private String name;
    private String slug;
    private String city;
    private String description;
    private String coverImageUrl;
    private String joinPolicy; // OPEN, APPROVAL_REQUIRED, CLOSED

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getJoinPolicy() { return joinPolicy; }
    public void setJoinPolicy(String joinPolicy) { this.joinPolicy = joinPolicy; }
}
