package com.example.MiaoShaSystem.mq;

import com.example.MiaoShaSystem.config.RabbitMQConfig;
import com.example.MiaoShaSystem.entity.SeckillGoods;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.SeckillGoodsMapper;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 秒杀消息消费者
 * <p>
 * 监听 seckill.queue 队列，收到消息后异步创建订单。
 * 这样秒杀接口不用等数据库操作，直接返回。
 */
@Component
public class SeckillConsumer {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 监听队列，有消息时自动调用
     *
     * @param message 消息对象，Spring AMQP 自动从 JSON 反序列化
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handle(SeckillMessage message) {
        Long userId = message.getUserId();
        Long seckillGoodsId = message.getSeckillGoodsId();

        // 1. 查秒杀商品信息
        SeckillGoods goods = seckillGoodsMapper.selectById(seckillGoodsId);
        if (goods == null) {
            return;
        }

        // 2. 扣减数据库库存
        // 用 SQL 直接扣，并带上 stock > 0 条件，防止数据库层面超卖
        int rows = seckillGoodsMapper.decreaseStock(seckillGoodsId);
        if (rows == 0) {
            // 数据库库存已经为 0，说明被其他渠道扣完了，不创建订单
            return;
        }

        // 3. 创建订单
        SeckillOrder order = new SeckillOrder();
        order.setUserId(userId);
        order.setGoodsId(seckillGoodsId);
        order.setOrderNo(generateOrderNo());
        order.setCreateTime(LocalDateTime.now());
        seckillOrderMapper.insert(order);
    }

    /**
     * 生成订单号：时间戳 + 三位随机数
     */
    private String generateOrderNo() {
        return System.currentTimeMillis() + String.format("%03d", (int) (Math.random() * 1000));
    }
}