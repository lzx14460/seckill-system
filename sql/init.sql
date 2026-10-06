-- 建库
CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seckill;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `role` VARCHAR(16) DEFAULT 'USER' COMMENT 'USER/MERCHANT',
    `phone` VARCHAR(20),
    `email` VARCHAR(64),
    `shop_name` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
    );

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
                                         `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         `merchant_id` BIGINT NOT NULL,
                                         `title` VARCHAR(128) NOT NULL,
    `sub_title` VARCHAR(256),
    `cover_img` VARCHAR(512),
    `detail` TEXT,
    `price` DECIMAL(10,2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `status` TINYINT DEFAULT 1 COMMENT '0下架 1上架',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_merchant` (`merchant_id`),
    INDEX `idx_status` (`status`)
    );

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `seckill_activity` (
                                                  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                  `product_id` BIGINT NOT NULL,
                                                  `merchant_id` BIGINT,
                                                  `name` VARCHAR(128),
    `seckill_price` DECIMAL(10,2) NOT NULL,
    `stock` INT NOT NULL,
    `total_stock` INT DEFAULT 0,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0未开始 1进行中 2已结束 3已取消',
    INDEX `idx_product` (`product_id`),
    INDEX `idx_merchant` (`merchant_id`),
    INDEX `idx_status` (`status`)
    );

-- 订单表
CREATE TABLE IF NOT EXISTS `seckill_order` (
                                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               `user_id` BIGINT NOT NULL,
                                               `product_id` BIGINT,
                                               `activity_id` BIGINT,
                                               `order_no` VARCHAR(32) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_activity` (`activity_id`)
    );

-- 测试账号（密码都是 123456）
INSERT INTO `user` (`username`, `password`, `role`, `phone`, `shop_name`) VALUES
                                                                              ('merchant001', 'e10adc3949ba59abbe56e057f20f883e', 'MERCHANT', '13800000001', '测试店铺'),
                                                                              ('user001',     'e10adc3949ba59abbe56e057f20f883e', 'USER',     '13800000002', NULL);

-- 测试商品
INSERT INTO `product` (`merchant_id`, `title`, `sub_title`, `cover_img`, `price`, `stock`, `status`) VALUES
    (1, 'iPhone 16 Pro', '限时秒杀', 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=400', 8999.00, 100, 1);

-- 测试秒杀活动
INSERT INTO `seckill_activity` (`product_id`, `merchant_id`, `name`, `seckill_price`, `stock`, `total_stock`, `start_time`, `end_time`, `status`) VALUES
    (1, 1, 'iPhone 秒杀', 4999.00, 100, 100, NOW() - INTERVAL 10 MINUTE, NOW() + INTERVAL 24 HOUR, 1);