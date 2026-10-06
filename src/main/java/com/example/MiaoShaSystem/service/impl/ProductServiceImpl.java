package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.dto.ProductQueryDTO;
import com.example.MiaoShaSystem.dto.ProductSaveDTO;
import com.example.MiaoShaSystem.entity.Product;
import com.example.MiaoShaSystem.mapper.ProductMapper;
import com.example.MiaoShaSystem.service.IProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author lzx
 * @since 2026-10-05
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Override
    public IPage<Product> pageQuery(ProductQueryDTO dto) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getMerchantId, CurrentUserUtil.getUserId());
        qw.like(StringUtils.hasText(dto.getTitle()), Product::getTitle, dto.getTitle());
        qw.eq(dto.getStatus() != null, Product::getStatus, dto.getStatus());
        qw.orderByDesc(Product::getCreatedAt);
        return page(new Page<>(dto.getPageNum(), dto.getPageSize()), qw);
    }

    @Override
    public Product getOwnedProduct(Long id) {
        Product p = getById(id);
        if (p == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!p.getMerchantId().equals(CurrentUserUtil.getUserId())) {
            throw new BizException(ResultCode.PRODUCT_NOT_OWNED);
        }
        return p;
    }

    @Override
    @Transactional
    public boolean saveOrUpdateByMerchant(ProductSaveDTO dto) {
        Product product;
        if (dto.getId() == null) {
            product = new Product();
            product.setMerchantId(CurrentUserUtil.getUserId());
            product.setStatus((byte) 1);
        } else {
            product = getOwnedProduct(dto.getId());
        }

        product.setTitle(dto.getTitle());
        product.setSubTitle(dto.getSubTitle());
        product.setCoverImg(dto.getCoverImg());
        product.setDetail(dto.getDetail());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        return saveOrUpdate(product);
    }
    @Override
    public boolean updateStatus(Long id, Byte status) {
        Product p = getOwnedProduct(id);
        p.setStatus(status);
        return updateById(p);
    }
    @Override
    public boolean softDelete(Long id) {
        Product p = getOwnedProduct(id);
        p.setStatus((byte) 0);   // 下架 = 软删除
        return updateById(p);
    }
}
