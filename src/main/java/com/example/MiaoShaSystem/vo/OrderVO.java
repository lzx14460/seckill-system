package com.example.MiaoShaSystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long activityId;
    private String activityName;
    private Long productId;
    private String productTitle;
    private BigDecimal seckillPrice;
    private Byte status;
    private LocalDateTime createTime;
    private String coverImg;
}