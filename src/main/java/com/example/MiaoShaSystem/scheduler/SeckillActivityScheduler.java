package com.example.MiaoShaSystem.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.service.ISeckillActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeckillGoodsScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeckillGoodsScheduler.class);

    @Autowired
    private ISeckillActivityService seckillGoodsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 每分钟执行：
     * 1. 把到点的活动从 0 → 1（预热 Redis）
     * 2. 把过期的活动从 1 → 2（清理 Redis）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void refreshStatus() {
        LocalDateTime now = LocalDateTime.now();
        startActivities(now);
        endActivities(now);
    }

    /**
     * 0 → 1：未开始 → 进行中
     * 条件：status=0 且 start_time <= now 且 end_time > now
     */
    private void startActivities(LocalDateTime now) {
        List<SeckillActivity> toStart = seckillGoodsService.list(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 0)
                        .le(SeckillActivity::getStartTime, now)
                        .gt(SeckillActivity::getEndTime, now)
        );

        for (SeckillActivity g : toStart) {
            try {
                String stockKey = "seckill:stock:" + g.getId();
                redisTemplate.opsForValue().set(stockKey, g.getStock());

                g.setStatus((byte) 1);
                seckillGoodsService.updateById(g);

                log.info("活动开始: id={}, title={}, 库存已预热={}", g.getId(), g.getName(), g.getStock());
            } catch (Exception e) {
                log.error("预热活动失败: id={}", g.getId(), e);
            }
        }
    }

    /**
     * 1 → 2：进行中 → 已结束
     * 条件：status=1 且 end_time <= now
     */
    private void endActivities(LocalDateTime now) {
        List<SeckillActivity> toEnd = seckillGoodsService.list(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 1)
                        .le(SeckillActivity::getEndTime, now)
        );

        for (SeckillActivity g : toEnd) {
            try {
                String stockKey = "seckill:stock:" + g.getId();
                redisTemplate.delete(stockKey);

                g.setStatus((byte) 2);
                seckillGoodsService.updateById(g);

                log.info("活动结束: id={}, title={}", g.getId(), g.getName());
            } catch (Exception e) {
                log.error("结束活动失败: id={}", g.getId(), e);
            }
        }
    }
}