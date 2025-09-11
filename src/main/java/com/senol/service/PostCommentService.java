package com.senol.service;

import com.senol.entity.Post;
import com.senol.entity.PostComment;
import com.senol.entity.User;
import com.senol.repository.PostCommentRepository;
import com.senol.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    // 获取帖子的评论列表
    public Page<PostComment> getCommentsByPost(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postCommentRepository.findByPostIdAndParentCommentIdIsNullAndIsActiveTrueOrderByCreatedAtAsc(postId, pageable);
    }

    // 获取评论的回复列表
    public Page<PostComment> getRepliesByComment(Long commentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postCommentRepository.findByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(commentId, pageable);
    }

    // 创建评论
    @Transactional
    public PostComment createComment(Long postId, String content, Long userId, Long parentCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        
        // 如果是回复评论
        if (parentCommentId != null) {
            PostComment parentComment = postCommentRepository.findById(parentCommentId).orElse(null);
            if (parentComment != null) {
                comment.setParentComment(parentComment);
            }
        }

        PostComment savedComment = postCommentRepository.save(comment);
        
        // 更新帖子评论数
        postRepository.updateCommentCount(postId, 1);
        
        return savedComment;
    }

    // 获取用户的评论列表
    public Page<PostComment> getCommentsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postCommentRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);
    }

    // 获取用户的评论数量
    public int getCommentCountByUser(Long userId) {
        return postCommentRepository.countByUserIdAndIsActiveTrue(userId);
    }

    // 删除评论（软删除）
    @Transactional
    public void deleteComment(Long id, Long userId) {
        PostComment comment = postCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 检查是否是评论作者
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }

        comment.setIsActive(false);
        postCommentRepository.save(comment);
        
        // 更新帖子评论数
        postRepository.updateCommentCount(comment.getPost().getId(), -1);
    }
}
