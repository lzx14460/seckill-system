package com.example.MiaoShaSystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductWithSeckillVO {

    private Long id;
    private String title;
    private String subTitle;
    private String coverImg;
    private BigDecimal price;
    private Integer stock;
    private String shopName;
    private String category;
    /** 秒杀信息（为 null 表示不参加秒杀） */
    private SeckillInfo seckill;

    @Data
    public static class SeckillInfo {
        private Long activityId;
        private BigDecimal seckillPrice;
        private Integer stock;
        private Integer totalStock;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Byte status;
    }
}