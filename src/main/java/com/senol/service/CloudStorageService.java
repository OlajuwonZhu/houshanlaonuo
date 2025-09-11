package com.senol.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class CloudStorageService {

    @Value("${cos.secret-id}")
    private String secretId;

    @Value("${cos.secret-key}")
    private String secretKey;

    @Value("${cos.region}")
    private String region;

    @Value("${cos.bucket-name}")
    private String bucketName;

    @Value("${cos.domain}")
    private String domain;

    private COSClient cosClient;

    /**
     * 初始化COS客户端
     */
    private COSClient getCOSClient() {
        if (cosClient == null) {
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            Region regionObj = new Region(region);
            ClientConfig clientConfig = new ClientConfig(regionObj);
            cosClient = new COSClient(cred, clientConfig);
        }
        return cosClient;
    }

    /**
     * 上传用户头像
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = String.format("avatars/%d/%s", userId, fileName);
        return uploadFile(file, key);
    }

    public String uploadFreshmenImage(MultipartFile file) throws IOException {
        return uploadCategorizedImage(file, "teams", "freshmen");
    }

    /**
     * 上传暑期队图片
     */
    public String uploadSummerImage(MultipartFile file) throws IOException {
        return uploadCategorizedImage(file, "teams", "summer");
    }

    /**
     * 上传团队图片
     */
    public String uploadTeamImage(MultipartFile file, String teamType, Long teamId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = String.format("teams/%s/%d/images/%s", teamType, teamId, fileName);
        return uploadFile(file, key);
    }

    /**
     * 上传会刊PDF
     */
    public String uploadPublication(MultipartFile file, Integer year, Integer issue) throws IOException {
        String fileName = String.format("issue-%d.pdf", issue);
        String key = String.format("publications/%d/%s", year, fileName);
        return uploadFile(file, key);
    }

    /**
     * 上传社区帖子图片
     */
    public String uploadPostImage(MultipartFile file, Long postId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = String.format("community/posts/%d/%s", postId, fileName);
        return uploadFile(file, key);
    }

    /**
     * 上传临时文件
     */
    public String uploadTempFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = String.format("temp/%s/%s", getCurrentDate(), fileName);
        return uploadFile(file, key);
    }

    /**
     * 通用文件上传方法
     */
    private String uploadFile(MultipartFile file, String key) throws IOException {
        try {
            COSClient client = getCOSClient();
            
            // 设置对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            // 创建上传请求
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            
            // 执行上传
            PutObjectResult result = client.putObject(putObjectRequest);
            
            // 关闭输入流
            inputStream.close();
            
            // 返回文件访问URL
            String fileUrl = String.format("https://%s/%s", domain, key);
            
            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), fileUrl);
            return fileUrl;
            
        } catch (CosServiceException e) {
            log.error("COS服务异常: {}", e.getMessage(), e);
            throw new IOException("文件上传失败: " + e.getErrorMessage());
        } catch (CosClientException e) {
            log.error("COS客户端异常: {}", e.getMessage(), e);
            throw new IOException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取key
            String key = extractKeyFromUrl(fileUrl);
            if (key == null) {
                log.warn("无法从URL中提取key: {}", fileUrl);
                return false;
            }
            
            COSClient client = getCOSClient();
            client.deleteObject(bucketName, key);
            
            log.info("文件删除成功: {}", fileUrl);
            return true;
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 从URL中提取COS对象key
     */
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(domain)) {
            return null;
        }
        
        try {
            String[] parts = fileUrl.split(domain + "/");
            return parts.length > 1 ? parts[1] : null;
        } catch (Exception e) {
            log.error("提取key失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 上传用户头像
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadUserAvatar(MultipartFile file, Long userId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = String.format("avatars/users/%d/%s", userId, fileName);
        return uploadFile(file, key);
    }

    /**
     * 通用分类图片上传方法
     * @param file 上传的文件
     * @param category 分类 (teams, community, history, departments 等)
     * @param subCategory 子分类 (freshmen, summer, secretariat, climbing-team, posts 等)
     * @return 文件访问URL
     */
    public String uploadCategorizedImage(MultipartFile file, String category, String subCategory) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key;
        
        // 根据分类构建路径
        if ("teams".equals(category)) {
            key = String.format("teams/%s/%s/%s", subCategory, getCurrentDate(), fileName);
        } else if ("community".equals(category)) {
            key = String.format("community/%s/%s/%s", subCategory, getCurrentDate(), fileName);
        } else if ("avatars".equals(category)) {
            // 头像按日期存储的旧逻辑，保持兼容性
            key = String.format("avatars/%s/%s", getCurrentDate(), fileName);
        } else {
            key = String.format("%s/%s/%s", category, getCurrentDate(), fileName);
        }
        
        return uploadFile(file, key);
    }

    /**
     * 获取当前日期字符串
     */
    private String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 关闭COS客户端
     */
    public void shutdown() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }
}
