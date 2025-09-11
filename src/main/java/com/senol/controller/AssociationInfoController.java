package com.senol.controller;

import com.senol.entity.AssociationInfo;
import com.senol.entity.User;
import com.senol.service.AssociationInfoService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/association")
public class AssociationInfoController {
    
    @Autowired
    private AssociationInfoService associationInfoService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/info")
    public ResponseUtil<List<AssociationInfo>> getAllInfo() {
        List<AssociationInfo> infoList = associationInfoService.getAllInfo();
        return ResponseUtil.success(infoList);
    }
    
    @GetMapping("/info/type/{type}")
    public ResponseUtil<List<AssociationInfo>> getInfoByType(@PathVariable String type) {
        try {
            AssociationInfo.InfoType infoType = AssociationInfo.InfoType.valueOf(type);
            List<AssociationInfo> infoList = associationInfoService.getInfoByType(infoType);
            return ResponseUtil.success(infoList);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error("无效的信息类型");
        }
    }
    
    @GetMapping("/info/{id}")
    public ResponseUtil<AssociationInfo> getInfo(@PathVariable Long id) {
        Optional<AssociationInfo> info = associationInfoService.getInfoById(id);
        if (info.isPresent()) {
            return ResponseUtil.success(info.get());
        } else {
            return ResponseUtil.error("信息不存在");
        }
    }
    
    @PostMapping("/info")
    public ResponseUtil<AssociationInfo> createInfo(@RequestBody AssociationInfo info, Authentication authentication) {
        String openId = authentication.getName();
        User user = userService.findByOpenId(openId);
        
        AssociationInfo savedInfo = associationInfoService.createInfo(info, user);
        return ResponseUtil.success(savedInfo);
    }
    
    @PutMapping("/info/{id}")
    public ResponseUtil<AssociationInfo> updateInfo(
            @PathVariable Long id,
            @RequestBody AssociationInfo infoData,
            Authentication authentication) {
        
        Optional<AssociationInfo> existingInfo = associationInfoService.getInfoById(id);
        if (!existingInfo.isPresent()) {
            return ResponseUtil.error("信息不存在");
        }
        
        String openId = authentication.getName();
        User user = userService.findByOpenId(openId);
        
        AssociationInfo info = existingInfo.get();
        info.setTitle(infoData.getTitle());
        info.setContent(infoData.getContent());
        info.setImageUrl(infoData.getImageUrl());
        info.setFileUrl(infoData.getFileUrl());
        info.setDisplayOrder(infoData.getDisplayOrder());
        info.setUpdatedBy(user);
        
        AssociationInfo updatedInfo = associationInfoService.updateInfo(info);
        return ResponseUtil.success(updatedInfo);
    }
    
    @DeleteMapping("/info/{id}")
    public ResponseUtil<String> deleteInfo(@PathVariable Long id, Authentication authentication) {
        associationInfoService.deleteInfo(id);
        return ResponseUtil.success("删除成功");
    }
}
