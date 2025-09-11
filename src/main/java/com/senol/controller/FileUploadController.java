package com.senol.controller;

import com.senol.entity.User;
import com.senol.service.CloudStorageService;
import com.senol.service.LocalStorageService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    @Autowired
    private CloudStorageService cloudStorageService;
    
    @Autowired
    private LocalStorageService localStorageService;
    
    @Autowired
    private UserService userService;
    
    @Value("${file.upload.image-path}")
    private String imageUploadPath;
    
    @Value("${file.upload.pdf-path}")
    private String pdfUploadPath;
    
    @Value("${cos.secret-id}")
    private String cosSecretId;
    
    @PostMapping("/image")
    public ResponseUtil<String> uploadImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("只支持图片文件");
            }
            
            // 验证文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseUtil.error("图片大小不能超过10MB");
            }
            
            // 如果配置了COS，使用云存储，否则使用本地存储
            if (!"your-secret-id".equals(cosSecretId)) {
                String fileUrl = cloudStorageService.uploadTempFile(file);
                return ResponseUtil.success(fileUrl);
            } else {
                String fileUrl = localStorageService.uploadTempFile(file);
                return ResponseUtil.success(fileUrl);
            }
            
        } catch (IOException e) {
            return ResponseUtil.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/avatar")
    public ResponseUtil<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                           Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("只支持图片文件");
            }
            
            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseUtil.error("头像大小不能超过5MB");
            }
            
            // 获取当前用户ID
            String openId = authentication.getName();
            // 通过openId查询用户信息获取数字ID
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            Long userId = user.getId();
            
            String fileUrl;
            // 使用云存储上传头像
            if (cosSecretId != null && !cosSecretId.isEmpty() && !"your-secret-id".equals(cosSecretId)) {
                fileUrl = cloudStorageService.uploadUserAvatar(file, userId);
            } else {
                // 使用本地存储
                fileUrl = localStorageService.uploadUserAvatar(file, userId);
            }

            return ResponseUtil.success(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.error("头像上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/team-image")
    public ResponseUtil<String> uploadTeamImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("teamType") String teamType,
                                              @RequestParam("teamId") Long teamId,
                                              Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("只支持图片文件");
            }
            
            // 验证文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseUtil.error("图片大小不能超过10MB");
            }
            
            // 使用云存储上传团队图片
            if (!"your-secret-id".equals(cosSecretId)) {
                String fileUrl = cloudStorageService.uploadTeamImage(file, teamType, teamId);
                return ResponseUtil.success(fileUrl);
            } else {
                String fileUrl = localStorageService.uploadTeamImage(file, teamType, teamId);
                return ResponseUtil.success(fileUrl);
            }
            
        } catch (IOException e) {
            return ResponseUtil.error("团队图片上传失败: " + e.getMessage());
        }
    }
    
    private ResponseUtil<String> uploadImageLocal(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(imageUploadPath);
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
        
        // 返回文件访问URL
        String fileUrl = "/api/upload/images/" + fileName;
        return ResponseUtil.success(fileUrl);
    }
    
    @PostMapping("/pdf")
    public ResponseUtil<String> uploadPDF(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (!"application/pdf".equals(contentType)) {
                return ResponseUtil.error("只支持PDF文件");
            }
            
            // 验证文件大小（50MB）
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseUtil.error("文件大小不能超过50MB");
            }
            
            // 如果配置了COS，使用云存储，否则使用本地存储
            if (!"your-secret-id".equals(cosSecretId)) {
                String fileUrl = cloudStorageService.uploadTempFile(file);
                return ResponseUtil.success(fileUrl);
            } else {
                String fileUrl = localStorageService.uploadTempFile(file);
                return ResponseUtil.success(fileUrl);
            }
            
        } catch (IOException e) {
            return ResponseUtil.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/publication")
    public ResponseUtil<String> uploadPublication(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("year") Integer year,
                                                 @RequestParam("issue") Integer issue,
                                                 Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (!"application/pdf".equals(contentType)) {
                return ResponseUtil.error("只支持PDF文件");
            }
            
            // 验证文件大小（50MB）
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseUtil.error("文件大小不能超过50MB");
            }
            
            // 使用云存储上传会刊
            if (!"your-secret-id".equals(cosSecretId)) {
                String fileUrl = cloudStorageService.uploadPublication(file, year, issue);
                return ResponseUtil.success(fileUrl);
            } else {
                String fileUrl = localStorageService.uploadPublication(file, year, issue);
                return ResponseUtil.success(fileUrl);
            }
            
        } catch (IOException e) {
            return ResponseUtil.error("会刊上传失败: " + e.getMessage());
        }
    }
    
    private ResponseUtil<String> uploadPDFLocal(MultipartFile file) throws IOException {
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
        
        return ResponseUtil.success(fileName);
    }
    
    @GetMapping("/images/{fileName}")
    public void getImage(@PathVariable String fileName, 
                        jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        Path filePath = Paths.get(imageUploadPath, fileName);
        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }
        
        String contentType = Files.probeContentType(filePath);
        response.setContentType(contentType != null ? contentType : "image/jpeg");
        Files.copy(filePath, response.getOutputStream());
    }
    
    @GetMapping("/publications/{fileName}")
    public void getPDF(@PathVariable String fileName, 
                      jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        Path filePath = Paths.get(pdfUploadPath, fileName);
        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        Files.copy(filePath, response.getOutputStream());
    }

    @PostMapping("/freshmen-image")
    public ResponseUtil<String> uploadFreshmenImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseUtil.error("文件大小不能超过5MB");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("只支持图片文件");
            }
            
            String fileUrl;
            if (!"your-secret-id".equals(cosSecretId)) {
                // 使用云存储
                fileUrl = cloudStorageService.uploadFreshmenImage(file);
            } else {
                // 使用本地存储
                fileUrl = localStorageService.uploadFreshmenImage(file);
            }
            
            return ResponseUtil.success(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.error("新生队图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/summer-image")
    public ResponseUtil<String> uploadSummerImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseUtil.error("文件大小不能超过5MB");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("只支持图片文件");
            }
            
            String fileUrl;
            if (!"your-secret-id".equals(cosSecretId)) {
                // 使用云存储
                fileUrl = cloudStorageService.uploadSummerImage(file);
            } else {
                // 使用本地存储
                fileUrl = localStorageService.uploadSummerImage(file);
            }
            
            return ResponseUtil.success(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.error("暑期队图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 通用分类图片上传接口
     * @param file 上传的文件
     * @param category 分类 (teams, community, history, departments 等)
     * @param subCategory 子分类 (freshmen, summer, secretariat, climbing-team, posts 等)
     * @param authentication 认证信息
     * @return 上传结果
     */
    @PostMapping("/categorized-image")
    public ResponseUtil<String> uploadCategorizedImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("subCategory") String subCategory,
            Authentication authentication) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseUtil.error("文件不能为空");
            }
            
            // 验证文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseUtil.error("文件大小不能超过10MB");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("只支持图片文件");
            }
            
            String fileUrl;
            if (!"your-secret-id".equals(cosSecretId)) {
                // 使用云存储
                fileUrl = cloudStorageService.uploadCategorizedImage(file, category, subCategory);
            } else {
                // 使用本地存储
                fileUrl = localStorageService.uploadCategorizedImage(file, category, subCategory);
            }
            
            return ResponseUtil.success(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.error(String.format("%s图片上传失败: %s", subCategory, e.getMessage()));
        }
    }
}
