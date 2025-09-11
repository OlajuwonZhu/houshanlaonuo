package com.senol.repository;

import com.senol.entity.ClimbingTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClimbingTeamRepository extends JpaRepository<ClimbingTeam, Long> {
    
    List<ClimbingTeam> findByYear(Integer year);
    
    List<ClimbingTeam> findAllByOrderByYearDesc();
    
    List<ClimbingTeam> findByYearBetweenOrderByYearDesc(Integer startYear, Integer endYear);
    
    @Query("SELECT DISTINCT c.year FROM ClimbingTeam c ORDER BY c.year DESC")
    List<Integer> findDistinctYears();
    
    @Query("SELECT COUNT(c) FROM ClimbingTeam c")
    Long countAll();
}
