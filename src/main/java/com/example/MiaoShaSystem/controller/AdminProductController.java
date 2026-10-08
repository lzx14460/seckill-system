package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.ProductAuditDTO;
import com.example.MiaoShaSystem.entity.Product;
import com.example.MiaoShaSystem.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.example.MiaoShaSystem.annotation.OpLog;@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    @Autowired
    private IProductService productService;

    /** 商品列表 */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Byte auditStatus,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(auditStatus != null, Product::getAuditStatus, auditStatus)
                .like(StringUtils.hasText(keyword), Product::getTitle, keyword)
                .orderByDesc(Product::getCreatedAt);

        IPage<Product> page = productService.page(new Page<>(pageNum, pageSize), qw);
        return Result.success(page);
    }

    /** 审核商品 */
    @PutMapping("/{id}/audit")
    public Result audit(@PathVariable Long id, @RequestBody ProductAuditDTO dto) {
        if (dto.getStatus() == null || (dto.getStatus() != 1 && dto.getStatus() != 2)) {
            throw new BizException("审核状态不合法");
        }

        Product product = productService.getById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }

        product.setAuditStatus(dto.getStatus());
        product.setAuditRemark(dto.getRemark());
        productService.updateById(product);

        return Result.success(dto.getStatus() == 1 ? "已通过" : "已拒绝", null);
    }

    /** 强制下架（管理员专用） */
    @PutMapping("/{id}/force-off")
    @OpLog(action = "AUDIT_PRODUCT", targetType = "PRODUCT")

    public Result forceOff(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        product.setStatus((byte) 0);   // 下架
        productService.updateById(product);
        return Result.success("已强制下架", null);
    }
}