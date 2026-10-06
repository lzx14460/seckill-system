-- KEYS[1] = seckill:stock:{seckillGoodsId}
-- ARGV[1] = 补货数量
local stock = redis.call('GET', KEYS[1])
if not stock then
    return -1        -- 活动库存未初始化
end
redis.call('INCRBY', KEYS[1], ARGV[1])
return redis.call('GET', KEYS[1])