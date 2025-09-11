package com.senol.controller;

import com.senol.entity.HistoryEvent;
import com.senol.entity.User;
import com.senol.service.HistoryEventService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/history")
public class HistoryEventController {
    
    @Autowired
    private HistoryEventService historyEventService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/events")
    public ResponseUtil<Page<HistoryEvent>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String type) {
        
        Integer yearParam = null;
        if (year != null && !year.equals("null") && !year.trim().isEmpty()) {
            try {
                yearParam = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                return ResponseUtil.error("年份参数格式错误");
            }
        }
        
        HistoryEvent.EventType eventType = null;
        if (type != null && !type.equals("null") && !type.trim().isEmpty()) {
            try {
                eventType = HistoryEvent.EventType.valueOf(type);
            } catch (IllegalArgumentException e) {
                return ResponseUtil.error("无效的事件类型");
            }
        }
        
        Page<HistoryEvent> events = historyEventService.getEvents(page, size, yearParam, eventType);
        return ResponseUtil.success(events);
    }
    
    @GetMapping("/events/{id}")
    public ResponseUtil<HistoryEvent> getEvent(@PathVariable Long id) {
        Optional<HistoryEvent> event = historyEventService.getEventById(id);
        if (event.isPresent()) {
            return ResponseUtil.success(event.get());
        } else {
            return ResponseUtil.error("事件不存在");
        }
    }
    
    @PostMapping("/events")
    public ResponseUtil<HistoryEvent> createEvent(@RequestBody HistoryEvent event, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseUtil.error("请先登录");
        }
        
        // 从JWT token中获取用户openId
        String openId = authentication.getName();
        User user = userService.findByOpenId(openId);
        if (user == null) {
            return ResponseUtil.error("用户不存在");
        }
        
        HistoryEvent savedEvent = historyEventService.createEvent(event, user);
        return ResponseUtil.success(savedEvent);
    }
    
    @PutMapping("/events/{id}")
    public ResponseUtil<HistoryEvent> updateEvent(
            @PathVariable Long id,
            @RequestBody HistoryEvent eventInfo,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseUtil.error("请先登录");
        }
        
        Optional<HistoryEvent> existingEvent = historyEventService.getEventById(id);
        if (!existingEvent.isPresent()) {
            return ResponseUtil.error("事件不存在");
        }
        
        // 验证用户权限 - 只能编辑自己创建的事件
        String openId = authentication.getName();
        User user = userService.findByOpenId(openId);
        if (user == null) {
            return ResponseUtil.error("用户不存在");
        }
        
        HistoryEvent event = existingEvent.get();
        if (!event.getCreatedBy().getId().equals(user.getId())) {
            return ResponseUtil.error("无权限编辑此事件");
        }
        
        event.setTitle(eventInfo.getTitle());
        event.setDescription(eventInfo.getDescription());
        event.setEventDate(eventInfo.getEventDate());
        event.setEventYear(eventInfo.getEventYear());
        event.setEventMonth(eventInfo.getEventMonth());
        event.setEventType(eventInfo.getEventType());
        event.setImageUrls(eventInfo.getImageUrls());
        
        HistoryEvent updatedEvent = historyEventService.updateEvent(event);
        return ResponseUtil.success(updatedEvent);
    }
    
    @DeleteMapping("/events/{id}")
    public ResponseUtil<String> deleteEvent(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseUtil.error("请先登录");
        }
        
        Optional<HistoryEvent> existingEvent = historyEventService.getEventById(id);
        if (!existingEvent.isPresent()) {
            return ResponseUtil.error("事件不存在");
        }
        
        // 验证用户权限 - 只能删除自己创建的事件
        String openId = authentication.getName();
        User user = userService.findByOpenId(openId);
        if (user == null) {
            return ResponseUtil.error("用户不存在");
        }
        
        HistoryEvent event = existingEvent.get();
        if (!event.getCreatedBy().getId().equals(user.getId())) {
            return ResponseUtil.error("无权限删除此事件");
        }
        
        historyEventService.deleteEvent(id);
        return ResponseUtil.success("删除成功");
    }
    
    @GetMapping("/years")
    public ResponseUtil<List<Integer>> getEventYears() {
        List<Integer> years = historyEventService.getEventYears();
        return ResponseUtil.success(years);
    }
}
