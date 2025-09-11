package com.senol.controller;

import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/overview")
    public ResponseUtil<Map<String, Object>> getOverviewStats() {
        Long memberCount = userService.getActiveUserCount();
        
        Map<String, Object> stats = Map.of(
            "memberCount", memberCount,
            "publicationCount", 0, // TODO: 从PublicationService获取
            "eventCount", 0 // TODO: 从HistoryEventService获取
        );
        
        return ResponseUtil.success(stats);
    }
}
