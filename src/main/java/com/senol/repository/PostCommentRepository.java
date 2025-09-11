package com.senol.repository;

import com.senol.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    
    // 根据帖子ID获取评论，按创建时间正序
    Page<PostComment> findByPostIdAndIsActiveTrueOrderByCreatedAtAsc(Long postId, Pageable pageable);
    
    // 根据帖子ID获取顶级评论（没有父评论的评论）
    Page<PostComment> findByPostIdAndParentCommentIdIsNullAndIsActiveTrueOrderByCreatedAtAsc(Long postId, Pageable pageable);
    
    // 根据父评论ID获取回复
    Page<PostComment> findByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(Long parentCommentId, Pageable pageable);
    
    // 根据用户ID获取评论，按创建时间倒序
    Page<PostComment> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 根据用户ID统计活跃评论数量
    int countByUserIdAndIsActiveTrue(Long userId);
}
