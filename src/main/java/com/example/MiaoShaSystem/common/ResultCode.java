package com.example.MiaoShaSystem.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200,"success"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    USERNAME_EXIT(1001,"用户名已存在"),
    LOGIN_ERROR(1002,"用户名或密码错误"),
    STOCK_EMPTY(2001, "库存不足"),
    REPEAT_SECKILL(2002, "请勿重复秒杀"),
    SECKILL_NOT_START(2003, "秒杀未开始"),
    SECKILL_END(2004, "秒杀已结束"),

    NO_PERMISSION(403, "无权限"),
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_NOT_OWNED(3002, "无权操作该商品"),
    ACTIVITY_NOT_FOUND(3003, "活动不存在"),
    ACTIVITY_NOT_OWNED(3004, "无权操作该活动");

    private final Integer code;
    private final String message;
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
