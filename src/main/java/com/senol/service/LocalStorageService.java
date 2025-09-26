package com.senol.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService {

    @Value("${file.upload.path}")
    private String uploadBasePath;

    /**
     * 上传用户头像到本地存储
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        String fileName = generateFileName("avatar", file.getOriginalFilename());
        String relativePath = String.format("avatars/%d/%s", userId, fileName);
        String fullPath = saveFile(file, relativePath);
        
        // 返回访问URL
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 上传团队图片到本地存储
     */
    public String uploadTeamImage(MultipartFile file, String teamType, Long teamId) throws IOException {
        String fileName = generateFileName("team_image", file.getOriginalFilename());
        String relativePath = String.format("teams/%s/%d/images/%s", teamType, teamId, fileName);
        String fullPath = saveFile(file, relativePath);
        
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 上传会刊PDF到本地存储
     */
    public String uploadPublication(MultipartFile file, Integer year, Integer issue) throws IOException {
        String fileName = String.format("issue-%d.pdf", issue);
        String relativePath = String.format("publications/%d/%s", year, fileName);
        String fullPath = saveFile(file, relativePath);
        
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 上传社区帖子图片到本地存储
     */
    public String uploadPostImage(MultipartFile file, Long postId) throws IOException {
        String fileName = generateFileName("post_image", file.getOriginalFilename());
        String relativePath = String.format("community/posts/%d/images/%s", postId, fileName);
        String fullPath = saveFile(file, relativePath);
        
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 上传临时文件到本地存储
     */
    public String uploadTempFile(MultipartFile file) throws IOException {
        String fileName = generateFileName("temp", file.getOriginalFilename());
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String relativePath = String.format("temp/%s/%s", dateStr, fileName);
        String fullPath = saveFile(file, relativePath);
        
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 上传新生队图片到本地存储
     */
    public String uploadFreshmenImage(MultipartFile file) throws IOException {
        return uploadCategorizedImage(file, "teams", "freshmen");
    }

    /**
     * 上传暑期队图片到本地存储
     */
    public String uploadSummerImage(MultipartFile file) throws IOException {
        return uploadCategorizedImage(file, "teams", "summer");
    }

    /**
     * 上传用户头像
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadUserAvatar(MultipartFile file, Long userId) throws IOException {
        String fileName = generateFileName("avatar", file.getOriginalFilename());
        String relativePath = String.format("avatars/users/%d/%s", userId, fileName);
        String fullPath = saveFile(file, relativePath);
        
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 从字节数组上传用户头像
     */
    public String uploadUserAvatarFromBytes(byte[] data, String originalFileName, Long userId) throws IOException {
        String fileName = generateFileName("avatar", originalFileName);
        String relativePath = String.format("avatars/users/%d/%s", userId, fileName);
        String fullPath = saveBytes(data, relativePath);
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 通用分类图片上传方法
     * @param file 上传的文件
     * @param category 分类 (teams, community, history, departments 等)
     * @param subCategory 子分类 (freshmen, summer, secretariat, climbing-team, posts 等)
     * @return 文件访问URL
     */
    public String uploadCategorizedImage(MultipartFile file, String category, String subCategory) throws IOException {
        String fileName = generateFileName(subCategory, file.getOriginalFilename());
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String relativePath;
        
        // 根据分类构建路径
        if ("teams".equals(category)) {
            relativePath = String.format("teams/%s/%s/%s", subCategory, dateStr, fileName);
        } else if ("community".equals(category)) {
            relativePath = String.format("community/%s/%s/%s", subCategory, dateStr, fileName);
        } else if ("avatars".equals(category)) {
            // 头像按日期存储的旧逻辑，保持兼容性
            relativePath = String.format("avatars/%s/%s", dateStr, fileName);
        } else {
            relativePath = String.format("%s/%s/%s", category, dateStr, fileName);
        }
        
        String fullPath = saveFile(file, relativePath);
        return String.format("/api/files/%s", relativePath);
    }

    /**
     * 通用文件保存方法
     */
    private String saveFile(MultipartFile file, String relativePath) throws IOException {
        // 构建完整路径
        Path fullPath = Paths.get(uploadBasePath, relativePath);
        
        log.info("准备保存文件: {} -> {}", file.getOriginalFilename(), fullPath);
        log.info("上传基础路径: {}", uploadBasePath);
        log.info("相对路径: {}", relativePath);
        
        // 创建目录
        Path parentDir = fullPath.getParent();
        if (!Files.exists(parentDir)) {
            log.info("创建目录: {}", parentDir);
            Files.createDirectories(parentDir);
        }
        
        // 检查文件是否已存在，如果存在则删除
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
            log.info("删除已存在的文件: {}", fullPath);
        }
        
        // 保存文件
        try {
            Files.copy(file.getInputStream(), fullPath);
            log.info("文件保存成功: {} -> {}", file.getOriginalFilename(), fullPath);
            
            // 验证文件是否真的保存成功
            if (Files.exists(fullPath)) {
                long fileSize = Files.size(fullPath);
                log.info("文件保存验证成功，大小: {} bytes", fileSize);
            } else {
                log.error("文件保存失败，文件不存在: {}", fullPath);
                throw new IOException("文件保存失败，文件不存在");
            }
        } catch (IOException e) {
            log.error("文件保存异常: {}", e.getMessage(), e);
            throw e;
        }
        
        return fullPath.toString();
    }

    /**
     * 以字节数组形式保存文件
     */
    private String saveBytes(byte[] data, String relativePath) throws IOException {
        Path fullPath = Paths.get(uploadBasePath, relativePath);
        log.info("准备保存二进制文件 -> {} ({} bytes)", fullPath, data != null ? data.length : 0);
        Path parentDir = fullPath.getParent();
        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
        try {
            Files.write(fullPath, data);
            if (Files.exists(fullPath)) {
                long size = Files.size(fullPath);
                log.info("二进制文件保存成功: {} bytes -> {}", size, fullPath);
            } else {
                throw new IOException("文件保存失败，文件不存在");
            }
        } catch (IOException e) {
            log.error("二进制文件保存失败: {}", e.getMessage(), e);
            throw e;
        }
        return fullPath.toString();
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取相对路径
            String relativePath = extractRelativePathFromUrl(fileUrl);
            if (relativePath == null) {
                log.warn("无法从URL中提取路径: {}", fileUrl);
                return false;
            }
            
            Path fullPath = Paths.get(uploadBasePath, relativePath);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("文件删除成功: {}", fullPath);
                return true;
            } else {
                log.warn("文件不存在: {}", fullPath);
                return false;
            }
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String fileUrl) {
        String relativePath = extractRelativePathFromUrl(fileUrl);
        if (relativePath == null) {
            return false;
        }
        
        Path fullPath = Paths.get(uploadBasePath, relativePath);
        return Files.exists(fullPath);
    }

    /**
     * 获取当前日期字符串
     */
    private String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 获取文件完整路径
     */
    public Path getFilePath(String fileUrl) {
        String relativePath = extractRelativePathFromUrl(fileUrl);
        if (relativePath == null) {
            return null;
        }
        
        return Paths.get(uploadBasePath, relativePath);
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String prefix, String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s%s", prefix, timestamp, uuid, extension);
    }

    /**
     * 从URL中提取相对路径
     */
    private String extractRelativePathFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/api/files/")) {
            return null;
        }
        
        try {
            String[] parts = fileUrl.split("/api/files/");
            return parts.length > 1 ? parts[1] : null;
        } catch (Exception e) {
            log.error("提取相对路径失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String fileUrl) {
        try {
            Path filePath = getFilePath(fileUrl);
            if (filePath != null && Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (Exception e) {
            log.error("获取文件大小失败: {}", fileUrl, e);
        }
        return 0;
    }

    /**
     * 获取存储统计信息
     */
    public StorageStats getStorageStats() {
        try {
            Path basePath = Paths.get(uploadBasePath);
            if (!Files.exists(basePath)) {
                return new StorageStats(0, 0);
            }
            
            final long[] totalSize = {0};
            final int[] fileCount = {0};
            
            Files.walk(basePath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        totalSize[0] += Files.size(path);
                        fileCount[0]++;
                    } catch (IOException e) {
                        log.warn("获取文件大小失败: {}", path, e);
                    }
                });
            
            return new StorageStats(fileCount[0], totalSize[0]);
            
        } catch (Exception e) {
            log.error("获取存储统计失败", e);
            return new StorageStats(0, 0);
        }
    }

    /**
     * 存储统计信息
     */
    public static class StorageStats {
        public final int fileCount;
        public final long totalSize;
        
        public StorageStats(int fileCount, long totalSize) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
        }
        
        public String getTotalSizeFormatted() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return String.format("%.2f KB", totalSize / 1024.0);
            if (totalSize < 1024 * 1024 * 1024) return String.format("%.2f MB", totalSize / (1024.0 * 1024));
            return String.format("%.2f GB", totalSize / (1024.0 * 1024 * 1024));
        }
    }
}
