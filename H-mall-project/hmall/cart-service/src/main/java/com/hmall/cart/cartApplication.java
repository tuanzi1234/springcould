package com.hmall.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages = "com.hmall.api.client")
@MapperScan("com.hmall.cart.mapper")
@SpringBootApplication
@ComponentScan(basePackages = {"com.hmall.cart", "com.hmall.api.client"}) // 添加这个注解确保 fallback 被扫
public class cartApplication {
    public static void main(String[] args) {
        SpringApplication.run(cartApplication.class, args);
    }
}