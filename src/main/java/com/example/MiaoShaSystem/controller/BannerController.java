package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.entity.Banner;
import com.example.MiaoShaSystem.service.IBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lzx
 * @since 2026-10-07
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private IBannerService bannerService;

    @GetMapping("/list")
    public Result list() {
        List<Banner> all = bannerService.list(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, (byte) 1)
                        .orderByAsc(Banner::getSortOrder)
        );

        Map<String, List<Banner>> grouped = all.stream()
                .collect(Collectors.groupingBy(b -> b.getType() != null ? b.getType() : "carousel"));

        Map<String, Object> data = new HashMap<>();
        data.put("carousel", grouped.getOrDefault("carousel", Collections.emptyList()));
        data.put("wallLeft", grouped.getOrDefault("wall_left", Collections.emptyList()));
        data.put("wallMid", grouped.getOrDefault("wall_mid", Collections.emptyList()));
        data.put("wallRight", grouped.getOrDefault("wall_right", Collections.emptyList()));

        return Result.success(data);
    }
}