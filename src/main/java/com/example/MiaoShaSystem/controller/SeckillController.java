package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.service.ISeckillService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀接口
 */
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private ISeckillService seckillService;

    /**
     * 执行秒杀
     * <p>
     * 路径示例：POST /seckill/1
     * 1 是秒杀商品 ID
     *
     * @param seckillGoodsId 秒杀商品 ID，从 URL 路径取
     * @param session        当前会话，用来取 userId
     * @return 秒杀结果
     */
    @PostMapping("/{seckillGoodsId}")
    public Result seckill(@PathVariable Long seckillGoodsId, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        Long userId = ((Number) userIdObj).longValue();

        // 调用 Service 执行秒杀
        return seckillService.seckill(userId, seckillGoodsId);
    }
}