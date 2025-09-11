package com.senol.repository;

import com.senol.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    // 检查用户是否已点赞某帖子
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    
    // 根据帖子ID和用户ID查找点赞记录
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    
    // 统计帖子的点赞数
    long countByPostId(Long postId);
}
