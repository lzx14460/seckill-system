package com.example.MiaoShaSystem.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class MerchantOrderExcelVO {

    @ExcelProperty("订单号")
    private String orderNo;

    @ExcelProperty("商品名称")
    private String productTitle;

    @ExcelProperty("活动名称")
    private String activityName;

    @ExcelProperty("成交价")
    private String seckillPrice;

    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("订单状态")
    private String statusText;

    @ExcelProperty("下单时间")
    private String createTime;
}