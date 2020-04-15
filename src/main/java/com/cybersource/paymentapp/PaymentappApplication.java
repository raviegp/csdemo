package com.cybersource.paymentapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class PaymentappApplication {

	public static void main(String[] args) {
		System.out.println("Spring boot PaymentappAppliation");
		SpringApplication.run(PaymentappApplication.class, args);
	}

}



