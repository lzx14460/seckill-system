package com.example.MiaoShaSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSaveDTO {
    private Long id;
    private String title;
    private String subTitle;
    private String coverImg;
    private String detail;
    private BigDecimal price;
    private Integer stock;
}