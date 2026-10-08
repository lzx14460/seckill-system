package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/dashboard")
public class MerchantDashboardController {

    @Autowired
    private IDashboardService dashboardService;

    @GetMapping("/stats")
    public Result stats() {
        Long merchantId = CurrentUserUtil.getUserId();
        return Result.success(dashboardService.getStats(merchantId));
    }
}