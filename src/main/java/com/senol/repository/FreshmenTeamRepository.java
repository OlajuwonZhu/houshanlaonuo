package com.senol.repository;

import com.senol.entity.FreshmenTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreshmenTeamRepository extends JpaRepository<FreshmenTeam, Long> {
    
    Page<FreshmenTeam> findByIsActiveTrueOrderByYearDesc(Pageable pageable);
    
    Page<FreshmenTeam> findByYearAndIsActiveTrueOrderByCreatedAtDesc(Integer year, Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM FreshmenTeam f WHERE f.isActive = true")
    Long countActiveTeams();
    
    @Query("SELECT DISTINCT f.year FROM FreshmenTeam f WHERE f.year IS NOT NULL ORDER BY f.year DESC")
    List<Integer> findDistinctYears();
}
