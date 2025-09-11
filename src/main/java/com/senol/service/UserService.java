package com.senol.service;

import com.senol.dto.UserProfileRequest;
import com.senol.entity.User;
import com.senol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByOpenId(username);
    }
    
    public UserDetails loadUserByOpenId(String openId) throws UsernameNotFoundException {
        User user = userRepository.findByOpenId(openId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + openId));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getOpenId())
                .password("") // 微信登录不需要密码
                .authorities(new ArrayList<>())
                .build();
    }
    
    public User findByOpenId(String openId) {
        return userRepository.findByOpenId(openId).orElse(null);
    }
    
    public User createOrUpdateUser(String openId, String unionId) {
        Optional<User> existingUser = userRepository.findByOpenId(openId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (unionId != null && !unionId.equals(user.getUnionId())) {
                user.setUnionId(unionId);
                return userRepository.save(user);
            }
            return user;
        } else {
            User newUser = new User();
            newUser.setOpenId(openId);
            newUser.setUnionId(unionId);
            newUser.setIsActive(true);
            return userRepository.save(newUser);
        }
    }
    
    public User updateUserInfo(User user) {
        return userRepository.save(user);
    }
    
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    public List<User> getUsersByYear(Integer year) {
        return userRepository.findByEnrollmentYearAndIsActiveTrueOrderByCreatedAtDesc(year);
    }
    
    public Long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }
    
    public List<Integer> getEnrollmentYears() {
        return userRepository.findDistinctEnrollmentYears();
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUserProfile(String openId, UserProfileRequest request) {
        User user = findByOpenId(openId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getMountainName() != null) {
            user.setMountainName(request.getMountainName());
        }
        if (request.getEnrollmentYear() != null) {
            user.setEnrollmentYear(request.getEnrollmentYear());
        }
        if (request.getClassName() != null) {
            user.setClassName(request.getClassName());
        }
        if (request.getCurrentJob() != null) {
            user.setCurrentJob(request.getCurrentJob());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        return userRepository.save(user);
    }

    // 搜索用户（支持姓名和山号搜索）
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchKeyword = "%" + keyword.trim() + "%";
        return userRepository.findByNameContainingIgnoreCaseOrMountainNameContainingIgnoreCaseAndIsActiveTrue(searchKeyword);
    }
}
