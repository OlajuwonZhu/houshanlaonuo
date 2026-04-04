package com.senol.repository;

import com.senol.entity.EventSignup;
import com.senol.entity.EventSignup.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSignupRepository extends JpaRepository<EventSignup, Long> {
    Optional<EventSignup> findByEventIdAndUserId(Long eventId, Long userId);
    long countByEventIdAndStatus(Long eventId, Status status);
    List<EventSignup> findByEventIdAndStatus(Long eventId, Status status);
    List<EventSignup> findByEventId(Long eventId);
    Page<EventSignup> findByEventId(Long eventId, Pageable pageable);
    Page<EventSignup> findByEventIdAndStatus(Long eventId, Status status, Pageable pageable);
    Optional<EventSignup> findTopByEventIdAndStatusOrderBySignupAtAsc(Long eventId, Status status);
    Page<EventSignup> findByUserId(Long userId, Pageable pageable);
    Page<EventSignup> findByUserIdAndStatus(Long userId, Status status, Pageable pageable);
}
