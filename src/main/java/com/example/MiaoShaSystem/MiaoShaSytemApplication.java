package com.example.MiaoShaSystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.example.MiaoShaSystem.mapper")
@EnableRedisHttpSession
@EnableScheduling
public class MiaoShaSytemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaoShaSytemApplication.class, args);
    }

}
