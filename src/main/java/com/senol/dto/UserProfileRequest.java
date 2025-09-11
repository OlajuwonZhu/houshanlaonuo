package com.senol.dto;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String name;              // 真实姓名
    private String mountainName;      // 山号
    private Integer enrollmentYear;   // 入学年份
    private String className;         // 班级
    private String currentJob;        // 当前职业
    private String bio;              // 山路历程/个人简介
    private String phoneNumber;       // 手机号
    private String email;            // 邮箱
    private String avatarUrl;        // 头像URL
}
