package com.example.MiaoShaSystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.MiaoShaSystem.dto.OrderQueryDTO;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.MiaoShaSystem.vo.OrderVO;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {
    IPage<OrderVO> pageMerchantOrders(OrderQueryDTO dto);

    void updateStatus(Long orderId, Byte status);
    IPage<OrderVO> pageUserOrders(Long userId, OrderQueryDTO dto);

    void cancelByUser(Long orderId, Long userId);

    Map<String, Long> getUserStats(Long userId);   // { total, pending, done }
}
