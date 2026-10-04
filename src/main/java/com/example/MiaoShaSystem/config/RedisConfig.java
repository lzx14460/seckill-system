package com.example.MiaoShaSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 主要做两件事：
 * 1. 配置业务代码用的 RedisTemplate（key 用 String，value 用 JSON）
 * 2. 配置 Spring Session 的序列化器（让 Session 在 Redis 里以 JSON 存储）
 */
@Configuration
public class RedisConfig {

    /**
     * 业务代码用的 RedisTemplate
     * <p>
     * 不配置的话，Spring Boot 默认用 JDK 序列化，存到 Redis 里是二进制乱码，
     * 用 RedisInsight 看的时候完全读不懂。
     * <p>
     * 这里配置成：
     * - key：String 序列化，看到的是 "seckill:stock:1" 这样的可读字符串
     * - value：JSON 序列化，看到的是 {"userId":1} 这样的 JSON
     *
     * @param factory Redis 连接工厂，Spring 自动注入（连接配置在 application.yml 里）
     * @return 配置好的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 设置连接工厂，告诉它连哪个 Redis
        template.setConnectionFactory(factory);

        // String 序列化器：把字符串直接转成字节，可读性好
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // JSON 序列化器：把对象转成 JSON 字符串再存
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // key 用 String 序列化（比如 "seckill:stock:1"）
        template.setKeySerializer(stringSerializer);

        // hash 的 key 也用 String 序列化
        template.setHashKeySerializer(stringSerializer);

        // value 用 JSON 序列化（比如 {"userId":1,"seckillGoodsId":1}）
        template.setValueSerializer(jsonSerializer);

        // hash 的 value 也用 JSON 序列化
        template.setHashValueSerializer(jsonSerializer);

        // 检查所有必要属性是否已设置，完成初始化（不调用可能无法正常工作）
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Spring Session 专用的序列化器
     * <p>
     * Spring Session 默认用 JDK 序列化，存到 Redis 里是二进制乱码。
     * 配置这个 Bean 后，Session 数据会以 JSON 格式存储，方便调试。
     * <p>
     * 注意：Bean 名字必须是 "springSessionDefaultRedisSerializer"，
     * Spring Session 会按这个名字去找序列化器。
     *
     * @return JSON 序列化器
     */
    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}