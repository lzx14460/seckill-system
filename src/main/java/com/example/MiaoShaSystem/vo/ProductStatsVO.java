package com.example.MiaoShaSystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductStatsVO {

    private Long productId;
    private String productTitle;

    private Long totalOrderCount;
    private BigDecimal totalSales;

    /** 该商品的秒杀活动列表 */
    private List<ActivityStat> activities;

    /** 近 7 天趋势 */
    private List<DailyStat> recentDays;

    @Data
    public static class ActivityStat {
        private Long activityId;
        private String name;
        private BigDecimal seckillPrice;
        private Integer stock;
        private Integer totalStock;
        private Byte status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long orderCount;
    }

    @Data
    public static class DailyStat {
        private String date;
        private Long orderCount;
        private BigDecimal sales;
    }
}