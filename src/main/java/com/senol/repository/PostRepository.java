package com.senol.repository;

import com.senol.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 获取活跃的帖子，按创建时间倒序
    Page<Post> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // 获取所有帖子（用于调试）
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 根据用户ID获取帖子
    Page<Post> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 根据用户ID获取所有帖子（不分页）
    List<Post> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
    
    // 更新点赞数
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + :increment WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("increment") int increment);
    
    // 更新评论数
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :increment WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);
    
    // 统计用户发布数量
    Long countByUserIdAndIsActiveTrue(Long userId);
}
