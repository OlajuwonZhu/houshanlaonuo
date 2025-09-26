package com.senol.repository;

import com.senol.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    // 检查用户是否已点赞某帖子
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    
    // 根据帖子ID和用户ID查找点赞记录
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    
    // 统计帖子的点赞数
    long countByPostId(Long postId);

    // 批量查询多个帖子的所有点赞
    List<PostLike> findByPostIdIn(List<Long> postIds);

    // 查询单个帖子的所有点赞
    List<PostLike> findByPostId(Long postId);

    // 使用fetch join，避免懒加载问题
    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.user WHERE pl.post.id IN ?1")
    List<PostLike> findWithUserByPostIdIn(List<Long> postIds);

    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.user WHERE pl.post.id = ?1")
    List<PostLike> findWithUserByPostId(Long postId);
}
