package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.OrderQueryDTO;
import com.example.MiaoShaSystem.entity.SeckillOrder;
import com.example.MiaoShaSystem.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.excel.EasyExcel;
import com.example.MiaoShaSystem.vo.MerchantOrderExcelVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    @Autowired
    private ISeckillOrderService orderService;

    /** 订单列表 */
    @GetMapping("/list")
    public Result list(OrderQueryDTO dto) {
        return Result.success(orderService.pageMerchantOrders(dto));
    }

    /** 改订单状态（发货 / 完成 / 取消） */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam Byte status) {
        orderService.updateStatus(id, status);
        return Result.success("更新成功", null);
    }
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        // 1. 查商家所有订单
        Long merchantId = CurrentUserUtil.getUserId();

        List<SeckillOrder> orders = orderService.list(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getMerchantId, merchantId)
                        .orderByDesc(SeckillOrder::getCreateTime)
        );

        // 2. 转 VO
        List<MerchantOrderExcelVO> excelList = orders.stream().map(o -> {
            MerchantOrderExcelVO vo = new MerchantOrderExcelVO();
            vo.setOrderNo(o.getOrderNo());
            vo.setProductTitle(o.getProductTitle());
            vo.setSeckillPrice(o.getSeckillPrice() != null ? "¥" + o.getSeckillPrice() : "—");
            vo.setUserId(o.getUserId());
            vo.setStatusText(statusText(o.getStatus()));
            vo.setCreateTime(o.getCreateTime() != null
                    ? o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "—");
            return vo;
        }).collect(Collectors.toList());

        // 3. 写 Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("订单_" + System.currentTimeMillis(), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), MerchantOrderExcelVO.class)
                .sheet("订单")
                .doWrite(excelList);
    }

    private String statusText(Byte s) {
        return new String[]{"待发货", "已发货", "已完成", "已取消"}[s != null && s < 4 ? s : 0];
    }
}