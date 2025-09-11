package com.senol.controller;

import com.senol.entity.ClimbingTeam;
import com.senol.entity.User;
import com.senol.service.ClimbingTeamService;
import com.senol.service.UserService;
import com.senol.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/climbing-team")
@CrossOrigin(origins = "*")
public class ClimbingTeamController {
    
    @Autowired
    private ClimbingTeamService climbingTeamService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClimbingTeam>>> getAllClimbingTeams(
            @RequestParam(required = false) Integer year) {
        try {
            List<ClimbingTeam> teams;
            if (year != null) {
                // 如果提供了年份参数，按年份过滤
                teams = climbingTeamService.getClimbingTeamsByYear(year);
            } else {
                // 否则返回所有记录
                teams = climbingTeamService.getAllClimbingTeams();
            }
            return ResponseEntity.ok(ApiResponse.success(teams));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取攀岩队列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<ClimbingTeam>>> getClimbingTeamByYear(@PathVariable Integer year) {
        try {
            List<ClimbingTeam> teams = climbingTeamService.getClimbingTeamsByYear(year);
            return ResponseEntity.ok(ApiResponse.success(teams));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取攀岩队信息失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClimbingTeam>> getClimbingTeamById(@PathVariable Long id) {
        try {
            Optional<ClimbingTeam> team = climbingTeamService.getClimbingTeamById(id);
            if (team.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(team.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取攀岩队信息失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears() {
        try {
            List<Integer> years = climbingTeamService.getAvailableYears();
            return ResponseEntity.ok(ApiResponse.success(years));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取年份列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ClimbingTeam>> createClimbingTeam(
            @RequestBody ClimbingTeam climbingTeam, 
            Authentication authentication) {
        try {
            // 获取当前登录用户的openId
            String openId = authentication.getName();
            
            // 根据openId查找用户
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户未找到，请重新登录"));
            }
            
            climbingTeam.setCreatedBy(currentUser);
            
            // 处理成员信息
            if (climbingTeam.getMembers() != null) {
                // 成员信息已经是JSON字符串格式，直接保存
                System.out.println("创建攀岩队成员: " + climbingTeam.getMembers());
            }
            
            ClimbingTeam created = climbingTeamService.createClimbingTeam(climbingTeam);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建攀岩队记录失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClimbingTeam>> updateClimbingTeam(
            @PathVariable Long id, 
            @RequestBody ClimbingTeam climbingTeam,
            Authentication authentication) {
        try {
            // 获取当前登录用户的openId
            String openId = authentication.getName();
            
            // 根据openId查找用户
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户未找到，请重新登录"));
            }
            
            // 检查权限：只有创建者可以编辑
            Optional<ClimbingTeam> existingTeam = climbingTeamService.getClimbingTeamById(id);
            if (!existingTeam.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("攀岩队信息不存在"));
            }
            
            ClimbingTeam existing = existingTeam.get();
            if (!existing.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无权限编辑此攀岩队信息"));
            }
            
            climbingTeam.setUpdatedBy(currentUser);
            
            // 处理成员信息
            if (climbingTeam.getMembers() != null) {
                // 成员信息已经是JSON字符串格式，直接保存
                System.out.println("更新攀岩队成员: " + climbingTeam.getMembers());
            }
            
            ClimbingTeam updated = climbingTeamService.updateClimbingTeam(id, climbingTeam);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新攀岩队记录失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClimbingTeam(@PathVariable Long id, Authentication authentication) {
        try {
            // 获取当前登录用户的openId
            String openId = authentication.getName();
            
            // 根据openId查找用户
            User currentUser = userService.findByOpenId(openId);
            if (currentUser == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户未找到，请重新登录"));
            }
            
            // 检查权限：只有创建者可以删除
            Optional<ClimbingTeam> existingTeam = climbingTeamService.getClimbingTeamById(id);
            if (!existingTeam.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("攀岩队信息不存在"));
            }
            
            ClimbingTeam existing = existingTeam.get();
            if (!existing.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无权限删除此攀岩队信息"));
            }
            
            climbingTeamService.deleteClimbingTeam(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除攀岩队记录失败: " + e.getMessage()));
        }
    }
}
