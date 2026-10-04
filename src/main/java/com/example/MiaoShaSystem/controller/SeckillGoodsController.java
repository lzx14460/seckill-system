package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.entity.SeckillGoods;
import com.example.MiaoShaSystem.mapper.SeckillGoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lzx
 * @since 2026-10-04
 */
/**
 * 秒杀商品接口
 */
@RestController
@RequestMapping("/seckill/goods")
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询秒杀商品列表
     * 路径：GET /seckill/goods/list
     */
    @GetMapping("/list")
    public Result list() {
        List<SeckillGoods> list = seckillGoodsMapper.selectList(null);
        return Result.success(list);
    }
}