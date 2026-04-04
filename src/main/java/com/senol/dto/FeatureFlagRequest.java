package com.senol.dto;

public class FeatureFlagRequest {
    private Boolean enabled;
    private String description;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
