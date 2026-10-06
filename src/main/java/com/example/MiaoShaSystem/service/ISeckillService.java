package com.example.MiaoShaSystem.service;

import com.example.MiaoShaSystem.common.Result;

public interface ISeckillService {

    Result seckill(Long userId, Long seckillGoodsId);

    Result getSeckillResult(String requestId);
}