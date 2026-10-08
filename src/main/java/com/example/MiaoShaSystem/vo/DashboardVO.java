package com.example.MiaoShaSystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {

    /** 今日订单数 */
    private Long todayOrderCount;

    /** 今日销售额 */
    private BigDecimal todaySales;

    /** 进行中活动数 */
    private Long activeActivityCount;

    /** 商品总数 */
    private Long totalProductCount;

    /** 低库存商品数（库存 < 10） */
    private Long lowStockCount;

    /** 近 7 天订单趋势 */
    private List<DailyStat> recentDays;

    @Data
    public static class DailyStat {
        private String date;
        private Long orderCount;
        private BigDecimal sales;
    }
}