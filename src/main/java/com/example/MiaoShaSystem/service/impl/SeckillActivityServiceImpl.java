package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.dto.SeckillActivitySaveDTO;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.SeckillActivityMapper;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import com.example.MiaoShaSystem.service.ISeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements ISeckillActivityService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private volatile String addStockLua;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @Transactional
    public Long addStock(Long activityId, Integer count) {
        if (count == null || count <= 0) {
            throw new BizException("补货数量必须大于0");
        }

        // 1. 校验活动存在 + 归属
        SeckillActivity activity = getById(activityId);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        if (activity.getMerchantId() == null
                || !activity.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.ACTIVITY_NOT_OWNED);
        }

        // 2. 只有进行中的活动能补货
        if (activity.getStatus() == null || activity.getStatus() != (byte) 1) {
            throw new BizException("只有进行中的活动可以补货");
        }

        // 3. Lua INCRBY（原子加）
        String stockKey = "seckill:stock:" + activityId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(loadAddStockLua());
        script.setResultType(Long.class);

        Long newStock = stringRedisTemplate.execute(
                script,
                Collections.singletonList(stockKey),
                String.valueOf(count)
        );

        if (newStock == null || newStock < 0) {
            throw new BizException("补货失败，活动库存未初始化");
        }

        // 4. 同步 DB
        activity.setStock(activity.getStock() + count);
        activity.setTotalStock(activity.getTotalStock() + count);
        updateById(activity);

        return newStock;
    }

    @Override
    public IPage<SeckillOrder> pageOrdersByActivity(Long activityId, Integer pageNum, Integer pageSize) {
        // 1. 校验活动归属
        SeckillActivity activity = getById(activityId);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        if (activity.getMerchantId() == null
                || !activity.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.ACTIVITY_NOT_OWNED);
        }

        // 2. 查订单（新老兼容）
        LambdaQueryWrapper<SeckillOrder> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w
                .eq(SeckillOrder::getActivityId, activityId)
                .or(sub -> sub
                        .isNull(SeckillOrder::getActivityId)
                        .eq(SeckillOrder::getProductId, activity.getProductId())
                )
        );
        qw.orderByDesc(SeckillOrder::getCreateTime);

        return seckillOrderMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    @Override
    public IPage<SeckillActivity> pageActivities(Integer pageNum, Integer pageSize, Byte status, String name) {
        LambdaQueryWrapper<SeckillActivity> qw = new LambdaQueryWrapper<>();
        qw.eq(SeckillActivity::getMerchantId, CurrentUserUtil.getUserId());
        if (status != null) {
            qw.eq(SeckillActivity::getStatus, status);
        }
        if (name != null && !name.isEmpty()) {
            qw.like(SeckillActivity::getName, name);
        }
        qw.orderByDesc(SeckillActivity::getStartTime);
        return page(new Page<>(pageNum, pageSize), qw);
    }

    @Override
    @Transactional
    public Long createOrUpdateActivity(SeckillActivitySaveDTO dto) {
        // 1. 校验
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BizException("开始/结束时间不能为空");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new BizException("开始时间必须早于结束时间");
        }
        if (dto.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BizException("结束时间必须晚于当前时间");
        }
        if (dto.getSeckillPrice() == null || dto.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("秒杀价必须大于0");
        }
        if (dto.getTotalStock() == null || dto.getTotalStock() <= 0) {
            throw new BizException("库存必须大于0");
        }

        Long userId = CurrentUserUtil.getUserId();

        if (dto.getId() == null) {
            // 新建
            SeckillActivity activity = new SeckillActivity();
            activity.setMerchantId(userId);
            activity.setProductId(dto.getProductId());
            activity.setName(dto.getName());
            activity.setSeckillPrice(dto.getSeckillPrice());
            activity.setStock(dto.getTotalStock());
            activity.setTotalStock(dto.getTotalStock());
            activity.setStartTime(dto.getStartTime());
            activity.setEndTime(dto.getEndTime());
            activity.setStatus((byte) 0);
            save(activity);
            return activity.getId();
        } else {
            // 编辑
            SeckillActivity activity = getById(dto.getId());
            if (activity == null) {
                throw new BizException("活动不存在");
            }
            if (activity.getMerchantId() == null
                    || !activity.getMerchantId().equals(userId)) {
                throw new BizException(ResultCode.ACTIVITY_NOT_OWNED);
            }
            if (activity.getStatus() == null || activity.getStatus() != (byte) 0) {
                throw new BizException("只有未开始的活动可以编辑");
            }
            activity.setName(dto.getName());
            activity.setSeckillPrice(dto.getSeckillPrice());
            activity.setStock(dto.getTotalStock());
            activity.setTotalStock(dto.getTotalStock());
            activity.setStartTime(dto.getStartTime());
            activity.setEndTime(dto.getEndTime());
            updateById(activity);
            return activity.getId();
        }
    }

    private String loadAddStockLua() {
        if (addStockLua == null) {
            synchronized (this) {
                if (addStockLua == null) {
                    try {
                        ClassPathResource resource = new ClassPathResource("lua/addStock.lua");
                        byte[] bytes = resource.getInputStream().readAllBytes();
                        addStockLua = new String(bytes, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw new RuntimeException("读取 addStock.lua 失败", e);
                    }
                }
            }
        }
        return addStockLua;

    }
    @Override
    @Transactional
    public void cancelActivity(Long activityId) {
        // 1. 校验
        SeckillActivity activity = getById(activityId);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        if (activity.getMerchantId() == null
                || !activity.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.ACTIVITY_NOT_OWNED);
        }
        if (activity.getStatus() == null) {
            throw new BizException("活动状态异常");
        }
        // 只有未开始 / 进行中可以取消
        if (activity.getStatus() != (byte) 0 && activity.getStatus() != (byte) 1) {
            throw new BizException("只有未开始或进行中的活动可以取消");
        }

        // 2. 如果进行中，清理 Redis 库存
        if (activity.getStatus() == (byte) 1) {
            stringRedisTemplate.delete("seckill:stock:" + activityId);
        }

        // 3. 改成已取消
        activity.setStatus((byte) 3);
        updateById(activity);
    }
}