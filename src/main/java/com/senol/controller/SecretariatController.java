package com.senol.controller;

import com.senol.entity.Secretariat;
import com.senol.entity.User;
import com.senol.service.SecretariatService;
import com.senol.service.UserService;
import com.senol.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/secretariat")
@CrossOrigin(origins = "*")
public class SecretariatController {
    
    @Autowired
    private SecretariatService secretariatService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Secretariat>>> getAllSecretariats(
            @RequestParam(required = false) Integer year) {
        try {
            List<Secretariat> secretariats;
            if (year != null) {
                // 如果提供了年份参数，按年份过滤
                secretariats = secretariatService.getSecretariatsByYear(year);
            } else {
                // 否则返回所有记录
                secretariats = secretariatService.getAllSecretariats();
            }
            return ResponseEntity.ok(ApiResponse.success(secretariats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取秘书处列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<Secretariat>>> getSecretariatByYear(@PathVariable Integer year) {
        try {
            List<Secretariat> secretariats = secretariatService.getSecretariatsByYear(year);
            return ResponseEntity.ok(ApiResponse.success(secretariats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取秘书处信息失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Secretariat>> getSecretariatById(@PathVariable Long id) {
        try {
            Optional<Secretariat> secretariat = secretariatService.getSecretariatById(id);
            if (secretariat.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(secretariat.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取秘书处信息失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears() {
        try {
            List<Integer> years = secretariatService.getAvailableYears();
            return ResponseEntity.ok(ApiResponse.success(years));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取年份列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Secretariat>> createSecretariat(
            @RequestBody Secretariat secretariat, 
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
            
            secretariat.setCreatedBy(currentUser);
            
            // 处理成员信息
            if (secretariat.getMembers() != null) {
                // 成员信息已经是JSON字符串格式，直接保存
                System.out.println("创建秘书处成员: " + secretariat.getMembers());
            }
            
            Secretariat created = secretariatService.createSecretariat(secretariat);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建秘书处记录失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Secretariat>> updateSecretariat(
            @PathVariable Long id, 
            @RequestBody Secretariat secretariat,
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
            Optional<Secretariat> existingSecretariat = secretariatService.getSecretariatById(id);
            if (!existingSecretariat.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("秘书处信息不存在"));
            }
            
            Secretariat existing = existingSecretariat.get();
            if (!existing.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无权限编辑此秘书处信息"));
            }
            
            secretariat.setUpdatedBy(currentUser);
            
            // 处理成员信息
            if (secretariat.getMembers() != null) {
                // 成员信息已经是JSON字符串格式，直接保存
                System.out.println("更新秘书处成员: " + secretariat.getMembers());
            }
            
            Secretariat updated = secretariatService.updateSecretariat(id, secretariat);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新秘书处记录失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSecretariat(@PathVariable Long id, Authentication authentication) {
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
            Optional<Secretariat> existingSecretariat = secretariatService.getSecretariatById(id);
            if (!existingSecretariat.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("秘书处信息不存在"));
            }
            
            Secretariat existing = existingSecretariat.get();
            if (!existing.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无权限删除此秘书处信息"));
            }
            
            secretariatService.deleteSecretariat(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除秘书处记录失败: " + e.getMessage()));
        }
    }
}
