package com.pigxia.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.pigxia.gmall.manager.mapper")
public class GmallServiceManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallServiceManagerApplication.class, args);
	}

}
