package com.senol.repository;

import com.senol.entity.Secretariat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecretariatRepository extends JpaRepository<Secretariat, Long> {
    
    List<Secretariat> findByYear(Integer year);
    
    List<Secretariat> findAllByOrderByYearDesc();
    
    List<Secretariat> findByYearBetweenOrderByYearDesc(Integer startYear, Integer endYear);
    
    @Query("SELECT DISTINCT s.year FROM Secretariat s ORDER BY s.year DESC")
    List<Integer> findDistinctYears();
    
    @Query("SELECT COUNT(s) FROM Secretariat s")
    Long countAll();
}
