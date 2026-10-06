package com.example.MiaoShaSystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.MiaoShaSystem.dto.SeckillActivitySaveDTO;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.MiaoShaSystem.entity.SeckillOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzx
 * @since 2026-10-04
 */
public interface ISeckillGoodsService extends IService<SeckillActivity> {
    /**
     * 补货
     * @param seckillGoodsId 活动 id
     * @param count 补货数量
     * @return 补货后的 Redis 剩余库存
     */
    Long addStock(Long seckillGoodsId, Integer count);
    /**
     * 分页查询某活动的订单
     */
    IPage<SeckillOrder> pageOrdersByActivity(Long activityId, Integer pageNum, Integer pageSize);
    IPage<SeckillActivity> pageActivities(Integer pageNum, Integer pageSize, Byte status, String name);
    Long createOrUpdateActivity(SeckillActivitySaveDTO dto);
}
