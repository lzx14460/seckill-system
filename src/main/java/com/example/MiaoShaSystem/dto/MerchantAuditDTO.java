package com.example.MiaoShaSystem.dto;

import lombok.Data;

@Data
public class MerchantAuditDTO {
    private Byte status;        // 1通过 2拒绝
    private String remark;      // 拒绝原因（可选）
}