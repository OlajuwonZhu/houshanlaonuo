package com.senol.controller;

import com.senol.entity.FreshmenTeam;
import com.senol.entity.User;
import com.senol.service.FreshmenTeamService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/freshmen")
public class FreshmenTeamController {
    
    @Autowired
    private FreshmenTeamService freshmenTeamService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/teams")
    public ResponseUtil<Page<FreshmenTeam>> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String year) {
        
        Integer yearParam = null;
        if (year != null && !year.equals("null") && !year.trim().isEmpty()) {
            try {
                yearParam = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                return ResponseUtil.error("年份参数格式错误");
            }
        }
        
        Page<FreshmenTeam> teams = freshmenTeamService.getTeams(page, size, yearParam);
        return ResponseUtil.success(teams);
    }
    
    @GetMapping("/teams/{id}")
    public ResponseUtil<FreshmenTeam> getTeam(@PathVariable Long id) {
        Optional<FreshmenTeam> team = freshmenTeamService.getTeamById(id);
        if (team.isPresent()) {
            return ResponseUtil.success(team.get());
        } else {
            return ResponseUtil.error("新生队信息不存在");
        }
    }
    
    @PostMapping("/teams")
    public ResponseUtil<FreshmenTeam> createTeam(@RequestBody FreshmenTeam team, Authentication authentication) {
        try {
            String openId = authentication.getName();
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            FreshmenTeam savedTeam = freshmenTeamService.createTeam(team, user);
            return ResponseUtil.success(savedTeam);
        } catch (Exception e) {
            return ResponseUtil.error("创建新生队失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/teams/{id}")
    public ResponseUtil<FreshmenTeam> updateTeam(
            @PathVariable Long id,
            @RequestBody FreshmenTeam teamInfo) {
        
        Optional<FreshmenTeam> existingTeam = freshmenTeamService.getTeamById(id);
        if (!existingTeam.isPresent()) {
            return ResponseUtil.error("新生队信息不存在");
        }
        
        FreshmenTeam team = existingTeam.get();
        team.setTitle(teamInfo.getTitle());
        team.setDescription(teamInfo.getDescription());
        team.setYear(teamInfo.getYear());
        team.setTeamLeader(teamInfo.getTeamLeader());
        team.setMemberCount(teamInfo.getMemberCount());
        team.setActivities(teamInfo.getActivities());
        team.setAchievements(teamInfo.getAchievements());
        team.setImageUrls(teamInfo.getImageUrls());
        team.setMembers(teamInfo.getMembers());
        
        FreshmenTeam updatedTeam = freshmenTeamService.updateTeam(team);
        return ResponseUtil.success(updatedTeam);
    }
    
    @DeleteMapping("/teams/{id}")
    public ResponseUtil<String> deleteTeam(@PathVariable Long id) {
        freshmenTeamService.deleteTeam(id);
        return ResponseUtil.success("删除成功");
    }
    
    @GetMapping("/years")
    public ResponseUtil<List<Integer>> getTeamYears() {
        List<Integer> years = freshmenTeamService.getTeamYears();
        return ResponseUtil.success(years);
    }
}
