package com.example.MiaoShaSystem.mq;

import com.example.MiaoShaSystem.common.SeckillResult;
import com.example.MiaoShaSystem.config.RabbitMQConfig;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.SeckillActivityMapper;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class SeckillConsumer {
    private static final Logger log = LoggerFactory.getLogger(SeckillConsumer.class);
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handle(SeckillMessage message) {
        Long userId = message.getUserId();
        Long activityId = message.getActivityId();         // ← 改这里
        String requestId = message.getRequestId();
        String resultKey = "seckill:result:" + requestId;

        try {
            // 1. 查活动
            SeckillActivity activity = seckillActivityMapper.selectById(activityId);
            if (activity == null) {
                redisTemplate.opsForValue().set(resultKey, SeckillResult.fail("活动不存在"), 5, TimeUnit.MINUTES);
                return;
            }

            // 2. 扣数据库库存
            int rows = seckillActivityMapper.decreaseStock(activityId);
            if (rows == 0) {
                redisTemplate.opsForValue().set(resultKey, SeckillResult.fail("库存不足"), 5, TimeUnit.MINUTES);
                return;
            }

            // 3. 创建订单（写 activityId + 真实 goodsId）
            SeckillOrder order = new SeckillOrder();
            order.setUserId(userId);
            order.setActivityId(activityId);
            order.setProductId(activity.getProductId());
            order.setOrderNo(generateOrderNo());
            order.setCreateTime(LocalDateTime.now());
            seckillOrderMapper.insert(order);

            // 4. 更新结果为成功
            redisTemplate.opsForValue().set(resultKey, SeckillResult.success(order.getOrderNo()), 5, TimeUnit.MINUTES);

        } catch (Exception e) {
            log.error("秒杀消费异常, requestId={}, activityId={}, userId={}",
                    requestId, activityId, userId, e);    // ← 加这行
            redisTemplate.opsForValue().set(resultKey, SeckillResult.fail("系统繁忙"), 5, TimeUnit.MINUTES);
        }
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + String.format("%03d", (int) (Math.random() * 1000));
    }
}