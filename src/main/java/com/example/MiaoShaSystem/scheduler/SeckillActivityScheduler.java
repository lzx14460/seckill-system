package com.example.MiaoShaSystem.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.service.ISeckillActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeckillActivityScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeckillActivityScheduler.class);

    @Autowired
    private ISeckillActivityService seckillActivityService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 * * * * ?")
    public void refreshStatus() {
        LocalDateTime now = LocalDateTime.now();
        startActivities(now);
        endActivities(now);
    }

    private void startActivities(LocalDateTime now) {
        List<SeckillActivity> toStart = seckillActivityService.list(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, (byte) 0)
                        .le(SeckillActivity::getStartTime, now)
                        .gt(SeckillActivity::getEndTime, now)
        );

        for (SeckillActivity a : toStart) {
            try {
                String stockKey = "seckill:stock:" + a.getId();
                Boolean success = stringRedisTemplate.opsForValue()
                        .setIfAbsent(stockKey, String.valueOf(a.getStock()));
                if (Boolean.TRUE.equals(success)) {
                    log.info("活动开始: id={}, 库存已预热={}", a.getId(), a.getStock());
                }

                a.setStatus((byte) 1);
                seckillActivityService.updateById(a);
            } catch (Exception e) {
                log.error("预热活动失败: id={}", a.getId(), e);
            }
        }
    }

    private void endActivities(LocalDateTime now) {
        List<SeckillActivity> toEnd = seckillActivityService.list(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, (byte) 1)
                        .le(SeckillActivity::getEndTime, now)
        );

        for (SeckillActivity a : toEnd) {
            try {
                String stockKey = "seckill:stock:" + a.getId();
                stringRedisTemplate.delete(stockKey);

                a.setStatus((byte) 2);
                seckillActivityService.updateById(a);

                log.info("活动结束: id={}", a.getId());
            } catch (Exception e) {
                log.error("结束活动失败: id={}", a.getId(), e);
            }
        }
    }
}