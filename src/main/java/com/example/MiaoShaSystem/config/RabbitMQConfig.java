package com.example.MiaoShaSystem.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * <p>
 * 主要负责声明队列。生产者（秒杀服务）往队列发消息，
 * 消费者（订单服务）从队列取消息，实现异步下单。
 */
@Configuration
public class RabbitMQConfig {

    /** 秒杀队列名称，生产者和消费者都用这个常量 */
    public static final String SECKILL_QUEUE = "seckill.queue";

    /**
     * 声明秒杀队列
     * <p>
     * new Queue(name, durable)：
     * - 第一个参数：队列名
     * - 第二个参数 durable=true：队列持久化，RabbitMQ 重启后队列还在
     * <p>
     * Spring 启动时会自动在 RabbitMQ 里创建这个队列（如果不存在）。
     *
     * @return 队列对象
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }
    /**
     * 配置 RabbitMQ 用 JSON 序列化消息
     * 不加这个的话，默认的 SimpleMessageConverter 只支持 String、byte[] 和 Serializable
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}