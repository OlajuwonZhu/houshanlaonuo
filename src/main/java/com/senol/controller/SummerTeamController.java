package com.senol.controller;

import com.senol.entity.SummerTeam;
import com.senol.entity.User;
import com.senol.service.SummerTeamService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/summer")
public class SummerTeamController {
    
    @Autowired
    private SummerTeamService summerTeamService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/teams")
    public ResponseUtil<Page<SummerTeam>> getTeams(
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
        
        Page<SummerTeam> teams = summerTeamService.getTeams(page, size, yearParam);
        return ResponseUtil.success(teams);
    }
    
    @GetMapping("/teams/{id}")
    public ResponseUtil<SummerTeam> getTeam(@PathVariable Long id) {
        Optional<SummerTeam> team = summerTeamService.getTeamById(id);
        if (team.isPresent()) {
            return ResponseUtil.success(team.get());
        } else {
            return ResponseUtil.error("暑期队信息不存在");
        }
    }
    
    @PostMapping("/teams")
    public ResponseUtil<SummerTeam> createTeam(@RequestBody SummerTeam team, Authentication authentication) {
        try {
            // 获取当前登录用户的openId
            String openId = authentication.getName();
            
            // 根据openId查找用户
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseUtil.error("用户未找到，请重新登录");
            }
            SummerTeam savedTeam = summerTeamService.createTeam(team, currentUser);
            return ResponseUtil.success(savedTeam);
        } catch (Exception e) {
            return ResponseUtil.error("创建暑期队失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/teams/{id}")
    public ResponseUtil<SummerTeam> updateTeam(
            @PathVariable Long id,
            @RequestBody SummerTeam teamInfo,
            Authentication authentication) {
        
        try {
            // 获取当前登录用户的openId
            String openId = authentication.getName();
            
            // 根据openId查找用户
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseUtil.error("用户未找到，请重新登录");
            }
            
            Optional<SummerTeam> existingTeam = summerTeamService.getTeamById(id);
            if (!existingTeam.isPresent()) {
                return ResponseUtil.error("暑期队信息不存在");
            }
            
            SummerTeam team = existingTeam.get();
            
            // 检查权限：只有创建者可以编辑
            if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseUtil.error("无权限编辑此暑期队信息");
            }
            
            team.setTitle(teamInfo.getTitle());
            team.setDescription(teamInfo.getDescription());
            team.setYear(teamInfo.getYear());
            team.setDestination(teamInfo.getDestination());
            team.setTeamLeader(teamInfo.getTeamLeader());
            team.setMemberCount(teamInfo.getMemberCount());
            team.setDurationDays(teamInfo.getDurationDays());
            team.setActivities(teamInfo.getActivities());
            team.setAchievements(teamInfo.getAchievements());
            team.setImageUrls(teamInfo.getImageUrls());
            team.setMembers(teamInfo.getMembers());
            
            SummerTeam updatedTeam = summerTeamService.updateTeam(team);
            return ResponseUtil.success(updatedTeam);
        } catch (Exception e) {
            return ResponseUtil.error("更新暑期队失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/teams/{id}")
    public ResponseUtil<String> deleteTeam(@PathVariable Long id, Authentication authentication) {
        try {
            // 获取当前登录用户的openId
            String openId = authentication.getName();
            
            // 根据openId查找用户
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseUtil.error("用户未找到，请重新登录");
            }
            
            Optional<SummerTeam> existingTeam = summerTeamService.getTeamById(id);
            if (!existingTeam.isPresent()) {
                return ResponseUtil.error("暑期队信息不存在");
            }
            
            SummerTeam team = existingTeam.get();
            
            // 检查权限：只有创建者可以删除
            if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseUtil.error("无权限删除此暑期队信息");
            }
            
            summerTeamService.deleteTeam(id);
            return ResponseUtil.success("删除成功");
        } catch (Exception e) {
            return ResponseUtil.error("删除暑期队失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/years")
    public ResponseUtil<List<Integer>> getTeamYears() {
        List<Integer> years = summerTeamService.getTeamYears();
        return ResponseUtil.success(years);
    }
}
