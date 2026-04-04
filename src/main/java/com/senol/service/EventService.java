package com.senol.service;

import com.senol.dto.GroupEventRequest;
import com.senol.entity.EventSignup;
import com.senol.entity.Group;
import com.senol.entity.GroupEvent;
import com.senol.entity.GroupMember;
import com.senol.entity.User;
import com.senol.repository.EventSignupRepository;
import com.senol.repository.GroupEventRepository;
import com.senol.repository.GroupMemberRepository;
import com.senol.repository.GroupRepository;
import com.senol.service.UserService;
import com.senol.service.FeatureFlagService;
import com.senol.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupEventRepository groupEventRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private EventSignupRepository eventSignupRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private NotificationService notificationService;

    public Page<GroupEvent> listEvents(Long groupId, String status, boolean upcoming, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isEmpty()) {
            GroupEvent.Status st = java.lang.Enum.valueOf(GroupEvent.Status.class, status.toUpperCase());
            if (upcoming) {
                return groupEventRepository.findByGroupIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(groupId, st, LocalDateTime.now(), pageable);
            }
            return groupEventRepository.findByGroupIdAndStatusOrderByStartTimeAsc(groupId, st, pageable);
        } else {
            if (upcoming) {
                return groupEventRepository.findByGroupIdAndStartTimeAfterOrderByStartTimeAsc(groupId, LocalDateTime.now(), pageable);
            }
            return groupEventRepository.findByGroupIdOrderByStartTimeAsc(groupId, pageable);
        }
    }

    public Optional<GroupEvent> getEvent(Long eventId) {
        return groupEventRepository.findById(eventId);
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

    private void validateTimes(GroupEventRequest req) {
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new RuntimeException("活动时间不完整");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new RuntimeException("结束时间需晚于开始时间");
        }
        if (req.getSignupStartTime() != null && req.getSignupEndTime() != null) {
            if (!req.getSignupEndTime().isBefore(req.getStartTime()) && !req.getSignupEndTime().isEqual(req.getStartTime())) {
                throw new RuntimeException("报名截止需不晚于开始时间");
            }
            if (req.getSignupEndTime().isBefore(req.getSignupStartTime())) {
                throw new RuntimeException("报名截止需晚于报名开始");
            }
        }
    }

    @Transactional
    public GroupEvent createEvent(Long groupId, Long actorUserId, GroupEventRequest req) {
        if (!featureFlagService.isEnabled("events.enabled")) {
            throw new RuntimeException("活动功能已关闭");
        }
        ensureManager(groupId, actorUserId);
        validateTimes(req);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("小组不存在"));
        User organizer = userService.findById(actorUserId).orElseThrow(() -> new RuntimeException("用户不存在"));
        GroupEvent ev = new GroupEvent();
        ev.setGroup(group);
        ev.setOrganizer(organizer);
        ev.setTitle(req.getTitle());
        ev.setDescription(req.getDescription());
        ev.setLocation(req.getLocation());
        ev.setStartTime(req.getStartTime());
        ev.setEndTime(req.getEndTime());
        ev.setSignupStartTime(req.getSignupStartTime());
        ev.setSignupEndTime(req.getSignupEndTime());
        ev.setCapacity(req.getCapacity());
        // Waitlist globally disabled
        ev.setWaitlistEnabled(Boolean.FALSE);
        // Approval flow removed: always no approval required
        ev.setRequireApproval(Boolean.FALSE);
        ev.setCoverImageUrl(req.getCoverImageUrl());
        ev.setExternalLink(req.getExternalLink());
        ev.setFormSchema(req.getFormSchema());
        ev.setStatus(GroupEvent.Status.DRAFT);
        return groupEventRepository.save(ev);
    }

    @Transactional
    public GroupEvent updateEvent(Long eventId, Long actorUserId, GroupEventRequest req) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        ensureManager(ev.getGroup().getId(), actorUserId);
        validateTimes(req);
        ev.setTitle(req.getTitle());
        ev.setDescription(req.getDescription());
        ev.setLocation(req.getLocation());
        ev.setStartTime(req.getStartTime());
        ev.setEndTime(req.getEndTime());
        ev.setSignupStartTime(req.getSignupStartTime());
        ev.setSignupEndTime(req.getSignupEndTime());
        ev.setCapacity(req.getCapacity());
        // Waitlist globally disabled
        ev.setWaitlistEnabled(Boolean.FALSE);
        // Approval flow removed: always no approval required
        ev.setRequireApproval(Boolean.FALSE);
        ev.setCoverImageUrl(req.getCoverImageUrl());
        ev.setExternalLink(req.getExternalLink());
        ev.setFormSchema(req.getFormSchema());
        return groupEventRepository.save(ev);
    }

    @Transactional
    public GroupEvent publishEvent(Long eventId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        if (!featureFlagService.isEnabled("events.enabled")) {
            throw new RuntimeException("活动功能已关闭");
        }
        ensureManager(ev.getGroup().getId(), actorUserId);
        ev.setStatus(GroupEvent.Status.PUBLISHED);
        GroupEvent saved = groupEventRepository.saveAndFlush(ev);
        try { notificationService.notifyEventPublished(eventId); } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public GroupEvent cancelEvent(Long eventId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        ensureManager(ev.getGroup().getId(), actorUserId);
        ev.setStatus(GroupEvent.Status.CANCELED);
        // Optionally cascade effects on signups (notify etc.)
        GroupEvent saved = groupEventRepository.save(ev);
        try { notificationService.notifyEventCanceled(eventId); } catch (Exception ignored) {}
        return saved;
    }

    private void ensureApprovedMember(Long groupId, Long userId) {
        boolean ok = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupMember.Status.APPROVED);
        if (!ok) {
            throw new RuntimeException("仅限小组成员报名");
        }
    }

    @Transactional
    public EventSignup signup(Long eventId, Long userId, String answers, String note) {
        GroupEvent ev = groupEventRepository.findWithLockingById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        if (!featureFlagService.isEnabled("signups.enabled")) {
            throw new RuntimeException("报名功能已关闭");
        }
        ensureApprovedMember(ev.getGroup().getId(), userId);

        // Time window checks
        LocalDateTime now = LocalDateTime.now();
        if (ev.getSignupStartTime() != null && now.isBefore(ev.getSignupStartTime())) {
            throw new RuntimeException("报名未开始");
        }
        if (ev.getSignupEndTime() != null && now.isAfter(ev.getSignupEndTime())) {
            throw new RuntimeException("报名已截止");
        }
        if (ev.getEndTime() != null && now.isAfter(ev.getEndTime())) {
            throw new RuntimeException("活动已结束，无法报名");
        }

        EventSignup existing = eventSignupRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        if (existing != null) {
            // idempotent: if existing active, return as-is; if canceled/rejected/checked-in allow re-activate
            if (existing.getStatus() == EventSignup.Status.APPROVED || existing.getStatus() == EventSignup.Status.WAITLISTED || existing.getStatus() == EventSignup.Status.PENDING) {
                return existing;
            }
        }

        EventSignup signup = (existing != null) ? existing : new EventSignup();
        signup.setEvent(ev);
        signup.setUser(userService.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在")));
        signup.setAnswers(answers);
        signup.setNote(note);
        // re-signup: reset timestamps/fields
        if (existing != null) {
            signup.setCancelAt(null);
            signup.setCheckInAt(null);
            signup.setSignupAt(LocalDateTime.now());
        }

        boolean capacityUnlimited = (ev.getCapacity() == null || ev.getCapacity() <= 0);
        if (capacityUnlimited) {
            signup.setStatus(EventSignup.Status.APPROVED);
        } else {
            long approved = eventSignupRepository.countByEventIdAndStatus(eventId, EventSignup.Status.APPROVED);
            if (approved < ev.getCapacity()) {
                signup.setStatus(EventSignup.Status.APPROVED);
            } else if (Boolean.TRUE.equals(ev.getWaitlistEnabled())) {
                signup.setStatus(EventSignup.Status.WAITLISTED);
            } else {
                throw new RuntimeException("名额已满");
            }
        }
        EventSignup savedSignup = eventSignupRepository.save(signup);
        try {
            if (savedSignup.getStatus() == EventSignup.Status.APPROVED) {
                notificationService.notifySignupApproved(eventId, savedSignup.getId());
            }
        } catch (Exception ignored) {}
        return savedSignup;
    }

    public Page<EventSignup> listSignups(Long eventId, Long actorUserId, String status, int page, int size) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        if (ev.getStatus() != GroupEvent.Status.PUBLISHED) {
            if (actorUserId == null) {
                throw new RuntimeException("仅限小组成员查看");
            }
            ensureApprovedMember(ev.getGroup().getId(), actorUserId);
        }
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isEmpty()) {
            EventSignup.Status st = java.lang.Enum.valueOf(EventSignup.Status.class, status.toUpperCase());
            return eventSignupRepository.findByEventIdAndStatus(eventId, st, pageable);
        }
        // return all signups when no status filter provided
        return eventSignupRepository.findByEventId(eventId, pageable);
    }

    public EventSignup mySignup(Long eventId, Long userId) {
        return eventSignupRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
    }

    public Page<EventSignup> listMySignups(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isEmpty()) {
            EventSignup.Status st = java.lang.Enum.valueOf(EventSignup.Status.class, status.toUpperCase());
            return eventSignupRepository.findByUserIdAndStatus(userId, st, pageable);
        }
        return eventSignupRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void cancelSignup(Long eventId, Long signupId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findWithLockingById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        EventSignup signup = eventSignupRepository.findById(signupId).orElseThrow(() -> new RuntimeException("报名不存在"));
        if (!signup.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("报名与活动不匹配");
        }
        // Owner or manager
        boolean isOwner = signup.getUser().getId().equals(actorUserId);
        boolean isManager = false;
        try {
            ensureManager(ev.getGroup().getId(), actorUserId);
            isManager = true;
        } catch (Exception ignored) {}
        if (!isOwner && !isManager) {
            throw new RuntimeException("无权限取消");
        }
        signup.setStatus(EventSignup.Status.CANCELED);
        signup.setCancelAt(LocalDateTime.now());
        eventSignupRepository.save(signup);
        try { notificationService.notifySignupCanceled(eventId, signup.getId()); } catch (Exception ignored) {}

        // if seat freed, promote waitlist
        if (ev.getCapacity() != null && ev.getCapacity() > 0) {
            long approved = eventSignupRepository.countByEventIdAndStatus(eventId, EventSignup.Status.APPROVED);
            if (approved < ev.getCapacity()) {
                EventSignup next = eventSignupRepository.findTopByEventIdAndStatusOrderBySignupAtAsc(eventId, EventSignup.Status.WAITLISTED).orElse(null);
                if (next != null) {
                    next.setStatus(EventSignup.Status.APPROVED);
                    eventSignupRepository.save(next);
                    try { notificationService.notifyWaitlistPromoted(eventId, next.getId()); } catch (Exception ignored) {}
                }
            }
        }
    }

    @Transactional
    public EventSignup approveSignup(Long eventId, Long signupId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findWithLockingById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        ensureManager(ev.getGroup().getId(), actorUserId);
        EventSignup signup = eventSignupRepository.findById(signupId).orElseThrow(() -> new RuntimeException("报名不存在"));
        if (!signup.getEvent().getId().equals(eventId)) throw new RuntimeException("报名与活动不匹配");
        boolean capacityUnlimited = (ev.getCapacity() == null || ev.getCapacity() <= 0);
        if (capacityUnlimited) {
            signup.setStatus(EventSignup.Status.APPROVED);
        } else {
            long approved = eventSignupRepository.countByEventIdAndStatus(eventId, EventSignup.Status.APPROVED);
            if (approved < ev.getCapacity()) {
                signup.setStatus(EventSignup.Status.APPROVED);
            } else if (Boolean.TRUE.equals(ev.getWaitlistEnabled())) {
                signup.setStatus(EventSignup.Status.WAITLISTED);
            } else {
                throw new RuntimeException("名额已满");
            }
        }
        EventSignup savedApprove = eventSignupRepository.save(signup);
        try { notificationService.notifySignupApproved(eventId, savedApprove.getId()); } catch (Exception ignored) {}
        return savedApprove;
    }

    @Transactional
    public EventSignup rejectSignup(Long eventId, Long signupId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        ensureManager(ev.getGroup().getId(), actorUserId);
        EventSignup signup = eventSignupRepository.findById(signupId).orElseThrow(() -> new RuntimeException("报名不存在"));
        if (!signup.getEvent().getId().equals(eventId)) throw new RuntimeException("报名与活动不匹配");
        signup.setStatus(EventSignup.Status.REJECTED);
        EventSignup savedReject = eventSignupRepository.save(signup);
        try { notificationService.notifySignupRejected(eventId, savedReject.getId()); } catch (Exception ignored) {}
        return savedReject;
    }

    @Transactional
    public EventSignup moveToWaitlist(Long eventId, Long signupId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        ensureManager(ev.getGroup().getId(), actorUserId);
        EventSignup signup = eventSignupRepository.findById(signupId).orElseThrow(() -> new RuntimeException("报名不存在"));
        if (!signup.getEvent().getId().equals(eventId)) throw new RuntimeException("报名与活动不匹配");
        signup.setStatus(EventSignup.Status.WAITLISTED);
        return eventSignupRepository.save(signup);
    }

    @Transactional
    public EventSignup checkIn(Long eventId, Long signupId, Long actorUserId) {
        GroupEvent ev = groupEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("活动不存在"));
        ensureManager(ev.getGroup().getId(), actorUserId);
        EventSignup signup = eventSignupRepository.findById(signupId).orElseThrow(() -> new RuntimeException("报名不存在"));
        if (!signup.getEvent().getId().equals(eventId)) throw new RuntimeException("报名与活动不匹配");
        signup.setStatus(EventSignup.Status.CHECKED_IN);
        signup.setCheckInAt(LocalDateTime.now());
        EventSignup savedCheck = eventSignupRepository.save(signup);
        try { notificationService.notifyCheckIn(eventId, savedCheck.getId()); } catch (Exception ignored) {}
        return savedCheck;
    }
}
