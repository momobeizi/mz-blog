package com.mz.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@MapperScan("com.mz.blog")
@SpringBootApplication
public class MzBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MzBlogApplication.class, args);
    }
}
