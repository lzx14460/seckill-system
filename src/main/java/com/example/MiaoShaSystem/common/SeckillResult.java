package com.example.MiaoShaSystem.common;

import lombok.Data;

/**
 * 秒杀结果对象
 * 存到 Redis，供前端轮询查询
 */
@Data
public class SeckillResult {

    /** 状态：0-处理中，1-成功，2-失败 */
    private Integer status;

    /** 订单号（成功时有） */
    private String orderNo;

    /** 失败原因（失败时有） */
    private String message;

    public static SeckillResult pending() {
        SeckillResult r = new SeckillResult();
        r.setStatus(0);
        r.setMessage("订单处理中");
        return r;
    }

    public static SeckillResult success(String orderNo) {
        SeckillResult r = new SeckillResult();
        r.setStatus(1);
        r.setOrderNo(orderNo);
        r.setMessage("秒杀成功");
        return r;
    }

    public static SeckillResult fail(String message) {
        SeckillResult r = new SeckillResult();
        r.setStatus(2);
        r.setMessage(message);
        return r;
    }
}