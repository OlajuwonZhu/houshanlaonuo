package com.senol.repository;

import com.senol.entity.HistoryEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryEventRepository extends JpaRepository<HistoryEvent, Long> {
    
    Page<HistoryEvent> findByIsActiveTrueOrderByEventDateDesc(Pageable pageable);
    
    Page<HistoryEvent> findByEventYearAndIsActiveTrueOrderByEventDateDesc(Integer eventYear, Pageable pageable);
    
    Page<HistoryEvent> findByEventTypeAndIsActiveTrueOrderByEventDateDesc(HistoryEvent.EventType eventType, Pageable pageable);
    
    Page<HistoryEvent> findByEventYearAndEventTypeAndIsActiveTrueOrderByEventDateDesc(
            Integer eventYear, HistoryEvent.EventType eventType, Pageable pageable);
    
    @Query("SELECT COUNT(h) FROM HistoryEvent h WHERE h.isActive = true")
    Long countActiveEvents();
    
    @Query("SELECT DISTINCT h.eventYear FROM HistoryEvent h WHERE h.eventYear IS NOT NULL ORDER BY h.eventYear DESC")
    List<Integer> findDistinctEventYears();
}
