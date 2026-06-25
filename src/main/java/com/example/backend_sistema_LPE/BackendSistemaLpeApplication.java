package com.example.backend_sistema_LPE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendSistemaLpeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendSistemaLpeApplication.class, args);
	}

}
