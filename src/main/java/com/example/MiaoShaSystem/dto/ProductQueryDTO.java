package com.example.MiaoShaSystem.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String title;
    private Byte status;
}