package com.example.MiaoShaSystem.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Byte status;           // 可选，按状态筛选
    private String orderNo;        // 可选，按订单号搜索
}