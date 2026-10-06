package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.dto.SeckillActivitySaveDTO;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import com.example.MiaoShaSystem.service.ISeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/seckill")
public class MerchantSeckillController {

    @Autowired
    private ISeckillActivityService seckillGoodsService;

    /** 活动列表 */
    @GetMapping("/list")
    public Result listActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String name) {
        return Result.success(seckillGoodsService.pageActivities(pageNum, pageSize, status, name));
    }

    /** 活动详情 */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        SeckillActivity activity = seckillGoodsService.getById(id);
        if (activity == null) {
            throw new BizException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        if (activity.getMerchantId() == null
                || !activity.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.ACTIVITY_NOT_OWNED);   // ← 3004
        }
        return Result.success(activity);
    }

    /** 创建 / 编辑 */
    @PostMapping
    public Result createOrUpdate(@RequestBody SeckillActivitySaveDTO dto) {
        Long id = seckillGoodsService.createOrUpdateActivity(dto);
        return Result.success("保存成功", id);
    }

    /** 补货 */
    @PostMapping("/{id}/stock")
    public Result addStock(@PathVariable Long id, @RequestParam Integer count) {
        Long newStock = seckillGoodsService.addStock(id, count);
        return Result.success("补货成功", newStock);
    }

    /** 订单列表 */
    @GetMapping("/{id}/orders")
    public Result listOrders(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(seckillGoodsService.pageOrdersByActivity(id, pageNum, pageSize));
    }
    /**
     * 取消活动
     * DELETE /api/merchant/seckill/{id}
     */
    @DeleteMapping("/{id}")
    public Result cancel(@PathVariable Long id) {
        seckillGoodsService.cancelActivity(id);
        return Result.success("已取消", null);
    }
}