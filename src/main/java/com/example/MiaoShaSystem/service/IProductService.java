package com.example.MiaoShaSystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.MiaoShaSystem.dto.ProductQueryDTO;
import com.example.MiaoShaSystem.dto.ProductSaveDTO;
import com.example.MiaoShaSystem.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.MiaoShaSystem.vo.ProductStatsVO;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author lzx
 * @since 2026-10-05
 */
public interface IProductService extends IService<Product> {
    /** 分页查询当前商家的商品 */
    IPage<Product> pageQuery(ProductQueryDTO dto);

    /** 查询商品并校验归属（不是自己的商品会抛异常） */
    Product getOwnedProduct(Long id);

    /** 新增或编辑（编辑时忽略 seckillStock） */
    boolean saveOrUpdateByMerchant(ProductSaveDTO dto);

    /** 上下架 */
    boolean updateStatus(Long id, Byte status);
    boolean softDelete(Long id);
    ProductStatsVO getStats(Long productId);
}
