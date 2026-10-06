package com.example.MiaoShaSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author lzx
 * @since 2026-10-05
 */
@Getter
@Setter
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属商家 user_id
     */
    @TableField("merchant_id")
    private Long merchantId;

    @TableField("title")
    private String title;

    @TableField("sub_title")
    private String subTitle;

    @TableField("cover_img")
    private String coverImg;

    @TableField("detail")
    private String detail;

    /**
     * 原价
     */
    @TableField("price")
    private BigDecimal price;


    /**
     * 普通库存
     */
    @TableField("stock")
    private Integer stock;


    /**
     * 0下架 1上架
     */
    @TableField("status")
    private Byte status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
