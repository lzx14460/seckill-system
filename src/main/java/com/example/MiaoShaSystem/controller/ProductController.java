package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.entity.Product;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.service.IProductService;
import com.example.MiaoShaSystem.service.ISeckillActivityService;
import com.example.MiaoShaSystem.service.IUserService;
import com.example.MiaoShaSystem.vo.ProductWithSeckillVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private ISeckillActivityService seckillActivityService;

    @Autowired
    private IUserService userService;

    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        // 1. 分页查上架商品
        IPage<Product> productPage = productService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, (byte) 1)
                        .orderByDesc(Product::getCreatedAt)
        );

        List<Product> products = productPage.getRecords();
        if (products.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("records", Collections.emptyList());
            empty.put("total", 0L);
            empty.put("pages", 0L);
            empty.put("current", pageNum);
            return Result.success(empty);
        }

        // 2. 批量查进行中的秒杀活动
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Long, SeckillActivity> seckillMap = new HashMap<>();
        List<SeckillActivity> activities = seckillActivityService.list(
                new LambdaQueryWrapper<SeckillActivity>()
                        .in(SeckillActivity::getProductId, productIds)
                        .eq(SeckillActivity::getStatus, (byte) 1)
        );
        for (SeckillActivity a : activities) {
            seckillMap.put(a.getProductId(), a);
        }

        // 3. 批量查商家店铺名
        Set<Long> merchantIds = products.stream()
                .map(Product::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> merchantNameMap = new HashMap<>();
        if (!merchantIds.isEmpty()) {
            List<User> merchants = userService.listByIds(merchantIds);
            for (User u : merchants) {
                String name = (u.getShopName() != null && !u.getShopName().isEmpty())
                        ? u.getShopName()
                        : u.getUsername();
                merchantNameMap.put(u.getId(), name);
            }
        }

        // 4. 组装 VO
        List<ProductWithSeckillVO> records = products.stream()
                .map(p -> {
                    ProductWithSeckillVO vo = new ProductWithSeckillVO();
                    vo.setId(p.getId());
                    vo.setTitle(p.getTitle());
                    vo.setSubTitle(p.getSubTitle());
                    vo.setCoverImg(p.getCoverImg());
                    vo.setPrice(p.getPrice());
                    vo.setStock(p.getStock());
                    vo.setShopName(merchantNameMap.getOrDefault(p.getMerchantId(), "官方店铺"));

                    SeckillActivity a = seckillMap.get(p.getId());
                    if (a != null) {
                        ProductWithSeckillVO.SeckillInfo info = new ProductWithSeckillVO.SeckillInfo();
                        info.setActivityId(a.getId());
                        info.setSeckillPrice(a.getSeckillPrice());
                        info.setStock(a.getStock());
                        info.setTotalStock(a.getTotalStock());
                        info.setStartTime(a.getStartTime());
                        info.setEndTime(a.getEndTime());
                        info.setStatus(a.getStatus());
                        vo.setSeckill(info);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", productPage.getTotal());
        data.put("pages", productPage.getPages());
        data.put("current", productPage.getCurrent());
        return Result.success(data);
    }
}