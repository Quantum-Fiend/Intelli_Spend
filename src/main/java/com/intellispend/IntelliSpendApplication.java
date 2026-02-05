package com.intellispend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@org.springframework.scheduling.annotation.EnableScheduling
public class IntelliSpendApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntelliSpendApplication.class, args);
	}

}
