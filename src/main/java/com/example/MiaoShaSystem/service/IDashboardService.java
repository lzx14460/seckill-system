package com.example.MiaoShaSystem.service;

import com.example.MiaoShaSystem.vo.DashboardVO;

public interface IDashboardService {
    DashboardVO getStats(Long merchantId);
}