package com.senol.service;

import com.senol.entity.HistoryEvent;
import com.senol.entity.User;
import com.senol.repository.HistoryEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoryEventService {
    
    @Autowired
    private HistoryEventRepository historyEventRepository;
    
    public Page<HistoryEvent> getEvents(int page, int size, Integer year, HistoryEvent.EventType eventType) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (year != null && eventType != null) {
            return historyEventRepository.findByEventYearAndEventTypeAndIsActiveTrueOrderByEventDateDesc(
                    year, eventType, pageable);
        } else if (year != null) {
            return historyEventRepository.findByEventYearAndIsActiveTrueOrderByEventDateDesc(year, pageable);
        } else if (eventType != null) {
            return historyEventRepository.findByEventTypeAndIsActiveTrueOrderByEventDateDesc(eventType, pageable);
        } else {
            return historyEventRepository.findByIsActiveTrueOrderByEventDateDesc(pageable);
        }
    }
    
    public Optional<HistoryEvent> getEventById(Long id) {
        return historyEventRepository.findById(id);
    }
    
    public HistoryEvent createEvent(HistoryEvent event, User createdBy) {
        event.setCreatedBy(createdBy);
        event.setIsActive(true);
        return historyEventRepository.save(event);
    }
    
    public HistoryEvent updateEvent(HistoryEvent event) {
        return historyEventRepository.save(event);
    }
    
    public void deleteEvent(Long id) {
        Optional<HistoryEvent> event = historyEventRepository.findById(id);
        if (event.isPresent()) {
            HistoryEvent historyEvent = event.get();
            historyEvent.setIsActive(false);
            historyEventRepository.save(historyEvent);
        }
    }
    
    public Long getActiveEventCount() {
        return historyEventRepository.countActiveEvents();
    }
    
    public List<Integer> getEventYears() {
        return historyEventRepository.findDistinctEventYears();
    }
}
