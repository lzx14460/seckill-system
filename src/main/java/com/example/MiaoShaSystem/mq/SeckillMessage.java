package com.example.MiaoShaSystem.mq;

import lombok.Data;

/**
 * 秒杀消息对象
 * <p>
 * 秒杀成功后，把这条消息发给 RabbitMQ，
 * 消费者拿到消息后异步创建订单。
 * <p>
 * 消息里只需要携带两个关键信息：
 * - 谁在秒杀（userId）
 * - 秒杀哪个活动（activityId）
 */
@Data
public class SeckillMessage {

    /** 秒杀的用户 ID */
    private Long userId;

    /** 秒杀活动 ID（seckill_goods 表的主键） */
    private Long activityId;

    /** 请求 ID（用于前端轮询结果） */
    private String requestId;
}