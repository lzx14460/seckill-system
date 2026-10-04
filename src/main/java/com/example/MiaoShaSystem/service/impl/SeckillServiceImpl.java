package com.example.MiaoShaSystem.service.impl;

import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.config.RabbitMQConfig;
import com.example.MiaoShaSystem.entity.SeckillGoods;
import com.example.MiaoShaSystem.mapper.SeckillGoodsMapper;
import com.example.MiaoShaSystem.mq.SeckillMessage;
import com.example.MiaoShaSystem.service.ISeckillService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现类
 * <p>
 * 核心流程：
 * 1. 校验秒杀时间
 * 2. 防重复秒杀（Redis SETNX）
 * 3. Lua 脚本原子扣减库存
 * 4. 发送消息到 RabbitMQ，异步下单
 */
@Service
public class SeckillServiceImpl implements ISeckillService {

    /** 业务代码操作 Redis 用 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 发送消息到 RabbitMQ 用 */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /** 查秒杀商品信息用 */
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public Result seckill(Long userId, Long seckillGoodsId) {

        // ========== 1. 校验秒杀时间 ==========
        SeckillGoods goods = seckillGoodsMapper.selectById(seckillGoodsId);
        if (goods == null) {
            return Result.fail("秒杀商品不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(goods.getStartTime())) {
            return Result.fail(ResultCode.SECKILL_NOT_START);
        }
        if (now.isAfter(goods.getEndTime())) {
            return Result.fail(ResultCode.SECKILL_END);
        }

        // ========== 2. 防重复秒杀 ==========
        // 用 SETNX：key 不存在时才设置成功
        // 同一个用户对同一个商品只能秒杀一次
        String userKey = "seckill:user:" + userId + ":" + seckillGoodsId;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(userKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(success)) {
            return Result.fail(ResultCode.REPEAT_SECKILL);
        }

        // ========== 3. Lua 脚本原子扣减库存 ==========
        String stockKey = "seckill:stock:" + seckillGoodsId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(loadLuaScript());
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(stockKey));

        if (result == null || result == 0) {
            return Result.fail(ResultCode.STOCK_EMPTY);
        }

        // ========== 4. 发送消息到 RabbitMQ，异步下单 ==========
        SeckillMessage message = new SeckillMessage();
        message.setUserId(userId);
        message.setSeckillGoodsId(seckillGoodsId);

        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_QUEUE, message);

        return Result.success("秒杀成功，订单处理中", null);
    }

    /**
     * 从 resources/lua/seckill.lua 读取 Lua 脚本内容
     */
    private String loadLuaScript() {
        try {
            ClassPathResource resource = new ClassPathResource("lua/seckill.lua");
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("读取 Lua 脚本失败", e);
        }
    }
}