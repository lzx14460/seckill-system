package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.OrderQueryDTO;
import com.example.MiaoShaSystem.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/order")
public class UserOrderController {

    @Autowired
    private ISeckillOrderService orderService;

    /** 我的订单列表 */
    @GetMapping("/list")
    public Result list(OrderQueryDTO dto) {
        Long userId = CurrentUserUtil.getUserId();
        return Result.success(orderService.pageUserOrders(userId, dto));
    }

    /** 取消订单（仅待发货可取消） */
    @PutMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getUserId();
        orderService.cancelByUser(id, userId);
        return Result.success("已取消", null);
    }

    /** 订单统计 */
    @GetMapping("/stats")
    public Result stats() {
        Long userId = CurrentUserUtil.getUserId();
        return Result.success(orderService.getUserStats(userId));
    }
}