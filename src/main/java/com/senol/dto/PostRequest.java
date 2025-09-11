package com.senol.dto;

import java.util.List;

public class PostRequest {
    private String content;
    private List<String> imageUrls;
    
    public PostRequest() {}
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
