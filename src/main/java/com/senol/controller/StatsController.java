package com.senol.controller;

import com.senol.service.UserService;
import com.senol.service.PublicationService;
import com.senol.service.HistoryEventService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PublicationService publicationService;
    
    @Autowired
    private HistoryEventService historyEventService;
    
    @GetMapping("/overview")
    public ResponseUtil<Map<String, Object>> getOverviewStats() {
        Long memberCount = userService.getActiveUserCount();
        Long publicationCount = publicationService.getActivePublicationCount();
        Long eventCount = historyEventService.getActiveEventCount();
        
        Map<String, Object> stats = Map.of(
            "memberCount", memberCount,
            "publicationCount", publicationCount,
            "eventCount", eventCount
        );
        
        return ResponseUtil.success(stats);
    }
}
