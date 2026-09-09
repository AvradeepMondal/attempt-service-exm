package com.avradeep.AttemptService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AttemptServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttemptServiceApplication.class, args);
	}

}
