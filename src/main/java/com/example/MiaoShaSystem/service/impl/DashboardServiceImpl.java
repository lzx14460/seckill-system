package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.entity.Product;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.ProductMapper;
import com.example.MiaoShaSystem.mapper.SeckillActivityMapper;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import com.example.MiaoShaSystem.service.IDashboardService;
import com.example.MiaoShaSystem.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private SeckillOrderMapper orderMapper;

    @Autowired
    private SeckillActivityMapper activityMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public DashboardVO getStats(Long merchantId) {
        DashboardVO vo = new DashboardVO();

        // 1. 今日时间范围
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 2. 找出该商家的所有活动 id
        List<Long> activityIds = activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getMerchantId, merchantId)
                        .select(SeckillActivity::getId)
        ).stream().map(SeckillActivity::getId).toList();

        // 3. 今日订单数
        Long todayOrderCount = 0L;
        BigDecimal todaySales = BigDecimal.ZERO;
        if (!activityIds.isEmpty()) {
            List<SeckillOrder> todayOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<SeckillOrder>()
                            .in(SeckillOrder::getActivityId, activityIds)
                            .between(SeckillOrder::getCreateTime, todayStart, todayEnd)
            );
            todayOrderCount = (long) todayOrders.size();
            // 销售额需要从活动里查秒杀价 —— 这里简化，用活动秒杀价 × 订单数
            for (SeckillOrder o : todayOrders) {
                SeckillActivity a = activityMapper.selectById(o.getActivityId());
                if (a != null && a.getSeckillPrice() != null) {
                    todaySales = todaySales.add(a.getSeckillPrice());
                }
            }
        }
        vo.setTodayOrderCount(todayOrderCount);
        vo.setTodaySales(todaySales);

        // 4. 进行中活动数
        Long activeCount = activityMapper.selectCount(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getMerchantId, merchantId)
                        .eq(SeckillActivity::getStatus, (byte) 1)
        );
        vo.setActiveActivityCount(activeCount);

        // 5. 商品总数
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
        );
        vo.setTotalProductCount(productCount);

        // 6. 低库存商品数
        Long lowStockCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .lt(Product::getStock, 10)
        );
        vo.setLowStockCount(lowStockCount);

        // 7. 近 7 天趋势
        List<DashboardVO.DailyStat> recentDays = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(d, LocalTime.MAX);

            DashboardVO.DailyStat stat = new DashboardVO.DailyStat();
            stat.setDate(d.format(fmt));
            stat.setOrderCount(0L);
            stat.setSales(BigDecimal.ZERO);

            if (!activityIds.isEmpty()) {
                List<SeckillOrder> orders = orderMapper.selectList(
                        new LambdaQueryWrapper<SeckillOrder>()
                                .in(SeckillOrder::getActivityId, activityIds)
                                .between(SeckillOrder::getCreateTime, start, end)
                );
                stat.setOrderCount((long) orders.size());
                BigDecimal daySales = BigDecimal.ZERO;
                for (SeckillOrder o : orders) {
                    SeckillActivity a = activityMapper.selectById(o.getActivityId());
                    if (a != null && a.getSeckillPrice() != null) {
                        daySales = daySales.add(a.getSeckillPrice());
                    }
                }
                stat.setSales(daySales);
            }
            recentDays.add(stat);
        }
        vo.setRecentDays(recentDays);

        return vo;
    }
}