package com.example.MiaoShaSystem.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String role;          // USER / MERCHANT
    private String phone;
    private String email;         // 可选
    private String shopName;      // 商家必填
}