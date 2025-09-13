package com.senol.controller;

import com.senol.entity.Publication;
import com.senol.entity.User;
import com.senol.service.PublicationService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/publications")
public class PublicationController {
    
    @Autowired
    private PublicationService publicationService;
    
    @Autowired
    private UserService userService;
    
    
    @GetMapping
    public ResponseUtil<Page<Publication>> getPublications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String year) {
        
        Integer yearParam = null;
        if (year != null && !year.equals("null") && !year.trim().isEmpty()) {
            try {
                yearParam = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                return ResponseUtil.error("年份参数格式错误");
            }
        }
        
        Page<Publication> publications = publicationService.getPublications(page, size, yearParam);
        return ResponseUtil.success(publications);
    }
    
    @GetMapping("/{id}")
    public ResponseUtil<Publication> getPublication(@PathVariable Long id) {
        Optional<Publication> publication = publicationService.getPublicationById(id);
        if (publication.isPresent()) {
            return ResponseUtil.success(publication.get());
        } else {
            return ResponseUtil.error("会刊不存在");
        }
    }
    
    @PostMapping
    public ResponseUtil<Publication> createPublication(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("publishYear") Integer publishYear,
            @RequestParam(value = "issueNumber", required = false) String issueNumber,
            Authentication authentication) {
        
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            
            Publication publication = new Publication();
            publication.setTitle(title);
            publication.setDescription(description);
            publication.setPublishYear(publishYear);
            publication.setIssueNumber(issueNumber);
            
            Publication savedPublication = publicationService.createPublication(publication, file, user);
            return ResponseUtil.success(savedPublication);
            
        } catch (IOException e) {
            return ResponseUtil.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseUtil<Publication> updatePublication(
            @PathVariable Long id,
            @RequestBody Publication publicationInfo,
            Authentication authentication) {
        
        Optional<Publication> existingPublication = publicationService.getPublicationById(id);
        if (!existingPublication.isPresent()) {
            return ResponseUtil.error("会刊不存在");
        }
        
        Publication publication = existingPublication.get();
        publication.setTitle(publicationInfo.getTitle());
        publication.setDescription(publicationInfo.getDescription());
        publication.setPublishYear(publicationInfo.getPublishYear());
        publication.setIssueNumber(publicationInfo.getIssueNumber());
        
        Publication updatedPublication = publicationService.updatePublication(publication);
        return ResponseUtil.success(updatedPublication);
    }
    
    @DeleteMapping("/{id}")
    public ResponseUtil<String> deletePublication(@PathVariable Long id, Authentication authentication) {
        publicationService.deletePublication(id);
        return ResponseUtil.success("删除成功");
    }
    
    @GetMapping("/download/{id}")
    public ResponseUtil<Map<String, String>> getDownloadUrl(@PathVariable Long id) {
        Optional<Publication> publication = publicationService.getPublicationById(id);
        if (!publication.isPresent()) {
            return ResponseUtil.error("会刊不存在");
        }
        
        Publication pub = publication.get();
        publicationService.incrementDownloadCount(id);
        
        // 直接返回COS文件URL
        return ResponseUtil.success(Map.of("downloadUrl", pub.getFilePath()));
    }
    
    @GetMapping("/years")
    public ResponseUtil<List<Integer>> getPublishYears() {
        List<Integer> years = publicationService.getPublishYears();
        return ResponseUtil.success(years);
    }
}
