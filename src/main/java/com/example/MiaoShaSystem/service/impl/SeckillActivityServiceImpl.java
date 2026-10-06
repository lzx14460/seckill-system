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
import com.example.MiaoShaSystem.service.ISeckillGoodsService;
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
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzx
 * @since 2026-10-04
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements ISeckillGoodsService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private volatile String addStockLua;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @Transactional
    public Long addStock(Long seckillGoodsId, Integer count) {
        if (count == null || count <= 0) {
            throw new BizException("补货数量必须大于0");
        }

        // 1. 校验活动存在 + 归属
        SeckillActivity goods = getById(seckillGoodsId);
        if (goods == null) {
            throw new BizException("活动不存在");
        }
        if (goods.getMerchantId() == null
                || !goods.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.NO_PERMISSION);
        }

        // 2. 只有进行中的活动能补货
        if (goods.getStatus() == null || goods.getStatus() != 1) {
            throw new BizException("只有进行中的活动可以补货");
        }

        // 3. Lua INCRBY（原子加）
        String stockKey = "seckill:stock:" + seckillGoodsId;
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
        goods.setStock(goods.getStock() + count);
        goods.setTotalStock(goods.getTotalStock() + count);
        updateById(goods);

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
            throw new BizException(ResultCode.NO_PERMISSION);
        }

        // 2. 查订单（新老兼容）
        LambdaQueryWrapper<SeckillOrder> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w
                .eq(SeckillOrder::getActivityId, activityId)
                .or(sub -> sub
                        .isNull(SeckillOrder::getActivityId)
                        .eq(SeckillOrder::getGoodsId, activity.getGoodsId())
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
    public Long createOrUpdateActivity(SeckillActivitySaveDTO dto) {
        // 1. 校验时间
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
            activity.setGoodsId(dto.getProductId());   // 也可以 = dto.getGoodsId()
            activity.setName(dto.getName());
            activity.setSeckillPrice(dto.getSeckillPrice());
            activity.setStock(dto.getTotalStock());
            activity.setTotalStock(dto.getTotalStock());
            activity.setStartTime(dto.getStartTime());
            activity.setEndTime(dto.getEndTime());
            activity.setStatus((byte) 0);   // 未开始
            save(activity);
            return activity.getId();
        } else {
            // 编辑：只能改未开始的活动
            SeckillActivity activity = getById(dto.getId());
            if (activity == null) {
                throw new BizException("活动不存在");
            }
            if (!activity.getMerchantId().equals(userId)) {
                throw new BizException(ResultCode.NO_PERMISSION);
            }
            if (activity.getStatus() != 0) {
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
}