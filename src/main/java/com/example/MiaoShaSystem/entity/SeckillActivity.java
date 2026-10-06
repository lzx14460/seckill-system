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
 * @since 2026-10-04
 */
@Data
@TableName("seckill_goods")
public class SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("goods_id")
    private Long goodsId;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;
    @TableField("product_id")
    private Long productId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("name")
    private String name;

    @TableField("total_stock")
    private Integer totalStock;
    @TableField("status")
    private Byte status;

}
