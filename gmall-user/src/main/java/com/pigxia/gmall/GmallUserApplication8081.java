package com.pigxia.gmall;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Created by absen on 2020/5/27 12:04
 */
@SpringBootApplication
@MapperScan(basePackages = "com.pigxia.gmall.mapper")
public class GmallUserApplication8081 {
    public static void main(String[] args) {
        SpringApplication.run(GmallUserApplication8081.class,args);
    }
}
