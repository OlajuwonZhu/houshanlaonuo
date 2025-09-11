package com.senol.controller;

import com.senol.entity.PostComment;
import com.senol.entity.User;
import com.senol.service.PostCommentService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private UserService userService;

    // 获取帖子的评论列表
    @GetMapping("/post/{postId}")
    public ResponseUtil<Page<PostComment>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PostComment> comments = postCommentService.getCommentsByPost(postId, page, size);
        return ResponseUtil.success(comments);
    }

    // 获取评论的回复列表
    @GetMapping("/{commentId}/replies")
    public ResponseUtil<Page<PostComment>> getRepliesByComment(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Page<PostComment> replies = postCommentService.getRepliesByComment(commentId, page, size);
        return ResponseUtil.success(replies);
    }

    // 获取当前用户的评论
    @GetMapping("/my")
    public ResponseUtil<Page<PostComment>> getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            Page<PostComment> comments = postCommentService.getCommentsByUser(user.getId(), page, size);
            return ResponseUtil.success(comments);
        } catch (Exception e) {
            return ResponseUtil.error("获取我的评论失败: " + e.getMessage());
        }
    }

    // 创建评论
    @PostMapping
    public ResponseUtil<PostComment> createComment(@RequestBody CommentRequest request, Authentication authentication) {
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            PostComment comment = postCommentService.createComment(
                request.getPostId(), 
                request.getContent(), 
                user.getId(), 
                request.getParentCommentId()
            );
            return ResponseUtil.success(comment);
        } catch (Exception e) {
            return ResponseUtil.error("创建评论失败: " + e.getMessage());
        }
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public ResponseUtil<Void> deleteComment(@PathVariable Long id, Authentication authentication) {
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            postCommentService.deleteComment(id, user.getId());
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("删除评论失败: " + e.getMessage());
        }
    }

    // 评论请求DTO
    public static class CommentRequest {
        private Long postId;
        private String content;
        private Long parentCommentId;

        public CommentRequest() {}

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(Long parentCommentId) {
            this.parentCommentId = parentCommentId;
        }
    }
}
