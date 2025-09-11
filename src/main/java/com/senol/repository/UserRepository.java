package com.senol.repository;

import com.senol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByOpenId(String openId);
    
    Optional<User> findByUnionId(String unionId);
    
    List<User> findByEnrollmentYearOrderByCreatedAt(Integer enrollmentYear);
    
    List<User> findByIsActiveTrueOrderByEnrollmentYearDesc();
    
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();
    
    List<User> findByEnrollmentYearAndIsActiveTrueOrderByCreatedAtDesc(Integer enrollmentYear);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
    
    @Query("SELECT DISTINCT u.enrollmentYear FROM User u WHERE u.enrollmentYear IS NOT NULL ORDER BY u.enrollmentYear DESC")
    List<Integer> findDistinctEnrollmentYears();
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (LOWER(u.name) LIKE LOWER(?1) OR LOWER(u.mountainName) LIKE LOWER(?1)) ORDER BY u.enrollmentYear DESC, u.name ASC")
    List<User> findByNameContainingIgnoreCaseOrMountainNameContainingIgnoreCaseAndIsActiveTrue(String keyword);
}
