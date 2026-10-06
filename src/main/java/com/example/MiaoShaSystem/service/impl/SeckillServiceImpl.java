package com.example.MiaoShaSystem.service.impl;

import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.common.SeckillResult;
import com.example.MiaoShaSystem.config.RabbitMQConfig;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.mapper.SeckillActivityMapper;
import com.example.MiaoShaSystem.mq.SeckillMessage;
import com.example.MiaoShaSystem.service.ISeckillService;
import com.example.MiaoShaSystem.service.IUserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements ISeckillService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;
    @Autowired
    private IUserService userService;
    @Override
    public Result seckill(Long userId, Long activityId) {
// 商家不能秒杀

        User user = userService.getById(userId);
        if (user != null && "MERCHANT".equals(user.getRole())) {
            return Result.fail("商家账号不能参与秒杀");
        }
        // 1. 校验秒杀时间
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            return Result.fail("秒杀活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            return Result.fail(ResultCode.SECKILL_NOT_START);
        }
        if (now.isAfter(activity.getEndTime())) {
            return Result.fail(ResultCode.SECKILL_END);
        }


        // 2. 防重复秒杀（TTL 到活动结束）
        long ttlSeconds = java.time.Duration.between(now, activity.getEndTime()).getSeconds();
        String userKey = "seckill:user:" + userId + ":" + activityId;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(userKey, "1", ttlSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            return Result.fail(ResultCode.REPEAT_SECKILL);
        }

// 3. Lua 脚本扣库存
        String stockKey = "seckill:stock:" + activityId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(loadLuaScript());
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(stockKey));

// ★★★ 扣库存失败 → 回滚防重 key ★★★
        if (result == null || result <= 0) {
            redisTemplate.delete(userKey);     // ← 关键：回滚
            return Result.fail(ResultCode.STOCK_EMPTY);
        }

        // 4. 生成 requestId，存初始结果到 Redis
        String requestId = UUID.randomUUID().toString().replace("-", "");
        String resultKey = "seckill:result:" + requestId;
        redisTemplate.opsForValue().set(
                resultKey,
                SeckillResult.pending(),
                5, TimeUnit.MINUTES
        );

        // 5. 发消息到 RabbitMQ，带上 requestId
        SeckillMessage message = new SeckillMessage();
        message.setUserId(userId);
        message.setActivityId(activityId);       // ← 改这一处
        message.setRequestId(requestId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_QUEUE, message);

        // 6. 返回 requestId 给前端
        Map<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        return Result.success("秒杀成功，订单处理中", data);
    }

    @Override
    public Result getSeckillResult(String requestId) {
        String resultKey = "seckill:result:" + requestId;
        Object result = redisTemplate.opsForValue().get(resultKey);
        if (result == null) {
            return Result.fail("查询结果已过期");
        }
        return Result.success(result);
    }

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