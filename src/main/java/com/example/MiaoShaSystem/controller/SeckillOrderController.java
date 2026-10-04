package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.mapper.SeckillOrderMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
/**
 * 秒杀订单接口
 */
@RestController
@RequestMapping("/seckill/order")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    /**
     * 查询当前用户的秒杀订单
     * 路径：GET /seckill/order/list
     */
    @GetMapping("/list")
    public Result list(HttpSession session) {
        // 从 Session 取当前用户 ID
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        Long userId = ((Number) userIdObj).longValue();

        // 查当前用户的订单，按时间倒序
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getUserId, userId)
                .orderByDesc(SeckillOrder::getCreateTime);

        List<SeckillOrder> list = seckillOrderMapper.selectList(wrapper);
        return Result.success(list);
    }
}