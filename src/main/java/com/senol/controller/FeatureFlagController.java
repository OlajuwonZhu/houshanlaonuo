package com.senol.controller;

import com.senol.dto.FeatureFlagRequest;
import com.senol.entity.FeatureFlag;
import com.senol.service.FeatureFlagService;
import com.senol.service.UserService;
import com.senol.entity.User;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/feature-flags")
public class FeatureFlagController {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private UserService userService;

    private boolean isAdmin(User user) {
        if (user.getIsAdmin() != null && user.getIsAdmin()) return true;
        String admin = System.getenv("ADMIN_OPENID");
        return admin != null && admin.equals(user.getOpenId());
    }

    @GetMapping
    public ResponseUtil<List<FeatureFlag>> list(Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            if (!isAdmin(user)) return ResponseUtil.error("无权限");
            List<FeatureFlag> flags = featureFlagService.listAll();
            return ResponseUtil.success(flags);
        } catch (Exception e) {
            return ResponseUtil.error("获取开关失败: " + e.getMessage());
        }
    }

    @PutMapping("/{key}")
    public ResponseUtil<FeatureFlag> set(@PathVariable String key, @RequestBody FeatureFlagRequest request, Authentication authentication) {
        try {
            if (authentication == null) return ResponseUtil.error("未登录");
            User user = userService.findByOpenId(authentication.getName());
            if (user == null) return ResponseUtil.error("用户不存在");
            if (!isAdmin(user)) return ResponseUtil.error("无权限");
            FeatureFlag flag = featureFlagService.setFlag(key, request.getEnabled(), request.getDescription());
            return ResponseUtil.success(flag);
        } catch (Exception e) {
            return ResponseUtil.error("设置开关失败: " + e.getMessage());
        }
    }
}
