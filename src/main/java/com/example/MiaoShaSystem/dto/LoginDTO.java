package com.example.MiaoShaSystem.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
    private String role;          // USER / MERCHANT（登录时的身份选择）
}