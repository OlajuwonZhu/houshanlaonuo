package com.senol.repository;

import com.senol.entity.Publication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    
    Page<Publication> findByIsActiveTrueOrderByPublishYearDescCreatedAtDesc(Pageable pageable);
    
    Page<Publication> findByPublishYearAndIsActiveTrueOrderByCreatedAtDesc(Integer publishYear, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Publication p WHERE p.isActive = true")
    Long countActivePublications();
    
    @Query("SELECT DISTINCT p.publishYear FROM Publication p WHERE p.isActive = true ORDER BY p.publishYear DESC")
    List<Integer> findDistinctPublishYears();
    
    List<Publication> findByPublishYearAndIsActiveTrueOrderByIssueNumberDesc(Integer year);
    
    Long countByIsActiveTrue();
}
