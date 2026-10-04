package com.example.MiaoShaSystem;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
                        "root",
                        "root"
                )
                .globalConfig(builder -> {
                    builder.author("lzx")
                            .outputDir(System.getProperty("user.dir") + "/src/main/java")
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    builder.parent("com.example.MiaoShaSystem")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .xml("mapper")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper"
                            ));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("user", "goods", "seckill_order","seckill_goods")
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .mapperBuilder()
                            .enableBaseResultMap()
                            .enableBaseColumnList();
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}