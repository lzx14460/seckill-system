package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.dto.OrderQueryDTO;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.SeckillActivityMapper;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import com.example.MiaoShaSystem.service.ISeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.MiaoShaSystem.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Override
    public IPage<OrderVO> pageMerchantOrders(OrderQueryDTO dto) {
        Long merchantId = CurrentUserUtil.getUserId();

        LambdaQueryWrapper<SeckillOrder> qw = new LambdaQueryWrapper<>();
        qw.eq(SeckillOrder::getMerchantId, merchantId);
        qw.eq(dto.getStatus() != null, SeckillOrder::getStatus, dto.getStatus());
        qw.like(StringUtils.hasText(dto.getOrderNo()), SeckillOrder::getOrderNo, dto.getOrderNo());
        qw.orderByDesc(SeckillOrder::getCreateTime);

        IPage<SeckillOrder> orderPage = page(
                new Page<>(dto.getPageNum(), dto.getPageSize()), qw
        );

        // 转换 VO
        List<OrderVO> voList = orderPage.getRecords().stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(o.getId());
            vo.setOrderNo(o.getOrderNo());
            vo.setUserId(o.getUserId());
            vo.setActivityId(o.getActivityId());
            vo.setProductId(o.getProductId());
            vo.setProductTitle(o.getProductTitle());
            vo.setSeckillPrice(o.getSeckillPrice());
            vo.setStatus(o.getStatus());
            vo.setCreateTime(o.getCreateTime());
            return vo;
        }).toList();

        // 批量补活动名
        Set<Long> activityIds = voList.stream()
                .map(OrderVO::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!activityIds.isEmpty()) {
            List<SeckillActivity> activities = seckillActivityMapper.selectBatchIds(activityIds);
            Map<Long, String> nameMap = activities.stream()
                    .collect(Collectors.toMap(SeckillActivity::getId, SeckillActivity::getName));
            voList.forEach(v -> v.setActivityName(nameMap.get(v.getActivityId())));
        }

        Page<OrderVO> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, Byte status) {
        if (status == null || status < 0 || status > 3) {
            throw new BizException("订单状态不合法");
        }

        SeckillOrder order = getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getMerchantId() == null
                || !order.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.NO_PERMISSION);
        }

        // 状态流转校验
        Byte current = order.getStatus();
        if (current != null) {
            // 已完成 / 已取消 → 不能再改
            if (current == 2 || current == 3) {
                throw new BizException("订单已完成或已取消，不能修改");
            }
            // 待发货 → 已发货 / 已取消
            // 已发货 → 已完成 / 已取消
        }

        order.setStatus(status);
        updateById(order);
    }
    @Override
    public IPage<OrderVO> pageUserOrders(Long userId, OrderQueryDTO dto) {
        LambdaQueryWrapper<SeckillOrder> qw = new LambdaQueryWrapper<>();
        qw.eq(SeckillOrder::getUserId, userId);
        qw.eq(dto.getStatus() != null, SeckillOrder::getStatus, dto.getStatus());
        qw.like(StringUtils.hasText(dto.getOrderNo()), SeckillOrder::getOrderNo, dto.getOrderNo());
        qw.orderByDesc(SeckillOrder::getCreateTime);

        IPage<SeckillOrder> orderPage = page(
                new Page<>(dto.getPageNum(), dto.getPageSize()), qw
        );

        // 转 VO
        List<OrderVO> voList = orderPage.getRecords().stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(o.getId());
            vo.setOrderNo(o.getOrderNo());
            vo.setUserId(o.getUserId());
            vo.setActivityId(o.getActivityId());
            vo.setProductId(o.getProductId());
            vo.setProductTitle(o.getProductTitle());
            vo.setSeckillPrice(o.getSeckillPrice());
            vo.setStatus(o.getStatus());
            vo.setCreateTime(o.getCreateTime());
            vo.setCoverImg(o.getCoverImg());              // ← 加
            return vo;
        }).collect(Collectors.toList());

        // 批量补活动名
        Set<Long> activityIds = voList.stream()
                .map(OrderVO::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!activityIds.isEmpty()) {
            List<SeckillActivity> activities = seckillActivityMapper.selectBatchIds(activityIds);
            Map<Long, String> nameMap = activities.stream()
                    .collect(Collectors.toMap(SeckillActivity::getId, SeckillActivity::getName));
            voList.forEach(v -> v.setActivityName(nameMap.get(v.getActivityId())));
        }

        Page<OrderVO> result = new Page<>(
                orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal()
        );
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional
    public void cancelByUser(Long orderId, Long userId) {
        SeckillOrder order = getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NO_PERMISSION);
        }
        // 只有待发货可取消
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new BizException("只有待发货订单可以取消");
        }
        order.setStatus((byte) 3);
        updateById(order);
    }

    @Override
    public Map<String, Long> getUserStats(Long userId) {
        LambdaQueryWrapper<SeckillOrder> base = new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getUserId, userId);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", count(base.clone()));
        stats.put("pending", count(base.clone().eq(SeckillOrder::getStatus, (byte) 0)));
        stats.put("shipping", count(base.clone().eq(SeckillOrder::getStatus, (byte) 1)));
        stats.put("done", count(base.clone().eq(SeckillOrder::getStatus, (byte) 2)));
        stats.put("canceled", count(base.clone().eq(SeckillOrder::getStatus, (byte) 3)));
        return stats;
    }
}
