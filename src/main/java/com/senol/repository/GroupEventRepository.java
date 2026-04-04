package com.senol.repository;

import com.senol.entity.GroupEvent;
import com.senol.entity.GroupEvent.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface GroupEventRepository extends JpaRepository<GroupEvent, Long> {
    Page<GroupEvent> findByGroupIdOrderByStartTimeAsc(Long groupId, Pageable pageable);
    Page<GroupEvent> findByGroupIdAndStatusOrderByStartTimeAsc(Long groupId, Status status, Pageable pageable);
    Page<GroupEvent> findByGroupIdAndStartTimeAfterOrderByStartTimeAsc(Long groupId, LocalDateTime startAfter, Pageable pageable);
    Page<GroupEvent> findByGroupIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(Long groupId, Status status, LocalDateTime startAfter, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GroupEvent> findWithLockingById(Long id);

    boolean existsByGroupId(Long groupId);
}
