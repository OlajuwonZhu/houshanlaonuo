package com.senol.service;

import com.senol.entity.Publication;
import com.senol.entity.User;
import com.senol.repository.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PublicationService {
    
    @Autowired
    private PublicationRepository publicationRepository;
    
    @Value("${file.upload.pdf-path}")
    private String pdfUploadPath;
    
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
        // 保存文件
        String fileName = saveFile(file);
        
        publication.setFileName(file.getOriginalFilename());
        publication.setFilePath(fileName);
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
        Optional<Publication> publication = publicationRepository.findById(id);
        if (publication.isPresent()) {
            Publication pub = publication.get();
            pub.setIsActive(false);
            publicationRepository.save(pub);
        }
    }
    
    public void incrementDownloadCount(Long id) {
        Optional<Publication> publication = publicationRepository.findById(id);
        if (publication.isPresent()) {
            Publication pub = publication.get();
            pub.setDownloadCount(pub.getDownloadCount() + 1);
            publicationRepository.save(pub);
        }
    }
    
    public Long getActivePublicationCount() {
        return publicationRepository.countActivePublications();
    }
    
    public List<Integer> getPublishYears() {
        return publicationRepository.findDistinctPublishYears();
    }
    
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(pdfUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 生成唯一文件名
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
}
