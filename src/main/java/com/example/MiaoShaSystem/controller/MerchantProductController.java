package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.ProductQueryDTO;
import com.example.MiaoShaSystem.dto.ProductSaveDTO;
import com.example.MiaoShaSystem.entity.Product;
import com.example.MiaoShaSystem.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/product")
public class MerchantProductController {

    @Autowired
    private IProductService productService;

    /** 分页查询自己的商品 */
    @GetMapping("/list")
    public Result list(ProductQueryDTO dto) {
        return Result.success(productService.pageQuery(dto));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.success(productService.getOwnedProduct(id));
    }

    /** 新增或编辑 */
    @PostMapping
    public Result save(@RequestBody ProductSaveDTO dto) {
        return Result.success(productService.saveOrUpdateByMerchant(dto));
    }

    /** 上下架 */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam Byte status) {
        return Result.success(productService.updateStatus(id, status));
    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(productService.softDelete(id));
    }
}