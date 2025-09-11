package com.senol.controller;

import com.senol.service.LocalStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileAccessController {

    @Autowired
    private LocalStorageService localStorageService;

    /**
     * 访问头像文件
     */
    @GetMapping("/avatars/{userId}/{fileName}")
    public void getAvatar(@PathVariable Long userId, 
                         @PathVariable String fileName,
                         HttpServletResponse response) throws IOException {
        
        String fileUrl = String.format("/api/files/avatars/%d/%s", userId, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 访问团队图片
     */
    @GetMapping("/teams/{teamType}/{teamId}/images/{fileName}")
    public void getTeamImage(@PathVariable String teamType,
                           @PathVariable Long teamId,
                           @PathVariable String fileName,
                           HttpServletResponse response) throws IOException {
        
        String fileUrl = String.format("/api/files/teams/%s/%d/images/%s", teamType, teamId, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 访问会刊PDF
     */
    @GetMapping("/publications/{year}/{fileName}")
    public void getPublication(@PathVariable Integer year,
                             @PathVariable String fileName,
                             HttpServletResponse response) throws IOException {
        
        String fileUrl = String.format("/api/files/publications/%d/%s", year, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 访问社区帖子图片
     */
    @GetMapping("/community/posts/{postId}/images/{fileName}")
    public void getPostImage(@PathVariable Long postId,
                           @PathVariable String fileName,
                           HttpServletResponse response) throws IOException {
        
        String fileUrl = String.format("/api/files/community/posts/%d/images/%s", postId, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 访问临时文件
     */
    @GetMapping("/temp/{date}/{fileName}")
    public void getTempFile(@PathVariable String date, @PathVariable String fileName, 
                           HttpServletResponse response) throws IOException {
        serveFile("/api/files/temp/" + date + "/" + fileName, response);
    }

    @GetMapping("/teams/freshmen/{date}/{fileName}")
    public void getFreshmenFile(@PathVariable String date, @PathVariable String fileName, 
                               HttpServletResponse response) throws IOException {
        serveFile("/api/files/teams/freshmen/" + date + "/" + fileName, response);
    }

    @GetMapping("/teams/summer/{date}/{fileName}")
    public void getSummerFile(@PathVariable String date, @PathVariable String fileName, 
                             HttpServletResponse response) throws IOException {
        serveFile("/api/files/teams/summer/" + date + "/" + fileName, response);
    }

    /**
     * 用户头像文件访问路由 (按用户ID存储)
     * 支持路径: /api/files/avatars/users/{userId}/{fileName}
     */
    @GetMapping("/avatars/users/{userId}/{fileName}")
    public void getUserAvatarFile(@PathVariable String userId, @PathVariable String fileName,
                                 HttpServletResponse response) throws IOException {
        String fileUrl = String.format("/api/files/avatars/users/%s/%s", userId, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 头像文件访问路由 (按日期存储，兼容旧数据)
     * 支持路径: /api/files/avatars/{date}/{fileName}
     */
    @GetMapping("/avatars/{date}/{fileName}")
    public void getAvatarFileByDate(@PathVariable String date, @PathVariable String fileName,
                                   HttpServletResponse response) throws IOException {
        String fileUrl = String.format("/api/files/avatars/%s/%s", date, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 历史事件文件访问路由
     * 支持路径: /api/files/history/{date}/{fileName}
     */
    @GetMapping("/history/{date}/{fileName}")
    public void getHistoryFile(@PathVariable String date, @PathVariable String fileName,
                              HttpServletResponse response) throws IOException {
        String fileUrl = String.format("/api/files/history/%s/%s", date, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 部门文件访问路由
     * 支持路径: /api/files/departments/{date}/{fileName}
     */
    @GetMapping("/departments/{date}/{fileName}")
    public void getDepartmentFile(@PathVariable String date, @PathVariable String fileName,
                                 HttpServletResponse response) throws IOException {
        String fileUrl = String.format("/api/files/departments/%s/%s", date, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 通用分类文件访问路由
     * 支持路径: /api/files/{category}/{subCategory}/{date}/{fileName}
     * 例如: /api/files/teams/secretariat/2025-09-08/file.jpg
     */
    @GetMapping("/{category}/{subCategory}/{date}/{fileName}")
    public void getCategorizedFile(@PathVariable String category,
                                  @PathVariable String subCategory,
                                  @PathVariable String date,
                                  @PathVariable String fileName,
                                  HttpServletResponse response) throws IOException {
        String fileUrl = String.format("/api/files/%s/%s/%s/%s", category, subCategory, date, fileName);
        serveFile(fileUrl, response);
    }

    /**
     * 通用文件服务方法
     */
    private void serveFile(String fileUrl, HttpServletResponse response) throws IOException {
        Path filePath = localStorageService.getFilePath(fileUrl);
        
        if (filePath == null || !Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        try {
            // 设置内容类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // 根据文件扩展名设置默认类型
                String fileName = filePath.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (fileName.endsWith(".webp")) {
                    contentType = "image/webp";
                } else if (fileName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            response.setContentType(contentType);
            
            // 设置文件大小
            long fileSize = Files.size(filePath);
            response.setContentLengthLong(fileSize);
            
            // 对于PDF文件，设置inline显示
            if (contentType.equals("application/pdf")) {
                response.setHeader("Content-Disposition", "inline; filename=\"" + filePath.getFileName() + "\"");
            }
            
            // 设置缓存头
            response.setHeader("Cache-Control", "public, max-age=31536000"); // 1年缓存
            
            // 复制文件内容到响应
            Files.copy(filePath, response.getOutputStream());
            
            log.debug("文件访问成功: {}", fileUrl);
            
        } catch (IOException e) {
            log.error("文件访问失败: {}", fileUrl, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取存储统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<LocalStorageService.StorageStats> getStorageStats() {
        LocalStorageService.StorageStats stats = localStorageService.getStorageStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFileExists(@RequestParam String fileUrl) {
        boolean exists = localStorageService.fileExists(fileUrl);
        return ResponseEntity.ok(exists);
    }
}
