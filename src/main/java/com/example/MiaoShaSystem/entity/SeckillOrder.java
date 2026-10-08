package com.example.MiaoShaSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
@Data
@TableName("seckill_order")
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    @TableField("order_no")
    private String orderNo;

    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("activity_id")
    private Long activityId;
    @TableField("status")
    private Byte status;

    @TableField("product_title")
    private String productTitle;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("merchant_id")
    private Long merchantId;
    @TableField("cover_img")
    private String coverImg;
}
