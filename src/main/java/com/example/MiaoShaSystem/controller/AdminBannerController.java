package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.entity.Banner;
import com.example.MiaoShaSystem.service.IBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.example.MiaoShaSystem.annotation.OpLog;
@RestController
@RequestMapping("/api/admin/banner")
public class AdminBannerController {

    @Autowired
    private IBannerService bannerService;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String type) {
        LambdaQueryWrapper<Banner> qw = new LambdaQueryWrapper<Banner>()
                .eq(StringUtils.hasText(type), Banner::getType, type)
                .orderByAsc(Banner::getSortOrder);
        return Result.success(bannerService.list(qw));
    }

    @PostMapping
    @OpLog(action = "SAVE_BANNER", targetType = "BANNER")

    public Result save(@RequestBody Banner banner) {
        if (banner.getStatus() == null) banner.setStatus((byte) 1);
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        bannerService.saveOrUpdate(banner);
        return Result.success("保存成功", banner.getId());
    }

    @DeleteMapping("/{id}")
    @OpLog(action = "SAVE_BANNER", targetType = "BANNER")

    public Result delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success("已删除", null);
    }
}