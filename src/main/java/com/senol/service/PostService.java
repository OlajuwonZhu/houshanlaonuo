package com.senol.service;

import com.senol.entity.Post;
import com.senol.entity.PostLike;
import com.senol.entity.User;
import com.senol.repository.PostRepository;
import com.senol.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private UserService userService;

    // 获取帖子列表
    public Page<Post> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("查询帖子 - 页码: " + page + ", 大小: " + size);
        
        // 先查询所有帖子进行调试
        Page<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        System.out.println("所有帖子总数: " + allPosts.getTotalElements());
        
        // 再查询活跃帖子
        Page<Post> result = postRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        System.out.println("活跃帖子总数: " + result.getTotalElements() + ", 当前页数量: " + result.getNumberOfElements());
        
        // 如果活跃帖子为空但所有帖子不为空，说明is_active字段有问题
        if (result.getTotalElements() == 0 && allPosts.getTotalElements() > 0) {
            System.out.println("警告: 存在帖子但is_active字段可能有问题，返回所有帖子");
            return allPosts;
        }
        
        return result;
    }

    // 根据用户ID获取帖子
    public Page<Post> getPostsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);
    }

    // 根据ID获取帖子
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    // 创建帖子
    @Transactional
    public Post createPost(String openId, String content, List<String> imageUrls) {
        User user = userService.findByOpenId(openId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 将图片URL列表转换为JSON字符串
        String imageUrlsJson = imageUrls != null && !imageUrls.isEmpty() ? 
            "[" + String.join(",", imageUrls.stream().map(url -> "\"" + url + "\"").toArray(String[]::new)) + "]" : "[]";
        
        Post post = new Post(user, content, imageUrlsJson);
        return postRepository.save(post);
    }

    // 创建帖子（通过用户ID）
    @Transactional
    public Post createPost(String content, String imageUrls, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Post post = new Post(user, content, imageUrls);
        return postRepository.save(post);
    }

    // 更新帖子
    @Transactional
    public Post updatePost(Long id, String content, String imageUrls, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // 检查是否是帖子作者
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("只能编辑自己的帖子");
        }

        post.setContent(content);
        post.setImageUrls(imageUrls);
        return postRepository.save(post);
    }

    // 删除帖子（软删除）
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // 检查是否是帖子作者
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("只能删除自己的帖子");
        }

        post.setIsActive(false);
        postRepository.save(post);
    }

    // 点赞/取消点赞
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否已点赞
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);
        
        if (isLiked) {
            // 取消点赞
            PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
            if (like != null) {
                postLikeRepository.delete(like);
                postRepository.updateLikeCount(postId, -1);
            }
            return false;
        } else {
            // 点赞
            PostLike like = new PostLike(post, user);
            postLikeRepository.save(like);
            postRepository.updateLikeCount(postId, 1);
            return true;
        }
    }

    // 检查用户是否已点赞
    public boolean isLikedByUser(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    // 根据用户ID获取帖子列表
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
    }

    // 根据用户ID获取指定数量的最新帖子
    public List<Post> getRecentPostsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);
        return postPage.getContent();
    }

    // 获取用户发布数量
    public Long getPostCountByUser(Long userId) {
        return postRepository.countByUserIdAndIsActiveTrue(userId);
    }
}
