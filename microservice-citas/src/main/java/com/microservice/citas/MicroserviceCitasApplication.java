package com.microservice.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroserviceCitasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceCitasApplication.class, args);
	}

}
