package com.example.MiaoShaSystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.MiaoShaSystem.entity.SeckillActivity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SeckillGoodsMapper extends BaseMapper<SeckillActivity> {

    /**
     * 扣减数据库库存
     * 用 stock > 0 条件防止超卖，返回受影响行数
     */
    @Update("UPDATE seckill_goods SET stock = stock - 1 WHERE id = #{id} AND stock > 0")
    int decreaseStock(@Param("id") Long id);
}