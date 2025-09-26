package com.senol.controller;

import com.senol.dto.CreatePostRequest;
import com.senol.dto.PostRequest;
import com.senol.entity.Post;
import com.senol.entity.User;
import com.senol.service.PostService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    // 获取帖子列表
    @GetMapping
    public ResponseUtil<Page<Post>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<Post> posts = postService.getPosts(page, size);
            System.out.println("查询到帖子数量: " + posts.getTotalElements());
            return ResponseUtil.success(posts);
        } catch (Exception e) {
            System.err.println("获取帖子列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseUtil.error("获取帖子列表失败: " + e.getMessage());
        }
    }

    // 根据用户ID获取帖子
    @GetMapping("/user/{userId}")
    public ResponseUtil<Page<Post>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Post> posts = postService.getPostsByUser(userId, page, size);
        return ResponseUtil.success(posts);
    }

    // 获取当前用户的帖子
    @GetMapping("/my")
    public ResponseUtil<Page<Post>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            Page<Post> posts = postService.getPostsByUser(user.getId(), page, size);
            return ResponseUtil.success(posts);
        } catch (Exception e) {
            return ResponseUtil.error("获取我的发布失败: " + e.getMessage());
        }
    }

    // 根据ID获取帖子详情
    @GetMapping("/{id}")
    public ResponseUtil<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseUtil.error("帖子不存在");
        }
        return ResponseUtil.success(post);
    }

    // 创建帖子
    @PostMapping
    public ResponseUtil<Post> createPost(@RequestBody CreatePostRequest request, Authentication authentication) {
        try {
            String openId = authentication.getName();
            System.out.println("JWT解析出的openId: " + openId);
            
            // 验证用户是否存在
            User user = userService.findByOpenId(openId);
            if (user == null) {
                System.out.println("用户不存在，openId: " + openId);
                return ResponseUtil.error("用户不存在");
            }
            System.out.println("找到用户: ID=" + user.getId() + ", 姓名=" + user.getName() + ", 山号=" + user.getMountainName());
            
            Post post = postService.createPost(openId, request.getContent(), request.getImageUrls());
            return ResponseUtil.success(post);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error("发布失败: " + e.getMessage());
        }
    }

    // 更新帖子
    @PutMapping("/{id}")
    public ResponseUtil<Post> updatePost(@PathVariable Long id, @RequestBody PostRequest request) {
        try {
            // 使用默认系统用户ID=1
            Long userId = 1L;
            
            Post post = postService.updatePost(id, request.getContent(), request.getImageUrls(), userId);
            return ResponseUtil.success(post);
        } catch (Exception e) {
            return ResponseUtil.error("更新帖子失败: " + e.getMessage());
        }
    }

    // 删除帖子
    @DeleteMapping("/{id}")
    public ResponseUtil<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            postService.deletePost(id, user.getId());
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("删除帖子失败: " + e.getMessage());
        }
    }

    // 点赞/取消点赞（使用当前登录用户）
    @PostMapping("/{id}/like")
    public ResponseUtil<Boolean> toggleLike(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseUtil.error("未登录");
            }
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            boolean isLiked = postService.toggleLike(id, user.getId());
            return ResponseUtil.success(isLiked);
        } catch (Exception e) {
            return ResponseUtil.error("操作失败: " + e.getMessage());
        }
    }

    // 检查是否已点赞（使用当前登录用户）
    @GetMapping("/{id}/liked")
    public ResponseUtil<Boolean> isLiked(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseUtil.error("未登录");
            }
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            boolean isLiked = postService.isLikedByUser(id, user.getId());
            return ResponseUtil.success(isLiked);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败: " + e.getMessage());
        }
    }

    // 获取某个帖子的点赞用户（返回用户ID与山号）
    @GetMapping("/{id}/likers")
    public ResponseUtil<List<Map<String, Object>>> getLikers(@PathVariable Long id) {
        try {
            List<User> users = postService.getLikers(id);
            List<Map<String, Object>> result = users.stream().map(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("userId", u.getId());
                m.put("mountainName", u.getMountainName());
                return m;
            }).collect(Collectors.toList());
            return ResponseUtil.success(result);
        } catch (Exception e) {
            return ResponseUtil.error("获取点赞用户失败: " + e.getMessage());
        }
    }

    // 批量获取多个帖子的点赞用户
    @GetMapping("/likers")
    public ResponseUtil<Map<Long, List<Map<String, Object>>>> getLikersBatch(@RequestParam("ids") String idsParam) {
        try {
            List<Long> ids = Arrays.stream(idsParam.split(","))
                    .filter(s -> !s.trim().isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            Map<Long, List<User>> map = postService.getLikersForPosts(ids);
            Map<Long, List<Map<String, Object>>> result = new HashMap<>();
            for (Map.Entry<Long, List<User>> entry : map.entrySet()) {
                List<Map<String, Object>> arr = entry.getValue().stream().map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", u.getId());
                    m.put("mountainName", u.getMountainName());
                    return m;
                }).collect(Collectors.toList());
                result.put(entry.getKey(), arr);
            }
            return ResponseUtil.success(result);
        } catch (Exception e) {
            return ResponseUtil.error("获取点赞用户失败: " + e.getMessage());
        }
    }

    // 帖子请求DTO
    public static class PostRequest {
        private String content;
        private String imageUrls;

        public PostRequest() {}

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(String imageUrls) {
            this.imageUrls = imageUrls;
        }
    }
}
