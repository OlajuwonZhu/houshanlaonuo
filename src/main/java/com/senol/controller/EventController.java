package com.senol.controller;

import com.senol.dto.EventSignupRequest;
import com.senol.dto.GroupEventRequest;
import com.senol.entity.EventSignup;
import com.senol.entity.GroupEvent;
import com.senol.entity.GroupMember;
import com.senol.entity.User;
import com.senol.service.EventService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.senol.repository.GroupMemberRepository;

@RestController
@RequestMapping("/api")
public class  EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    // List events for a group (public: only PUBLISHED if not member)
    @GetMapping("/groups/{groupId}/events")
    public ResponseUtil<Page<GroupEvent>> listEvents(
            @PathVariable Long groupId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean upcoming,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        try {
            // If not authenticated or not an approved member, force status=PUBLISHED
            boolean isApprovedMember = false;
            if (authentication != null) {
                User user = userService.findByOpenId(authentication.getName());
                if (user != null) {
                    isApprovedMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(
                            groupId, user.getId(), GroupMember.Status.APPROVED);
                }
            }
            String effectiveStatus = status;
            if (!isApprovedMember) effectiveStatus = "PUBLISHED";
            Page<GroupEvent> result = eventService.listEvents(groupId, effectiveStatus, upcoming, page, size);
            return ResponseUtil.success(result);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("无效的状态参数");
        } catch (Exception e) {
            return ResponseUtil.error("获取活动列表失败: " + e.getMessage());
        }
    }

    // Create event (leader/moderator)
    @PostMapping("/groups/{groupId}/events")
    public ResponseUtil<GroupEvent> createEvent(@PathVariable Long groupId, @RequestBody GroupEventRequest request, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupEvent ev = eventService.createEvent(groupId, user.getId(), request);
            return ResponseUtil.success(ev);
        } catch (Exception e) {
            return ResponseUtil.error("创建活动失败: " + e.getMessage());
        }
    }

    // Get event detail (public)
    @GetMapping("/events/{eventId}")
    public ResponseUtil<GroupEvent> getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId)
                .map(ResponseUtil::success)
                .orElseGet(() -> ResponseUtil.error("活动不存在"));
    }

    // Update event (leader/moderator)
    @PutMapping("/events/{eventId}")
    public ResponseUtil<GroupEvent> updateEvent(@PathVariable Long eventId, @RequestBody GroupEventRequest request, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupEvent ev = eventService.updateEvent(eventId, user.getId(), request);
            return ResponseUtil.success(ev);
        } catch (Exception e) {
            return ResponseUtil.error("更新活动失败: " + e.getMessage());
        }
    }

    @PostMapping("/events/{eventId}/publish")
    public ResponseUtil<GroupEvent> publishEvent(@PathVariable Long eventId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupEvent ev = eventService.publishEvent(eventId, user.getId());
            GroupEvent fresh = eventService.getEvent(ev.getId()).orElse(ev);
            return ResponseUtil.success(fresh);
        } catch (Exception e) {
            return ResponseUtil.error("发布活动失败: " + e.getMessage());
        }
    }

    @PostMapping("/events/{eventId}/cancel")
    public ResponseUtil<GroupEvent> cancelEvent(@PathVariable Long eventId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupEvent ev = eventService.cancelEvent(eventId, user.getId());
            return ResponseUtil.success(ev);
        } catch (Exception e) {
            return ResponseUtil.error("取消活动失败: " + e.getMessage());
        }
    }

    // Signups
    @PostMapping("/events/{eventId}/signups")
    public ResponseUtil<EventSignup> signup(@PathVariable Long eventId, @RequestBody EventSignupRequest request, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            EventSignup s = eventService.signup(eventId, user.getId(), request.getAnswers(), request.getNote());
            return ResponseUtil.success(s);
        } catch (Exception e) {
            return ResponseUtil.error("报名失败: " + e.getMessage());
        }
    }

    @GetMapping("/events/{eventId}/signups")
    public ResponseUtil<Page<EventSignup>> listSignups(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Long userId = null;
            if (authentication != null) {
                User user = userService.findByOpenId(authentication.getName());
                if (user != null) userId = user.getId();
            }
            Page<EventSignup> list = eventService.listSignups(eventId, userId, status, page, size);
            return ResponseUtil.success(list);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("无效的状态参数");
        } catch (Exception e) {
            return ResponseUtil.error("获取报名列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/events/{eventId}/signups/me")
    public ResponseUtil<EventSignup> mySignup(@PathVariable Long eventId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            EventSignup s = eventService.mySignup(eventId, user.getId());
            return ResponseUtil.success(s);
        } catch (Exception e) {
            return ResponseUtil.error("获取我的报名失败: " + e.getMessage());
        }
    }

    @PostMapping("/events/{eventId}/signups/{signupId}/cancel")
    public ResponseUtil<Void> cancelSignup(@PathVariable Long eventId, @PathVariable Long signupId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            eventService.cancelSignup(eventId, signupId, user.getId());
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("取消报名失败: " + e.getMessage());
        }
    }

    @GetMapping("/me/signups")
    public ResponseUtil<Page<EventSignup>> listMySignups(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            Page<EventSignup> list = eventService.listMySignups(user.getId(), status, page, size);
            return ResponseUtil.success(list);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("无效的状态参数");
        } catch (Exception e) {
            return ResponseUtil.error("获取我的活动失败: " + e.getMessage());
        }
    }

    

    @PostMapping("/events/{eventId}/signups/{signupId}/check-in")
    public ResponseUtil<EventSignup> checkIn(@PathVariable Long eventId, @PathVariable Long signupId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            EventSignup s = eventService.checkIn(eventId, signupId, user.getId());
            return ResponseUtil.success(s);
        } catch (Exception e) {
            return ResponseUtil.error("签到失败: " + e.getMessage());
        }
    }
}
