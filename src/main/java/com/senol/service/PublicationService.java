package com.senol.service;

import com.senol.entity.Publication;
import com.senol.entity.User;
import com.senol.repository.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PublicationService {
    
    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);
    
    @Autowired
    private PublicationRepository publicationRepository;
    
    @Autowired
    private CloudStorageService cloudStorageService;
    
    public Page<Publication> getPublications(int page, int size, Integer year) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (year != null) {
            return publicationRepository.findByPublishYearAndIsActiveTrueOrderByCreatedAtDesc(year, pageable);
        } else {
            return publicationRepository.findByIsActiveTrueOrderByPublishYearDescCreatedAtDesc(pageable);
        }
    }
    
    public Optional<Publication> getPublicationById(Long id) {
        return publicationRepository.findById(id);
    }
    
    public Publication createPublication(Publication publication, MultipartFile file, User uploadedBy) throws IOException {
        // 验证文件类型
        if (!isPdfFile(file)) {
            throw new IOException("只支持PDF文件格式");
        }
        
        // 如果没有提供期号，自动生成
        if (publication.getIssueNumber() == null || publication.getIssueNumber().trim().isEmpty()) {
            publication.setIssueNumber(generateIssueNumber(publication.getPublishYear()));
        }
        
        // 上传文件到COS，按照目录结构：publications/{year}/issue-{issue}.pdf
        String fileUrl = cloudStorageService.uploadPublication(file, 
            publication.getPublishYear(), 
            Integer.parseInt(publication.getIssueNumber()));
        
        publication.setFileName(file.getOriginalFilename());
        publication.setFilePath(fileUrl);
        publication.setFileSize(file.getSize());
        publication.setUploadedBy(uploadedBy);
        publication.setIsActive(true);
        publication.setDownloadCount(0);
        
        return publicationRepository.save(publication);
    }
    
    public Publication updatePublication(Publication publication) {
        return publicationRepository.save(publication);
    }
    
    public void deletePublication(Long id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会刊不存在"));
        
        // 从COS删除文件
        if (publication.getFilePath() != null) {
            try {
                cloudStorageService.deleteFile(publication.getFilePath());
            } catch (Exception e) {
                log.warn("删除COS文件失败: {}", e.getMessage());
            }
        }
        
        // 软删除
        publication.setIsActive(false);
        publicationRepository.save(publication);
    }
    
    public Long getActivePublicationCount() {
        return publicationRepository.countByIsActiveTrue();
    }
    
    public void incrementDownloadCount(Long id) {
        Optional<Publication> publication = publicationRepository.findById(id);
        if (publication.isPresent()) {
            Publication pub = publication.get();
            pub.setDownloadCount(pub.getDownloadCount() + 1);
            publicationRepository.save(pub);
        }
    }
    
    public List<Integer> getPublishYears() {
        return publicationRepository.findDistinctPublishYears();
    }
    
    /**
     * 验证是否为PDF文件
     */
    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }
    
    /**
     * 生成期号
     */
    private String generateIssueNumber(Integer year) {
        // 查找该年份已有的最大期号
        List<Publication> yearPublications = publicationRepository.findByPublishYearAndIsActiveTrueOrderByIssueNumberDesc(year);
        
        if (yearPublications.isEmpty()) {
            return "1";
        }
        
        try {
            String lastIssue = yearPublications.get(0).getIssueNumber();
            int nextIssue = Integer.parseInt(lastIssue) + 1;
            return String.valueOf(nextIssue);
        } catch (NumberFormatException e) {
            return "1";
        }
    }
}
