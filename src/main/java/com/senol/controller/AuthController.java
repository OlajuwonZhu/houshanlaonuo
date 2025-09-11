package com.senol.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.senol.dto.LoginRequest;
import com.senol.dto.LoginResponse;
import com.senol.entity.User;
import com.senol.service.UserService;
import com.senol.util.JwtUtil;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private WxMaService wxMaService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseUtil<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            String openId;
            String unionId = null;
            
            // 开发环境模拟登录
            if (request.getCode().equals("test") || request.getCode().startsWith("dev_")) {
                openId = "dev_user_" + System.currentTimeMillis();
                System.out.println("开发环境模拟登录，生成openId: " + openId);
            } else {
                // 生产环境真实微信登录
                WxMaJscode2SessionResult session = wxMaService.getUserService()
                        .getSessionInfo(request.getCode());
                
                openId = session.getOpenid();
                unionId = session.getUnionid();
                
                if (openId == null || openId.isEmpty()) {
                    return ResponseUtil.error("微信登录失败，请重试");
                }
            }
            
            // 创建或更新用户
            User user = userService.createOrUpdateUser(openId, unionId);
            
            // 生成JWT token
            String token = jwtUtil.generateToken(openId);
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserInfo(user);
            response.setIsNewUser(user.getName() == null || user.getMountainName() == null);
            
            return ResponseUtil.success(response);
            
        } catch (Exception e) {
            return ResponseUtil.error("登录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/refresh")
    public ResponseUtil<String> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String openId = jwtUtil.extractOpenId(token);
            
            User user = userService.findByOpenId(openId);
            if (user == null) {
                return ResponseUtil.error("用户不存在");
            }
            
            String newToken = jwtUtil.generateToken(openId);
            return ResponseUtil.success(newToken);
            
        } catch (Exception e) {
            return ResponseUtil.error("Token刷新失败: " + e.getMessage());
        }
    }
}
