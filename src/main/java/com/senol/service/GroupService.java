package com.senol.service;

import com.senol.entity.Group;
import com.senol.entity.GroupMember;
import com.senol.repository.GroupMemberRepository;
import com.senol.repository.GroupRepository;
import com.senol.repository.GroupEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.senol.entity.User;
import com.senol.service.UserService;
import com.senol.dto.GroupRequest;
import com.senol.service.FeatureFlagService;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupEventRepository groupEventRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FeatureFlagService featureFlagService;

    public Page<Group> listGroups(String q, String city, String my, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // my filter overrides search filters
        if (my != null && !my.isEmpty() && currentUserId != null) {
            GroupMember.Status approved = GroupMember.Status.APPROVED;
            List<GroupMember> memberships;
            if ("managed".equalsIgnoreCase(my)) {
                memberships = groupMemberRepository.findByUserIdAndStatus(currentUserId, approved)
                        .stream()
                        .filter(m -> m.getRole() == GroupMember.Role.LEADER || m.getRole() == GroupMember.Role.MODERATOR)
                        .collect(Collectors.toList());
            } else { // joined
                memberships = groupMemberRepository.findByUserIdAndStatus(currentUserId, approved);
            }
            List<Long> ids = memberships.stream().map(m -> m.getGroup().getId()).distinct().collect(Collectors.toList());
            if (ids.isEmpty()) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
            Page<Group> result = groupRepository.findByIdIn(ids, pageable);
            // enrich memberCount for each group
            result.getContent().forEach(g -> {
                long cnt = groupMemberRepository.countByGroupIdAndStatus(g.getId(), GroupMember.Status.APPROVED);
                g.setMemberCount(cnt);
            });
            return result;
        }
        String nameLike = q == null ? "" : q;
        String cityLike = city == null ? "" : city;
        Page<Group> result = groupRepository.findByNameContainingIgnoreCaseAndCityContainingIgnoreCase(nameLike, cityLike, pageable);
        // enrich memberCount for each group
        result.getContent().forEach(g -> {
            long cnt = groupMemberRepository.countByGroupIdAndStatus(g.getId(), GroupMember.Status.APPROVED);
            g.setMemberCount(cnt);
        });
        return result;
    }

    public Optional<Group> getGroup(Long id) {
        return groupRepository.findById(id);
    }

    @Transactional
    public Group createGroup(Long actorUserId, GroupRequest req) {
        if (!featureFlagService.isEnabled("groups.enabled")) {
            throw new RuntimeException("小组功能已关闭");
        }
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new RuntimeException("小组名称不能为空");
        }
        if (req.getSlug() == null || req.getSlug().trim().isEmpty()) {
            throw new RuntimeException("标识(slug)不能为空");
        }
        if (groupRepository.existsBySlug(req.getSlug())) {
            throw new RuntimeException("标识(slug)已存在");
        }
        Group g = new Group();
        g.setName(req.getName().trim());
        g.setSlug(req.getSlug().trim());
        g.setCity(req.getCity());
        g.setDescription(req.getDescription());
        g.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getJoinPolicy() != null && !req.getJoinPolicy().isEmpty()) {
            Group.JoinPolicy jp = java.lang.Enum.valueOf(Group.JoinPolicy.class, req.getJoinPolicy().toUpperCase());
            g.setJoinPolicy(jp);
        }
        Group saved = groupRepository.save(g);

        // creator becomes LEADER & APPROVED
        User creator = userService.findById(actorUserId).orElseThrow(() -> new RuntimeException("用户不存在"));
        GroupMember gm = new GroupMember();
        gm.setGroup(saved);
        gm.setUser(creator);
        gm.setRole(GroupMember.Role.LEADER);
        gm.setStatus(GroupMember.Status.APPROVED);
        gm.setJoinedAt(LocalDateTime.now());
        groupMemberRepository.save(gm);
        return saved;
    }

    @Transactional
    public Group updateGroup(Long groupId, Long actorUserId, GroupRequest req) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("小组不存在"));
        // only leader or moderator can update
        GroupMember actor = ensureManager(groupId, actorUserId);
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            group.setName(req.getName().trim());
        }
        if (req.getSlug() != null && !req.getSlug().trim().isEmpty() && !req.getSlug().equals(group.getSlug())) {
            if (groupRepository.existsBySlug(req.getSlug())) {
                throw new RuntimeException("标识(slug)已存在");
            }
            group.setSlug(req.getSlug().trim());
        }
        if (req.getCity() != null) group.setCity(req.getCity());
        if (req.getDescription() != null) group.setDescription(req.getDescription());
        if (req.getCoverImageUrl() != null) group.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getJoinPolicy() != null && !req.getJoinPolicy().isEmpty()) {
            Group.JoinPolicy jp = java.lang.Enum.valueOf(Group.JoinPolicy.class, req.getJoinPolicy().toUpperCase());
            group.setJoinPolicy(jp);
        }
        return groupRepository.save(group);
    }

    @Transactional
    public Group adminUpdateGroup(Long groupId, GroupRequest req) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("小组不存在"));
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            group.setName(req.getName().trim());
        }
        if (req.getSlug() != null && !req.getSlug().trim().isEmpty() && !req.getSlug().equals(group.getSlug())) {
            if (groupRepository.existsBySlug(req.getSlug())) {
                throw new RuntimeException("标识(slug)已存在");
            }
            group.setSlug(req.getSlug().trim());
        }
        if (req.getCity() != null) group.setCity(req.getCity());
        if (req.getDescription() != null) group.setDescription(req.getDescription());
        if (req.getCoverImageUrl() != null) group.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getJoinPolicy() != null && !req.getJoinPolicy().isEmpty()) {
            Group.JoinPolicy jp = java.lang.Enum.valueOf(Group.JoinPolicy.class, req.getJoinPolicy().toUpperCase());
            group.setJoinPolicy(jp);
        }
        return groupRepository.save(group);
    }

    @Transactional
    public GroupMember applyToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("小组不存在"));
        User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElse(null);
        if (member == null) {
            member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
        }
        // Idempotent: if already approved, return as is
        if (member.getStatus() == GroupMember.Status.APPROVED) {
            return member;
        }
        // immediate join: no approval workflow (except CLOSED groups)
        if (group.getJoinPolicy() == Group.JoinPolicy.CLOSED) {
            throw new RuntimeException("该小组暂不接受新成员");
        }
        member.setRejectedAt(null);
        member.setAppliedAt(LocalDateTime.now());
        member.setStatus(GroupMember.Status.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
        return groupMemberRepository.save(member);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("未加入该小组"));
        if (member.getStatus() != GroupMember.Status.APPROVED) {
            throw new RuntimeException("当前不是小组成员");
        }
        if (member.getRole() == GroupMember.Role.LEADER) {
            long leaders = groupMemberRepository.countByGroupIdAndRoleAndStatus(groupId, GroupMember.Role.LEADER, GroupMember.Status.APPROVED);
            if (leaders <= 1) {
                throw new RuntimeException("离开前请先指派新的舵主");
            }
        }
        member.setStatus(GroupMember.Status.REMOVED);
        groupMemberRepository.save(member);
    }

    public Page<GroupMember> listMembers(Long groupId, GroupMember.Status status, Long actorUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        GroupMember.Status st = (status == null) ? GroupMember.Status.APPROVED : status;
        if (st == GroupMember.Status.APPROVED) {
            return groupMemberRepository.findByGroupIdAndStatus(groupId, st, pageable);
        }
        if (actorUserId == null) {
            throw new RuntimeException("需要舵主或管理员权限");
        }
        GroupMember actor = groupMemberRepository.findByGroupIdAndUserId(groupId, actorUserId)
                .orElseThrow(() -> new RuntimeException("非小组成员，无权限"));
        if (actor.getStatus() != GroupMember.Status.APPROVED ||
                (actor.getRole() != GroupMember.Role.LEADER && actor.getRole() != GroupMember.Role.MODERATOR)) {
            throw new RuntimeException("需要舵主或管理员权限");
        }
        return groupMemberRepository.findByGroupIdAndStatus(groupId, st, pageable);
    }

    private GroupMember ensureManager(Long groupId, Long actorUserId) {
        GroupMember actor = groupMemberRepository.findByGroupIdAndUserId(groupId, actorUserId)
                .orElseThrow(() -> new RuntimeException("非小组成员，无权限"));
        if (actor.getStatus() != GroupMember.Status.APPROVED ||
                (actor.getRole() != GroupMember.Role.LEADER && actor.getRole() != GroupMember.Role.MODERATOR)) {
            throw new RuntimeException("需要舵主或管理员权限");
        }
        return actor;
    }

    private GroupMember ensureLeader(Long groupId, Long actorUserId) {
        GroupMember actor = ensureManager(groupId, actorUserId);
        if (actor.getRole() != GroupMember.Role.LEADER) {
            throw new RuntimeException("需要舵主权限");
        }
        return actor;
    }

    @Transactional
    public GroupMember approveMember(Long groupId, Long memberId, Long actorUserId) {
        ensureManager(groupId, actorUserId);
        GroupMember target = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("成员不属于该小组");
        }
        target.setStatus(GroupMember.Status.APPROVED);
        target.setJoinedAt(LocalDateTime.now());
        GroupMember saved = groupMemberRepository.save(target);
        try { notificationService.notifyGroupApplicationApproved(groupId, target.getUser().getId()); } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public GroupMember rejectMember(Long groupId, Long memberId, Long actorUserId) {
        ensureManager(groupId, actorUserId);
        GroupMember target = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("成员不属于该小组");
        }
        target.setStatus(GroupMember.Status.REJECTED);
        target.setRejectedAt(LocalDateTime.now());
        GroupMember saved = groupMemberRepository.save(target);
        try { notificationService.notifyGroupApplicationRejected(groupId, target.getUser().getId()); } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public void removeMember(Long groupId, Long memberId, Long actorUserId) {
        ensureManager(groupId, actorUserId);
        GroupMember target = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("成员不属于该小组");
        }
        if (target.getRole() == GroupMember.Role.LEADER && target.getStatus() == GroupMember.Status.APPROVED) {
            long leaders = groupMemberRepository.countByGroupIdAndRoleAndStatus(groupId, GroupMember.Role.LEADER, GroupMember.Status.APPROVED);
            if (leaders <= 1) {
                throw new RuntimeException("至少保留一名舵主");
            }
        }
        target.setStatus(GroupMember.Status.REMOVED);
        groupMemberRepository.save(target);
    }

    @Transactional
    public GroupMember promoteToModerator(Long groupId, Long memberId, Long actorUserId) {
        ensureLeader(groupId, actorUserId);
        GroupMember target = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("成员不属于该小组");
        }
        if (target.getStatus() != GroupMember.Status.APPROVED) {
            throw new RuntimeException("仅能调整已通过成员的角色");
        }
        target.setRole(GroupMember.Role.MODERATOR);
        return groupMemberRepository.save(target);
    }

    @Transactional
    public GroupMember demoteToMember(Long groupId, Long memberId, Long actorUserId) {
        ensureLeader(groupId, actorUserId);
        GroupMember target = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("成员不属于该小组");
        }
        if (target.getRole() == GroupMember.Role.LEADER) {
            throw new RuntimeException("不能直接降级舵主");
        }
        target.setRole(GroupMember.Role.MEMBER);
        return groupMemberRepository.save(target);
    }

    @Transactional
    public GroupMember transferLeadership(Long groupId, Long targetMemberId, Long actorUserId) {
        GroupMember actor = ensureLeader(groupId, actorUserId);
        if (actor.getId().equals(targetMemberId)) {
            throw new RuntimeException("不能将舵主转移给自己");
        }
        GroupMember target = groupMemberRepository.findById(targetMemberId)
                .orElseThrow(() -> new RuntimeException("目标成员不存在"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("成员不属于该小组");
        }
        if (target.getStatus() != GroupMember.Status.APPROVED) {
            throw new RuntimeException("仅能将舵主转移给已通过成员");
        }
        // Promote target to LEADER
        target.setRole(GroupMember.Role.LEADER);
        groupMemberRepository.save(target);
        actor.setRole(GroupMember.Role.MEMBER);
        groupMemberRepository.save(actor);
        return target;
    }

    @Transactional
    public void deleteGroup(Long groupId, Long actorUserId) {
        ensureLeader(groupId, actorUserId);
        // safe rules: no active members (approved/pending) and no events
        long approved = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMember.Status.APPROVED);
        long pending = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMember.Status.PENDING);
        if (approved > 0 || pending > 0) {
            throw new RuntimeException("请先清空成员(含待审批/已通过)后再删除小组");
        }
        boolean hasEvents = groupEventRepository.existsByGroupId(groupId);
        if (hasEvents) {
            throw new RuntimeException("存在关联活动，无法删除小组");
        }
        // cleanup any remaining membership records (rejected/removed)
        groupMemberRepository.deleteByGroupId(groupId);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("小组不存在"));
        groupRepository.delete(group);
    }

    @Transactional
    public void adminDeleteGroup(Long groupId) {
        // safe rules: no active members (approved/pending) and no events
        long approved = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMember.Status.APPROVED);
        long pending = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMember.Status.PENDING);
        if (approved > 0 || pending > 0) {
            throw new RuntimeException("请先清空成员(含待审批/已通过)后再删除小组");
        }
        boolean hasEvents = groupEventRepository.existsByGroupId(groupId);
        if (hasEvents) {
            throw new RuntimeException("存在关联活动，无法删除小组");
        }
        // cleanup any remaining membership records (rejected/removed)
        groupMemberRepository.deleteByGroupId(groupId);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("小组不存在"));
        groupRepository.delete(group);
    }
}
