package com.senol.dto;

import com.senol.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User userInfo;
    private Boolean isNewUser; // 是否为新用户，需要完善信息
}
