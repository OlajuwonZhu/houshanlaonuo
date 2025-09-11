package com.senol.controller;

import com.senol.dto.UserProfileRequest;
import com.senol.entity.User;
import com.senol.entity.Post;
import com.senol.service.UserService;
import com.senol.service.PostService;
import com.senol.service.PostCommentService;
import com.senol.util.ResponseUtil;
import com.senol.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private PostCommentService postCommentService;
    
    @GetMapping("/info")
    public ResponseUtil<User> getUserInfo(Authentication authentication) {
        String openId = authentication.getName();
        User user = userService.findByOpenId(openId);
        
        if (user != null) {
            return ResponseUtil.success(user);
        } else {
            return ResponseUtil.error("用户不存在");
        }
    }
    
    @PutMapping("/profile")
    public ResponseUtil<User> updateProfile(@RequestBody UserProfileRequest request, Authentication authentication) {
        try {
            String openId = authentication.getName();
            User user = userService.updateUserProfile(openId, request);
            return ResponseUtil.success(user);
        } catch (Exception e) {
            return ResponseUtil.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/profile/complete")
    public ResponseEntity<ApiResponse<Boolean>> isProfileComplete(Authentication authentication) {
        String openId = authentication.getName();
        User currentUser = userService.findByOpenId(openId);
        if (currentUser == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("用户不存在"));
        }
        boolean isComplete = currentUser.getName() != null && 
                           currentUser.getMountainName() != null && 
                           currentUser.getEnrollmentYear() != null;
        return ResponseEntity.ok(ApiResponse.success(isComplete));
    }

    
    @GetMapping("/list")
    public ResponseUtil<List<User>> getAllUsers() {
        List<User> users = userService.getAllActiveUsers();
        return ResponseUtil.success(users);
    }
    
    @GetMapping("/list/year/{year}")
    public ResponseUtil<List<User>> getUsersByYear(@PathVariable Integer year) {
        List<User> users = userService.getUsersByYear(year);
        return ResponseUtil.success(users);
    }
    
    @GetMapping("/years")
    public ResponseUtil<List<Integer>> getEnrollmentYears() {
        List<Integer> years = userService.getEnrollmentYears();
        return ResponseUtil.success(years);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(Authentication authentication) {
        try {
            String openId = authentication.getName();
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户不存在"));
            }
            
            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户发布的帖子数量
            Long postCount = postService.getPostCountByUser(currentUser.getId());
            stats.put("postCount", postCount);
            
            // 获取用户评论数量
            int commentCount = postCommentService.getCommentCountByUser(currentUser.getId());
            stats.put("commentCount", commentCount);
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取用户统计失败: " + e.getMessage()));
        }
    }

    // 搜索用户（支持姓名和山号搜索）
    @GetMapping("/search")
    public ResponseUtil<List<User>> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            return ResponseUtil.success(users);
        } catch (Exception e) {
            return ResponseUtil.error("搜索用户失败: " + e.getMessage());
        }
    }

    // 根据用户ID获取用户信息
    @GetMapping("/{userId}")
    public ResponseUtil<User> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.findById(userId);
            if (user.isPresent()) {
                return ResponseUtil.success(user.get());
            } else {
                return ResponseUtil.error("用户不存在");
            }
        } catch (Exception e) {
            return ResponseUtil.error("获取用户信息失败: " + e.getMessage());
        }
    }

    // 获取用户详细信息（包含帖子和统计）
    @GetMapping("/{userId}/detail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetail(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
            }

            User user = userOpt.get();
            Map<String, Object> result = new HashMap<>();
            
            // 用户基本信息
            result.put("userInfo", user);
            
            // 获取用户发布的帖子数量
            Long postCount = postService.getPostCountByUser(userId);
            result.put("postCount", postCount);
            
            // 获取用户评论数量
            int commentCount = postCommentService.getCommentCountByUser(userId);
            result.put("commentCount", commentCount);
            
            // 获取用户最近的帖子（最多10条）
            List<Post> recentPosts = postService.getRecentPostsByUser(userId, 0, 10);
            result.put("recentPosts", recentPosts);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取用户详情失败: " + e.getMessage()));
        }
    }
}
