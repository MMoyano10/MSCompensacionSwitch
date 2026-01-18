package com.bancario.compensacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class CompensacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompensacionApplication.class, args);
	}

}
