package com.example.MiaoShaSystem.dto;

import lombok.Data;

@Data
public class ProductAuditDTO {
    private Byte status;      // 1通过 2拒绝
    private String remark;
}