package com.pigxia.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.pigxia.gmall.order.mapper")
public class GmallServiceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallServiceOrderApplication.class, args);
	}

}
