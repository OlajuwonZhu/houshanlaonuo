package com.senol.repository;

import com.senol.entity.SummerTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SummerTeamRepository extends JpaRepository<SummerTeam, Long> {
    
    Page<SummerTeam> findByIsActiveTrueOrderByYearDesc(Pageable pageable);
    
    Page<SummerTeam> findByYearAndIsActiveTrueOrderByCreatedAtDesc(Integer year, Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM SummerTeam s WHERE s.isActive = true")
    Long countActiveTeams();
    
    @Query("SELECT DISTINCT s.year FROM SummerTeam s WHERE s.year IS NOT NULL ORDER BY s.year DESC")
    List<Integer> findDistinctYears();
}
