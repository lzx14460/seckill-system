package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.dto.ProductQueryDTO;
import com.example.MiaoShaSystem.dto.ProductSaveDTO;
import com.example.MiaoShaSystem.entity.Product;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.ProductMapper;
import com.example.MiaoShaSystem.mapper.SeckillActivityMapper;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import com.example.MiaoShaSystem.service.IProductService;
import com.example.MiaoShaSystem.vo.ProductStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    public IPage<Product> pageQuery(ProductQueryDTO dto) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getMerchantId, CurrentUserUtil.getUserId());
        qw.like(StringUtils.hasText(dto.getTitle()), Product::getTitle, dto.getTitle());
        qw.eq(dto.getStatus() != null, Product::getStatus, dto.getStatus());
        qw.orderByDesc(Product::getCreatedAt);
        qw.eq(StringUtils.hasText(dto.getCategory()), Product::getCategory, dto.getCategory());
        return page(new Page<>(dto.getPageNum(), dto.getPageSize()), qw);
    }

    @Override
    public Product getOwnedProduct(Long id) {
        Product p = getById(id);
        if (p == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!p.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.PRODUCT_NOT_OWNED);
        }
        return p;
    }

    @Override
    @Transactional
    public boolean saveOrUpdateByMerchant(ProductSaveDTO dto) {
        Product product;
        if (dto.getId() == null) {
            product = new Product();
            product.setMerchantId(CurrentUserUtil.getUserId());
            product.setStatus((byte) 1);
            product.setAuditStatus((byte) 0);
            product.setAuditRemark(null);
        } else {
            product = getOwnedProduct(dto.getId());
        }
        product.setCategory(dto.getCategory());
        product.setTitle(dto.getTitle());
        product.setSubTitle(dto.getSubTitle());
        product.setCoverImg(dto.getCoverImg());
        product.setDetail(dto.getDetail());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setAuditStatus((byte) 0);   // ← 编辑 → 重新审核
        product.setAuditRemark(null);

        return saveOrUpdate(product);
    }

    @Override
    public boolean updateStatus(Long id, Byte status) {
        Product p = getOwnedProduct(id);
        p.setStatus(status);
        return updateById(p);
    }

    @Override
    public boolean softDelete(Long id) {
        Product p = getOwnedProduct(id);
        p.setStatus((byte) 0);
        return updateById(p);
    }

    // ==================== 商品数据统计 ====================

    @Override
    public ProductStatsVO getStats(Long productId) {
        // 1. 校验归属
        Product product = getOwnedProduct(productId);

        ProductStatsVO vo = new ProductStatsVO();
        vo.setProductId(product.getId());
        vo.setProductTitle(product.getTitle());

        // 2. 查该商品的所有活动
        List<SeckillActivity> activities = seckillActivityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getProductId, productId)
                        .orderByDesc(SeckillActivity::getStartTime)
        );

        List<Long> activityIds = activities.stream()
                .map(SeckillActivity::getId)
                .toList();

        // 3. 每个活动的订单数
        Map<Long, Long> activityOrderCount = new HashMap<>();
        if (!activityIds.isEmpty()) {
            List<SeckillOrder> allOrders = seckillOrderMapper.selectList(
                    new LambdaQueryWrapper<SeckillOrder>()
                            .in(SeckillOrder::getActivityId, activityIds)
            );
            for (SeckillOrder o : allOrders) {
                if (o.getActivityId() != null) {
                    activityOrderCount.merge(o.getActivityId(), 1L, Long::sum);
                }
            }
        }

        // 4. 组装活动 + 累计
        List<ProductStatsVO.ActivityStat> actList = new ArrayList<>();
        BigDecimal totalSales = BigDecimal.ZERO;
        long totalOrderCount = 0L;

        for (SeckillActivity a : activities) {
            ProductStatsVO.ActivityStat stat = new ProductStatsVO.ActivityStat();
            stat.setActivityId(a.getId());
            stat.setName(a.getName());
            stat.setSeckillPrice(a.getSeckillPrice());
            stat.setStock(a.getStock());
            stat.setTotalStock(a.getTotalStock());
            stat.setStatus(a.getStatus());
            stat.setStartTime(a.getStartTime());
            stat.setEndTime(a.getEndTime());

            Long cnt = activityOrderCount.getOrDefault(a.getId(), 0L);
            stat.setOrderCount(cnt);

            totalOrderCount += cnt;
            if (a.getSeckillPrice() != null) {
                totalSales = totalSales.add(
                        a.getSeckillPrice().multiply(BigDecimal.valueOf(cnt))
                );
            }
            actList.add(stat);
        }

        vo.setActivities(actList);
        vo.setTotalOrderCount(totalOrderCount);
        vo.setTotalSales(totalSales);

        // 5. 近 7 天趋势
        List<ProductStatsVO.DailyStat> recentDays = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(d, LocalTime.MAX);

            ProductStatsVO.DailyStat ds = new ProductStatsVO.DailyStat();
            ds.setDate(d.format(fmt));
            ds.setOrderCount(0L);
            ds.setSales(BigDecimal.ZERO);

            if (!activityIds.isEmpty()) {
                List<SeckillOrder> dayOrders = seckillOrderMapper.selectList(
                        new LambdaQueryWrapper<SeckillOrder>()
                                .in(SeckillOrder::getActivityId, activityIds)
                                .between(SeckillOrder::getCreateTime, start, end)
                );
                ds.setOrderCount((long) dayOrders.size());

                BigDecimal daySales = BigDecimal.ZERO;
                for (SeckillOrder o : dayOrders) {
                    for (SeckillActivity a : activities) {
                        if (a.getId().equals(o.getActivityId()) && a.getSeckillPrice() != null) {
                            daySales = daySales.add(a.getSeckillPrice());
                            break;
                        }
                    }
                }
                ds.setSales(daySales);
            }
            recentDays.add(ds);
        }
        vo.setRecentDays(recentDays);

        return vo;
    }
}