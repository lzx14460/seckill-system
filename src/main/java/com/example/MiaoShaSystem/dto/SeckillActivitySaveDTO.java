package com.example.MiaoShaSystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillActivitySaveDTO {
    private Long id;             // null = 新建，非 null = 编辑
    private Long productId;      // 选哪个商品
    private Long goodsId;        // 商品的 goods_id（可选，自动从 product 里取）
    private String name;         // 活动名
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}