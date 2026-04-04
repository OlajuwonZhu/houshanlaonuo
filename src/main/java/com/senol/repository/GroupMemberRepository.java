package com.senol.repository;

import com.senol.entity.GroupMember;
import com.senol.entity.GroupMember.Role;
import com.senol.entity.GroupMember.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMember> findByUserIdAndStatus(Long userId, Status status);

    long countByGroupIdAndRoleAndStatus(Long groupId, Role role, Status status);

    Page<GroupMember> findByGroupIdAndStatus(Long groupId, Status status, Pageable pageable);

    boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, Status status);

    List<GroupMember> findByGroupId(Long groupId);

    long countByGroupIdAndStatus(Long groupId, Status status);

    void deleteByGroupId(Long groupId);
}
