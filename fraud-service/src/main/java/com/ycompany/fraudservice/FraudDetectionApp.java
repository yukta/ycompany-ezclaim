package com.ycompany.fraudservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FraudDetectionApp {
	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionApp.class, args);
	}
}
