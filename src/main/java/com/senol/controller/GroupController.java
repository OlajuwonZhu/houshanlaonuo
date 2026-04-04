package com.senol.controller;

import com.senol.entity.Group;
import com.senol.entity.GroupMember;
import com.senol.entity.User;
import com.senol.service.GroupService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.senol.dto.GroupRequest;
import com.senol.repository.GroupMemberRepository;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private boolean isAdmin(User user) {
        if (user.getIsAdmin() != null && user.getIsAdmin()) return true;
        String admin = System.getenv("ADMIN_OPENID");
        return admin != null && admin.equals(user.getOpenId());
    }

    @GetMapping
    public ResponseUtil<Map<String, Object>> listGroups(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "my") String my,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        try {
            Long currentUserId = null;
            if (authentication != null && my != null && !my.isEmpty()) {
                String openId = authentication.getName();
                User user = userService.findByOpenId(openId);
                if (user == null) {
                    return ResponseUtil.error("用户不存在");
                }
                currentUserId = user.getId();
            }
            Page<Group> result = groupService.listGroups(q, city, my, currentUserId, page, size);
            Map<String, Object> data = new HashMap<>();
            data.put("items", result.getContent());
            data.put("page", result.getNumber());
            data.put("size", result.getSize());
            data.put("total", result.getTotalElements());
            return ResponseUtil.success(data);
        } catch (Exception e) {
            return ResponseUtil.error("获取小组列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseUtil<Group> createGroup(@RequestBody GroupRequest request, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            if (!isAdmin(user)) return ResponseUtil.error("无权限");
            Group g = groupService.createGroup(user.getId(), request);
            return ResponseUtil.success(g);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("参数错误");
        } catch (Exception e) {
            return ResponseUtil.error("创建小组失败: " + e.getMessage());
        }
    }

    @PutMapping("/{groupId}")
    public ResponseUtil<Group> updateGroup(@PathVariable Long groupId, @RequestBody GroupRequest request, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            if (!isAdmin(user)) return ResponseUtil.error("无权限");
            Group g = groupService.adminUpdateGroup(groupId, request);
            return ResponseUtil.success(g);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("参数错误");
        } catch (Exception e) {
            return ResponseUtil.error("更新小组失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseUtil<Map<String, Object>> getGroup(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Group> opt = groupService.getGroup(id);
            if (opt.isEmpty()) {
                return ResponseUtil.error("小组不存在");
            }
            Group g = opt.get();
            Map<String, Object> data = new HashMap<>();
            data.put("id", g.getId());
            data.put("name", g.getName());
            data.put("slug", g.getSlug());
            data.put("city", g.getCity());
            data.put("description", g.getDescription());
            data.put("coverImageUrl", g.getCoverImageUrl());
            data.put("joinPolicy", g.getJoinPolicy());
            data.put("createdAt", g.getCreatedAt());
            data.put("updatedAt", g.getUpdatedAt());

            if (authentication != null) {
                String openId = authentication.getName();
                User user = userService.findByOpenId(openId);
                if (user != null) {
                    Optional<GroupMember> mm = groupMemberRepository.findByGroupIdAndUserId(id, user.getId());
                    if (mm.isPresent()) {
                        GroupMember m = mm.get();
                        // 仅当成员未被移除时返回 myMembership，以便已退出的用户可再次申请加入
                        if (m.getStatus() != GroupMember.Status.REMOVED) {
                            Map<String, Object> my = new HashMap<>();
                            my.put("status", m.getStatus().name());
                            my.put("role", m.getRole().name());
                            data.put("myMembership", my);
                        }
                    }
                }
            }
            return ResponseUtil.success(data);
        } catch (Exception e) {
            return ResponseUtil.error("获取小组失败: " + e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members/apply")
    public ResponseUtil<Map<String, Object>> apply(@PathVariable Long groupId, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseUtil.error("未登录");
            }
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            GroupMember member = groupService.applyToGroup(groupId, user.getId());
            Map<String, Object> res = new HashMap<>();
            res.put("status", member.getStatus().name());
            res.put("role", member.getRole().name());
            return ResponseUtil.success(res);
        } catch (Exception e) {
            return ResponseUtil.error("申请加入失败: " + e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members/leave")
    public ResponseUtil<Void> leave(@PathVariable Long groupId, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseUtil.error("未登录");
            }
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            groupService.leaveGroup(groupId, user.getId());
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("退出失败: " + e.getMessage());
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseUtil<Page<GroupMember>> listMembers(
            @PathVariable Long groupId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        try {
            Long userId = null;
            if (authentication != null) {
                String openId = authentication.getName();
                User user = userService.findByOpenId(openId);
                if (user != null) {
                    userId = user.getId();
                }
            }
            GroupMember.Status st = null;
            if (status != null && !status.isEmpty()) {
                st = java.lang.Enum.valueOf(GroupMember.Status.class, status.toUpperCase());
            }
            Page<GroupMember> result = groupService.listMembers(groupId, st, userId, page, size);
            return ResponseUtil.success(result);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("无效的状态参数");
        } catch (Exception e) {
            return ResponseUtil.error("获取成员列表失败: " + e.getMessage());
        }
    }

    

    @PostMapping("/{groupId}/members/{memberId}/remove")
    public ResponseUtil<Void> removeMember(@PathVariable Long groupId, @PathVariable Long memberId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            groupService.removeMember(groupId, memberId, user.getId());
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("移除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members/{memberId}/promote")
    public ResponseUtil<GroupMember> promote(@PathVariable Long groupId, @PathVariable Long memberId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupMember gm = groupService.promoteToModerator(groupId, memberId, user.getId());
            return ResponseUtil.success(gm);
        } catch (Exception e) {
            return ResponseUtil.error("升为管理员失败: " + e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members/{memberId}/demote")
    public ResponseUtil<GroupMember> demote(@PathVariable Long groupId, @PathVariable Long memberId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupMember gm = groupService.demoteToMember(groupId, memberId, user.getId());
            return ResponseUtil.success(gm);
        } catch (Exception e) {
            return ResponseUtil.error("降为成员失败: " + e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members/{memberId}/transfer-leader")
    public ResponseUtil<GroupMember> transferLeader(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            GroupMember gm = groupService.transferLeadership(groupId, memberId, user.getId());
            return ResponseUtil.success(gm);
        } catch (Exception e) {
            return ResponseUtil.error("转移舵主失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseUtil<Void> deleteGroup(@PathVariable Long groupId, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            if (!isAdmin(user)) return ResponseUtil.error("无权限");
            groupService.adminDeleteGroup(groupId);
            return ResponseUtil.success(null);
        } catch (Exception e) {
            return ResponseUtil.error("删除小组失败: " + e.getMessage());
        }
    }
}
